package vivae.robots;

import vivae.robots.IRobotInterface;
import vivae.sensors.ISensor;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: drchaj1
 * Date: 1/12/11
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IRobotWithSensorsInterface extends IRobotInterface {

    double[][] getSensorData();

    void addSensor(ISensor s);

    List<ISensor> getSensors();
}