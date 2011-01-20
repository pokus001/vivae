/**
 * This is VIVAE (Visual Vector Agent Environment)
 * a library allowing for simulations of agents in co-evolution
 * written as a bachelor project
 * by Petr Smejkal
 * at Czech Technical University in Prague
 * in 2008
 */
package vivae.example;

import vivae.arena.Arena;
import vivae.arena.parts.Active;
import vivae.controllers.VivaeController;
import vivae.util.FrictionBuffer;

import javax.swing.*;
import java.util.Vector;

public class BuildExperiment {

    private Arena arena;
    private JFrame f;
    private Vector<Active> agents;
    private VivaeController controller;

    public BuildExperiment(VivaeController controller) {
        this.controller = controller;
    }

    public void createArena(String svgFilename, boolean visible) {
        f = new JFrame("Simple Experiment");
        arena = new Arena(f);
        arena.totalStepsPerSimulation = Integer.MAX_VALUE;
        arena.loadScenario(svgFilename);
        arena.setAllArenaPartsAntialiased(true);
        f.setBounds(50, 0, arena.screenWidth, arena.screenHeight + 30);
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(arena);
        f.setVisible(visible);
        arena.isVisible = visible;
        if (!visible) {
            arena.setLoopSleepTime(0);
        }
        arena.frictionBuffer = new FrictionBuffer(arena);
        agents = arena.getActives();
    }

    public void setupAgent(int number) {       
        Active agent = agents.get(number);
        arena.registerController(agent, controller);

        if (agent instanceof FRNNControlledRobot) {
            ((FRNNControlledRobot) agent).setSensors(3, -Math.PI / 2, Math.PI / 2, 30, 30);
        }
    }

    public void setupExperiment() {
        int agentnum = agents.size();
        for (int i = 0; i < agentnum; i++) {
            setupAgent(i);
        }
        arena.init();
    }

    public void startExperiment() {
        arena.run();
    }

    public void stepExperiment() {
        arena.step();
    }

    /*
    public static void main(String[] args) {

        SimpleExperiment exp = new SimpleExperiment(new UserController2());
        exp.createArena("cfg/vivae/scenarios/arena3.svg", true);
        exp.setupExperiment();
//        FitnessFunction mot = new MovablesOnTop(exp.arena);//initialize fitness
//        FitnessFunction avg = new AverageSpeed(exp.arena);
        exp.stepExperiment();
//        System.out.println("average speed fitness = "+ avg.getFitness());
//        System.out.println("average ontop fitness = "+ mot.getFitness());
    }
    */
}