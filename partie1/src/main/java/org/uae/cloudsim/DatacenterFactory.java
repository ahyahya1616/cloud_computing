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
 * Responsabilité unique : Création et instanciation du Datacenter universitaire de l'UAE
 * avec ses serveurs hôtes physiques multi-cœurs.
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
            hosts.add(host);
        }
        return new DatacenterSimple(simulation, hosts, new VmAllocationPolicySimple())
                .setSchedulingInterval(10.0); // Évaluation toutes les 10 secondes (rapide et efficace)
    }
}

