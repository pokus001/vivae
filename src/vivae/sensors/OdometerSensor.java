package vivae.sensors;

import vivae.arena.parts.VivaeRobotRepresent;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.robots.VivaeRobot;

/**
 * Created by IntelliJ IDEA.
 * User: mosquit
 * Date: 3/23/11
 * Time: 6:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class OdometerSensor implements ISensor {

    //private Sensor representant;
    private VivaeRobot owner;
    private VivaeRobotRepresent ownerRepresent;
    private double maxDistance;

    public OdometerSensor(IRobotWithSensorsInterface robot) {

        try {
            this.owner = (VivaeRobot) robot;
            this.ownerRepresent = this.owner.getRobotRepresent();
        } catch (ClassCastException e) {
            //TODO: error - cannot assign VivaeSensor to non-VivaeRobot !!!
            e.printStackTrace();
        }
    }

    public double[][] getSensoryData() {
        double res[][] = new double[1][1];
        res[0][0] = ownerRepresent.odometer;
        return res;
    }

    public void moveComponent() {
        // no need to do anything - no representative
        return;
    }

}
