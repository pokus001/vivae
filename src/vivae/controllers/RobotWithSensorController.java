/**
 * This is VIVAE (Visual Vector Agent Environment)
 * a library allowing for simulations of agents in co-evolution 
 * written as a bachelor project 
 * by Petr Smejkal
 * at Czech Technical University in Prague
 * in 2008
 */



package vivae.controllers;

import java.util.Vector;

import vivae.robots.IRobotInterface;
import vivae.arena.parts.VivaeObject;
import vivae.robots.IRobotWithSensorsInterface;

/**
 * One of the extensions of the basic VivaeController class specifiing Active object's behavior. 
 * This type of Controller is used for Robots equipped with sensors.
 * 
 * @author Petr Smejkal
 */
public abstract class RobotWithSensorController extends VivaeController{
    
    /**
     * A boolean variable specifiing if the VivaeRobotRepresent has been set up.
     */
    protected boolean isRobotSet = false;

    //why should controller has sensors array??? to be delted...
//    /**
//     * A Vector of Snesors of the controlled robot.
//     */
//    protected Vector<ISensor> sensors;

    /**
     * This Vector of VivaeObjects can be used to store all Objects in the Arena.
     * The sensors will then search them for those of them that are on sight. 
     */
    protected Vector<VivaeObject> allObjects = new Vector<VivaeObject>();
    /**
     * This Vector of VivaeObjects can be used to store those ArenaObjects in the Arena, that are on sight of the sensors.
     */
    protected Vector<VivaeObject> objectsOnSight = new Vector<VivaeObject>();

    @Override
    public IRobotWithSensorsInterface getControlledObject() {
        return (IRobotWithSensorsInterface) controlledObject;
    }

    @Override
    public void setControlledObject(IRobotInterface controlledObject) {
        //TODO: test na spravny typ pres diskriminator...
        this.controlledObject = (IRobotWithSensorsInterface) controlledObject;
        this.isRobotSet = true;
    }
    
    
    @Override
    public abstract void moveControlledObject();

}
