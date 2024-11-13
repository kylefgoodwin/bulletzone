package edu.unh.cs.cs619.bulletzone.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Implements a shake listener and determines what to do after a certain acceleration on a device.
 */
public class ClientActivityShakeDriver implements SensorEventListener {

    SensorManager sensorManager;
    private Sensor sensor;
    private long lastUpdate, actualTime;

    private OnShakeListener listener;

    public interface  OnShakeListener {
        void onShake();
    }

    public ClientActivityShakeDriver(Context context, OnShakeListener listener) {
        this.listener = listener;
        this.lastUpdate = System.currentTimeMillis();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (sensor == null) {
            Log.d("Sensor Failure", "No accelerometer detected");
        } else {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    /**
     * Tracks acceleration changes in accelerometer in device and determines appropriate "shake" level
     * @param sensorEvent SensorEvent object that has the accelerometer data from device
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = sensorEvent.values;
            float x = values[0];
            float y = values [1];
            float z = values[2];

            float EG = SensorManager.GRAVITY_EARTH;
            float dvAccel = (x*x*y*y*z*z)/(EG*EG);

            if (dvAccel >= 1.5) {
                actualTime = System.currentTimeMillis();
                if ((actualTime-lastUpdate) > 1000) {
                    lastUpdate = actualTime;

//                    Log.d("ShakeToFire", "BOOM");
                    listener.onShake();
                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
