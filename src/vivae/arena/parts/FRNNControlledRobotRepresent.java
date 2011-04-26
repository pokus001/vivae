/**
 * This is VIVAE (Visual Vector Agent Environment)
 * a library allowing for simulations of agents in co-evolution
 * written as a bachelor project
 * by Petr Smejkal
 * at Czech Technical University in Prague
 * in 2008
 */
package vivae.arena.parts;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import net.phys2d.raw.World;
import net.phys2d.raw.shapes.Box;
import vivae.arena.Arena;
import vivae.arena.parts.sensors.*;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.robots.IRobotInterface;
import vivae.util.Util;

/**
 * @author HKou
 */
public class FRNNControlledRobotRepresent extends VivaeRobotRepresent {

    protected double[][] sensoryData;

    public FRNNControlledRobotRepresent(float x, float y) {
        super(x, y);

        diameter = 12;
        boundingCircleRadius = (float) Math.sqrt(2 * diameter * diameter) / 2;
        myNumber = getNumber();
    }

    public FRNNControlledRobotRepresent(Shape shape, int layer, Arena arena, IRobotInterface owner) {
        this((float) shape.getBounds2D().getCenterX(),
                (float) shape.getBounds2D().getCenterY(), arena, owner);
    }

    public FRNNControlledRobotRepresent(float x, float y, Arena arena, IRobotInterface owner) {
        this(x, y);
        diameter = 12;
        boundingCircleRadius = (float) Math.sqrt(2 * diameter * diameter) / 2;
        myNumber = getNumber();
        this.arena = arena;
        this.owner = owner;
        this.world = arena.getWorld();
        body = new Body("VivaeRobotRepresent", new Box(diameter, diameter), 50f);
//        body.setPosition((float) x, (float) y);
        body.setRotation(0);
        body.setDamping(baseDamping);
        body.setRotDamping(ROT_DAMPING_MUTIPLYING_CONST * baseDamping);
        setShape(new Rectangle2D.Double(0, 0, diameter, diameter));
        Rectangle r = getShape().getBounds();
        centerX = (float) r.getCenterX();
        centerY = (float) r.getCenterY();
    }

    @Override
    public void moveComponent() {



        inMotion = true;

        setDamping(arena.getFrictionOfSurface(this));  //speedup

        direction = body.getRotation();
        net.phys2d.math.ROVector2f p = body.getPosition();
        x = p.getX();
        y = p.getY();

        //TODO: move this to controller !!

        final double distance = Util.euclideanDistance(lastX, lastY, x, y);
        final double velDist = lastVelocity - getSpeed() > 0 ? lastVelocity - getSpeed() : 0;
        if (velDist > 10) {
            crashmeter += velDist;
        }
        if (velDist > maxDeceleration) {
            maxDeceleration = velDist;
        }
        overallDeceleration += velDist;
        lastVelocity = getSpeed();

        odometer += distance;
        lastX = x;
        lastY = y;


    }

