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
 * Responsabilité unique : Création des Machines Virtuelles (VMs)
 * et attachement de l'auto-scaling horizontal natif (HorizontalVmScalingSimple).
 */
public class VmFactory {

    public static List<Vm> createInitialVms(boolean useTimeShared, boolean enableAutoScaling) {
        List<Vm> vms = new ArrayList<>();
        for (int i = 0; i < SimulationConfig.INITIAL_VMS; i++) {
            vms.add(createVm(useTimeShared, enableAutoScaling));
        }
        return vms;
    }

    private static Vm createVm(boolean useTimeShared, boolean enableAutoScaling) {
        CloudletScheduler scheduler = useTimeShared
                ? new CloudletSchedulerTimeShared()
                : new CloudletSchedulerSpaceShared();

        Vm vm = new VmSimple(SimulationConfig.VM_MIPS, SimulationConfig.VM_PES)
                .setRam(SimulationConfig.VM_RAM)
                .setBw(SimulationConfig.VM_BW)
                .setSize(SimulationConfig.VM_STORAGE)
                .setCloudletScheduler(scheduler);

        vm.enableUtilizationStats();

        if (enableAutoScaling) {
            attachHorizontalScaling(vm, useTimeShared);
        }
        return vm;
    }

    private static void attachHorizontalScaling(Vm vm, boolean useTimeShared) {
        HorizontalVmScaling scaling = new HorizontalVmScalingSimple();
        scaling.setVmSupplier(() -> createVm(useTimeShared, false));
        scaling.setOverloadPredicate(v ->
                v.getCpuPercentUtilization() > SimulationConfig.SCALING_CPU_OVERLOAD_THRESHOLD
                        && v.getBroker().getVmsNumber() < SimulationConfig.MAX_VMS);
        vm.setHorizontalScaling(scaling);
    }
}
