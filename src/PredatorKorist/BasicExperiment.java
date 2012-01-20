package PredatorKorist;

import vivae.robots.IRobotInterface;
import vivae.controllers.VivaeController;
import vivae.example.IExperiment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mosquit
 * Date: 3/16/11
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class BasicExperiment implements IExperiment{

    protected int stepsDone;
    protected ArrayList<IRobotInterface> robots = null;
    protected ArrayList<VivaeController> controllers = null;

    public int getStepsDone() {
        return stepsDone;
    }

    public List<IRobotInterface> getRobots() {
        return robots;
    }

}
