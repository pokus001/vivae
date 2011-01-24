package robot;

import robot.controller.*;
import robot.joystick.JoystickDriver;
import robot.ui.MainForm;
import vivae.example.BuildExperiment;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: drchaj1
 * Date: Apr 27, 2010
 * Time: 10:00:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    public static void runVivaeDemo() {
        IRobotInterface robot = new VivaeRobot(null);
        IRobotController controller = new DemoController(robot);
        VivaeControllerAdapter vivaeControllerAdapter = new VivaeControllerAdapter(controller);

        BuildExperiment experiment = new BuildExperiment(vivaeControllerAdapter);
        experiment.createArena("cfg/vivae/scenarios/arena3.svg", true);
        experiment.setupExperiment();
        experiment.startExperiment();
    }

    public static void runHWDemo() {
        try {
            HardwareRobot robot = new HardwareRobot("localhost", 6005);


            IRobotController controller = new DemoController(robot);

            try {
                robot.connect();
                Thread.sleep(500);
                for (int i = 0; i < 100; i++) {
                    controller.step();

                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            robot.setWheelSpeed(0.0, 0.0);
            robot.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runHWUserUIDemo() {
        IHardwareRobotInterface robot = null;
        try {
            robot = new HardwareRobot("localhost", 6005);
            //when using joystick don;t forget to set VM option -Djava.library.path=lib
            JoystickDriver joystickController = new JoystickDriver();
            JFrame frame = new MainForm(robot, joystickController);
//            JFrame frame = new MainForm(robot);
            frame.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void runJoystickHWDemo() {
        try {
            FileWriterRobot robot1 = new FileWriterRobot("robot_commands.txt");
            JoystickController controller1 = new JoystickController(robot1);

            HardwareRobot robot2 = new HardwareRobot("localhost", 6005);
            JoystickController controller2 = new JoystickController(robot2);
            try {
                robot2.connect();
                Thread.sleep(500);

                for (int i = 0; i < 300; i++) {
                    controller1.step();
                    controller2.step();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            robot1.setWheelSpeed(0.0, 0.0);
            robot1.close();

            robot2.setWheelSpeed(0.0, 0.0);
            robot2.disconnect();

            controller1.stop();
            controller2.stop();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runFileReaderHWDemo() {
        try {
            HardwareRobot robot = new HardwareRobot("localhost", 6005);
            FileReaderController controller = new FileReaderController(robot, "robot_commands.txt");

            try {
                robot.connect();
                Thread.sleep(500);

                for (int i = 0; i < 300; i++) {
                    controller.step();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            robot.setWheelSpeed(0.0, 0.0);
            robot.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runMultipleDemo() {
        try {
            FileWriterRobot robot1 = new FileWriterRobot(System.out);

            HardwareRobot robot2 = new HardwareRobot("localhost", 6005);
            robot2.connect();

            VivaeRobot robot3 = new VivaeRobot(null);

            IRobotController controller1 = new DemoController(robot1);
            IRobotController controller2 = new DemoController(robot2);
            IRobotController controller3 = new DemoController(robot3);
            IRobotController controller = new MultipleController(new IRobotController[]{controller1, controller2, controller3});

            VivaeControllerAdapter vivaeControllerAdapter = new VivaeControllerAdapter(controller3);

            BuildExperiment experiment = new BuildExperiment(vivaeControllerAdapter);
            experiment.createArena("cfg/vivae/scenarios/arena3_h.svg", true);
            experiment.setupExperiment();
//            experiment.stepExperiment();

            for (int i = 0; i < 1000; i++) {
                controller.step();
                experiment.stepExperiment();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            robot2.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runVivaeUserUI() {
//        IHardwareRobotInterface robot = new HardwareRobot(
//                "localhost", 6005);

        IRobotInterface robot = new VivaeRobot(null);

        IRobotController controller = new UIUserController(robot);
        VivaeControllerAdapter vivaeControllerAdapter = new VivaeControllerAdapter(controller);

        BuildExperiment experiment = new BuildExperiment(vivaeControllerAdapter);
        experiment.createArena("cfg/vivae/scenarios/arena3.svg", true);
        experiment.setupExperiment();

        //when using joystick don;t forget to set VM option -Djava.library.path=lib
        JoystickDriver joystickController = new JoystickDriver();
        JFrame frame = new MainForm(robot, joystickController);
//        JFrame frame = new MainForm(robot);
        frame.setVisible(true);

        experiment.startExperiment();
    }

    public static void runJoystickSWDemo() {
        FileWriterRobot robot1 = new FileWriterRobot("robot_commands.txt");
        JoystickController controller1 = new JoystickController(robot1);

        VivaeRobot robot2 = new VivaeRobot(null);
        JoystickController controller2 = new JoystickController(robot2);
        VivaeControllerAdapter vivaeControllerAdapter = new VivaeControllerAdapter(controller2);

        IRobotController controller = new MultipleController(new IRobotController[]{controller1, controller2});

        BuildExperiment experiment = new BuildExperiment(vivaeControllerAdapter);
        experiment.createArena("cfg/vivae/scenarios/arena3.svg", true);
        experiment.setupExperiment();
//            experiment.stepExperiment();

        for (int i = 0; i < 10000; i++) {
            controller.step();
            experiment.stepExperiment();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void runFileWriterDemo() {
        IRobotInterface robot = new FileWriterRobot(System.out);
        IRobotController controller = new DemoController(robot);
        controller.step();
        controller.step();
        controller.step();
    }

    public static void main(String[] args) {
//        runVivaeDemo();
//        runJoystickHWDemo();
//        runFileReaderHWDemo();
//        runMultipleDemo();
//        runHWDemo();
//        runHWUserUIDemo();
//        runVivaeUserUI();
//        runFileWriterDemo();
        runJoystickSWDemo();
    }
}
