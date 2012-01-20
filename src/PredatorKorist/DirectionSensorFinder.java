package PredatorKorist;

import java.util.Vector;

import vivae.arena.parts.VivaeObject;
import vivae.arena.parts.sensors.DistanceSensor;
import vivae.robots.VivaeRobot;

public class DirectionSensorFinder extends DistanceSensor{
	
	Target myTarget;
	
	public DirectionSensorFinder(VivaeRobot owner, double angle, int number,
			double maxDistance, Target t) {
		super(owner, angle, number, maxDistance);
		myTarget = t;
	}

	public void setTarget(Target t){
		myTarget = t;
	}

	@Override
    public double getDistance(Vector<VivaeObject> objects){
		
		
			double x = owner.getRobotRepresent().getX();
			
		
			double y = owner.getRobotRepresent().getY();
			
			
			double natoceniRobota = 180.0 * owner.getRobotRepresent().getDirection() / Math.PI;
			
			//System.out.println("UhelNatoceni: " + natoceniRobota );
			
			double vektorX = myTarget.getX() - x;
			double vektorY = myTarget.getY() - y;
			vektorY = -vektorY;
			//System.out.println("My target: " + myTarget.getX()+ " / " + myTarget.getY() );
	
			double absUhel = 0;
			if ( vektorY != 0 ) absUhel = Math.atan( Math.abs( vektorX ) / Math.abs( vektorY ) );
			absUhel = 180.0 * (double)absUhel / Math.PI;
			if ( vektorY == 0 && vektorX > 0 ) absUhel = 90;
			if ( vektorY == 0 && vektorX < 0 ) absUhel = -90;
			
			if ( vektorX < 0 && vektorY > 0 ) absUhel = -absUhel;
			if ( vektorX < 0 && vektorY < 0 ) absUhel = - 180 + absUhel;
			if ( vektorX > 0 && vektorY < 0 ) absUhel = 180 - absUhel;
			//System.out.println("UhelAbs: " + absUhel );
			
			double uhelMezi = ( absUhel - natoceniRobota ) % 360;
			if ( uhelMezi > 180 ) uhelMezi = -360 + uhelMezi; 
			
			return uhelMezi;
		
       
        

    }

}
