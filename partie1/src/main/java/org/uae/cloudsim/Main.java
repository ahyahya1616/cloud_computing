package org.uae.cloudsim;

import java.util.ArrayList;
import java.util.List;

/**
 * Point d'entrée principal (Main) de la simulation CloudSim Plus.
 * Coordonne les 4 scénarios d'évaluation :
 *  1. Période Normale (TimeShared)
 *  2. Période Normale (SpaceShared)
 *  3. Période de Pointe (4 VM fixes - Sans Auto-Scaling)
 *  4. Période de Pointe (Avec HorizontalVmScalingSimple)
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println(" SIMULATION CLOUDSIM PLUS - UNIVERSITÉ ABDELMALEK ESSAÂDI (UAE)");
        System.out.println(" Modélisation : 125 000 étudiants, 2 500 enseignants, 27 000 nouveaux bacheliers");
        System.out.println("================================================================================\n");

        List<SimulationResult> results = new ArrayList<>();

        // 1. Période Normale avec politique TimeShared
        results.add(SimulationEngine.executeScenario(
                "1. Période Normale (TimeShared)",
                SimulationConfig.NORMAL_CLOUDLETS_PER_WAVE,
                SimulationConfig.NORMAL_CLOUDLET_LENGTH,
                false,
                true
        ));

        // 2. Période Normale avec politique SpaceShared
        results.add(SimulationEngine.executeScenario(
                "2. Période Normale (SpaceShared)",
                SimulationConfig.NORMAL_CLOUDLETS_PER_WAVE,
                SimulationConfig.NORMAL_CLOUDLET_LENGTH,
                false,
                false
        ));

        // 3. Période de Pointe SANS Auto-Scaling (4 VM fixes)
        results.add(SimulationEngine.executeScenario(
                "3. Période de Pointe (4 VM fixes)",
                SimulationConfig.PEAK_CLOUDLETS_PER_WAVE,
                SimulationConfig.PEAK_CLOUDLET_LENGTH,
                false,
                false
        ));

        // 4. Période de Pointe AVEC HorizontalVmScalingSimple (démarre à 4 VM et s'adapte)
        results.add(SimulationEngine.executeScenario(
                "4. Période de Pointe (HorizontalVmScaling)",
                SimulationConfig.PEAK_CLOUDLETS_PER_WAVE,
                SimulationConfig.PEAK_CLOUDLET_LENGTH,
                true,
                false
        ));

        // 5. Affichage du tableau comparatif complet
        ResultPrinter.printComparisonTable(results);
    }
}
