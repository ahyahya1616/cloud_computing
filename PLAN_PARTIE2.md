# Plan de préparation — Projet Cloud Intelligence (Parties 2, 3, 4)

> Document de travail — préparation de l'environnement OpenStack local.
> Machine : ThinkPad T460s — Ubuntu 24.04.4 LTS.
> Dernière mise à jour : 2026-10-31.

---

## 1. Décision d'architecture : VM locale KVM/libvirt

Le sujet impose une installation **sur la machine de l'étudiant**, pas dans un cloud externe
(Azure, AWS, GCP...). Il cite VirtualBox/VMware comme **exemples**, pas comme obligation.

**Choix retenu : une VM locale KVM/libvirt.**

Justifications :

- KVM/libvirt est déjà installé et opérationnel sur l'hôte (`virsh` 10.0, `qemu`, `/dev/kvm`).
- VirtualBox n'est pas installé et serait moins performant (hyperviseur de type 2).
- La virtualisation imbriquée est active (`/sys/module/kvm_intel/parameters/nested = Y`),
  donc Nova pourra lancer des instances à l'intérieur de la VM.
- L'installation directe sur l'hôte est déconseillée : DevStack crée `br-ex`, modifie
  iptables/nftables et installe MariaDB/RabbitMQ/Apache, ce qui entrerait en conflit avec
  Docker, `virbr0` et `minionebr` (GNS3/Mininet) déjà présents.
- Une VM permet un **snapshot/rollback** immédiat en cas de casse.

### Conformité aux exigences du projet

| Exigence | Réponse KVM/libvirt | Statut |
|---|---|---|
| Installation sur la machine, pas un cloud externe | VM locale sur le ThinkPad | OK |
| Parties 2, 3, 4 sur la même installation OpenStack | une seule VM DevStack réutilisée | OK |
| Partie 1 indépendante (CloudSim Plus) | Java sur l'hôte, sans VM | OK |
| Nova doit lancer CirrOS et Ubuntu | nested KVM (`nested=Y`) | OK (à revalider dans la VM) |
| Horizon accessible | tunnel SSH `localhost:8080` | OK |
| Terraform/Ansible depuis la VM DevStack (ou WSL) | exécutés dans la VM | OK |
| Captures avec nom + date | hostname `yahya-devstack`, instances `yahya-*`, `date` affichée | OK |
| PuTTY **ou** ssh | `ssh` sous Ubuntu | OK |

---

## 2. Diagnostic de la machine hôte

| Élément | Valeur constatée | Verdict |
|---|---|---|
| OS | Ubuntu 24.04.4 LTS (Noble) | compatible DevStack 2024.2 |
| CPU | Intel i7-6600U, 2 cœurs / 4 threads | 4 vCPU max |
| RAM | 11 Go, ~5,3 Go disponibles (Chrome + Spotify ≈ 3 Go) | point critique |
| Swap hôte | 4 Go | OK |
| Disque | 233 Go, **93 Go libres** (`/dev/sda2`) | suffisant |
| Virtualisation | VT-x, KVM, `nested=Y`, `/dev/kvm` présent | excellent |
| Réseau | Wi-Fi `wlp4s0` 10.10.236.17/16, passerelle 10.10.1.1 | bridge Wi-Fi impossible → NAT |
| Hyperviseur | libvirt 10.0, qemu, `virsh`, réseau `default` 192.168.123.0/24 actif | prêt |
| Conteneurs | Docker + bridges `docker0`, `br-*` ; GNS3 `minionebr` | à ne pas perturber |
| Outils manquants | `virt-install`, `cloud-localds`, `genisoimage`, CLI `openstack` | à installer |
| Sudo | mot de passe requis | à saisir à l'exécution |

---

## 3. Dimensionnement retenu

| Paramètre | Valeur | Remarque |
|---|---|---|
| Hyperviseur | KVM/libvirt | local, conforme |
| Image | Ubuntu 24.04 Server cloud image (`noble-server-cloudimg-amd64.img`) | qcow2, cloud-init |
| vCPU | 4 | tous les threads de l'hôte |
| RAM | **6 Go courants / 8 Go max (ballon)** | `virsh setmem` pour monter à 7–8 Go |
| Disque | **60 Go qcow2 thin** | ~25–30 Go réels après installation |
| Swap VM | 2 Go (fichier) | évite les OOM |
| Réseau 1 | `eth0` NAT libvirt 192.168.123.0/24 | management : SSH, Horizon |
| Réseau 2 | `eth1` provider 10.20.20.0/24 | br-ex / IP flottantes |
| CPU model | `host-passthrough` | expose VT-x à la VM (nested) |
| OpenStack | DevStack `stable/2024.2` (Dalmatian) | compatible Ubuntu 24.04 |
| SaaS | Nextcloud (snap) sur Ubuntu, flavor `m1.saas` | 1536 Mo / 10 Go / 1 vCPU |
| Snapshots | externes (`--disk-only`) | évite de gonfler le qcow2 |

### Budget RAM au pic (projet complet)

