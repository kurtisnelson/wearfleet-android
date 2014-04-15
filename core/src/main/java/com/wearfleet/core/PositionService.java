package com.wearfleet.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.wearfleet.core.events.BearingEvent;
import com.wearfleet.core.events.LocationEvent;
import com.wearfleet.core.utils.Config;
import com.wearfleet.core.utils.MathUtils;

import de.greenrobot.event.EventBus;

public class PositionService extends Service {
    private static final String PREF_LOCATION_PERIOD = "LOCATION_PERIOD";
    private static final String PREF_BEARING_PERIOD = "BEARING_PERIOD";
    private static final int LOCATION_PERIOD_DEFAULT = 15000;
    private static final int BEARING_PERIOD_DEFAULT = 7500;
    private static final int ARM_DISPLACEMENT_DEGREES = 6; //Try and make up for prism rotation

    private LocationManager mLocationManager;
    private SensorManager mSensorManager;
    private float[] mRotationMatrix;
    private float[] mOrientation;
    private GeomagneticField mGeomagneticField;

    @Override
    public void onCreate() {
        super.onCreate();
        mRotationMatrix = new float[16];
        mOrientation = new float[9];
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(false);

        String locationProvider = mLocationManager.getBestProvider(criteria, true);
        mLocationManager.requestLocationUpdates(locationProvider, getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE).getInt(PREF_LOCATION_PERIOD, LOCATION_PERIOD_DEFAULT), 2, mLocationListener);

        EventBus.getDefault().postSticky(new LocationEvent(mLocationManager.getLastKnownLocation(locationProvider)));

        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mSensorListener);
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private SensorEventListener mSensorListener = new SensorEventListener() {

        private long lastBearingTime = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                if(System.currentTimeMillis() - getSharedPreferences(Config.PREFS_NAME, MODE_PRIVATE).getInt(PREF_BEARING_PERIOD, BEARING_PERIOD_DEFAULT) < lastBearingTime)
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

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location l) {
            updateGeomagneticField(l);
            EventBus.getDefault().postSticky(new LocationEvent(l));
            if(l.hasBearing())
                EventBus.getDefault().postSticky(new BearingEvent(l.getBearing()));
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void updateGeomagneticField(Location location) {
        mGeomagneticField = new GeomagneticField((float) location.getLatitude(),
                (float) location.getLongitude(), (float) location.getAltitude(),
                location.getTime());
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
}
