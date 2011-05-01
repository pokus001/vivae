/*
 * Tato trida reprezentuje rozhrani pro realneho robota
 * 
 */
package robot;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import robot.eye.EyeImage;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.sensors.ISensor;

import javax.imageio.ImageIO;

/**
 *
 * @author mirek
 */
public class HardwareRobot implements IHardwareRobotInterface, IRobotWithSensorsInterface {

    public static final String EYE_IMAGE_LEFT = "leftEye";
    public static final String EYE_IMAGE_RIGHT = "rightEye";
    public static final String EYE_IMAGE_DEPTH_MAP = "depthMap";
    public static final String RESP_DONE_STR = "done";
    public static final String RESP_OUT_OF_RANGE_STR = "out_of_range";
    public static final String RESP_UNKNOWN_COMMAND_STR = "unknown_command";
    public static final String RESP_ALERT_LEFT_STR = "alert_left";
    public static final String RESP_ALERT_RIGHT_STR = "alert_right";
    public static final String RESP_ALERT_BACK_STR = "alert_back";
    public static final String RESP_ALERT_FRONT_STR = "alert_front";
    public static final String RESP_ERROR_STR = "error";
    public static final int RESP_DONE_SZ = RESP_DONE_STR.length();
    public static final int RESP_OUT_OF_RANGE_SZ = RESP_OUT_OF_RANGE_STR.length();
    public static final int RESP_UNKNOWN_COMMAND_SZ = RESP_UNKNOWN_COMMAND_STR.length();
    public static final int RESP_ALERT_LEFT_SZ = RESP_ALERT_LEFT_STR.length();
    public static final int RESP_ALERT_RIGHT_SZ = RESP_ALERT_RIGHT_STR.length();
    public static final int RESP_ALERT_BACK_SZ = RESP_ALERT_BACK_STR.length();
    public static final int RESP_ALERT_FRONT_SZ = RESP_ALERT_FRONT_STR.length();
    public static final int RESP_ERROR_SZ = RESP_ERROR_STR.length();
    public static final int RESP_DONE = 0;
    public static final int RESP_OUT_OF_RANGE = 1;
    public static final int RESP_UNKNOWN_COMMAND = 2;
    public static final int RESP_ALERT_LEFT = 3;
    public static final int RESP_ALERT_RIGHT = 4;
    public static final int RESP_ALERT_BACK = 5;
    public static final int RESP_ALERT_FRONT = 6;
    public static final int RESP_ERROR = 7;
    public static final int RESP_TIMEOUT = -1;
    public static final int EYE_LEFT_RGB = 1;
    public static final int EYE_RIGHT_RGB = 2;
    public static final int EYE_DISTANCE_MAP = 3;

    //defines crop for distance image
    // default 0, 240, 640, 240 => bottom half of image
    private static final int CROP_X = 0;
    private static final int CROP_Y = 240;
    private static final int CROP_WIDTH = 640;
    private static final int CROP_HEIGHT = 240;

    //defines scale how distance should be changed
    //default = 0.1 => image 64 x 48
    private static final double DISTANCE_SCALE = 0.1;

    private int timeout = 1000;
    private String hostname = "localhost";
    private int port = 5000;
    private Socket socket = new Socket();
    private long time = System.currentTimeMillis();

    //async sensory data catching related variables:
    private static final int TIMER_VALUE_MS = 500;
    protected boolean isRunning = false;
    protected Timer updateTimer;
    protected double[][] cachedSensorData;
    private static final boolean USE_CACHED_DATA = true;
    private boolean sensorTimerRunning = false;
    private static final boolean OFFLINE_TEST_MODE = true;

    private class updateTime extends TimerTask {

        private HardwareRobot robot;

        updateTime(HardwareRobot robot) {
            this.robot = robot;
        }

        @Override
        public void run() {
            robot.updateSensoryData();
        }

    }



    public synchronized void setPidParams(double K, double Ti, double Td, double k0) throws IOException {
        int resp;
        String cmd;
        cmd = String.format(Locale.US,
                "set_pid_params %.3f %.3f %.3f %.3f;", K, Ti, Td, k0);
        resp = sendCommand(cmd);
        if (resp != RESP_DONE) {
            throw new IOException("BlueCar command Error");
        }

    }

    public void setWheelSpeed(double left, double right) {
        try{
            setWheelSpeed(left, right, 500);
        } catch (IOException e) {
            //TODO: add exception

        }
    }

