package com.peetie.rpidrone;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

/**
 * Created by Peetie_2 on 09.05.2015.
 */
public class TelemMock implements SensorEventListener {


    private Context context;
    private SensorManager mSensorManager;
    private Sensor compass;
    private Sensor accel;
    private float compassDegrees;
    float lastSin;
    float lastCos;
    float smoothingFactor = 0.5f;


    public TelemMock(Context context) {
        this.context = context;
        doMock();

    }

    public void doMock() {

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null)

        {
            // Success! There's a magnetometer.
            compass = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            accel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            // Failure! No magnetometer.
            return;
        }

    }

    public void onSensorChanged(Sensor sensor, float[] values) {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    float SmoothFactorCompass = 0.08f;
    float SmoothThresholdCompass = 35.0f;
    float oldCompass = 0.0f;

    float[] mGravity;
    float[] mGeomagnetic;
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                    mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuthInRadians = orientation[0];
                float azimuthInDegrees = (float)Math.toDegrees(azimuthInRadians)+360%360;




               // this.compassDegrees = azimuthInDegrees;

               /* lastSin = smoothingFactor * lastSin + (1-smoothingFactor) * (float)Math.sin(azimuthInDegrees);
                lastCos = smoothingFactor * lastCos + (1-smoothingFactor) * (float)Math.cos(azimuthInDegrees);
                this.compassDegrees = (float)Math.atan2((float)lastSin, (float)lastCos);*/
                if (Math.abs( azimuthInDegrees - oldCompass) < 180) {
                    if (Math.abs( azimuthInDegrees - oldCompass) > SmoothThresholdCompass) {
                        oldCompass =  azimuthInDegrees;
                    }
                    else {
                        oldCompass = oldCompass + SmoothFactorCompass * ( azimuthInDegrees - oldCompass);
                    }
                }
                else {
                    if (360.0 - Math.abs( azimuthInDegrees - oldCompass) > SmoothThresholdCompass) {
                        oldCompass =  azimuthInDegrees;
                    }
                    else {
                        if (oldCompass >  azimuthInDegrees) {
                            oldCompass = (oldCompass + SmoothFactorCompass * ((360 +  azimuthInDegrees - oldCompass) % 360) + 360) % 360;
                        }
                        else {
                            oldCompass = (oldCompass - SmoothFactorCompass * ((360 -  azimuthInDegrees + oldCompass) % 360) + 360) % 360;
                        }
                    }
                }
                this.compassDegrees = oldCompass;

            }
        }

    }




    public int getDegrees(){
        return (int)compassDegrees;
}


    protected void finalize ()  {
        mSensorManager.unregisterListener(this);
    }
}
