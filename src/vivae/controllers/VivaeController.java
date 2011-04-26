/**
 * This is VIVAE (Visual Vector Agent Environment)
 * a library allowing for simulations of agents in co-evolution 
 * written as a bachelor project 
 * by Petr Smejkal
 * at Czech Technical University in Prague
 * in 2008
 */

package vivae.controllers;

import vivae.robots.IRobotInterface;

/**
 * An abstract class for all controllers that are used to control the movement and behavior of active agents. 
 * You can create your own controllers by extending this class. Specify the logic of your controller in the moveControlledObject.
 * After creating a controller, register it to an Active ArenaPart and it will follow it's behavioral pattern.
 * @author Petr Smejkal
 */
public abstract class VivaeController implements IRobotController {

    /**
     * The Active ArenaPart that is to be controlled by this controller.
     */
    protected IRobotInterface controlledObject;

   /**
    * This procedure is called in every iteration of the Arena loop to determine Active objects' movement.
    * Controller just controls how robot wheels should be working - however, controlled object can have various logic how to work with these controlling
    * e.g. when controller set fullspeed to both wheels while robot is actually not moving, some fade-in acceleration can be implemented in robot.
    */
    abstract public void moveControlledObject();

    public IRobotInterface getControlledObject() {
        return controlledObject;
    }

    public void setControlledObject(IRobotInterface controlledObject) {
        this.controlledObject = controlledObject;
    }

}
