package org.uae.cloudsim;

/**
 * Responsabilité unique : Modèle de données stockant toutes les métriques d'évaluation
 * demandées (réponse, attente, exécution, utilisation hôtes, coût et VM créées).
 */
public record SimulationResult(
        String scenarioName,
        int cloudletsTotal,
        int vmsCount,
        double avgWaitTime,
        double avgExecTime,
        double avgResponseTime,
        double avgHostCpuUtilizationPercent,
        double totalCost,
        double simulationFinishTime
) {}
