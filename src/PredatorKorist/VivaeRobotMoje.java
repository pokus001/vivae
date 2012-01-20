package PredatorKorist;

import vivae.arena.Arena;
import vivae.arena.parts.*;
import vivae.robots.IRobotInterface;
import vivae.sensors.*;
import vivae.util.Util;

import java.util.*;

import PredatorKorist.DistanceSensorFinder;
import PredatorKorist.DistanceSensorFinderISensor;

/**
 * Created by IntelliJ IDEA.
 * User: komarivo
 * Date: Apr 27, 2010
 * Time: 9:12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class VivaeRobotMoje extends vivae.robots.VivaeRobot {


    public VivaeRobotMoje(String name, IRobotInterface duplicateRobot) {
		super(name, duplicateRobot);
		// TODO Auto-generated constructor stub
	}

	public VivaeRobotMoje(String string) {
		super( string );
		// TODO Auto-generated constructor stub
	}

	@Override
    public double[][] getSensorData() {
        int maxNeeded = 0, dsNeeded = 0, sdsNeeded = 0, sfsNeeded = 0;
        for (Iterator<ISensor> it = sensors.iterator(); it.hasNext();) {
            ISensor sensor = it.next();
            if (sensor instanceof DistanceSensor) {
                dsNeeded++;
            }
            if (sensor instanceof DistanceSensorFinderISensor) {
                dsNeeded++;
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
        double[][] data = new double[3][5];
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
                System.out.println("DistanceSensor");
            }
            if (sensor instanceof DistanceSensorFinderISensor) {
                double[][] sensorData = ((DistanceSensorFinderISensor) sensor).getSensoryData();
                //TODO; this construction is bullshit, do something with that...
                v =  sensorData[0][0];
                data[0][di++] = v;
                v = sensorData[0][1];
                data[0][di++] = v;
                //v = sensorData[0][2];
                //data[0][di++] = v;
                //System.out.println( "Uz je to " + di + " a zapsal jsem " + v );
              //  System.out.println("DistanceSensorFinder");
            }
            //TODO: change iof to discriminator
            if (sensor instanceof SurfaceFrictionSensor) {
                double[][] sensorData = ((SurfaceFrictionSensor) sensor).getSensoryData();
                v = sensorData[0][0];
                data[1][si] = Util.rescale(v, 1, 10);   // should min and max friction in the arena
                si++;
//                System.out.println( " a zapsal jsem " + v);
                //System.out.println("SurfaceSensor");
            }
            if (sensor instanceof ScalableDistanceSensor) {
                double[][] sensorData = ((ScalableDistanceSensor) sensor).getSensoryData();
                //TODO; this construction is bullshit, do something with that...
                vv =  sensorData[0];
                //System.out.println( " a zapsal jsem " + vv[0] + " " + vv[1] + " " + vv[2]);
                data[2] = vv;
                
                
                //System.out.println("ScalableDistanceSensor: " + vv.length );
            }
            if (sensor instanceof AsyncScalableDistanceSensor) {
                double[][] sensorData = ((AsyncScalableDistanceSensor) sensor).getSensoryData();
                //TODO; this construction is bullshit, do something with that...
                vv =  sensorData[0];
                data[0] = vv;
                di++;
                System.out.println("AszncScalableDistanceSensor");
            }
        }
        
        //data[0][2] = this.getRobotRepresent().getSpeed();
        //data[0][3] = this.getRobotRepresent().getDirection();
        sensoryData = data;
        
        return data;
    }



}
