package org.uae.cloudsim;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsabilité unique : Création du Datacenter universitaire de l'UAE,
 * activation des statistiques d'utilisation des hôtes et configuration des coûts.
 */
public class DatacenterFactory {

    public static Datacenter createUaeDatacenter(Simulation simulation) {
        List<Host> hosts = new ArrayList<>();
        for (int i = 0; i < SimulationConfig.HOSTS_COUNT; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < SimulationConfig.HOST_PES; j++) {
                peList.add(new PeSimple(SimulationConfig.HOST_MIPS));
            }
            Host host = new HostSimple(
                    SimulationConfig.HOST_RAM,
                    SimulationConfig.HOST_BW,
                    SimulationConfig.HOST_STORAGE,
                    peList
            ).setVmScheduler(new VmSchedulerTimeShared());
            host.enableUtilizationStats();
            hosts.add(host);
        }

        Datacenter datacenter = new DatacenterSimple(simulation, hosts, new VmAllocationPolicySimple())
                .setSchedulingInterval(SimulationConfig.DATACENTER_SCHEDULING_INTERVAL);

        datacenter.getCharacteristics()
                .setCostPerSecond(SimulationConfig.COST_PER_SECOND)
                .setCostPerMem(SimulationConfig.COST_PER_MEM)
                .setCostPerStorage(SimulationConfig.COST_PER_STORAGE)
                .setCostPerBw(SimulationConfig.COST_PER_BW);

        return datacenter;
    }
}
