terraform {
  required_version = ">= 1.0.0"
  required_providers {
    openstack = {
      source  = "terraform-provider-openstack/openstack"
      version = "~> 1.54.0"
    }
  }
}

# Configuration du provider OpenStack (utilise clouds.yaml)
provider "openstack" {
  cloud = "devstack-admin"
}

# 1. Groupe de sécurité pour l'instance (autorise SSH port 22 et HTTP port 80)
resource "openstack_networking_secgroup_v2" "secgroup_web" {
  name        = "yahya-terraform-sg"
  description = "Groupe de securite Terraform Yahya (SSH + HTTP)"
}

# Règle SSH (port 22)
resource "openstack_networking_secgroup_rule_v2" "rule_ssh" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 22
  port_range_max    = 22
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.secgroup_web.id
}

# Règle HTTP (port 80)
resource "openstack_networking_secgroup_rule_v2" "rule_http" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 80
  port_range_max    = 80
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.secgroup_web.id
}

# Règle ICMP (Ping)
resource "openstack_networking_secgroup_rule_v2" "rule_icmp" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "icmp"
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.secgroup_web.id
}

# 2. Allocation d'une adresse IP flottante (Floating IP sur le réseau public)
resource "openstack_networking_floatingip_v2" "fip" {
  pool = "public"
}

# 3. Création de l'instance Ubuntu Server
resource "openstack_compute_instance_v2" "vm_ubuntu" {
  name        = "yahya-terraform-ubuntu"
  image_name  = "ubuntu-24.04"
  flavor_name = "m1.saas"
  key_pair    = "yahya-key"

  security_groups = [
    openstack_networking_secgroup_v2.secgroup_web.name
  ]

  network {
    name = "yahya-net"
  }
}

# 4. Association de l'IP flottante à l'instance créée
resource "openstack_compute_floatingip_associate_v2" "fip_assoc" {
  floating_ip = openstack_networking_floatingip_v2.fip.address
  instance_id = openstack_compute_instance_v2.vm_ubuntu.id
}

# 5. Génération AUTOMATIQUE du fichier hosts.ini pour Ansible
#    Terraform écrit la bonne IP à chaque "apply", plus besoin de modifier à la main !
resource "local_file" "ansible_inventory" {
  filename = "${path.module}/hosts.ini"
  content  = <<EOT
[webservers]
${openstack_compute_instance_v2.vm_ubuntu.name} ansible_host=${openstack_networking_floatingip_v2.fip.address} ansible_user=ubuntu ansible_ssh_private_key_file=~/.ssh/yahya_cirros_key
EOT
}

# 6. Valeurs de sortie (Outputs)
output "instance_name" {
  description = "Nom de l'instance"
  value       = openstack_compute_instance_v2.vm_ubuntu.name
}

output "private_ip" {
  description = "Adresse IP privée de l'instance"
  value       = openstack_compute_instance_v2.vm_ubuntu.network[0].fixed_ip_v4
}

output "floating_ip" {
  description = "Adresse IP flottante externe de l'instance"
  value       = openstack_networking_floatingip_v2.fip.address
}