    public static class StatusValue {
        private Object value;
        private String name;
        public StatusValue(String name, Object value) {
            this.value = value;
            this.name = name;
        }
        public Object getValue() {
            return value;
        }
        public double getValueAsDouble() {
            return ((Double)value).doubleValue();
        }
        public String getName() {
            return name;
        }
    }

    private int readChar(InputStream is) throws IOException {
        long startTime = System.currentTimeMillis();
        while (is.available() == 0) {
            if ((System.currentTimeMillis() - startTime) > timeout) {
                throw new IOException("response timeouted");
            }
        }
        return is.read();
    }

    private int touchChar(InputStream is) throws IOException {
        long startTime = System.currentTimeMillis();
        while (is.available() == 0) {
            if ((System.currentTimeMillis() - startTime) > timeout) {
                throw new IOException("response timeouted");
            }
        }
        is.mark(1);
        int c = is.read();
        is.reset();
        return c;
    }

    private boolean isTokenChar(int c) {
        return Character.isJavaIdentifierPart(c) ||
                c == '.' || c == '+' || c == '-';

    }
    
    private String readToken(InputStream is) throws IOException {
        int c;
        String s = "";
        // vynech mezery
        do {
            c = readChar(is);
        } while (c == ' ' || c == '\r' || c == '\n' || c == '\t');

        s += (char) c;
        if (!isTokenChar(c)) {
            return s;
        }

        c = touchChar(is);
        // precti token
        while (isTokenChar(c)) {
            s += (char) c;
            readChar(is);
            c = touchChar(is);
        }
        // return token
        return s;
    }

    private byte[] readData(int size, InputStream is) throws IOException {
        int count = 0;
        byte[] data = new byte[size];
        do {
            long startTime = System.currentTimeMillis();
            int avail = is.available();
            count += is.read(data, count, Math.min(avail, size - count));
            if ((System.currentTimeMillis() - startTime) > timeout) {
                throw new IOException("response timeouted");
            }
        } while (count < size);
        return data;
    }

    private int readCmdAck(InputStream is) throws IOException {
        int sz = 0;
        String s = "";
        long startTime = System.currentTimeMillis();
        do {
            int len = is.available();
            if (len > 0) {
                int c = is.read();
                if (isTokenChar(c)) {
                    s = "" + (char)c;
                    sz = 1;
                    break;
                }
            }

            if ((System.currentTimeMillis() - startTime) > timeout) {
                throw new IOException("response timeouted");
            }
        } while (true);
        
        do {
            int len = is.available();
            if (len > 0) {
                s += (char) is.read();
                sz++;
                if (sz == RESP_DONE_SZ && s.equals(RESP_DONE_STR)) {
                    return RESP_DONE;
                }
                if (sz == RESP_OUT_OF_RANGE_SZ && s.equals(RESP_OUT_OF_RANGE_STR)) {
                    return RESP_OUT_OF_RANGE;
                }
                if (sz == RESP_UNKNOWN_COMMAND_SZ && s.equals(RESP_UNKNOWN_COMMAND_STR)) {
                    return RESP_UNKNOWN_COMMAND;
                }
                if (sz == RESP_ALERT_LEFT_SZ && s.equals(RESP_ALERT_LEFT_STR)) {
                    return RESP_ALERT_LEFT;
                }
                if (sz == RESP_ALERT_RIGHT_SZ && s.equals(RESP_ALERT_RIGHT_STR)) {
                    return RESP_ALERT_RIGHT;
                }
                if (sz == RESP_ALERT_BACK_SZ && s.equals(RESP_ALERT_BACK_STR)) {
                    return RESP_ALERT_BACK;
                }
                if (sz == RESP_ALERT_FRONT_SZ && s.equals(RESP_ALERT_FRONT_STR)) {
                    return RESP_ALERT_FRONT;
                }
                if (sz == RESP_ERROR_SZ && s.equals(RESP_ERROR_STR)) {
                    return RESP_ERROR;
                }
            }
        } while ((System.currentTimeMillis() - startTime) < timeout);
        throw new IOException("response timeouted");
    }

    private int sendCommand(String cmd) throws IOException {
        String s;
        socket.getOutputStream().write(cmd.getBytes("ISO8859-1"));
        InputStream is = socket.getInputStream();
        return readCmdAck(is);
    }

