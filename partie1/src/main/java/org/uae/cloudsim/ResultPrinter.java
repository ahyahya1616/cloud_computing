package org.uae.cloudsim;

import java.util.List;

/**
 * Responsabilité unique : Formatage et affichage du tableau comparatif complet.
 */
public class ResultPrinter {

    public static void printComparisonTable(List<SimulationResult> results) {
        System.out.println("=============================================================================================================================");
        System.out.println(" SYNTHÈSE COMPARATIVE DES RÉSULTATS (CLOUDSIM PLUS - UAE)");
        System.out.println("=============================================================================================================================");
        System.out.printf("%-42s | %-6s | %-4s | %-10s | %-10s | %-11s | %-10s | %-9s | %-9s\n",
                "Scénario Évalué", "Tasks", "VMs", "Attente", "Exécution", "Réponse Moy", "CPU Hôtes", "Coût ($)", "Fin Simu");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        for (SimulationResult r : results) {
            System.out.printf("%-42s | %-6d | %-4d | %8.2f s | %8.2f s | %9.2f s | %8.2f %% | %7.2f $ | %7.2f s\n",
                    r.scenarioName(),
                    r.cloudletsTotal(),
                    r.vmsCount(),
                    r.avgWaitTime(),
                    r.avgExecTime(),
                    r.avgResponseTime(),
                    r.avgHostCpuUtilizationPercent(),
                    r.totalCost(),
                    r.simulationFinishTime());
        }
        System.out.println("=============================================================================================================================\n");
    }
}
