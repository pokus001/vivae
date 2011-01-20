/**
 * This is VIVAE (Visual Vector Agent Environment)
 * a library allowing for simulations of agents in co-evolution 
 * written as a bachelor project 
 * by Petr Smejkal
 * at Czech Technical University in Prague
 * in 2008
 */

package vivae.arena.parts;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.Area;


/**
 * @author Petr Smejkal
 */
public abstract class Surface extends ArenaPart {
    protected Area area;
    protected int level;
    protected boolean isTextureEnabled = false;
    protected TexturePaint texture;
    protected boolean isTextureLoaded = false;
    protected static float FRICTION = 3;

    public Surface(float x, float y, Shape shape, int level) {
        super(x, y);
        setShape(shape);
        // TODO Auto-generated constructor stub
    }

    public Surface(float x, float y, Shape shape) {
        super(x, y);
        this.level = 1;
        setShape(shape);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void paintComponent(Graphics g) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setShape(Shape shape) {
        // TODO Auto-generated method stub
        super.setShape(shape);
        this.area = new Area(this.shape);
    }

    public int getLevel() {
        return level;
    }

    public Area getArea() {
        return area;
    }

    public String toString(){
        return "Surface at " + "[" + getX() + ", " + getY() + "]";
    }

    public abstract float getFriction();

    public boolean isTextureEnabled() {
        return isTextureEnabled;
    }

    public void setTextureEnabled(boolean isTextureEnabled) {
        this.isTextureEnabled = isTextureEnabled;
    }
}

