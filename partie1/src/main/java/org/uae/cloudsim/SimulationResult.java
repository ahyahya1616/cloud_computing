package org.uae.cloudsim;

/**
 * Responsabilité unique : Modèle de données pour stocker les métriques d'évaluation
 * d'un scénario de simulation CloudSim Plus.
 */
public record SimulationResult(
        String scenarioName,
        int cloudletsTotal,
        int cloudletsFinished,
        int vmsCount,
        double avgExecTime,
        double avgWaitTime,
        double simulationFinishTime
) {}

