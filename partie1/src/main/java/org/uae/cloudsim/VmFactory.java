package org.uae.cloudsim;

import org.cloudsimplus.autoscaling.HorizontalVmScaling;
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletScheduler;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsabilité unique : Création et configuration des Machines Virtuelles (VMs)
 * et gestion de leur stratégie d'auto-scaling horizontal (HorizontalVmScaling).
 */
public class VmFactory {

    public static List<Vm> createVms(boolean useTimeShared, int count) {
        List<Vm> vms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CloudletScheduler scheduler = useTimeShared
                    ? new CloudletSchedulerTimeShared()
                    : new CloudletSchedulerSpaceShared();

            Vm vm = new VmSimple(i, SimulationConfig.VM_MIPS, SimulationConfig.VM_PES)
                    .setRam(SimulationConfig.VM_RAM)
                    .setBw(SimulationConfig.VM_BW)
                    .setSize(SimulationConfig.VM_STORAGE)
                    .setCloudletScheduler(scheduler);

            vms.add(vm);
        }
        return vms;
    }

    public static Vm createSingleVm(int id) {
        return new VmSimple(id, SimulationConfig.VM_MIPS, SimulationConfig.VM_PES)
                .setRam(SimulationConfig.VM_RAM)
                .setBw(SimulationConfig.VM_BW)
                .setSize(SimulationConfig.VM_STORAGE)
                .setCloudletScheduler(new CloudletSchedulerTimeShared());
    }
}