    /** Konstruktor 
     * 
     * @param hostname IP adresa robota, na ktere robot nasloucha
     * @param port port, na kterem robot nasloucha
     * @throws java.io.IOException nastane v pripade, ze nedojde k navazani 
     * spojeni s robotem
     */
    public HardwareRobot(String hostname, int port) throws IOException {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Tato metoda vytvori spojeni s robotem
     */
    public synchronized void connect() throws IOException {
        socket.connect(new InetSocketAddress(hostname, port));
    }

    /**
     * Tato metoda uzavre spojeni s robotem
     */
    public synchronized void disconnect() throws IOException {
        socket.close();
        socket = new Socket();
    }

    /** Tato metoda nastavuje maximalni rychlost robota v cm/s. Tato funkce
     * svazuje relativni hodnotu rychosti (pouzitou ve funkci setWheelSpeed)
     * s realnou rychlosti v cm/s. Volani teto metody se v aplikaci vyskytne
     * pouze jednou, pokud vicenasobne tak ve vemi omezene mire.
     *
     * Pokud speed je zaporne, robot omezeni maximalni rychlosti ingnoruje.
     *
     */
    public synchronized void setMaxWheelSpeed(double speed) throws IOException {
        int resp;
        String cmd;
        cmd = String.format(Locale.US,
                "set_max_wheel_speed %.2f;", speed);
        resp = sendCommand(cmd);
        if (resp != RESP_DONE) {
            throw new IOException("BlueCar command Error");
        }
    }

    /**
     *  Tato metoda nastavuje rychlost robota pro obe kola najednou. K navratu z
     * metody nedojde drive, dokud neni nastaveni potvrzeno robotem.
     *
     * @param left rychlost leveho kola. Cislo v rozsahu (-1,1). Zaporne cislo
     * znamena vzad, kladne cislo znamena vpred. Pokud je absolutni hodnota
     * rovna jedne, znamena to maximalni rychlost.
     * @param right rychlost praveho kola. Cislo v rozsahu (-1,1). Zaporne cislo
     * znamena vzad, kladne cislo znamena vpred. Pokud je absolutni hodnota
     * rovna jedne, znamena to maximalni rychlost.
     *
     * Pokud byla nastavena maximalni rychlost (setMaxWheelSpeed) na zapornou
     * hodnotu, pak hodnota parametru left (nebo right) ovlivnuje vykon
     * motoru v rozsahu 0-100% bez ohledu na dosazenou rychlost a
     * jeji mozne zmeny vlivem ruzneho napeti baterie.
     *
     * Pokud byla nastavena maximalni rychlost na kladnou hodnotu, pak se
     * predpoklada linearni zavislost mezi parametrem left (nebo right) a
     * rychlosti robota. Toto zajistuje robot bud zpetnou vazbou z kol, nebo
     * na zaklade tabulek. Chovani robota by (az na limitni pripady) nemelo
     * byt zavisle na napeti baterie.
     *
     */
    public synchronized void setWheelSpeed(double left, double right, double duration)
            throws IOException {
        int resp;
        String cmd;

        cmd = String.format(Locale.US,
                "set_wheel_speed %.1f %.1f %.3f;",
                left, right, duration);
        resp = sendCommand(cmd);
        if (resp != RESP_DONE
                && resp != RESP_ALERT_LEFT
                && resp != RESP_ALERT_RIGHT
                && resp != RESP_ALERT_BACK
                && resp != RESP_ALERT_FRONT) {
            throw new IOException(String.format("BlueCar command Error (%d)", resp));
        }
    }

    public  synchronized EyeImage getEyeImage(String what, boolean capture) throws IOException {
        int w, h, size;
        String s;
        String cmd = String.format(Locale.US, "get_eye_image %s %d;", what,
                (capture) ? 1 : 0);
        socket.getOutputStream().write(cmd.getBytes("ISO8859-1"));
        s = "";
       
        InputStream is = new BufferedInputStream(socket.getInputStream());
        // image
        s = readToken(is);
        if (!s.equals("image")) {
            throw new IOException("invalid response format");
        }
        // width
        s = readToken(is);
        w = Integer.parseInt(s);
        // height
        s = readToken(is);
        h = Integer.parseInt(s);
        // image type
        String imageType = readToken(is);
        // #
        s = readToken(is);
        if (!s.equals("#")) {
            throw new IOException("invalid response format");
        }
        // len
        s = readToken(is);
        try {
            size = Integer.parseInt(s);
        } catch (Exception ex) {
            throw new IOException("invalid response format");
        }
        // @
        s = readToken(is);
        if (!s.equals("@")) {
            throw new IOException("invalid response format");
        }
        // data
        byte[] data = readData(size, is);
        // @
        s = readToken(is);
        if (!s.equals("@")) {
            throw new IOException("invalid response format");
        }

        int ack = readCmdAck(is);
        if (ack != RESP_DONE) {
            throw new IOException("error reading image");
        }

        EyeImage image = EyeImage.createEyeImage(w, h, imageType, data);

        return image;

    }

    private void parseStatusItem(String id, String name, double scale, InputStream is,
            String terminator, Map<String,StatusValue> status) throws IOException {
        String s;
        double v;
        s = readToken(is);
        v = Double.parseDouble(s) * scale;
        s = readToken(is);
        if (!s.equals(terminator)) {
           throw new IOException("invalid response format");
        }
        status.put(id, new StatusValue(name, v));
    }
    
    public synchronized Map<String, StatusValue> getStatus() throws IOException {
        String cmd, s;
        double v;
        Map<String,StatusValue> status = new HashMap<String,StatusValue>();
        
        cmd = String.format(Locale.US,
                "get_status;");
        socket.getOutputStream().write(cmd.getBytes("ISO8859-1"));

        InputStream is = new BufferedInputStream(socket.getInputStream());

        s = readToken(is);
        if (!s.equals("{")) {
           throw new IOException("invalid response format");
        }

        parseStatusItem("time", "Time [ms]", 0.001, is, ",", status);
        parseStatusItem("battery-current", "Battery current [A]", 0.001, is, ",", status);
        parseStatusItem("battery-voltage", "Battery voltage [V]", 0.001, is, ",", status);
        parseStatusItem("ir-front-left", "IR sensor front-left [cm]", 0.01, is, ",", status);
        parseStatusItem("ir-front-right", "IR sensor front-right [cm]", 0.01, is, ",", status);
        parseStatusItem("ir- back-center", "IR sensor back-center [cm]", 0.01, is, ",", status);
        parseStatusItem("acceleration-x", "Acceleration X [m/s2]", 9.81E-3,is, ",", status);
        parseStatusItem("acceleration-y", "Acceleration Y [m/s2]", 9.81E-3,is, ",", status);
        parseStatusItem("acceleration-z", "Acceleration Z [m/s2]", 9.81E-3,is, ",", status);
        parseStatusItem("velocity-left", "Velocity left [cm/s]", 0.1, is, ",", status);
        parseStatusItem("velocity-right", "Velocity right [cm/s]", 0.1, is, ",", status);
        parseStatusItem("distance-left", "Distance left [cm]", 1, is, ",", status);
        parseStatusItem("distance-right", "Distance right [cm]", 1, is, ",", status);
        parseStatusItem("desired-speed-left", "Desired speed left [%]", 0.1, is, ",", status);
        parseStatusItem("desired-speed-right", "Desired speed right [%]", 0.1, is, ",", status);
        parseStatusItem("motor-speed-left", "Motor speed left [%]", 0.1, is, ",", status);
        parseStatusItem("motor-speed-right", "Motor speed left [%]", 0.1, is, ",", status);
        parseStatusItem("max-wheel-speed", "Max wheel speed [cm/s]", 1, is, "}", status);
        
        int ack = readCmdAck(is);
        if (ack != RESP_DONE) {
            throw new IOException("error reading status");
        }

        return status;
    }

    public  synchronized String getHostname() {
        return hostname;
    }

    public  synchronized int getPort() {
        return port;
    }

    public  synchronized boolean isConnected() {
        return socket.isConnected();
    }


    /**
     *
     */

    public double[][] getSensorData() {
        if(!USE_CACHED_DATA) {
            //always update
            updateSensoryData();
            return cachedSensorData;
        } else {
            //updated using timer - let's start the timer
            if(!sensorTimerRunning) {
                updateTimer = new Timer();
                updateTimer.schedule (new updateTime(this) , 0, TIMER_VALUE_MS);
                sensorTimerRunning = true;
            }
            if(cachedSensorData == null) {
                updateSensoryData();
            }
            return cachedSensorData;
        }
    }

    public synchronized void updateSensoryData() {

        if(OFFLINE_TEST_MODE) {
           double[][] tst = new double[1][64];
            for(int i = 0; i < 64; i++) {
                tst[0][i] = Math.random();
            }
            cachedSensorData = tst;
        }

        int columnCnt = (int)(CROP_WIDTH * DISTANCE_SCALE);
        int linesCnt = (int)(CROP_HEIGHT * DISTANCE_SCALE);
        double columnMaxes[] = new double[columnCnt];

        try{


            long start = System.currentTimeMillis();
            Image im = getEyeImage(HardwareRobot.EYE_IMAGE_DEPTH_MAP, true).getImage(null);
            long end = System.currentTimeMillis();
            System.out.println("Getting distance map takes " + (end - start) +  " ms.");

            //for testing purposes, also possible to load from file
//            BufferedImage im = ImageIO.read(new File("depthMap.jpg"));

            BufferedImage fullsizeBi = new BufferedImage (im.getWidth(null),im.getHeight(null),BufferedImage.TYPE_INT_RGB);
            Graphics bg = fullsizeBi.getGraphics();
            bg.drawImage(im, 0, 0, null);
            bg.dispose();

            //now we have full-size BufferImage. Let's do crop:
            BufferedImage croppedBi = fullsizeBi.getSubimage(CROP_X, CROP_Y, CROP_WIDTH, CROP_HEIGHT);

            //

            int w = (int)(DISTANCE_SCALE*croppedBi.getWidth());
            int h = (int)(DISTANCE_SCALE*croppedBi.getHeight());
            BufferedImage scaledBi = new BufferedImage(w, h, croppedBi.getType());
            Graphics2D g2 = scaledBi.createGraphics();
            // scale on–the–fly
            AffineTransform at = AffineTransform.getScaleInstance(DISTANCE_SCALE, DISTANCE_SCALE);
            g2.drawRenderedImage(croppedBi, at);
            g2.dispose();

            //debuggin purposes - can save cropped and scaled image to filesystem
            File file = new File("img2save.jpg");
            ImageIO.write(scaledBi, "jpg", file);  // ignore returned boolean

            //get pixels from Image. Pixels are 4-bytes Integers in ARGB form - so need to split R,G,B and process it
            int pixels[] = scaledBi.getRGB(0, 0, columnCnt, linesCnt, null, 0, columnCnt);
            for(int columnIdx = 0; columnIdx < columnCnt; columnIdx++) {

                //higher number = lighter points
                double columnmax = 0;
                for(int rowIdx = 0; rowIdx < linesCnt; rowIdx++) {
                    int cellIdx = rowIdx * columnCnt + columnIdx;
                    int fourBytes = pixels[cellIdx];

                    int r,g,b;
                    //each color 0...255
                    r=((fourBytes & 0x00FF0000) >> 16);
                    g=((fourBytes & 0x0000FF00) >> 8);
                    b=((fourBytes & 0x000000FF));
                    //intensity in interval 0..255
                    double intensity = 0.2989*r +  0.5870*g + 0.1140*b;

                    //currently processing takes maximum from each column.
                    if(intensity > columnmax) {
                        columnmax = intensity;
                    }
                }
                columnMaxes[columnIdx] = columnmax;
//                System.out.println("[" + columnIdx + "] Columnmax = " + columnmax);
            }


            //rescale doubles 0..255 to 0..1 as usual for sensors
            double ret[][] = new double [1][columnCnt];
            for(int i = 0; i < columnCnt; i++) {
                ret[0][i] = columnMaxes[i] / 255;
                System.out.print(Math.round(columnMaxes[i]) + ", ");
            }
            System.out.println();

            cachedSensorData = ret;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    private static BufferedImage toBufferedImage(Image src) {
//        int w = src.getWidth(null);
//        int h = src.getHeight(null);
//        int type = BufferedImage.TYPE_INT_RGB;  // other options
//        BufferedImage dest = new BufferedImage(w, h, type);
//        Graphics2D g2 = dest.createGraphics();
//        g2.drawImage(src, 0, 0, null);
//        g2.dispose();
//        return dest;
//    }

    public List<ISensor> getSensors() {
        throw new IllegalStateException("HW robot does not allow to use its sensors from outside!");
    }

    public void addSensor(ISensor s) {
        throw new IllegalStateException("Cannot add new sensor to Hardware robot - reimplement HWrobot class instead!");
    }


    @Override
    public void finalize() {
        try {
            System.out.println("Closing socket");
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(HardwareRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
    
