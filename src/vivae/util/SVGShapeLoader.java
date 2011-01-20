/**
 * This is VIVAE (Visual Vector Agent Environment)
 * a library allowing for simulations of agents in co-evolution 
 * written as a bachelor project 
 * by Petr Smejkal
 * at Czech Technical University in Prague
 * in 2008
 */

package vivae.util;

import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GVTTreeWalker;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.svg.GVTTreeBuilder;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

/**
 * SVShapeLoader loads an SVG file, creates a DOM from it, 
 * searches for all known types of ArenaPart and can return a map of shapes indexed by their type. 
 * It also works with layers in the SVG file, so you can get a Vector with such a map for each layer.
 * To use it, create it's instance, run it as a thread and then get the Shapes by calling getShapesWithTypeMap.
 * 
 * @author Petr Smejkal
 */
public class SVGShapeLoader extends Thread{

    private Vector<Shape> shapes = new Vector<Shape>();
    private File file;
    private boolean isDoneGeneratingGVTTree = false;
    private boolean isAllDone = false;
    private boolean isReady = false;
    private BridgeContext ctx;
    private GVTTreeBuilder builder;
    private SVGDocument svgDoc = null;
    private GraphicsNode rootGraphicsNode = null;
    private HashMap<String, Vector<Shape>> shapesWithTypeMapActualLevel = new HashMap<String, Vector<Shape>>();
    private Vector<HashMap<String, Vector<Shape>>> shapesWithTypeMapInLevels = new Vector<HashMap<String,Vector<Shape>>>();
    private int shapesCount = 0;
    private int svgFileHeight = 0;
    private int svgFileWidth = 0;
    		

    /**
     * Sets up all inner structures and creates a build tree of SVG elements in the input file.
     * To start the creation of the shapes run an instance as a thread.
     * @param fileName Spcifies the file the shapes will be loaded from.
     */
    public SVGShapeLoader(String fileName){
    	file = new File(fileName);
    	UserAgent ua = new UserAgentAdapter();
    	DocumentLoader loader = new DocumentLoader(ua);
    	ctx = new BridgeContext (ua, loader);
    	ctx.setDynamic(true);
    	try {
            svgDoc = (SVGDocument)loader.loadDocument(file.toURI().toString());
        } 
        catch (IOException e2) {
            System.out.println(e2.toString());
            e2.printStackTrace();
          
	}
        builder = new GVTTreeBuilder(svgDoc, ctx);	
        builder.addGVTTreeBuilderListener(
            new GVTTreeBuilderAdapter() {
                @Override
                public void gvtBuildStarted(GVTTreeBuilderEvent e) {}
                @Override
                public void gvtBuildCompleted(GVTTreeBuilderEvent event) {
                        rootGraphicsNode = event.getGVTRoot();
                        isDoneGeneratingGVTTree = true;
                }
            }
        );
        isReady = true;
    }

    public Vector<Shape> getShapes(){
    	return shapes;
    }
    /**
     * This boolean tells if all the shapes have already been loaded.
     * @return
     */
    public boolean isAllDone() {
        return isAllDone;
    }

    public int getLoadedShapesCount(){
        return shapesCount;
    }

    /**
     * Call this method, or run an instance of this class to actually load the shapes and transfor them into a map of Java Shapes.
     * It also reads svg file width and height and stores them as global attributes svgFileWidth, svgFileHeight.
     */
    @Override
    public void run() {
        shapesCount = 0;
        String tagName;
        String type = "";
        boolean arenaPartDefiningTag;
        if (isReady) {
            builder.start();
        }
        try {
            builder.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(SVGShapeLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        GVTTreeWalker treeWalker = new GVTTreeWalker(rootGraphicsNode);
        GraphicsNode graphicsNode = treeWalker.getGVTRoot();
        while((graphicsNode = treeWalker.nextGraphicsNode()) != null) {      	
            Element el = ctx.getElement(graphicsNode);
            tagName = el.getTagName();
            arenaPartDefiningTag = false;
            if (el != null) {
                NamedNodeMap map = el.getAttributes();
                for (int i = 0; i < map.getLength(); i++) {
                    Node attr = map.item(i);
                    if(attr.getNodeName().equals("inkscape:label") && !tagName.equals("svg")) {
                        type = attr.getTextContent();
                        arenaPartDefiningTag = true;
                    }
                    else if("inkscape:groupmode".equals(attr.getNodeName()) && "layer".equals(attr.getTextContent())){
                        arenaPartDefiningTag = false;
                        addCurrentLayerMapToAllLayersMap();
                        break;
                    }
                    else if(attr.getNodeName().equals("width") && tagName.equals("svg")) {
                        try {
                            svgFileWidth = Integer.parseInt(attr.getTextContent());
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                    else if(attr.getNodeName().equals("height") && tagName.equals("svg")) {
                        try {
                            svgFileHeight = Integer.parseInt(attr.getTextContent());
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                    

                }
                if (arenaPartDefiningTag) {
                    Shape shape = graphicsNode.getOutline();
                    if(shapesWithTypeMapActualLevel.containsKey(type)) {
                        shapesWithTypeMapActualLevel.get(type).add(shape);
                    }
                    else{
                        Vector<Shape> v = new Vector<Shape>();
                        v.add(shape);
                        shapesWithTypeMapActualLevel.put(type, v);
                    }
                    shapesCount++;
                }
            }
        }
        addCurrentLayerMapToAllLayersMap();
        isAllDone = true;
    }


    private void addCurrentLayerMapToAllLayersMap(){
            HashMap<String, Vector<Shape>> mapForThisLevel = new HashMap<String, Vector<Shape>>();
            mapForThisLevel.putAll(shapesWithTypeMapActualLevel);
            shapesWithTypeMapInLevels.add(mapForThisLevel);
            shapesWithTypeMapActualLevel = new HashMap<String, Vector<Shape>>();
    }




    /**
     * After the maps of the shapes has been created when this instance had ran as a thread 
     * and the method isAllDone() returns true, 
     * the shapes are prepared in the structure you get from this method.
     * @return A vector that contains a map for each layer in the SVG input file. Each Map is indexed by a ArenaPart type, for example "obstacle" and contains a vector of shapes the Obstacles will be constructed from.
     */
    public Vector<HashMap<String, Vector<Shape>>> getShapesWithTypeMap() {
            if(isAllDone) return shapesWithTypeMapInLevels;
            else return null;
    }
    
    public int getWidth() {
        return svgFileWidth;
    }
    
    public int getHeight() {
        return svgFileHeight;
    }



}