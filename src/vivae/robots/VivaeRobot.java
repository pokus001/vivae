package vivae.robots;

import vivae.arena.Arena;
import vivae.arena.parts.*;
import vivae.sensors.*;
import vivae.util.Util;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: komarivo
 * Date: Apr 27, 2010
 * Time: 9:12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class VivaeRobot implements IRobotWithSensorsInterface {

    private static int robotCount = 0;

    // graphic component that belongs to this VivaeRobot
    protected VivaeRobotRepresent robotRepresent;

    // can be used for debugging, logging etc. purposes - each command send also to duplicateRobot
    protected IRobotInterface duplicateRobot;

    // Arena, where this VivaeRobot belongs
    protected Arena arena;

    // Sensors of this robot
    protected ArrayList<ISensor> sensors;
//    protected int sensorTypesCount;
//    protected int maxOneSensorTypeCount;
    protected double[][] sensoryData;
    protected Map<Integer, ISensor> sensorsMap;



    private String name;
    private int robotNumber;
    private double left;
    private double right;

    public VivaeRobot(String name) {
        //no duplicator robot
        this(name, null);
    }

    public VivaeRobot(String name, IRobotInterface duplicateRobot ) {
        this.arena = Arena.getArena();
        this.name  = name;
        this.robotNumber = robotCount++;
        this.duplicateRobot = duplicateRobot;

        List<PositionMark> positions = arena.getPositions();
        float x = positions.get(robotNumber % positions.size()).getCenterX();
        float y = positions.get(robotNumber % positions.size()).getCenterY();
//        TODO: change this to Represent...
        robotRepresent = new FRNNControlledRobotRepresent(x, y, arena, this);
        arena.assignRobotRepresent(robotRepresent);

        sensors = new ArrayList<ISensor>();
//        sensorsMap = new HashMap<Integer, ISensor>();
    }

    public VivaeRobotRepresent getRobotRepresent() {
        return robotRepresent;
    }

//    public void decelerate(float d) {
//            robotRepresent.decelerate(d);
//    }
//
//    public void accelerate(float a) {
//       robotRepresent.accelerate(a);
//    }

//    public void rotate(float r) {
//        robotRepresent.rotate(r);
//    }

    public void setWheelSpeed(double left, double right) {
        this.left = left;
        this.right = right;

        double angle;
        double acceleration = 5.0 * (left + right);

        double desiredAngle = left - right;//max is 2
        desiredAngle *= 10;

        double speed = Math.abs(robotRepresent.getSpeed() / robotRepresent.getMaxSpeed());
        speed = Math.min(Math.max(speed, -1), 1);

        angle = desiredAngle;

        robotRepresent.rotate((float) angle);
        robotRepresent.accelerate((float) acceleration);
        robotRepresent.moveComponent();


        for (Iterator<ISensor> sIter = sensors.iterator(); sIter.hasNext();) {
                ISensor s = (ISensor) sIter.next();
                s.moveComponent();
        }


        if(duplicateRobot != null) {
            duplicateRobot.setWheelSpeed(left, right);
        }

    }

    public Arena getArena() {
        return arena;
    }

    public double[][] getSensorData() {
        int maxNeeded = 0, dsNeeded = 0, sdsNeeded = 0, sfsNeeded = 0;
        for (Iterator<ISensor> it = sensors.iterator(); it.hasNext();) {
            ISensor sensor = it.next();
            if (sensor instanceof DistanceSensor) {
                dsNeeded++;
            }
            if (sensor instanceof SurfaceFrictionSensor) {
                sfsNeeded++;
            }
            if (sensor instanceof ScalableDistanceSensor) {
                sdsNeeded += ((ScalableDistanceSensor) sensor).getCount();
            }
            if (sensor instanceof AsyncScalableDistanceSensor) {
                sdsNeeded += ((AsyncScalableDistanceSensor) sensor).getCount();
            }
        }
        maxNeeded = Math.max(Math.max(dsNeeded, sfsNeeded), sdsNeeded);

        double[][] data = new double[2][maxNeeded];
        Vector<VivaeObject> allObjects = arena.getVivaes();
        int di = 0, si = 0;
        double v;
        double vv[];
        for (Iterator<ISensor> it = sensors.iterator(); it.hasNext();) {
            ISensor sensor = it.next();
            //TODO: change iof to discriminator
            if (sensor instanceof DistanceSensor) {
                double[][] sensorData = ((DistanceSensor) sensor).getSensoryData();
                //TODO; this construction is bullshit, do something with that...
                v =  sensorData[0][0];
                data[0][di] = v;
                di++;
            }
            //TODO: change iof to discriminator
            if (sensor instanceof SurfaceFrictionSensor) {
                double[][] sensorData = ((SurfaceFrictionSensor) sensor).getSensoryData();
                v = sensorData[0][0];
                data[1][si] = Util.rescale(v, 1, 10);   // should min and max friction in the arena
                si++;
            }
            if (sensor instanceof ScalableDistanceSensor) {
                double[][] sensorData = ((ScalableDistanceSensor) sensor).getSensoryData();
                //TODO; this construction is bullshit, do something with that...
                vv =  sensorData[0];
                data[0] = vv;
                di++;
            }
            if (sensor instanceof AsyncScalableDistanceSensor) {
                double[][] sensorData = ((AsyncScalableDistanceSensor) sensor).getSensoryData();
                //TODO; this construction is bullshit, do something with that...
                vv =  sensorData[0];
                data[0] = vv;
                di++;
            }
        }
        sensoryData = data;
        return data;
    }

    public void addSensor(ISensor s) {
//        sensorsMap.put(sensors.size(), s);
        sensors.add(s);
    }

    public void addDistanceSensor(Double angle, double maxDistance) {
        ISensor s = new DistanceSensor(this, angle, maxDistance);
        addSensor(s);
    }

    public void addFrictionSensor(Double angle, double frictionDistance) {
        ISensor s = new SurfaceFrictionSensor(this, angle, frictionDistance);
        addSensor(s);
    }

    public List<ISensor> getSensors() {
        return sensors;
    }

}
