package PredatorKorist;

import java.util.List;
import java.util.Set;

import vivae.example.IExperiment;
import vivae.fitness.FitnessFunction;
import vivae.robots.IRobotInterface;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.sensors.ISensor;
import vivae.sensors.OdometerSensor;

public class FitnessFinder extends FitnessFunctionMoje {

	   

	    public FitnessFinder(IExperiment exp) {
	        super( exp );
	    }

	    @Override
	    public double getFitness() {
	        int stepsDone = exp.getStepsDone();
	        List<IRobotInterface> robots = exp.getRobots();
	        double res = 0;
	        double vzdalenost = 0;
	        for (IRobotInterface r: robots)
	        {
//	            TODO: here should be discriminator IF - if has odometer;
	            if(r instanceof IRobotWithSensorsInterface)   {
	                List<ISensor> robotSensors = ((IRobotWithSensorsInterface)r).getSensors();
	                for (ISensor rs: robotSensors) {
	                    //TODO: also discriminator
	                	
	                    if(rs instanceof DistanceSensorFinderISensor) {
	                    	//System.out.println("zzz");
	                        //double[][] sensorData = ((DistanceSensorFinderISensor)rs).getSensoryData();
	                          //res += sensorData[0][1];
	                    	res += ((DistanceSensorFinderISensor)rs).getAverage();
	                          
	                          

	                    }
	                    
	                    if(rs instanceof OdometerSensor) {
	                        double[][] sensorData = ((OdometerSensor)rs).getSensoryData();
	                        vzdalenost += sensorData[0][0];
	                    }
                   
	                }

	            }
	        }

			stepsDone++;
			//double dist = ( res) / (3.0 );
			double dist = (res) / 4.0;
			double stepy = 0;
			if ( stepsDone < 3000 ) stepy = ( 1.0 - stepsDone / 3000.0 ) / 2.0;
			
			//double rychlost = (1 - vzdalenost / ( (double)stepsDone ) ) / 3.0; 
			double rychlost = ( vzdalenost / ( (double)stepsDone ) ) / 3.0;
			
			//System.out.println("Dist: " + dist);
			//System.out.println("Stepy: " + stepy);
			//System.out.println("Rychlost: " + rychlost);
			//if ( rychlost > 0.333 ) rychlost = 0.333;
			//System.out.println("aaa: " + dist );
	        return dist + stepy;// + rychlost;
			
	    }

}
