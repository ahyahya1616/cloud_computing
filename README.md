# Projet Cloud Intelligence — OpenStack

**Étudiant :** Yahya AHMANE — CNE : R139310095  
**Module :** Cloud Computing / Cloud Intelligence — Université Abdelmalek Essaâdi  
**Environnement :** VM locale KVM/libvirt `yahya-devstack` — Ubuntu 24.04 + DevStack 2024.2 (OpenStack Dalmatian)

## Contenu du dépôt

| Dossier / fichier | Description |
|---|---|
| `partie2/` | IaaS (CirrOS) + SaaS (Ubuntu/Nextcloud) + vérification DevStack/Horizon |
| `partie3/` | Infrastructure as Code : Terraform (`main.tf`) + Ansible (`playbook.yml`, `index.html.j2`) |
| `PLAN_PARTIE2.md` | Plan de préparation de la VM et de l'installation OpenStack |
| `Projet_Cloud_Computing.pdf` | Sujet du projet |

Chaque partie contient un `RAPPORT_PARTIE*.md` et un dossier `captures/` avec les preuves horodatées.

## Reprendre le projet demain

La VM est arrêtée proprement et un snapshot `partie3-complete` a été créé.

```bash
# 1. Démarrer la VM
virsh start yahya-devstack

# 2. Attendre ~1-2 min puis se connecter
ssh -i ~/.ssh/devstack_ed25519 stack@192.168.123.10

# 3. Charger les identifiants OpenStack
source /opt/stack/devstack/openrc admin admin

# 4. Vérifier que les services tournent
openstack service list
openstack compute service list
openstack network agent list
```

Horizon : `http://192.168.123.10/dashboard/` (utilisateur `admin`, domaine `Default`).

### Revenir au snapshot (rollback)

```bash
virsh shutdown yahya-devstack
virsh snapshot-revert yahya-devstack partie3-complete
virsh start yahya-devstack
```

## Partie 3 — Terraform + Ansible

```bash
cd partie3
terraform init
terraform plan
terraform apply -auto-approve      # écrit aussi hosts.ini
ansible-playbook -i hosts.ini playbook.yml
terraform destroy -auto-approve
```

`clouds.yaml` contient les identifiants du DevStack **local** (VM non exposée sur Internet).
`hosts.ini` est régénéré automatiquement par Terraform à chaque `apply`.
