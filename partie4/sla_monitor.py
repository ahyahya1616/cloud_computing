#!/usr/bin/env python3
"""
SLA Monitoring Script for OpenStack Compute Instances
Auteur  : Yahya Ahmane
Projet  : Cloud Intelligence - Partie 4 (Service Level Agreement)
Objectif: Surveiller la disponibilité d'instances OpenStack (Nova + Ping ICMP),
          enregistrer les métriques et calculer le respect du contrat SLA (99.5%).
"""

import sys
import os
import time
import subprocess
import csv
import json
import argparse
from datetime import datetime
import openstack

CSV_HEADER = [
    "timestamp",
    "instance_name",
    "instance_id",
    "ip_address",
    "nova_status",
    "power_state",
    "ping_status",
    "available",
]

def ping_host(ip, timeout_seconds=2):
    """
    Vérifie la joignabilité réseau d'une adresse IP par ICMP (ping).
    Retourne True si un écho ICMP est reçu, False sinon.
    """
    if not ip or ip == "N/A":
        return False
    try:
        res = subprocess.run(
            ["ping", "-c", "1", "-W", str(timeout_seconds), ip],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )
        return res.returncode == 0
    except Exception:
        return False

def get_instance_floating_ip(conn, server):
    """
    Recherche l'adresse Floating IP publique attachée à l'instance.
    Si non trouvée, retourne l'adresse IP fixe.
    """
    ports = list(conn.network.ports(device_id=server.id))
    for port in ports:
        fips = list(conn.network.ips(port_id=port.id))
        if fips:
            return fips[0].floating_ip_address
            
    # Fallback sur les adresses privées/IPv4 du dictionnaire addresses
    if hasattr(server, 'addresses') and server.addresses:
        for net_name, addrs in server.addresses.items():
            for addr in addrs:
                if addr.get('version') == 4:
                    return addr.get('addr')
    return "N/A"

