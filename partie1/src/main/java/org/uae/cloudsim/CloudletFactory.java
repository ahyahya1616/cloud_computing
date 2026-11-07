package org.uae.cloudsim;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsabilité unique : Génération des charges de travail (Cloudlets)
 * représentant les requêtes des étudiants et enseignants.
 */
public class CloudletFactory {

    public static List<Cloudlet> createCloudlets(int count, long length) {
        List<Cloudlet> cloudlets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Cloudlet cloudlet = new CloudletSimple(i, length, 1)
                    .setFileSize(300)
                    .setOutputSize(300)
                    .setUtilizationModelCpu(new UtilizationModelDynamic(0.8))
                    .setUtilizationModelRam(new UtilizationModelDynamic(0.0001)); // Empreinte RAM réaliste d'une requête HTTP
            cloudlets.add(cloudlet);
        }
        return cloudlets;
    }
}

