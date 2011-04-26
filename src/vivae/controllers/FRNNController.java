package vivae.controllers;

import vivae.robots.IRobotWithSensorsInterface;
import vivae.util.Util;


import vivae.controllers.nn.FRNN;

/**
 * Created by IntelliJ IDEA.
 * User: koutnij
 * Date: Nov 9, 2009
 * Time: 2:57:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class FRNNController extends RobotWithSensorController {

    protected FRNN frnn = new FRNN();
    protected final IRobotWithSensorsInterface controlledRobot;

    public FRNNController(IRobotWithSensorsInterface controlledRobot, double[][] wm) {
        this.controlledRobot = controlledRobot;
        setControlledObject(controlledRobot);

        int neurons = wm.length;
        int snum = (wm[0].length - neurons - 1);
        double sangle = -Math.PI / 2;
        double ai = Math.PI / (snum / 2 - 1);

        frnn.init(Util.subMat(wm, 0, snum - 1),
            Util.subMat(wm, snum, snum + neurons - 1),
            Util.flatten(Util.subMat(wm, snum + neurons, snum + neurons)));
    }

    public void step(){
        //step() just for backward compatibility
        moveControlledObject();
    }

    @Override
    public void moveControlledObject() {

//        if (controlledRobot instanceof FRNNControlledRobotRepresent) {
            double[] input = Util.flatten(controlledRobot.getSensorData());

            double[] eval = frnn.evalNetwork(input);

            double lWheel = eval[0];
            double rWheel = eval[eval.length - 1];
//            double angle;
//            double acceleration = 5.0 * (lWheel + rWheel);
//            if (acceleration < 0) {
//                acceleration = 0; // negative speed causes problems, why?
//            }
//            double speed = Math.abs(robot.getSpeed() / robot.getMaxSpeed());
//            speed = Math.min(Math.max(speed, -1), 1);
//            if (rWheel > lWheel) {
//                angle = 10 * (1.0 - speed);
//            } else {
//                angle = -10 * (1.0 - speed);
//            }
//
//            //TODO: [IKO] it will be useful to change this interface to robot.accelerate(acceleration, angle) because of Hardware robot motion controlling. Or maybe better to rename FRNNControler to FRNNvirtualController
//            robot.rotate((float) angle);
//            robot.accelerate((float) acceleration);
//            System.out.println("Setting wheel speeds: L="+lWheel + " R="+rWheel);
            controlledRobot.setWheelSpeed(lWheel, rWheel);
//        }

    }
//
//    protected int computeNeuronsCount(List<ISensor> sensors) {
//        int total = 0;
//        for(int i = 0; i < sensors.size(); i++) {
//            total += sensors.get(i).getValuesCount(); // # values corresponds with # neurons
//        }
//        return total;
//    }

    void setFRNN(FRNN net) {
        this.frnn = net;
    }
}
