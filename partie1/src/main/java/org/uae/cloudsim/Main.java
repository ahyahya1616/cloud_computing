package org.uae.cloudsim;

import java.util.ArrayList;
import java.util.List;

/**
 * Point d'entrée principal (Main) de la simulation CloudSim Plus.
 * Coordonne les 4 scénarios d'évaluation :
 *  1. Période Normale (TimeShared)
 *  2. Période Normale (SpaceShared)
 *  3. Période de Pointe (Inscriptions bacheliers - 4 VM fixes)
 *  4. Période de Pointe (Inscriptions bacheliers - Avec HorizontalVmScaling)
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
                SimulationConfig.NORMAL_CLOUDLETS,
                SimulationConfig.NORMAL_CLOUDLETS_LENGTH,
                false,
                true,
                SimulationConfig.INITIAL_VMS
        ));

        // 2. Période Normale avec politique SpaceShared
        results.add(SimulationEngine.executeScenario(
                "2. Période Normale (SpaceShared)",
                SimulationConfig.NORMAL_CLOUDLETS,
                SimulationConfig.NORMAL_CLOUDLETS_LENGTH,
                false,
                false,
                SimulationConfig.INITIAL_VMS
        ));

        // 3. Période de Pointe SANS Auto-Scaling (Charge lourde sur 4 VM fixes)
        results.add(SimulationEngine.executeScenario(
                "3. Période de Pointe (Fixe 4 VM - Inscriptions)",
                SimulationConfig.PEAK_CLOUDLETS,
                SimulationConfig.PEAK_CLOUDLETS_LENGTH,
                false,
                false, // SpaceShared : provoque une file d'attente massive montrant la saturation !
                4
        ));

        // 4. Période de Pointe AVEC Auto-Scaling (HorizontalVmScaling : absorption du pic à 12 VM)
        results.add(SimulationEngine.executeScenario(
                "4. Période de Pointe AVEC Auto-Scaling (HorizontalVmScaling)",
                SimulationConfig.PEAK_CLOUDLETS,
                SimulationConfig.PEAK_CLOUDLETS_LENGTH,
                true,
                false, // Même ordonnanceur mais avec élasticité horizontale (12 VM allouées)
                12
        ));

        // 5. Affichage du tableau comparatif
        ResultPrinter.printComparisonTable(results);
    }
}

