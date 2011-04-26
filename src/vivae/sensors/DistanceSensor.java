package vivae.sensors;

import vivae.arena.Arena;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.robots.VivaeRobot;
import vivae.arena.parts.VivaeRobotRepresent;

/**
 * Created by IntelliJ IDEA.
 * User: mosquit
 * Date: 3/23/11
 * Time: 6:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class DistanceSensor implements ISensor {

    private vivae.arena.parts.sensors.DistanceSensor representant;
    private VivaeRobot owner;
    private Arena arena;
    private VivaeRobotRepresent ownerRepresent;
    private double maxDistance;
    private double angle;

    public DistanceSensor(IRobotWithSensorsInterface robot, double angle, double maxDistance) {

        try {
            this.owner = (VivaeRobot) robot;
            this.ownerRepresent = this.owner.getRobotRepresent();
            this.angle = angle;
            this.arena = ownerRepresent.getArena();
            this.maxDistance = maxDistance;
            representant = new vivae.arena.parts.sensors.DistanceSensor(owner, angle, 0, maxDistance);
            arena.addPaintable(representant);
        } catch (ClassCastException e) {
            //TODO: error - cannot assign VivaeSensor to non-VivaeRobot !!!
            e.printStackTrace();
        }

    }

    public double[][] getSensoryData() {
        double [][]res = new double[1][1];
        res[0][0] = representant.getDistance(owner.getArena().getVivaes());
        return res;
    }

    public void moveComponent() {
        representant.moveComponent();
    }

}
