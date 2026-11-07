# Étape 2 : Modélisation des Charges (Période Normale vs Période de Pointe)

Pour simuler avec précision le comportement du Cloud de l'Université Abdelmalek Essaâdi (UAE) dans **CloudSim Plus**, nous devons modéliser la charge sous forme de **Cloudlets** (tâches de calcul élémentaires soumises aux machines virtuelles).

---

## 1. Définition d'un Cloudlet dans CloudSim Plus

Un **Cloudlet** représente une requête ou un traitement utilisateur. Il est caractérisé par :
* **Longueur (Length en Millions d'Instructions - MI)** : Complexité CPU de la requête.
* **Taille des données en entrée (File Size)** : En octets/kilo-octets (ex: formulaire d'inscription envoyé).
* **Taille des données en sortie (Output Size)** : En octets (ex: page HTML générée ou reçu d'inscription).
* **Nombre de PE (Processing Elements / cœurs requis)** : Généralement 1 cœur par requête web.

---

## 2. Profil des Deux Périodes d'Évaluation

### A. Période Normale (Activité courante durant le semestre)
* **Contexte** : Utilisation quotidienne par les étudiants et enseignants (consultation des cours sur Moodle, vérification des emplois du temps, téléchargement de documents, messagerie).
* **Nombre d'utilisateurs connectés simultanément** : Environ 5 000 à 10 000 utilisateurs répartis sur toute la journée.
* **Volume de requêtes modélisées pour la simulation** : **1 000 Cloudlets**.
* **Caractéristiques des requêtes** :
  * Requêtes de lecture légères et régulières : Longueur moyenne de **10 000 MI** (Millions d'Instructions).
  * Arrivée des requêtes étalée et uniforme dans le temps.
  * Charge CPU moyenne estimée sur les serveurs hôtes : **30 % à 45 %**.

### B. Période de Pointe (Rentrée Universitaire & Inscriptions)
* **Contexte** : Phase critique d'environ 1 à 2 semaines où les **27 000 nouveaux bacheliers** se connectent massivement au portail `pre-inscription.uae.ac.ma`, s'ajoutant aux 125 000 étudiants qui consultent leurs résultats d'examens ou de rattrapages.
* **Nombre d'utilisateurs connectés simultanément** : Jusqu'à 30 000 à 40 000 connexions concurrentes concentrées sur les mêmes plages horaires.
* **Volume de requêtes modélisées pour la simulation** : **5 000 Cloudlets** (charge multipliée par 5).
* **Caractéristiques des requêtes** :
  * Requêtes d'écriture lourdes en base de données (enregistrement d'état civil, téléversement de documents scannés, génération de récépissés PDF) : Longueur de **20 000 à 30 000 MI**.
  * Arrivée groupée (rafales de requêtes - *bursts*).
  * Risque majeur sans auto-scaling : Saturation des VM web, explosion du temps d'attente (file d'attente saturée), indisponibilité du service.

---

## 3. Synthèse des Paramètres pour la Simulation CloudSim Plus

| Paramètre de Simulation | Période Normale | Période de Pointe (Inscriptions) | Ratio de Charge |
| :--- | :---: | :---: | :---: |
| **Nombre de Cloudlets générés** | **1 000** | **5 000** | $\times 5$ |
| **Longueur unitaire moyenne** | **10 000 MI** | **20 000 MI** | $\times 2$ (requêtes plus lourdes) |
| **Charge totale d'instructions** | $10 \text{ Millions MI}$ | $100 \text{ Millions MI}$ | $\mathbf{\times 10 \text{ de charge globale}}$ |
| **Nombre initial de VM cibles** | 4 VM | 4 VM (fixes) $\rightarrow$ Élastiques avec Scaling | - |
| **Comportement attendu** | Exécution fluide, temps de réponse court | Saturation sans scaling ; absorption fluide avec auto-scaling | - |

---

## 4. Stratégie d'Adaptation en Période de Pointe (HorizontalVmScaling)

Pour que l'infrastructure de l'UAE ne s'effondre pas lors de la rentrée universitaire des 27 000 bacheliers :
* **Condition de déclenchement (Seuil)** : Dès que l'utilisation CPU moyenne des VM dépasse **70 %**.
* **Action** : Le gestionnaire d'élasticité (`HorizontalVmScaling`) instancie automatiquement de nouvelles VM sur les serveurs hôtes disponibles.
* **Objectif mesurable** : Réduire le temps d'attente des requêtes et maintenir un temps de réponse acceptable.

