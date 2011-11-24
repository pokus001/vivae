package vivae.example;

import vivae.fitness.AverageSpeed;
import vivae.fitness.FitnessFunction;
import vivae.util.Util;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: koutnij
 * Date: Nov 11, 2009
 * Time: 3:03:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class RandomSearch {

    public double[][][] search(String scenario, int sensors, int neurons, int evals) throws IOException {
        TestExperiment exp = new TestExperiment();
        double bestfit = 0;
        double fit;
        double[][][] res = null;
        for (int i = 0; i < evals; i++) {
            // the 3D matrix has toplevel size of 1, thus it is copied to all controllers
            double[][][] wm = Util.randomArray3D(1, neurons, 2 * sensors + neurons + 1, -15, 15);
            exp.setupExperiment(sensors, wm, scenario, false);

//            exp.setupExperiment(wm, 50, 25);
            FitnessFunction avg = new AverageSpeed(exp);

            exp.startExperiment();
            fit = avg.getFitness();
            System.out.println("evaluation = " + i + " fitness = " + fit);
            if (fit > bestfit) {
                bestfit = fit;
                res = wm;
            }
        }
        System.out.println("Best fitness: " + bestfit);
        return res;
    }

    public void play(String scenario, int sensors, double[][][] wm) throws IOException {
        TestExperiment exp = new TestExperiment();

        exp.setupExperiment(sensors, wm, scenario, true);

        FitnessFunction avg = new AverageSpeed(exp);
        exp.startExperiment();
        System.out.println("\nbest fitness = " + avg.getFitness());
    }

    public static void main(String[] arg) throws IOException {
//        String scenario = "cfg/vivae/scenarios/arena1.svg";
        String scenario = "cfg/vivae/scenarios/distance3_h.svg";

        RandomSearch s = new RandomSearch();
        int neurons = 2;
        int sensors = 5;
        int evaluations = 5;
//        int evaluations = 3;
        double[][][] wmbest = s.search(scenario, sensors, neurons, evaluations);
//        System.out.println(Util.toString2Darray(wmbest, ","));
        String someString = JOptionPane.showInputDialog("Show?");
        s.play(scenario, sensors, wmbest); // play the best one
    }
}
