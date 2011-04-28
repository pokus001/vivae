/**
 * This is VIVAE (Visual Vector Agent Environment)
 * a library allowing for simulations of agents in co-evolution 
 * written as a bachelor project 
 * by Petr Smejkal
 * at Czech Technical University in Prague
 * in 2008
 */

package vivae.controllers;

import vivae.arena.Arena;
import vivae.example.KeyboardLayout;
import vivae.robots.IRobotInterface;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.robots.VivaeRobot;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * An extension of VivaeController for controlling Active ArenaParts by Keyboard. 
 * KeyListener to Arena's parent window is added automatically, if controlled object is VivaeRobot
 * @author Petr Smejkal
 */
public class KeyboardVivaeController extends VivaeController implements KeyListener{
    protected boolean isLeftKeyDown = false,  
                    isRightKeyDown = false,  
                    isUpKeyDown = false,  
                    isDownKeyDown = false;

    public final KeyboardLayout layout;

    public KeyboardVivaeController(IRobotInterface controlledObject) {
        this(controlledObject, KeyboardLayout.ArrowsLayout());
    }

    public KeyboardVivaeController(IRobotInterface controlledObject, KeyboardLayout layout) {
        this.controlledObject = controlledObject;
        this.layout = layout ;

        //for virtual robots in Arnea, also assign keylistener...
        if(VivaeRobot.class.isAssignableFrom(controlledObject.getClass())) {
            Arena arena = ((VivaeRobot) controlledObject).getArena();
            if(arena.getParentVindow() != null) {
                arena.getParentVindow().addKeyListener(this);
            }
        }

    }



    public void step() {
        moveControlledObject();
    }

    @Override
    public void moveControlledObject() {

        //when turning while riding, defines requested turning radius - however, controlled object can have another limitations
        final float turnRatio = 0.3f;

        boolean leftRightPress = isLeftKeyDown || isRightKeyDown;
        boolean upDownPress = isUpKeyDown || isDownKeyDown;


        if(!upDownPress && !leftRightPress) {
            //nothing pressed - let"s break...
            controlledObject.setWheelSpeed(0, 0);

        } else if(upDownPress && !leftRightPress) {
            //simply move forward or backward with max speed
            float wheelsSpeed = isUpKeyDown? 1f : -1f;
            controlledObject.setWheelSpeed(wheelsSpeed, wheelsSpeed);

        } else if(leftRightPress && !upDownPress) {
            //perform only rotation - one whell to max; another one to max in opposite direction
            float wheelSpeed = isLeftKeyDown? -1f : 1f;
            controlledObject.setWheelSpeed(wheelSpeed, -wheelSpeed);

        } else {
            //the most difficult case - performing turning and acceleration/decelleration together.
            float lwheel = 1, rwheel = 1;
            if(isLeftKeyDown) lwheel = lwheel * turnRatio;
            if(isRightKeyDown) rwheel = rwheel * turnRatio;
            //now we know, where to turn (if happen, that both left and right keys pressed, it will not turn anywhere, however it goes slower)

            //let"s solve direction
            if(isDownKeyDown) {
                lwheel *= -1;
                rwheel *= -1;
            }
            controlledObject.setWheelSpeed(lwheel, rwheel);
        }
    }

    public void keyTyped(KeyEvent e) {
    }

//    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        //cannot use switcc - layout is not known at compile time
        if(code == layout.LAY_LEFT) {
            isLeftKeyDown = true;
            isRightKeyDown = false;
        } else if (code == layout.LAY_RIGHT) {
            isRightKeyDown = true;
            isLeftKeyDown = false;
        } else if (code == layout.LAY_DOWN) {
            isDownKeyDown = true;
            isUpKeyDown = false;
        } else if (code == layout.LAY_UP) {
            isUpKeyDown = true;
            isDownKeyDown = false;
        }
    }

//    @Override
    public void keyReleased(KeyEvent e) {

        int code = e.getKeyCode();

        //cannot use switcc - layout is not known at compile time
        if(code == layout.LAY_LEFT) {
            isLeftKeyDown = false;
        } else if (code == layout.LAY_RIGHT) {
            isRightKeyDown = false;
        } else if (code == layout.LAY_DOWN) {
            isDownKeyDown = false;
        } else if (code == layout.LAY_UP) {
            isUpKeyDown = false;
        }
    }
}
