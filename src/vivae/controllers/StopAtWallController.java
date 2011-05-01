package vivae.controllers;

import vivae.robots.IRobotInterface;
import vivae.robots.IRobotWithSensorsInterface;

/**
 * Created by IntelliJ IDEA.
 * User: drchaj1
 * Date: Apr 27, 2010
 * Time: 9:22:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class StopAtWallController extends VivaeController implements IRobotController {
    final private IRobotWithSensorsInterface controlledObject;
    private int stepCount = 0;

    public StopAtWallController(IRobotInterface robot) {
        if(IRobotWithSensorsInterface.class.isAssignableFrom(robot.getClass())) {
            this.controlledObject = (IRobotWithSensorsInterface)robot;
        } else {
            throw new IllegalStateException("Cannot use this controller for robot without sensors!!!");
        }
    }

    public void moveControlledObject() {
        step();
    }

    public void step() {
        double sens[][] = controlledObject.getSensorData();
        int sensDataSize = sens[0].length;
        if(sens[0][sensDataSize /2 ] > 0.3) {
            controlledObject.setWheelSpeed(0,0);
//            System.out.println("Wall reached at step " + stepCount);
        } else {
            controlledObject.setWheelSpeed(1,1);
        }

        stepCount++;
    }

    public IRobotInterface getControlledObject() {
        return controlledObject;
    }
}
