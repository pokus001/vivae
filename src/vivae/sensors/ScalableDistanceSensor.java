package vivae.sensors;

import vivae.arena.Arena;
import vivae.arena.parts.VivaeRobotRepresent;
import vivae.arena.parts.sensors.DistanceSensor;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.robots.VivaeRobot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mosquit
 * Date: 3/23/11
 * Time: 6:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScalableDistanceSensor implements ISensor {

    protected List<vivae.arena.parts.sensors.DistanceSensor> representants;
    protected VivaeRobot owner;
    protected Arena arena;
    protected VivaeRobotRepresent ownerRepresent;
    protected double maxDistance;
    protected double sangle;
    protected double eangle;
    protected int count;

    public ScalableDistanceSensor(IRobotWithSensorsInterface robot, double sangle, double eangle, int count, double maxDistance) {

        try {
            this.owner = (VivaeRobot) robot;
            this.ownerRepresent = this.owner.getRobotRepresent();
            this.sangle = sangle;
            this.eangle = eangle;
            this.count = count;
            this.arena = Arena.getArena();
            this.maxDistance = maxDistance;

            this.representants = new ArrayList<DistanceSensor>();

            double ai = (eangle - sangle) / (count - 1);
            for(int i = 0; i < count; i++) {
                DistanceSensor representant = new vivae.arena.parts.sensors.DistanceSensor(owner, sangle + i*ai, 0, maxDistance);
                this.representants.add(representant);
                arena.addPaintable(representant);
            }

        } catch (ClassCastException e) {
            //TODO: error - cannot assign VivaeSensor to non-VivaeRobot !!!
            e.printStackTrace();
        }

    }

    public double[][] getSensoryData() {
        double [][]res = new double[1][count];
        for(int i = 0; i < count; i++) {
            res[0][i] = representants.get(i).getDistance(owner.getArena().getVivaes());
        }
        return res;
    }

    public void moveComponent() {
        for(int i = 0; i<count; i++) {
            representants.get(i).moveComponent();
        }
    }

    public int getCount() {
        return count;
    }
}
