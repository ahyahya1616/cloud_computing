package org.uae.cloudsim;

import java.util.List;

/**
 * Responsabilité unique : Formatage et affichage soigné des résultats comparatifs.
 */
public class ResultPrinter {

    public static void printComparisonTable(List<SimulationResult> results) {
        System.out.println("=========================================================================================================");
        System.out.println(" SYNTHÈSE COMPARATIVE DES RÉSULTATS (CLOUDSIM PLUS - UAE)");
        System.out.println("=========================================================================================================");
        System.out.printf("%-45s | %-8s | %-6s | %-12s | %-12s | %-10s\n",
                "Scénario Évalué", "Cloudlets", "VMs", "Attente Moy", "Exéc Moy", "Fin Simu");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        for (SimulationResult r : results) {
            System.out.printf("%-45s | %-8d | %-6d | %-10.2f s | %-10.2f s | %-8.2f s\n",
                    r.scenarioName(),
                    r.cloudletsTotal(),
                    r.vmsCount(),
                    r.avgWaitTime(),
                    r.avgExecTime(),
                    r.simulationFinishTime());
        }
        System.out.println("=========================================================================================================\n");
    }
}

