package vivae.fitness;

import vivae.robots.IRobotInterface;
import vivae.example.IExperiment;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.sensors.ISensor;
import vivae.sensors.OdometerSensor;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: koutnij
 * Date: Nov 9, 2009
 * Time: 9:18:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class AverageSpeed extends FitnessFunction {

    IExperiment exp;

    public AverageSpeed(IExperiment exp) {
        this.exp = exp;
    }

    @Override
    public double getFitness() {
        int stepsDone = exp.getStepsDone();
        List<IRobotInterface> robots = exp.getRobots();
        double res = 0d;
        int robotsWithOdometer = 0;
        for (IRobotInterface r: robots)
        {
//            TODO: here should be discriminator IF - if has odometer;
            if(r instanceof IRobotWithSensorsInterface)   {
                List<ISensor> robotSensors = ((IRobotWithSensorsInterface)r).getSensors();
                for (ISensor rs: robotSensors) {
                    //TODO: also discriminator
                    if(rs instanceof OdometerSensor) {
                        double[][] sensorData = new double[1][1];
                        sensorData = ((OdometerSensor)rs).getSensoryData();
                        res += sensorData[0][0];
                    }
                }
            robotsWithOdometer++;
            }
        }

        if(robotsWithOdometer == 0) return 0;
        return res / robotsWithOdometer / stepsDone;
    }
}
