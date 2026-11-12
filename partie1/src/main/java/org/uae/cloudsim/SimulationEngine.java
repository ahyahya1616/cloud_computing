package org.uae.cloudsim;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimEntity;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.events.SimEvent;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmCost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsabilité unique : Orchestration d'un scénario de simulation,
 * soumission dynamique des vagues de requêtes et collecte des métriques (latence, CPU hôtes, coût).
 */
public class SimulationEngine {

    public static SimulationResult executeScenario(
            String name,
            int cloudletsPerWave,
            long cloudletLength,
            boolean enableScaling,
            boolean useTimeShared) {

        System.out.println(">>> Lancement : " + name);
        CloudSimPlus simulation = new CloudSimPlus();

        // 1. Datacenter UAE
        Datacenter datacenter = DatacenterFactory.createUaeDatacenter(simulation);

        // 2. Broker utilisateur (avec mapper conservant le Datacenter pour les VMs ajoutées à chaud par l'auto-scaling)
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
        broker.setDatacenterMapper((lastDc, vm) ->
                vm.getLastTriedDatacenter().equals(datacenter) ? Datacenter.NULL : datacenter);

        // 3. Création des 4 VMs initiales (avec ou sans HorizontalVmScalingSimple)
        List<Vm> initialVms = VmFactory.createInitialVms(useTimeShared, enableScaling);
        broker.submitVmList(initialVms);

        // 4. Soumission réaliste des requêtes par vagues dynamiques (1 vague toutes les 1 s)
        int totalCloudlets = SimulationConfig.WAVES_COUNT * cloudletsPerWave;
        broker.submitCloudletList(CloudletFactory.createWaveCloudlets(0, cloudletsPerWave, cloudletLength));

        new CloudSimEntity(simulation) {
            @Override
            protected void startInternal() {
                for (int wave = 1; wave < SimulationConfig.WAVES_COUNT; wave++) {
                    double delay = wave * SimulationConfig.WAVE_INTERVAL_SECONDS;
                    schedule(this, delay, CloudSimTag.NONE, wave);
                }
            }

            @Override
            public void processEvent(SimEvent evt) {
                if (evt.getData() instanceof Integer wave) {
                    List<Cloudlet> batch = CloudletFactory.createWaveCloudlets(
                            wave * cloudletsPerWave,
                            cloudletsPerWave,
                            cloudletLength
                    );
                    broker.submitCloudletList(batch);
                    if (wave == SimulationConfig.WAVES_COUNT - 1) {
                        shutdown();
                    }
                }
            }
        };

        // Capture du coût cumulé de chaque VM avant sa destruction en fin de simulation
        Map<Vm, Double> vmMaxCosts = new HashMap<>();
        simulation.addOnClockTickListener(evt -> {
            for (Vm vm : broker.getVmExecList()) {
                double cost = new VmCost(vm).getTotalCost();
                vmMaxCosts.merge(vm, cost, Math::max);
            }
        });

        // 5. Exécution de la simulation
        simulation.start();

        // 6. Extraction des métriques complètes
        List<Cloudlet> finished = broker.getCloudletFinishedList();
        double avgWaitTime = finished.stream().mapToDouble(Cloudlet::getWaitingTime).average().orElse(0.0);
        double avgExecTime = finished.stream().mapToDouble(Cloudlet::getActualCpuTime).average().orElse(0.0);
        double avgResponseTime = finished.stream()
                .mapToDouble(c -> c.getFinishTime() - c.getArrivalTime())
                .average().orElse(0.0);
        double finishTime = finished.stream().mapToDouble(Cloudlet::getFinishTime).max().orElse(0.0);
        int finalVmCount = broker.getVmCreatedList().size();
        double totalCost = vmMaxCosts.values().stream().mapToDouble(Double::doubleValue).sum();

        double avgHostCpuPercent = datacenter.getHostList().stream()
                .mapToDouble(h -> h.getCpuUtilizationStats().getMean() * 100.0)
                .average().orElse(0.0);

        System.out.printf("   [Terminé] Tasks: %d | VMs finales: %d | Réponse moy: %.2f s | CPU hôtes moy: %.2f %% | Coût: %.2f $ | Fin: %.2f s\n\n",
                finished.size(), finalVmCount, avgResponseTime, avgHostCpuPercent, totalCost, finishTime);

        return new SimulationResult(
                name,
                totalCloudlets,
                finalVmCount,
                avgWaitTime,
                avgExecTime,
                avgResponseTime,
                avgHostCpuPercent,
                totalCost,
                finishTime
        );
    }
}
