/**
 * This is VIVAE (Visual Vector Agent Environment)
 * a library allowing for simulations of agents in co-evolution 
 * written as a bachelor project 
 * by Petr Smejkal
 * at Czech Technical University in Prague
 * in 2008
 */
// svn test
package vivae.arena;

import net.phys2d.math.Vector2f;
import net.phys2d.raw.World;
import net.phys2d.raw.strategies.QuadSpaceStrategy;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;
import vivae.example.IExperiment;
import vivae.robots.IRobotInterface;
import vivae.arena.parts.*;
import vivae.arena.parts.VivaeRobotRepresent;
import vivae.controllers.KeyboardVivaeController;
import vivae.controllers.VivaeController;
import vivae.util.ArenaPartsGenerator;
import vivae.util.FrictionBuffer;
import vivae.util.SVGShapeLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Petr Smejkal
 */
@SuppressWarnings("serial")
public class Arena extends JPanel implements KeyListener, Runnable {

    /**
     * Instance of Arena, for handling Singleton pattern
     */
    private static Arena Ainstance;

    /**
     * Thread sleep time in milisecs.
     */
    public int loopSleepTime = 10;
    /**
     * Number of steps the physical world takes in one iteration of the main loop.
     */
    public int worldStepsPerLoop = 1;
    /**
     * Maximum number of steps before VIVAE stops.
     */
    public int totalStepsPerSimulation = 1000;
    public int stepsDone = 0;
    /**
     * Screen width.
     */
    public int screenWidth = 800;
    /**
     * Screen height.
     */
    public int screenHeight = 600;
    /**
     * The physical world representing the arena (see Phys2D docs).
     */
    protected World world = new World(new Vector2f(0.0f, 10.0f), 10, new QuadSpaceStrategy(2, 1));
    /**
     * Vector of Walls as Fixed objects in the arena.
     */
    protected Vector<Fixed> walls = new Vector<Fixed>();
    /**
     * Vector of Passive objects in the arena.
     */
    protected Vector<Passive> passives = new Vector<Passive>();
    /**
     * Vector of Surfaces in the arena.
     */
    protected Vector<Surface> surfaces = new Vector<Surface>();
    /**
     * Vector of Active objects in the arena.
     */
    protected Vector<Active> actives = new Vector<Active>();


    protected java.util.List<PositionMark> positions;
    protected List<VivaeObject> paintable;

//    /**
//     * Vector of Active objects in the arena.
//     */
//    protected Vector<Active> activesPositions = new Vector<Active>  ();
    /**
     * Vector of all VivaeObjects in the arena.
     */
    protected Vector<VivaeObject> vivaes = new Vector<VivaeObject>();
    /**
     * sets up if the parts of the arena are panited antialiased..
     */
    protected boolean isAllArenaPartsAntialiased = false;
    /**
     * sets up if the objects on sight of the sensors are blinking.
     */
    protected boolean isObjectsOnSightBlinking = true;
    /**
     * Buffer Graphics for doublebuffering.
     */
    protected Graphics2D bufferGraphics;
    /**
     * Off Screen image for doublebuffering.
     */
    protected Image offscreen;
    /**
     * a Vector of the registered controllers.
     */
    protected Vector<VivaeController> controllers = new Vector<VivaeController>();
    /**
     * A parent window where the arena will be painted on.
     */
    protected Component parent;
    /**
     * The output svg document.
     */
    protected SVGDocument svgDoc;
    /**
     * The output svg graphic canvas.
     */
    protected SVGGraphics2D svgGraphics;
    /**
     * A boolean deciding whether an output to SVG is possible.
     */
    protected boolean isSVGGraphicsInitialized = false;

    /**
     * Determines whether the simulation is running or not.
     */
    public boolean isRunning = false;
    /**
     * Switches on and off the visual output.
     */
    public boolean isVisible = false;
    /**
     * Implicit name of the output SVG file.
     */
    public String svgOutputFileName = "vivae-svg-output.svg";
    /**
     * Intern buffer for making friction calculations faster.
     */
    public FrictionBuffer frictionBuffer;// = new FrictionBuffer(this);
    private static Map<String, FrictionBuffer> frictionBufferCache =
            new ConcurrentHashMap<String, FrictionBuffer>();
//            new HashMap<String, FrictionBuffer>();

    public boolean capture = false;
    private boolean isEnclosedWithWalls = false;
    private final boolean DEBUG_FRICTION_CACHE = false;
    private String svgFileName = "";



