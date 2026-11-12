package org.uae.cloudsim;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsabilité unique : Génération des charges de travail (Cloudlets)
 * représentant une vague de requêtes web des étudiants et enseignants.
 */
public class CloudletFactory {

    public static List<Cloudlet> createWaveCloudlets(int startId, int count, long length) {
        List<Cloudlet> cloudlets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Cloudlet cloudlet = new CloudletSimple(startId + i, length, 1)
                    .setFileSize(300)
                    .setOutputSize(300)
                    .setUtilizationModelCpu(new UtilizationModelDynamic(0.8))
                    .setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
            cloudlets.add(cloudlet);
        }
        return cloudlets;
    }
}
