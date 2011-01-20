package robot;

import robot.controller.IRobotController;
import vivae.controllers.RobotWithSensorController;

/**
 * Created by IntelliJ IDEA.
 * User: drchaj1
 * Date: Apr 27, 2010
 * Time: 10:36:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class VivaeControllerAdapter extends RobotWithSensorController {
    IRobotController controller;

    public VivaeControllerAdapter(IRobotController controller) {
        this.controller = controller;
    }

    @Override
    public void moveControlledObject() {
        controller.step();

        VivaeRobot vivaeRobot = (VivaeRobot) controller.getRobot();

        double left = 5.0 * vivaeRobot.getLeft();
        double right = 5.0 * vivaeRobot.getRight();
        
        double angle;
        double acceleration = 0.25 * (left + right);
        angle = 0.1 * (left - right);

        robot.rotate((float) angle);
        robot.accelerate((float) acceleration);

//            System.out.println("L: " + left + " R:" + right + " ANG: " + angle + " ACC:" + acceleration);
    }
}
