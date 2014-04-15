package com.wearfleet.core;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.wearfleet.core.events.BearingEvent;
import com.wearfleet.core.events.LocationEvent;
import com.wearfleet.core.utils.Config;
import com.wearfleet.core.utils.MathUtils;

import de.greenrobot.event.EventBus;

public class BearingProvider implements Provider {
    private static final String PREF_BEARING_PERIOD = "BEARING_PERIOD";
    private static final int BEARING_PERIOD_DEFAULT = 7500;
    private static final int ARM_DISPLACEMENT_DEGREES = 6; //Try and make up for prism rotation
    private final SensorManager mSensorManager;
    private final int mPeriod;
    private float[] mRotationMatrix;
    private float[] mOrientation;

    private GeomagneticField mGeomagneticField;

    public BearingProvider(Context c) {
        EventBus.getDefault().register(this);
        mRotationMatrix = new float[16];
        mOrientation = new float[9];

        mPeriod = c.getSharedPreferences(Config.PREFS_NAME, c.MODE_PRIVATE).getInt(PREF_BEARING_PERIOD, BEARING_PERIOD_DEFAULT);

        mSensorManager = (SensorManager) c.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void shutdown() {
        EventBus.getDefault().unregister(this);
        mSensorManager.unregisterListener(mSensorListener);
    }

    public void onEvent(LocationEvent e) {
        mGeomagneticField = new GeomagneticField((float) e.getLocation().getLatitude(),
                (float) e.getLocation().getLongitude(), (float) e.getLocation().getAltitude(),
                e.getLocation().getTime());
    }

    /**
     * Use the magnetic field to compute true (geographic) north from the specified heading
     * relative to magnetic north.
     *
     * @param heading the heading (in degrees) relative to magnetic north
     * @return the heading (in degrees) relative to true north
     */
    private float computeTrueNorth(float heading) {
        if (mGeomagneticField != null) {
            return heading + mGeomagneticField.getDeclination();
        } else {
            return heading;
        }
    }

    private SensorEventListener mSensorListener = new SensorEventListener() {

        private long lastBearingTime = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                if (System.currentTimeMillis() - mPeriod < lastBearingTime)
                    return;
                lastBearingTime = System.currentTimeMillis();
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,
                        SensorManager.AXIS_Z, mRotationMatrix);
                SensorManager.getOrientation(mRotationMatrix, mOrientation);

                // Convert the heading (which is relative to magnetic north) to one that is
                // relative to true north, using the user's current location to compute this.
                float magneticHeading = (float) Math.toDegrees(mOrientation[0]);
                float mHeading = MathUtils.mod(computeTrueNorth(magneticHeading), 360.0f)
                        - ARM_DISPLACEMENT_DEGREES;

                EventBus.getDefault().postSticky(new BearingEvent(mHeading));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //nobody cares
        }
    };
}
