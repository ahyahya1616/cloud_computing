#!/usr/bin/env python3
"""
Gestionnaire d'état des instances SLA (Arrêt / Démarrage)
Auteur : Yahya Ahmane
Usage  :
    ./venv/bin/python3 manage_instances.py stop sla-instance-1
    ./venv/bin/python3 manage_instances.py start sla-instance-1
    ./venv/bin/python3 manage_instances.py status
"""

import sys
import argparse
import openstack

def main():
    parser = argparse.ArgumentParser(description="Gérer le statut des instances OpenStack (start/stop/status)")
    parser.add_argument("action", choices=["start", "stop", "status"], help="Action à exécuter : start, stop ou status")
    parser.add_argument("instances", nargs="*", default=["sla-instance-1"], help="Nom(s) des instances (ex: sla-instance-1 sla-instance-2)")
    parser.add_argument("--clouds-yaml", default="clouds.yaml", help="Fichier de configuration OpenStack")

    args = parser.parse_args()

    # Connexion à OpenStack via Keystone
    conn = openstack.connect(cloud="devstack", config_files=[args.clouds_yaml])

    if args.action == "status":
        print("=" * 60)
        print(" STATUT ACTUEL DES INSTANCES ")
        print("=" * 60)
        targets = args.instances if args.instances else ["sla-instance-1", "sla-instance-2"]
        for name in targets:
            server = conn.compute.find_server(name)
            if server:
                print(f"  * {name:<18} : {server.status}")
            else:
                print(f"  * {name:<18} : NON TROUVÉE")
        print("=" * 60)
        return

    for name in args.instances:
        server = conn.compute.find_server(name)
        if not server:
            print(f"[Erreur] Instance '{name}' introuvable.")
            continue

        if args.action == "stop":
            print(f"[Action] Arrêt forcé de {name} (simulation de panne)...")
            conn.compute.stop_server(server)
            conn.compute.wait_for_server(server, status="SHUTOFF", wait=60)
            print(f"[Succès] {name} est maintenant éteinte (SHUTOFF).")

        elif args.action == "start":
            print(f"[Action] Redémarrage de {name} (rétablissement du service)...")
            conn.compute.start_server(server)
            conn.compute.wait_for_server(server, status="ACTIVE", wait=60)
            print(f"[Succès] {name} est maintenant active (ACTIVE).")

if __name__ == "__main__":
    main()