    /**
     * Sets up a new screen size.
     *
     * @param width  Width of the new screen size
     * @param height Height of the new screen size
     */
    public void setScreenSize(int width, int height) {

        offscreen = createImage(width, height);
        if (this.parent != null) {
            bufferGraphics = (Graphics2D) offscreen.getGraphics();
        }
    }

    /**
     * Singleton-like method, however creation should be handled by renewArena()
     * @return Arena created before.
     */
    public static Arena getArena(){
        if(Ainstance == null) {
            throw new IllegalStateException("call to getArena() before arena was initialized!");
        }
        return Ainstance;
    }

    /**
     * This is a way how to create a new arena when a new experiment is being set-uped.
     * If normal singleton was used for Arena, different executions of experiment inside some high-level
     * application (algorithms etc.) caused that next execution used ending Arena from preivous one - incl. objects positions etc.
     * @param frame in which Arena should be placed. Null = no visualization.
     * @return newly created Arena
     */
    public static Arena renewArena(Frame frame) {
        Ainstance = new Arena(frame);
        return Ainstance;
    }

    /**
     *
     * @return truth value saying if Arena is initialized
     */
    public static boolean isArenaUsed() {
        return (Ainstance != null);
    }


    /**
     * Arena consturctor.
     *
     * @param parent The component where the arena will be displayed in.
     */
    public Arena(Component parent) {
        super();
        this.parent = parent;
        this.isVisible = false;
        if (parent == null) {
            this.isVisible = false;
        } else {
            parent.addKeyListener(this);
        }

        FrictionBuffer get = frictionBufferCache.get(this.svgFileName);
        if (get == null) { //if not in cache, create new instance and
            frictionBuffer = new FrictionBuffer(this);
            frictionBufferCache.put(this.svgFileName, frictionBuffer);
            if (DEBUG_FRICTION_CACHE) {
                System.out.println("Cached friction buffer not found, I have created new instance and cached it.");
            }
        } else {
            frictionBuffer = get;
            if (DEBUG_FRICTION_CACHE) {
                System.out.println("Cached friction buffer found.");
            }
        }
    }

    public void setParent(Component parent) {
        this.parent = parent;
        this.isVisible = false;
        if (parent == null) {
            this.isVisible = false;
        } else {
            parent.addKeyListener(this);
        }
    }

    /**
     * Initializates the World and adds all the actives and passives and physical boundaries inside.
     */
    public void initWorld() {
        encloseWithWalls(50);

        for (Passive passive : getPassives()) {
            world.add(passive.getBody());
        }

        for (Active active : actives) {
            world.add(active.getBody());
        }
        world.setGravity(0, 0);
        setAllArenaPartsAntialiased(isAllArenaPartsAntialiased);
    }

