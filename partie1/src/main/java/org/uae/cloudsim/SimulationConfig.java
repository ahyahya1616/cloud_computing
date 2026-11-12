package org.uae.cloudsim;

/**
 * Constantes matérielles, tarification et paramètres de charge du Cloud de l'Université Abdelmalek Essaâdi (UAE).
 * Modélise une infrastructure universitaire réaliste (125 000 étudiants, 2 500 enseignants, 27 000 nouveaux bacheliers).
 */
public final class SimulationConfig {

    private SimulationConfig() {}

    // Serveurs physiques hôtes (Hosts) - Cluster Dell PowerEdge R750 (4 serveurs x 16 cœurs = 64 vCPU)
    public static final int HOSTS_COUNT = 4;
    public static final int HOST_PES = 16;
    public static final long HOST_MIPS = 5000;
    public static final long HOST_RAM = 64 * 1024;     // 64 Go (en Mo)
    public static final long HOST_BW = 10000;          // 10 Gbps (en Mbps)
    public static final long HOST_STORAGE = 2000000;   // 2 To (en Mo)

    // Machines virtuelles (VMs)
    public static final int INITIAL_VMS = 4;
    public static final int MAX_VMS = 16;              // Capacité maximale du cluster (64 vCPU / 4 vCPU)
    public static final int VM_PES = 4;                // 4 vCPU
    public static final long VM_MIPS = 5000;           // 5 000 MIPS / vCPU (20 000 MIPS / VM)
    public static final long VM_RAM = 8 * 1024;        // 8 Go (en Mo)
    public static final long VM_BW = 1000;             // 1 Gbps (en Mbps)
    public static final long VM_STORAGE = 50000;       // 50 Go (en Mo)

    // Modèle d'arrivée en vagues réalistes (10 vagues espacées de 1 seconde)
    public static final int WAVES_COUNT = 10;
    public static final double WAVE_INTERVAL_SECONDS = 1.0;

    // Période Normale : requêtes web légères (consultation Moodle, messagerie)
    public static final int NORMAL_CLOUDLETS_PER_WAVE = 20;     // 200 requêtes au total
    public static final long NORMAL_CLOUDLET_LENGTH = 1500;     // 1 500 MI (~0.38 s par requête)

    // Période de Pointe : inscriptions massives des 27 000 bacheliers
    public static final int PEAK_CLOUDLETS_PER_WAVE = 100;      // 1 000 requêtes au total (x5)
    public static final long PEAK_CLOUDLET_LENGTH = 3000;       // 3 000 MI (~0.75 s par requête, charge totale x10)

    // Auto-Scaling Horizontal (HorizontalVmScaling)
    public static final double SCALING_CPU_OVERLOAD_THRESHOLD = 0.70; // Seuil > 70% CPU
    public static final double DATACENTER_SCHEDULING_INTERVAL = 1.0;  // Évaluation chaque seconde

    // Tarification CloudSim ($) pour calculer le coût d'infrastructure
    public static final double COST_PER_SECOND = 0.05;    // $/s de CPU serveur
    public static final double COST_PER_MEM = 0.0002;     // $/Mo de RAM allouée
    public static final double COST_PER_STORAGE = 0.00005;// $/Mo de disque
    public static final double COST_PER_BW = 0.0002;      // $/Mbps de bande passante
}
