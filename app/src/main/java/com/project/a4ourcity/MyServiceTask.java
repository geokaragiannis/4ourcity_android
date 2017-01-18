package com.project.a4ourcity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by georgekaragiannis on 22/02/16.
 */

public class MyServiceTask implements Runnable {


    public static final String LOG_TAG = "My Service Task";
    private boolean running;
    private Context context;

    private float last_x, last_y, last_z;
    private long lastUpdate = 0;
    // acceleration Threshold
    private static final float ACCZ_THRESHOLD = 6.5f;
    // boolean for an event (bump/pothole)
    boolean event = false;

    double longitude,latitude,speed;
    long timeOfPrevEvent;

    AppInfo appInfo;

    private SensorManager senSensorManager = null;
    private SensorManager senMagneticManager = null;
    private Sensor senAccelerometer;
    private Sensor senMagnetic;

    private SensorEventListener mListener;

    private LocationManager locationManager;
    //store location to share between activities
    private LocationData locationData = LocationData.getLocationData();

    private Vibrator v;

    float x,y,z = 7.8f;

    float[] inR = new float[9];
    float[] I = new float[9];
    float[] gravity = new float[3];
    float[] geomag = new float[3];


    public MyServiceTask(Context _context) {
        context = _context;
        appInfo = AppInfo.getInstance(_context);
        requestLocationUpdate();
        // Put here what to do at creation.

        // sensors
        senSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        senMagneticManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senMagnetic = senMagneticManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //location
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);

    }


    private void requestLocationUpdate(){
       // Log.i(LOG_TAG, "Inside requestLocationUpdate");
        if (locationManager != null) {
           // Log.i(LOG_TAG, "Location Manager is not NULL");
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Log.i("Location:" , "requesting location update");
            }
        }
    }

    @Override
    public void run() {

        running = true;

        while (running) {

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.getLocalizedMessage();
            }

            Log.i(LOG_TAG, "inside run of MyService");
            // add a listener to the sensor
            mListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    // if the service is no longer running then stop accessing
                    // the accelerometer
                    if(!running)
                        return;

                   event = false;

                    //Log.i(LOG_TAG, "inside onSensorChanged");
                    Sensor mySensor = sensorEvent.sensor;

                    if(mySensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){

                        geomag = sensorEvent.values.clone();
                    }

                    if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                        // current time
                        long curTime = System.currentTimeMillis();

                        long diffTime = (curTime - lastUpdate);
                        lastUpdate = curTime;

                        //gravity matrix (raw values)
                        gravity = sensorEvent.values.clone();

                        // smoothing factor
                        //final float alpha = (float) 0.15;

//                        // Isolate the force of gravity with the low-pass filter.
//                        x = alpha * x + (1 - alpha) * sensorEvent.values[0];
//                        y = alpha * y + (1 - alpha) * sensorEvent.values[1];
//                        z = alpha * z + (1 - alpha) * sensorEvent.values[2];

                        // Remove the gravity contribution with the high-pass filter.
                        //float linear_acceleration = sensorEvent.values[2] - z;

                        if(gravity != null && geomag != null){

                            boolean success = SensorManager.getRotationMatrix(inR,I,gravity,geomag);

                            //Log.i(LOG_TAG," " + success);
                        }

                        // rotation
                        float[] new_grav = new float[3];
                        new_grav[0] = inR[0]*gravity[0] + inR[1]*gravity[1] + inR[2]*gravity[2];
                        new_grav[1] = inR[3]*gravity[0] + inR[4]*gravity[1] + inR[5]*gravity[2];
                        new_grav[2] = inR[6]*gravity[0] + inR[7]*gravity[1] + inR[8]*gravity[2];

                        // subtract gravity
                        //new_grav[2] =  new_grav[2] -9.79379f;


                        // if the abs value of the linear acceleration in the z
                        // direction is above the Threshold, then an event has
                        // happened
                        if (Math.abs(new_grav[2]) > ACCZ_THRESHOLD +  9.79379f) {
                            Log.i(LOG_TAG,"linear acceleration in z = " + new_grav[2]);

                            // consider another event 2.5 seconds after the time
                            // of the previous event (do not count events twice)
                            if(curTime-timeOfPrevEvent > 2000){
                                event = true;
                                v.vibrate(200);
                                //appInfo.alist.add(new BumpElement(10, 10, 200));
                                Log.i(LOG_TAG, "Event happened in MyService!");
                                // when an event happens, update the location and
                                // go to eventHappened to add data in the List
                                updateLocation();
                                eventHappened();

                                Log.i(LOG_TAG, "appInfo size: " + appInfo.alist.size());
                                timeOfPrevEvent = curTime;

                                // display relative acceleration via a Toast
                                float relative_accl = Math.abs(new_grav[2]-9.79379f)/9.79379f;
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(context,(float)Math.round(relative_accl * 100d) / 100d + "g", duration);
                                toast.show();
                            }

                        } else{
                            event = false;
                        }

                        last_x = x;
                        last_y = y;
                        last_z = z;
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
//-----------------------------------------------------------------------------------------------

            senSensorManager.registerListener(mListener,senAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
            senMagneticManager.registerListener(mListener, senMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    // called when we wish to unbind from the service
    public void stopProcessing() {
        running = false;
        appInfo.alist.clear();
        
        //AppInfo.showlist.clear();

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {

                locationManager.removeUpdates(locationListener);
            }
        }

        //SensorManager senSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //senSensorManager.unregisterListener(context);

    }

    // location listener to get the location
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            Location lastLocation = locationData.getLocation();

            // Do something with the location you receive.
            double newAccuracy = location.getAccuracy();

            //get the speed
            speed = location.getSpeed();
            Log.i(LOG_TAG,"speed is: " + speed);

            long newTime = location.getTime();
            // Is this better than what we had?  We allow a bit of degradation in time.
            boolean isBetter = ((lastLocation == null) ||
                    newAccuracy < lastLocation.getAccuracy() + (newTime - lastLocation.getTime()));
            if (isBetter) {
                // We replace the old estimate by this one.
                locationData.setLocation(location);

                //Now we have the location.
                //showAccuracy(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    public void updateLocation() {

        //Log.i(LOG_TAG, "inside updateLocation");

        requestLocationUpdate();

        // add the location to the LocationData class
        if(locationData.getLocation() != null) {
            longitude = (locationData.getLocation().getLongitude());
            //longitude = String.format("%5.1f");
            latitude = (locationData.getLocation().getLatitude());
            //latitude = String.format("%5.1f");
            Log.i("Location: ","longitude = " + longitude + " latitude = " + latitude);
        }

    }

    // add data to a list and transfer it via EventBus to MainActivity
    private void eventHappened(){

        Log.i(LOG_TAG, "Time of event is bigger than time of previous event");
        Log.i(LOG_TAG, "adding one elemenet to AppInfo.alist");
        appInfo.alist.add(new BumpElement(longitude, latitude, speed));
        AppInfo.showlist.add(new BumpElement(longitude, latitude, speed));
        EventBus.getDefault().post(appInfo);


        for(int i = 0; i<appInfo.alist.size();i++){
            Log.i(LOG_TAG, "AppInfo longitude of element: " + i + " is" + appInfo.alist.get(i).getLongitude() );
        }
    }

}