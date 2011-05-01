package vivae.sensors;

import vivae.arena.Arena;
import vivae.robots.VivaeRobot;
import vivae.arena.parts.VivaeRobotRepresent;
import vivae.robots.IRobotWithSensorsInterface;

/**
 * Created by IntelliJ IDEA.
 * User: mosquit
 * Date: 3/23/11
 * Time: 6:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class SurfaceFrictionSensor implements ISensor {

    protected vivae.arena.parts.sensors.SurfaceFrictionSensor representant;
    protected VivaeRobot owner;
    protected VivaeRobotRepresent ownerRepresent;
    protected double frictionDistance;
    protected double angle;
    protected Arena arena;
    protected double maxDistance;

    public SurfaceFrictionSensor(IRobotWithSensorsInterface robot, double angle, double frictionDistance) {

        try {
            this.owner = (VivaeRobot) robot;
            this.ownerRepresent = this.owner.getRobotRepresent();
            this.angle = angle;
            this.arena = Arena.getArena();
            this.frictionDistance = frictionDistance;
            representant = new vivae.arena.parts.sensors.SurfaceFrictionSensor(owner, angle, 0,  frictionDistance);
            arena.addPaintable(representant);
        } catch (Exception e) {
            //TODO: error - cannot assign VivaeSensor to non-VivaeRobot !!!
            e.printStackTrace();
        }

    }

    public double[][] getSensoryData() {
        double res[][] = new double[1][1];
        res[0][0] = representant.getSurfaceFriction();
        return res;
    }

    public void moveComponent() {
        representant.moveComponent();
    }

}
