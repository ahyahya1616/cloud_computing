package org.uae.cloudsim;

/**
 * Constantes matérielles et dimensionnement du Cloud de l'Université Abdelmalek Essaâdi (UAE).
 * Modélise 2 datacenters (Tétouan et Tanger) pour 125 000 étudiants et 2 500 enseignants.
 */
public final class SimulationConfig {

    private SimulationConfig() {}

    // Serveurs physiques hôtes (Hosts)
    public static final int HOSTS_COUNT = 6;
    public static final int HOST_PES = 32;
    public static final long HOST_MIPS = 10000;
    public static final long HOST_RAM = 256 * 1024;    // 256 Go
    public static final long HOST_BW = 10000;          // 10 Gbps
    public static final long HOST_STORAGE = 2000000;   // 2 To

    // Machines virtuelles (VMs)
    public static final int INITIAL_VMS = 4;
    public static final int VM_PES = 4;                // 4 vCPU
    public static final long VM_MIPS = 5000;
    public static final long VM_RAM = 16 * 1024;       // 16 Go
    public static final long VM_BW = 1000;             // 1 Gbps
    public static final long VM_STORAGE = 50000;       // 50 Go

    // Période Normale (activités régulières Moodle, mails, consultation)
    public static final int NORMAL_CLOUDLETS = 200;
    public static final long NORMAL_CLOUDLETS_LENGTH = 10000; // MI

    // Période de Pointe (Inscriptions des 27 000 nouveaux bacheliers)
    public static final int PEAK_CLOUDLETS = 800;
    public static final long PEAK_CLOUDLETS_LENGTH = 20000;   // MI

    // Seuil de déclenchement du HorizontalVmScaling
    public static final double SCALING_CPU_OVERLOAD_THRESHOLD = 0.70; // 70% CPU
}

