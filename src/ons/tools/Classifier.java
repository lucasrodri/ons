/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.tools;

//import java.io.InputStream;
//import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author lucasrc Classificado do ONS para leitura de redes neurais
 */
public class Classifier {

    //private final MultiLayerNetwork model;

    public Classifier() throws Exception {
        String file1 = "classifier_SNR_LF.h5";
        String file2 = "/tmp/classifier_SNR_LF.h5";
        String file3 = "resources/classifier_SNR_LF.h5";
        //InputStream inputStream4 = getClass().getClassLoader().getResourceAsStream(file3);
        //InputStream inputStream4 = getClass().getResourceAsStream(file1);
        //this.model = KerasModelImport.importKerasSequentialModelAndWeights(inputStream4);
    }

    public int classify(double[] raw_input) {
        //INDArray input = Nd4j.create(scaler(raw_input));
        //INDArray output = model.output(input);
        //return output.getInt(0);
        return 0;
    }

    private static double[][] scaler(double[] values) {
        assert values.length == 7;

        double[][] scaled = {{0, 0, 0, 0, 0, 0, 0}};
        double[] scale = {119.83552861, 1.42122914, 1.31913874, 12.90645335, 1283.0616716, 45.84544104, 11.46346616};
        double[] mean = {1.67900462e+02, 3.11090588e+00, 4.43886897e+00, 4.27830029e+01, 4.23752228e+03, 9.29964257e+01,
            3.18708094e+01};

        for (int i = 0; i < 7; i++) {
            scaled[0][i] = (values[i] - mean[i]) / scale[i];
        }

        return scaled;
    }
}