    @Override
    public AffineTransform getTranslation() {
        AffineTransform af = AffineTransform.getTranslateInstance(x - diameter / 2, y - diameter / 2);
        af.rotate(direction, centerX, centerY);
        return af;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Object hint = new Object();
        if (isAntialiased()) {
            hint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        translation = getTranslation();
        Color oldColor = g2.getColor();
        g2.setColor(new Color(230, 230, 250));
        g2.fill(translation.createTransformedShape(getShape()));
        g2.setColor(Color.BLACK);
        g2.draw(translation.createTransformedShape(getShape()));

//        ///=-=========== painting of sensors
//        if(owner instanceof IRobotWithSensorsInterface)  {
//            IRobotWithSensorsInterface rs = (IRobotWithSensorsInterface)owner;
//            java.util.List <ISensor> sensors = rs.getSensors();
//
//
//            //TODO: THIS HAS TO BE GENERIC
//            for (ISensor s : sensors) {
//                if(DistanceSensor)
//            }
//
//            g2.drawString(s, baseX, baseY);
//            baseY += STATUS_FRAME_LINE_HEIGHT;
//            s = "";
//
////            for (int j = 0; j < sensoryData[1].length; j++) {
////                s += String.format("%1.1f ", sensoryData[1][j]);
////            }
//            g2.drawString(s, baseX, baseY);
//            g2.setComposite(oldComposite);
//            g2.setColor(oldColor);
//        }

        //TODO: solve painting of sensors. Sensor representatnt as graphical component should be contained in Arena containers.
//        if (isShowingSensors) {
//            for (Iterator<Sensor> sIter = sensors.iterator(); sIter.hasNext();) {
//                Sensor s = (Sensor) sIter.next();
//                s.paintComponent(g2);
//            }
//        }






        if (isShowingStatusFrame) {
            paintStatusFrame(g2);
        }
        g2.setColor(oldColor);
        if (isAntialiased()) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, hint);
        }
    }

    //TODO: this logic should be out of Represent...
    @Override
    public void accelerate(float s) {
        setSpeed(body.getVelocity().length());
        // if acceleration cause to exceed maxSpeed, decrease it

//        System.out.println("Input s = " + s + ", getSpeed = " +
//                getSpeed() + ", Maxspeed = " +
//                getMaxSpeed() + ", MaxReverseSpeed = " + getMaxReverseSpeed());

        //abs to protect flyaway when robot is pulled by other object (robot)
        s = Math.min(s, Math.abs(getMaxSpeed() - (float) getSpeed()));
//        System.out.print("S aftern Math.Min = "+ s);

        // if "acceleration" to backward direction causes exceed maxReverseSpeed, decrease it
        s = Math.max(s, -Math.abs((getMaxReverseSpeed() - (float) getSpeed())) );
//        System.out.println(", S aftern Math.MAX = "+ s);


        float dx = (float) (s * (float) Math.cos(body.getRotation() - Math.PI / 2));
        float dy = (float) (s * (float) Math.sin(body.getRotation() - Math.PI / 2));
        body.adjustVelocity(new Vector2f(dx, dy));

    }

    @Override
    public void decelerate(float s) {
        setSpeed(body.getVelocity().length());
        s = Math.max(s, 0);
        float dx = (float) (s * (float) Math.cos(body.getRotation() - Math.PI / 2));
        float dy = (float) (s * (float) Math.sin(body.getRotation() - Math.PI / 2));
        body.adjustVelocity(new Vector2f(-dx, -dy));
    }

    @Override
    public void rotate(float radius) {
        body.adjustAngularVelocity(radius);
        this.direction = body.getRotation();
    }

    @Override
    public int getNumber() {
        return robotsCounter++;
    }

    @Override
    public String getActiveName() {
        return "VivaeRobotRepresent";
    }

    @Override
    public float getAcceleration() {
        return VivaeRobotRepresent.ACCELERATION;
    }

    @Override
    public float getMaxSpeed() {
        return VivaeRobotRepresent.MAX_SPEED;
    }

    public float getMaxReverseSpeed() {
        return VivaeRobotRepresent.MAX_SPEED/5;
    }

    @Override
    public float getRotationIncrement() {
        return VivaeRobotRepresent.ROTATION;
    }

    @Override
    public String toString() {
        return "VivaeRobotRepresent " + myNumber;
    }

    @Override
    public void reportObjectOnSight(Sensor s, Body b) {
        System.out.println("Object seen from sensor " + s);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public void setShowingSensors(boolean showingSensors) {
        isShowingSensors = showingSensors;
    }

    @Override
    public void paintStatusFrame(Graphics g, int baseX, int baseY) {
        Graphics2D g2 = (Graphics2D) g;
        Color oldColor = g2.getColor();
        Composite oldComposite = g2.getComposite();
        g2.setComposite(opacityBack);
        g2.setColor(Color.BLACK);
        g2.fillRect(baseX, baseY, 100, 100);
        g2.setComposite(opacityFront);
        g2.setColor(Color.WHITE);
        g2.drawRect(baseX, baseY, 100, 100);
        if (isStatusFramePinedToPosition) {
            g2.drawLine((int) this.x, (int) this.y, baseX + 100, baseY + 100);
        }
        baseX += 5;
        baseY += 15;
        g2.drawString(String.format(getActiveName() + "  #%d", myNumber), baseX, baseY);
        baseY += STATUS_FRAME_LINE_HEIGHT;
        g2.drawString(String.format("x: %4.0f", x), baseX, baseY);
        baseY += STATUS_FRAME_LINE_HEIGHT;
        g2.drawString(String.format("y: %4.0f", y), baseX, baseY);
        baseY += STATUS_FRAME_LINE_HEIGHT;
        String s = "";
        if(owner instanceof IRobotWithSensorsInterface)  {
            IRobotWithSensorsInterface rs = (IRobotWithSensorsInterface)owner;
            sensoryData = rs.getSensorData();
            //TODO: THIS HAS TO BE GENERIC
            for (int j = 0; j < sensoryData[0].length; j++) {
                s += String.format("%1.1f ", sensoryData[0][j]);
            }

            g2.drawString(s, baseX, baseY);
            baseY += STATUS_FRAME_LINE_HEIGHT;
            s = "";

//            for (int j = 0; j < sensoryData[1].length; j++) {
//                s += String.format("%1.1f ", sensoryData[1][j]);
//            }
            g2.drawString(s, baseX, baseY);
            g2.setComposite(oldComposite);
            g2.setColor(oldColor);
        }
    }
}

