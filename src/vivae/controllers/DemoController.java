package vivae.controllers;

import robot.HardwareRobot;
import vivae.controllers.IRobotController;
import vivae.robots.IRobotInterface;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.robots.VivaeRobot;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by IntelliJ IDEA.
 * User: drchaj1
 * Date: Apr 27, 2010
 * Time: 9:22:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class DemoController extends VivaeController implements IRobotController {
    final private IRobotInterface controlledObject;
    private int stepCount = 0;

    public DemoController(IRobotInterface robot) {
        this.controlledObject = robot;
    }

    public void moveControlledObject() {
        step();
    }

    public void step() {
        if (stepCount < 50) {
            controlledObject.setWheelSpeed(1, 1);
        } else if (stepCount < 200) {
            controlledObject.setWheelSpeed(1, 0.3);
        } else if (stepCount < 300) {
            controlledObject.setWheelSpeed(1, 1);
        } else if (stepCount < 320) {
            controlledObject.setWheelSpeed(1, -1);
        } else if (stepCount < 420) {
            controlledObject.setWheelSpeed(1, 1);
        }
        //just for testing  [IKO]
        /*
        if(controlledObject instanceof HardwareRobot) {
            double sens[][] = ((HardwareRobot)controlledObject).getSensorData();

                try{

                FileWriter fstream = new FileWriter("hwSensorsOut.txt");
                BufferedWriter out = new BufferedWriter(fstream);
                for (int i = 0; i < 64; i++) {
                    out.write(sens[0][i] + ", ");
                }
                out.write("\n");
                out.close();

                }catch (Exception e){//Catch exception if any
                  System.err.println("Error: " + e.getMessage());
            }
        }
        */

        stepCount++;
    }

    public IRobotInterface getControlledObject() {
        return controlledObject;
    }
}
