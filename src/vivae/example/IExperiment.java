package vivae.example;

import vivae.robots.IRobotInterface;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mosquit
 * Date: 3/16/11
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IExperiment {

    List<IRobotInterface> getRobots();
    int getStepsDone();
}