| Composant | RAM estimée |
|---|---|
| DevStack (Keystone, Nova, Neutron, Glance, Placement, Cinder, Horizon, MariaDB, RabbitMQ, memcached) | ~4,0 Go |
| 3 × CirrOS (Partie 4) | ~0,5 Go |
| Ubuntu SaaS + Nextcloud (Partie 2.3) | ~1,7 Go |
| Terraform / Ansible / openstacksdk | ~0,3 Go |
| **Total au pic** | **~6,5 Go** |

→ Démarrer à 6 Go, monter le ballon à 7–8 Go pendant `stack.sh` et la démo SaaS.

### Budget disque

| Poste | Taille réelle estimée |
|---|---|
| OS VM + paquets | 2–3 Go |
| `/opt/stack` (venvs, code) + bases | 8–10 Go |
| Volumes Cinder | 3–5 Go |
| Image Ubuntu (Glance + copie) | ~1,5 Go |
| Instances (CirrOS, Ubuntu SaaS) | ~5 Go |
| Terraform/Ansible/openstacksdk | < 0,5 Go |
| **Total** | **~25–30 Go** (sur 60 Go alloués) |

→ Reste ~60 Go libres sur l'hôte après création.

---

## 4. Architecture cible

```
Hôte Ubuntu 24.04 (11 Go RAM, 93 Go libres)
│
├── libvirt/KVM
│   ├── réseau NAT       192.168.123.0/24  → management (SSH, Horizon)
│   └── réseau provider  10.20.20.0/24      → br-ex / IP flottantes
│
└── VM "yahya-devstack"  (Ubuntu 24.04 Server cloud image)
    ├── 4 vCPU, 6 Go → 8 Go (ballon), 60 Go qcow2, swap 2 Go
    ├── CPU host-passthrough (KVM imbriqué → Nova accéléré)
    ├── cloud-init : user "stack", clé SSH, sudo, qemu-guest-agent
    └── DevStack stable/2024.2
        ├── Keystone, Nova, Glance, Neutron, Placement, Cinder, Horizon
        ├── CirrOS                → IaaS (Partie 2.2 + Partie 4)
        ├── Ubuntu + Nextcloud    → SaaS (Partie 2.3), flavor m1.saas
        ├── Terraform + Ansible   → Partie 3
        └── openstacksdk (venv)   → Partie 4
```

---

## 5. Plan d'exécution détaillé

### Phase 0 — Hygiène ressources

1. Fermer Chrome/Spotify pendant les phases lourdes.
2. Vérifier ≥ 8 Go RAM libres et ≥ 60 Go disque.
3. Créer l'arborescence projet :
   `cloud_intelligence/{partie2,partie3,partie4,partie1}/{scripts,captures,docs}`.

### Phase A — Outils hôte et image

4. `sudo apt install virtinst cloud-image-utils genisoimage qemu-utils`.
5. Télécharger `noble-server-cloudimg-amd64.img` + vérification SHA256.
6. Générer une paire de clés SSH dédiée (`~/.ssh/devstack_ed25519`).
7. Rédiger le `cloud-init` (`user-data` / `meta-data`) : user `stack`, sudo NOPASSWD,
   clé SSH, mot de passe de secours, swap 2 Go, `qemu-guest-agent`, hostname `yahya-devstack`.
8. Créer le réseau libvirt `provider` (10.20.20.0/24, DHCP désactivé, NAT ou isolé).
9. Créer la VM :
   - `qemu-img create -f qcow2 -F qcow2 -b noble-server-cloudimg-amd64.img vm.qcow2 60G`
   - 4 vCPU, RAM 6 Go (max 8 Go), `host-passthrough`, 2 NICs, disque virtio.
10. Démarrer, vérifier `/dev/kvm` **dans** la VM, SSH, accès Internet.

### Phase B — DevStack (Partie 2.1)

11. Créer l'utilisateur `stack` (DevStack refuse root), cloner `stable/2024.2`.
12. Écrire `local.conf` :
    - `ADMIN_PASSWORD`, `DATABASE_PASSWORD`, `RABBIT_PASSWORD`, `SERVICE_PASSWORD` ;
    - `HOST_IP` (eth0), `PUBLIC_INTERFACE=eth1`, `FLOATING_RANGE=10.20.20.128/25`,
      `PUBLIC_NETWORK_GATEWAY=10.20.20.1`, `Q_FLOATING_ALLOCATION_POOL` ;
    - désactiver Swift, Heat, Ceilometer, Aodh, Gnocchi, Tempest ;
    - garder Cinder (service demandé dans le rapport).
13. `./stack.sh` (~30–60 min), puis validation :
    - `openstack service list`
    - `openstack compute service list`
    - `openstack network agent list`
    - accès Horizon via tunnel SSH `localhost:8080`.
14. **Snapshot libvirt « devstack-clean »** (rollback instantané).

### Phase C — IaaS CirrOS (Partie 2.2)

15. Créer réseau privé + sous-réseau, routeur + interface gateway.
16. Groupe de sécurité `yahya-sg` : SSH (22) et ICMP autorisés.
17. Paire de clés `yahya-key`, instance CirrOS `yahya-cirros-1`.
18. Associer une IP flottante, tester `ping` puis `ssh cirros@<IP>`.
19. Captures : Horizon, topologie réseau, connexion SSH, avec date visible.

