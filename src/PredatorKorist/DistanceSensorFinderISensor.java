package PredatorKorist;

import vivae.arena.Arena;
import vivae.arena.parts.VivaeRobotRepresent;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.robots.VivaeRobot;
import vivae.sensors.DistanceSensor;
import vivae.sensors.ISensor;

public class DistanceSensorFinderISensor implements ISensor{
    private DistanceSensorFinder representantDist;
    private DirectionSensorFinder representantDir;
    private Target target;
    private VivaeRobot owner;
    private Arena arena;
    private VivaeRobotRepresent ownerRepresent;
    private double maxDistance;
    private double angle;
    private double avg = 0;
    private double avg0 = 0;
    private double steps = 0;
	public DistanceSensorFinderISensor(IRobotWithSensorsInterface robot, Target t) {
	try {
            this.owner = (VivaeRobot) robot;
            this.ownerRepresent = this.owner.getRobotRepresent();
            
            this.arena = Arena.getArena();
            
            representantDir = new DirectionSensorFinder(owner, angle, 0, maxDistance, t);
            representantDist = new DistanceSensorFinder(owner, angle, 0, maxDistance, t);
            arena.addPaintable(representantDir);
            arena.addPaintable(representantDist);
        } catch (ClassCastException e) {
            //TODO: error - cannot assign VivaeSensor to non-VivaeRobot !!!
            e.printStackTrace();
        }
		setTarget( t );
	}
	
	public void setTarget(Target t){
		representantDir.setTarget( t );
		representantDist.setTarget( t );
		target = t;
	}
	public double getAverage(){
		return 0.5 + (avg / (2*steps));
	}

    public double[][] getSensoryData() {
        double [][]res = new double[1][2];
        double dir = representantDir.getDistance(owner.getArena().getVivaes());
        res[0][0] = dir;
        //if ( dir < 0 ) res[0][2] = 180 + dir;
        //if ( dir >= 0 ) res[0][2] = -180 + dir;
        res[0][1] = representantDist.getDistance(owner.getArena().getVivaes());
    //    System.out.println( "Dir1: " + res[0][0] );
  //      System.out.println( "Dir2: " + res[0][1] );
//        System.out.println( "Distance: " + res[0][2] );
        if ( steps == 0 ) {
        	
        	avg0 = res[0][1];
//        	System.out.println( "Nulaa " +  avg0 );
        }
        
        if ( res[0][1] > avg0 ) {
        	avg0 = res[0][1];
        	avg += res[0][1];
        }
        
        if ( res[0][1] < avg0 ) avg -= res[0][1];
        //System.out.println( "Nulaa " +  (res[0][1] - avg0) );
        
        
        steps++;
        return res;
    }

    public void moveComponent() {
        representantDir.moveComponent();
        representantDist.moveComponent();
    }

	public void randomize() {
		Target t = new Target( (int)(Math.random() * 1000)%640 , (int)(Math.random() * 1000)%480);
		setTarget(t);
	}

	public Target getTarget() {
		return target;
	}
}
