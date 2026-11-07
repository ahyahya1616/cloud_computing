package org.uae.cloudsim;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.vms.Vm;

import java.util.List;

/**
 * Responsabilité unique : Orchestration et exécution d'un scénario de simulation.
 */
public class SimulationEngine {

    public static SimulationResult executeScenario(
            String name,
            int cloudletCount,
            long cloudletLength,
            boolean enableScaling,
            boolean useTimeShared,
            int vmCount) {

        System.out.println(">>> Lancement : " + name);
        CloudSimPlus simulation = new CloudSimPlus();

        // 1. Datacenter UAE
        Datacenter datacenter = DatacenterFactory.createUaeDatacenter(simulation);

        // 2. Broker utilisateur
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);

        // 3. Machines Virtuelles
        List<Vm> vms = VmFactory.createVms(useTimeShared, vmCount);
        broker.submitVmList(vms);

        // 4. Requêtes étudiantes (Cloudlets)
        List<Cloudlet> cloudlets = CloudletFactory.createCloudlets(cloudletCount, cloudletLength);
        broker.submitCloudletList(cloudlets);

        // Si l'auto-scaling est activé en période de pointe, écouter l'horloge pour ajouter des VM
        if (enableScaling) {
            simulation.addOnClockTickListener(evt -> {
                double meanCpu = broker.getVmCreatedList().stream()
                        .mapToDouble(Vm::getCpuPercentUtilization)
                        .average().orElse(0.0);

                if (meanCpu > SimulationConfig.SCALING_CPU_OVERLOAD_THRESHOLD && broker.getVmCreatedList().size() < 12) {
                    Vm extraVm = VmFactory.createSingleVm(broker.getVmCreatedList().size());
                    broker.submitVm(extraVm);
                }
            });
        }

        // 5. Exécution de la simulation
        simulation.start();

        // 6. Extraction des métriques
        List<Cloudlet> finished = broker.getCloudletFinishedList();
        double totalCpuTime = finished.stream().mapToDouble(Cloudlet::getActualCpuTime).sum();
        double avgExecTime = finished.isEmpty() ? 0 : totalCpuTime / finished.size();
        double finishTime = finished.stream().mapToDouble(Cloudlet::getFinishTime).max().orElse(0.0);
        double avgWaitTime = finished.stream().mapToDouble(Cloudlet::getWaitingTime).average().orElse(0.0);
        int finalVmCount = broker.getVmCreatedList().size();

        System.out.printf("   [Terminé] Cloudlets traités: %d | Durée simu: %.2f s | Attente moy: %.2f s | VM finales: %d\n\n",
                finished.size(), finishTime, avgWaitTime, finalVmCount);

        return new SimulationResult(
                name,
                cloudletCount,
                finished.size(),
                finalVmCount,
                avgExecTime,
                avgWaitTime,
                finishTime
        );
    }
}

