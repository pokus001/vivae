package PredatorKorist;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Random;
import java.util.Vector;

import vivae.arena.parts.VivaeObject;
import vivae.arena.parts.sensors.DistanceSensor;
import vivae.robots.VivaeRobot;
import vivae.util.PathIntersection;

public class DistanceSensorFinder extends DistanceSensor {

	Target myTarget;
	
	
	
	public String osa;
	
	public DistanceSensorFinder(VivaeRobot owner, double angle, int number,
			double maxDistance, Target t) {
		super(owner, angle, number, maxDistance);
		myTarget = t;
	}

	@Override
    public double getDistance(Vector<VivaeObject> objects){
			double destX = myTarget.getX();
			double destY = myTarget.getY();
			double x = owner.getRobotRepresent().getX();
			//owner.getRobotRepresent().getDirection()
			double diffx = (destX - x) * (destX - x);
			//System.out.println("XX: " + 0.5 + ( diff / 640.0 ) / 2.0);
			
		
			double y = owner.getRobotRepresent().getY();
			double diffy = (destY - y)*(destY - y);

			return 1 - Math.sqrt( diffx + diffy ) / 800.0;

    }
	
	public void setTarget(Target t){
		myTarget = t;
	}
}