### Phase D — SaaS Nextcloud (Partie 2.3)

20. Uploader l'image Ubuntu dans Glance :
    `openstack image create --disk-format qcow2 --file noble-server-cloudimg-amd64.img ubuntu-24.04`.
21. Créer le flavor :
    `openstack flavor create --ram 1536 --disk 10 --vcpus 1 m1.saas`.
22. Lancer `yahya-saas` sur le réseau privé, associer une IP flottante, SG HTTP/HTTPS/SSH.
23. Dans l'instance : `snap install nextcloud`, `nextcloud.manual-install`, port 80.
24. Ouvrir Nextcloud dans le navigateur (IP flottante ou tunnel) + capture.
25. Arrêter l'instance SaaS après les captures (libère ~1,7 Go).

### Phase E — Terraform + Ansible (Partie 3)

26. Récupérer `clouds.yaml` depuis DevStack (`~/.config/openstack/clouds.yaml`).
27. Installer Terraform et Ansible dans la VM.
28. Écrire les fichiers `.tf` : provider OpenStack, groupe de sécurité SSH+HTTP,
    instance Ubuntu, IP flottante, sorties.
29. `terraform init` → `plan` → `apply` ; capture des sorties.
30. Playbook Ansible : installer Nginx, démarrer/activer le service, déployer une page
    affichant nom, numéro d'étudiant et nom de l'instance.
31. `terraform destroy` à la fin ; expliquer la différence provisionnement/config.
32. Captures : page web Nginx, `terraform apply`.

### Phase F — SLA (Partie 4)

33. Rédiger `sla.json` : 99,5 % / jour, instances concernées, définition de
    « disponible », fréquence 5 min, formule, conséquences en cas de non-respect.
34. Créer un venv + `openstacksdk` dans la VM.
35. Script Python : authentification Keystone, état des instances via Nova,
    test réseau (ping/port 22), journal CSV horodaté, calcul de disponibilité.
36. Surveiller 2–3 CirrOS, provoquer une panne (`openstack server stop` puis `start`),
    montrer la détection et la baisse de disponibilité.
37. Ajouter le rapport de surveillance au SLA (période, mesures, dispo par instance,
    objectif respecté ou non).
38. Répondre à la question : sur 24 h avec 1 mesure/5 min (288 mesures),
    99,5 % ⇒ au plus **1 mesure en échec** (0,5 % de 288 = 1,44), soit ~5 minutes de panne.

### Phase G — Livrables

39. Arborescence finale et conventions de nommage (`yahya-*`, hostname `yahya-devstack`).
40. Captures horodatées (commande `date` visible ou horloge du système).
41. Rapport PDF : étapes, commandes, captures, explications pour chaque partie.
42. Archive `Nom_Prenom_Code.zip` : code Java CloudSim Plus, `.tf`, Ansible,
    `sla.json`, script Python.

### Partie 1 — CloudSim Plus (ultérieure, indépendante)

43. Sur l'hôte : JDK 17 + Maven, projet CloudSim Plus.
44. Documentation UAE (établissements, villes, effectifs, services), architecture,
    modélisation des charges (normale/pointe), simulation des politiques
    (TimeShared/SpaceShared, Simple/BestFit, HorizontalVmScaling), analyse
    (temps de réponse, exécution, utilisation, coût).

---

## 6. Risques et parades

| Risque | Impact | Parade |
|---|---|---|
| RAM hôte insuffisante (11 Go) | swap/OOM pendant `stack.sh` | fermer Chrome/Spotify, ballon 6→8 Go, swap VM 2 Go |
| Wi-Fi incompatible avec le bridge | IP flottantes non joignables depuis l'hôte | NAT + tunnel SSH / port forwarding |
| KVM imbriqué indisponible dans la VM | instances Nova lentes (QEMU TCG) | `host-passthrough`, vérifier `/dev/kvm` |
| Conflit avec Docker/GNS3/libvirt | réseau instable | VM isolée, réseau provider dédié |
| Disque 93 Go | saturation avec snapshots | qcow2 thin, snapshots externes, nettoyage |
| DevStack long/échec partiel | perte de temps | snapshot après succès, logs `stack.sh` |
| Mot de passe sudo exposé | sécurité | ne jamais l'écrire dans un fichier, le changer après le projet |

---

## 7. Conventions pour les captures (exigence du professeur)

- Nom d'hôte de la VM : `yahya-devstack`.
- Toutes les instances : préfixe `yahya-` (`yahya-cirros-1`, `yahya-saas`, ...).
- Afficher la date dans chaque capture : `date` dans le terminal ou horloge visible.
- Captures minimales :
  - Horizon (connexion + liste des instances) ;
  - `openstack service list`, `compute service list`, `network agent list` ;
  - topologie réseau (Horizon) ;
  - `ping` + session SSH vers CirrOS ;
  - Nextcloud ouvert dans le navigateur ;
  - `terraform apply` + page Nginx ;
  - panne détectée par le script SLA + journal CSV.
