package vivae.sensors;

import vivae.arena.Arena;
import vivae.arena.parts.VivaeRobotRepresent;
import vivae.arena.parts.sensors.DistanceSensor;
import vivae.robots.IRobotWithSensorsInterface;
import vivae.robots.VivaeRobot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Asynchronnous version of ScalableDistanceSensor.
 * Tries to reproduce asynchronnous getting hardware robot's sensory data because of long delays
 * Purpose is to simulate this behaviour also in virtual arenas.
 */
public class AsyncScalableDistanceSensor extends ScalableDistanceSensor implements ISensor {

    private static final int TIMER_VALUE_MS = 500;

    protected double cachedSensoryData[][];
    protected boolean isRunning = false;
    protected Timer updateTimer;

    private class updateTime extends TimerTask {

        private AsyncScalableDistanceSensor sensor;

        updateTime(AsyncScalableDistanceSensor sensor) {
            this.sensor = sensor;
        }

        @Override
        public void run() {
            sensor.updateSensoryData();
        }

    }

    public double[][] updateSensoryData() {
        double [][]res = new double[1][count];
        for(int i = 0; i < count; i++) {
            res[0][i] = representants.get(i).getDistance(owner.getArena().getVivaes());
        }
        cachedSensoryData = res;
        return res;
    }

    public AsyncScalableDistanceSensor(IRobotWithSensorsInterface robot, double sangle, double eangle, int count, double maxDistance) {
        super(robot, sangle, eangle, count, maxDistance);
    }

    @Override
    public double[][] getSensoryData() {

        //fist call for getSensoryData() - let's start the timer
        if(!isRunning) {
            isRunning = true;
            updateTimer = new Timer();
            updateTimer.schedule (new updateTime(this) , 0, TIMER_VALUE_MS);
        }

        if(cachedSensoryData == null) {
            //first initialization
            updateSensoryData();
        }

        return cachedSensoryData;
    }

    public int getCount() {
        return count;
    }
}