    /**
     * Loads a scenario (an SVG file specifiing location and shape of surfaces and objects in the arena)from a file.
     *
     * @param svgFileName specifies the file the scenario is loaded from.
     */
    public void loadScenario(String svgFileName) {
        this.svgFileName = svgFileName;
        SVGShapeLoader loader = new SVGShapeLoader(svgFileName);
        try {
            loader.start();
            loader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        screenWidth = loader.getWidth();
        screenHeight = loader.getHeight();
        positions = new ArrayList<PositionMark>();
        paintable = new ArrayList<VivaeObject>();
        Vector<HashMap<String, Vector<Shape>>> shapeMap = loader.getShapesWithTypeMap();
        int l = 1;
        for (HashMap<String, Vector<Shape>> m : shapeMap) {
            if (m == null) ;//System.out.println("No shapes loaded on layer " + l);
            else {
                Vector<ArenaPart> v = ArenaPartsGenerator.createParts(m, l, this);
                for (ArenaPart vivae : v) {
                    if (Surface.class.isAssignableFrom(vivae.getClass())) {
                        addSurface((Surface) vivae);
                    } else if (PositionMark.class.isAssignableFrom(vivae.getClass())) {
                        addPosition((PositionMark) vivae);
                    }
                    else if (Passive.class.isAssignableFrom(vivae.getClass())) {
                        addPassive((Passive) vivae);
//                    this should not happen anymore - actives (robots added by experiment, not SVG file !!!)
// } else if (Active.class.isAssignableFrom(vivae.getClass())) {
//                        activesPositions.add((Active) vivae);
                    }
                }
            }
            l++;
        }
    }

    public void addPosition(PositionMark pos) {
        positions.add(pos);
    }

    public void addPaintable(VivaeObject vo) {
        paintable.add(vo);
    }

    public void assignRobotRepresent(VivaeRobotRepresent repr) {
        int activePositionsCnt = positions.size();
        PositionMark pattern = positions.get(actives.size() % activePositionsCnt);
        repr.getBody().setPosition((float) pattern.getX(), (float) pattern.getY());
        repr.setArena(this);
        repr.setWorld(getWorld());
        addActive((Active) repr);
    }

    /**
     * Registers a controller to an Active object. If the controller extends KeyboardVivaeController it also
     * registers it to the parent component as a new KeyListener.
     *
     * @param agent      The Active the controller will control.
     * @param controller The controller specifing behavior of the Active object.
     */
    public void registerController(IRobotInterface agent, VivaeController controller) {
        controller.setControlledObject(agent);
        controllers.add(controller);
        if (agent instanceof VivaeRobotRepresent && controller instanceof KeyboardVivaeController) {
            if (parent != null) {
                parent.addKeyListener((KeyboardVivaeController) controller);
            }
            this.addKeyListener((KeyboardVivaeController) controller);
        }
    }

    /**
     * Sets up whether the Passive objects in the arena are antialiased.
     *
     * @param isAntialiased
     */
    public void setPassivesAntialiased(boolean isAntialiased) {
        for (Passive passive : passives) {
            passive.setAntialiased(isAntialiased);
        }
    }

    /**
     * Sets up whether the Active objects in the arena are antialiased.
     *
     * @param isAntialiased
     */
    public void setActivesAntialiased(boolean isAntialiased) {
        for (Active active : actives) {
            active.setAntialiased(isAntialiased);
        }
    }

    /**
     * Sets up whether the Surfaces in the arena are antialiased.
     *
     * @param isAntialiased
     */
    public void setSurfacesAntialiased(boolean isAntialiased) {
        for (Surface surface : surfaces) {
            surface.setAntialiased(isAntialiased);
        }
    }

    /**
     * Sets up whether the objects in the arena are antialiased.
     *
     * @param isAntialiased
     */
    public void setAllArenaPartsAntialiased(boolean isAntialiased) {
        setActivesAntialiased(isAntialiased);
        setPassivesAntialiased(isAntialiased);
        setSurfacesAntialiased(isAntialiased);
        isAllArenaPartsAntialiased = isAntialiased;
    }

    /**
     * Initializes the simulation.
     */
    public void init() {
        if (isVisible) {
            this.setScreenSize(screenWidth, screenHeight);
        }
        initWorld();
        setRunning(true);
    }

    /**
     * Starts up the simulation.
     */
    public void start() {
        init();
        this.run();
    }

    /**
     * Adds a Passive object to the arena.
     *
     * @param p
     */
    public void addPassive(Passive p) {
        this.getPassives().add(p);
        this.getVivaes().add(p);
    }

    /**
     * Adds a Vector of Passive objects to the arena.
     *
     * @param p
     */
    public void addPassives(Vector<Passive> p) {
        this.getPassives().addAll(p);
        this.getVivaes().addAll(p);
    }

    /**
     * Adds an Active object to the arena.
     *
     * @param a
     */
    public void addActive(Active a) {
        this.getActives().add(a);
        this.getVivaes().add(a);
    }

    /**
     * Adds a Vector of Active objects to the arena.
     *
     * @param a
     */
    public void addActives(Vector<Active> a) {
        this.getActives().addAll(a);
        this.getVivaes().addAll(a);
    }

    /**
     * Adds a Surfece to the arena.
     *
     * @param s
     */
    public void addSurface(Surface s) {
        this.surfaces.add(s);
    }

    /**
     * Adds a Vector of Surfaces to the arena.
     *
     * @param s
     */
    public void addSurfaces(Vector<Surface> s) {
        this.surfaces.addAll(s);
    }

    @Override
    /**
     * Paints up the arena and all the surfaces and objects inside using DoubleBuffering.
     */
    public void paint(Graphics g) {
        if (offscreen == null) {
            return;
        }
        bufferGraphics.setColor(Color.WHITE);
        bufferGraphics.fillRect(this.getX(), this.getY(), screenWidth, screenHeight);

        for (Surface vivaeObject : surfaces) {
            vivaeObject.paintComponent(bufferGraphics);
        }
        for (Passive vivaeObject : getPassives()) {
            vivaeObject.paintComponent(bufferGraphics, isObjectsOnSightBlinking);
        }
        for (VivaeObject vivaeObject : getPaintable()) {
            vivaeObject.paintComponent(bufferGraphics, isObjectsOnSightBlinking);
        }
        for (Active active : actives) {
            active.paintComponent(bufferGraphics, isObjectsOnSightBlinking);
        }
        g.drawImage(offscreen, 0, 0, this);
    }

    /**
     * Paints up the arena and all the surfaces and objects inside without DoubleBuffering.
     *
     * @param svgGraphics
     */
    public void paintUnbuffered(Graphics svgGraphics) {

        svgGraphics.setColor(Color.WHITE);
        svgGraphics.fillRect(this.getX(), this.getY(), screenWidth, screenHeight);

        for (Surface vivaeObject : surfaces) {
            vivaeObject.paintComponent(svgGraphics);
        }
        for (Passive vivaeObject : getPassives()) {
            vivaeObject.paintComponent(svgGraphics, false);
        }
        for (Active active : actives) {
            active.paintComponent(svgGraphics, false);
        }
    }

    /**
     * Returns a coefficient of the surface the VivaeObject is on or 0 if there is no surface.
     *
     * @param actor
     */
    public float getFrictionOfSurface(VivaeObject actor) {

        ArrayList<Surface> surfacesActorIsOn = new ArrayList<Surface>();
        Surface srfc;
        Area actArea = actor.getArea();

        for (Surface surface : surfaces) {
            srfc = surface;
            Area actor2intersect = (Area)actArea.clone();
            actor2intersect.intersect(srfc.getArea());
            if (!actor2intersect.isEmpty()) {
                surfacesActorIsOn.add(srfc);
            }
        }
        if (surfacesActorIsOn.isEmpty()) {
            return 0f;
        }
        srfc = surfacesActorIsOn.get(surfacesActorIsOn.size() - 1);

        float res = srfc.getFriction();
//        System.out.println("Surface = " + srfc.getClass().toString() + " has friction " + res);
        return res;
    }

    public Surface getSurfaceUnderVivaeObject(VivaeObject actor) {
        Vector<Surface> surfacesActorIsOn = new Vector<Surface>();
        Surface srfc = null;
        for (Surface surface : surfaces) {
            srfc = surface;
            Area actArea = actor.getArea();
            actArea.intersect(srfc.getArea());
            if (!actArea.isEmpty()) {
                surfacesActorIsOn.add(srfc);
            }
        }
        if (surfacesActorIsOn.isEmpty()) {
            return null;
        }
        srfc = surfacesActorIsOn.get(surfacesActorIsOn.size() - 1);
        return srfc;
    }

    public void moveActive(Active a) {
        a.moveComponent();
    }

    /**
     * Sets up new coordinates and rotation of all VivaeObjects according to their movement.
     */
    public void moveVivaes() {
//        for (Active active : actives) {
//            active.moveComponent();
//        }
        for (Passive passive : passives) {
            passive.moveComponent();
        }
        for (VivaeController controller : controllers) {
            controller.moveControlledObject();
        }
    }

    /**
     * Defines reactions on pressed keys for the arena.
     */
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                // switch between antialiased and not antialiased painting.
                setAllArenaPartsAntialiased(!isAllArenaPartsAntialiased);
                break;
            case KeyEvent.VK_V:
                // toggles on and off the visibility of the status frames.
                for (Active active : actives) {
                    active.setShowingStatusFrame(!active.isShowingStatusFrame());
                }
                break;
            case KeyEvent.VK_P:
                // Sets the status frames to be binded to their owner.
                for (Active active : actives) {
                    active.setStatusFramePinedToPosition(!active.isStatusFramePinedToPosition());
                }
                break;
            case KeyEvent.VK_C:
                // captures an SVG output
                captureToSVG(svgOutputFileName);
                break;
            case KeyEvent.VK_N:
                // start caturing
                capture = true;
                break;
            case KeyEvent.VK_M:
                // stop capturing
                capture = false;
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent arg0) {
    }