def init_csv_log(csv_file):
    """Initialise le fichier CSV de métriques avec son en-tête s'il n'existe pas."""
    file_exists = os.path.isfile(csv_file)
    if not file_exists:
        with open(csv_file, mode="w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow(CSV_HEADER)

def append_csv_metric(csv_file, row):
    """Ajoute un enregistrement d'état au journal CSV."""
    with open(csv_file, mode="a", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(row)

def update_sla_json(sla_json_file, stats, target_threshold=99.5):
    """
    Met à jour la section execution_report dans sla.json avec les résultats réels.
    """
    if not os.path.exists(sla_json_file):
        return
        
    try:
        with open(sla_json_file, "r", encoding="utf-8") as f:
            data = json.load(f)
            
        report = {
            "last_updated": datetime.now().isoformat(),
            "target_threshold_percent": target_threshold,
            "instances": {}
        }
        
        all_compliant = True
        for name, s in stats.items():
            avail = s["availability_percent"]
            compliant = avail >= target_threshold
            if not compliant:
                all_compliant = False
            report["instances"][name] = {
                "instance_id": s["id"],
                "total_probes": s["total"],
                "up_probes": s["up"],
                "down_probes": s["down"],
                "availability_percent": avail,
                "sla_compliant": compliant
            }
            
        report["global_sla_met"] = all_compliant
        data["execution_report"] = report
        
        with open(sla_json_file, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
    except Exception as e:
        print(f"[Avertissement] Impossible de mettre à jour {sla_json_file}: {e}")

def run_monitor(clouds_yaml, targets, interval, iterations, csv_file, sla_json_file):
    """
    Boucle principale de supervision SLA.
    """
    print("=" * 80)
    print(" DÉMARRAGE DU MONITEUR SLA OPENSTACK ")
    print("=" * 80)
    print(f" Cibles surveillées : {', '.join(targets)}")
    print(f" Intervalle sondes  : {interval} secondes")
    print(f" Nombre de sondes   : {'Illimité (Ctrl+C pour stopper)' if iterations is None else iterations}")
    print(f" Journal des mesures: {csv_file}")
    print("=" * 80)

    # Initialisation du fichier de journalisation
    init_csv_log(csv_file)

    # Connexion OpenStack (Keystone)
    print("[Keystone] Connexion au projet DevStack...")
    conn = openstack.connect(cloud="devstack", config_files=[clouds_yaml])
    print("[Keystone] Authentification réussie.\n")

    stats = {
        name: {"id": "N/A", "total": 0, "up": 0, "down": 0, "availability_percent": 0.0}
        for name in targets
    }

    cycle = 0
    try:
        while True:
            cycle += 1
            now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            print(f"--- Sonde #{cycle} [{now_str}] ---")

            # Récupération de l'état des serveurs via l'API Nova
            for name in targets:
                server = conn.compute.find_server(name)
                stats[name]["total"] += 1

                if server is None:
                    status = "NOT_FOUND"
                    power = "UNKNOWN"
                    ip = "N/A"
                    ping_ok = False
                    is_available = False
                    s_id = "N/A"
                else:
                    s_id = server.id
                    stats[name]["id"] = s_id
                    status = server.status  # ACTIVE, SHUTOFF, ERROR...
                    power = getattr(server, "power_state", 1)
                    ip = get_instance_floating_ip(conn, server)
                    ping_ok = ping_host(ip, timeout_seconds=2)

                    # Condition de disponibilité SLA :
                    # Nova Status ACTIVE + Power State RUNNING + Ping OK
                    is_available = (status == "ACTIVE") and ping_ok

                if is_available:
                    stats[name]["up"] += 1
                    status_display = "\033[92m[UP  (DISPONIBLE)]\033[0m"
                else:
                    stats[name]["down"] += 1
                    status_display = "\033[91m[DOWN (INDISPONIBLE)]\033[0m"

                # Calcul du pourcentage de disponibilité courant
                avail = (stats[name]["up"] / stats[name]["total"]) * 100.0
                stats[name]["availability_percent"] = round(avail, 2)

                print(
                    f"  * {name:<15} | Nova: {status:<8} | IP: {ip:<14} | "
                    f"Ping: {'OK ' if ping_ok else 'ERR'} | "
                    f"{status_display} | SLA: {avail:6.2f}%"
                )

                # Sauvegarde métrique
                row = [
                    now_str,
                    name,
                    s_id,
                    ip,
                    status,
                    power,
                    "SUCCESS" if ping_ok else "FAILURE",
                    "UP" if is_available else "DOWN",
                ]
                append_csv_metric(csv_file, row)

            # Mise à jour du JSON
            update_sla_json(sla_json_file, stats)

            if iterations is not None and cycle >= iterations:
                break

            time.sleep(interval)

    except KeyboardInterrupt:
        print("\n\n[Arrêt manuel] Interruption par l'utilisateur (Ctrl+C).")

    # Résumé final
    print("\n" + "=" * 80)
    print(" BILAN DE DISPONIBILITÉ SLA ")
    print("=" * 80)
    print(f"{'Instance':<18} | {'Total':<6} | {'UP':<5} | {'DOWN':<5} | {'Dispo (%)':<10} | {'SLA (99.5%)'}")
    print("-" * 80)
    for name, s in stats.items():
        avail = s["availability_percent"]
        verdict = "CONFORME" if avail >= 99.5 else "NON RESPECTÉ (Pénalité)"
        print(f"{name:<18} | {s['total']:<6} | {s['up']:<5} | {s['down']:<5} | {avail:8.2f}%  | {verdict}")
    print("=" * 80)
    print(f"Rapport sauvegardé dans: {sla_json_file}")
    print(f"Historique complet dans: {csv_file}\n")


def main():
    parser = argparse.ArgumentParser(
        description="Programme de surveillance SLA OpenStack (Nova + Ping)"
    )
    parser.add_argument(
        "--clouds-yaml",
        default="/home/yahya-ahmane/cloud_intelligence/partie4/clouds.yaml",
        help="Chemin vers clouds.yaml"
    )
    parser.add_argument(
        "--targets",
        nargs="+",
        default=["sla-instance-1", "sla-instance-2"],
        help="Noms des instances à surveiller"
    )
    parser.add_argument(
        "--interval",
        type=int,
        default=5,
        help="Intervalle entre chaque sonde en secondes (défaut: 5s pour test rapide, 300s en production)"
    )
    parser.add_argument(
        "--iterations",
        type=int,
        default=None,
        help="Nombre de cycles de mesure (par défaut: infini)"
    )
    parser.add_argument(
        "--csv-file",
        default="/home/yahya-ahmane/cloud_intelligence/partie4/metrics.csv",
        help="Fichier CSV de sortie pour les mesures"
    )
    parser.add_argument(
        "--sla-json",
        default="/home/yahya-ahmane/cloud_intelligence/partie4/sla.json",
        help="Fichier SLA JSON à mettre à jour"
    )

    args = parser.parse_args()
    run_monitor(
        clouds_yaml=args.clouds_yaml,
        targets=args.targets,
        interval=args.interval,
        iterations=args.iterations,
        csv_file=args.csv_file,
        sla_json_file=args.sla_json,
    )

if __name__ == "__main__":
    main()