    /**
     * Runs the main loop.
     * Several number of world steps (according to worldStepsPerLoop property) is taken,
     * a friction coefficient is set up to all Actives and Passives,
     * the VivaeObjects are moved,
     * if the visible output is turned on, the painting method is called
     * and the thread sleeps for a several (according to loopSleepTime property) milisecs.
     */
    public void run() {
        while (isRunning) {
            step();

            //Kod k animaci..
            //TODO: oddelit prezentaci od simulacni logiky.
            if (isVisible && loopSleepTime > 0) {
                repaint();
                if (loopSleepTime > 0) {
                    try {
                        Thread.sleep(loopSleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (stepsDone > totalStepsPerSimulation) {
                isRunning = false;
            }
        }
    }

    public void step() {
        for (int i = 0; i < worldStepsPerLoop; i++) {
            world.step();
//                if (stepsDone % 100 == 0) {
//                    System.out.println("println = "+stepsDone);

//                }
            if (capture) {
                if (stepsDone % 5 == 0) {
                    String s = String.format("%06d", stepsDone);
                    this.captureToSVG("step" + s + ".svg");
                }
            }
            stepsDone++;
        }

        for (Active active : getActives()) {
            active.setDamping(getFrictionOfSurface(active));  //speedup
//            active.setDamping((float) frictionBuffer.getFriction((int) active.getX(), (int) active.getY()));
        }
        for (Passive passive : getPassives()) {
            passive.setDamping(getFrictionOfSurface(passive));  //speedup
//            passive.setDamping((float) frictionBuffer.getFriction((int) passive.getX(), (int) passive.getY()));

        }
        moveVivaes();

        repaint();
    }


    /**
     * Initializes structures necessary for the SVG output
     */
    protected void initializeSVGGraphics() {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        svgDoc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
        svgGraphics = new SVGGraphics2D(svgDoc);
        isSVGGraphicsInitialized = true;
    }

    /**
     * Captures the actual state of arena to the output SVG file.
     *
     * @param name
     */
    public void captureToSVG(String name) {
        boolean oldIsAllArenaPartsAntialiased = isAllArenaPartsAntialiased;
        System.out.println("Capturing scenario to " + name);
        if (!isSVGGraphicsInitialized) {
            initializeSVGGraphics();
        }
        setAllArenaPartsAntialiased(false);
        try {
            paintUnbuffered(svgGraphics);
            File outputFile = new File(name);
            FileWriter out = new FileWriter(outputFile);
            svgGraphics.stream(out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setAllArenaPartsAntialiased(oldIsAllArenaPartsAntialiased);

    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public Vector<Passive> getPassives() {
        return passives;
    }

    public Vector<Active> getActives() {
        return actives;
    }

    public void setPassives(Vector<Passive> passives) {
        this.passives = passives;
    }

//    public Vector<Active> getActivesPositions() {
//        return activesPositions;
//    }


    public Component getParentVindow() {
        return parent;
    }

    public List<PositionMark> getPositions() {
        return positions;
    }

    public Vector<VivaeObject> getVivaes() {
        return vivaes;
    }

    public Vector<Surface> getSurfaces() {
        return surfaces;
    }

    public void setVivaes(Vector<VivaeObject> vivaes) {
        this.vivaes = vivaes;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void setTotalStepsPerSimulation(int totalStepsPerSimulation) {
        this.totalStepsPerSimulation = totalStepsPerSimulation;
    }

    public void setLoopSleepTime(int loopSleepTime) {
        this.loopSleepTime = loopSleepTime;
    }

    public void setWorldStepsPerLoop(int worldStepsPerLoop) {
        this.worldStepsPerLoop = worldStepsPerLoop;
    }

    public int getStepsDone() {
        return stepsDone;

    }

    public FrictionBuffer getFrictionBuffer() {
        return frictionBuffer;
    }

    public void setFrictionBuffer(FrictionBuffer frictionBuffer) {
        this.frictionBuffer = frictionBuffer;
    }

    /**
     * Method that surrounds whole Arena with 4 rectangles, one for each side of Arena,
     * that blocks objects in Arena from falling out of it. The walls are placed outside
     * the Arena so that they won't reduce the space in Arena with their bodies.
     *
     * @param thickness Width of the walls.
     */
    private void encloseWithWalls(int thickness) {

        Fixed northWall = (Fixed) ArenaPartsGenerator.createPart(
                new Rectangle(0, -thickness, screenWidth, thickness), "FixedObstacle", 1, this);
        Fixed southWall = (Fixed) ArenaPartsGenerator.createPart(
                new Rectangle(0, screenHeight, screenWidth, thickness), "FixedObstacle", 1, this);
        Fixed eastWall = (Fixed) ArenaPartsGenerator.createPart(
                new Rectangle(screenWidth, 0, thickness, screenHeight), "FixedObstacle", 1, this);
        Fixed westWall = (Fixed) ArenaPartsGenerator.createPart(
                new Rectangle(-thickness, 0, thickness, screenHeight), "FixedObstacle", 1, this);

        walls.add(northWall);
        walls.add(southWall);
        walls.add(eastWall);
        walls.add(westWall);

        world.add(northWall.getBody());
        world.add(southWall.getBody());
        world.add(eastWall.getBody());
        world.add(westWall.getBody());

        isEnclosedWithWalls = true;
    }

    public boolean isEnclosedWithWalls() {
        return isEnclosedWithWalls;
    }

    public Vector<Fixed> getWalls() {
        return walls;
    }

    public List<VivaeObject> getPaintable() {
        return paintable;
    }
}

