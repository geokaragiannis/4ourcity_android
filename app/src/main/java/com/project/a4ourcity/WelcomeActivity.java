package com.project.a4ourcity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.LightingColorFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class WelcomeActivity extends AppCompatActivity {

    AppInfo appInfo;
    public static String LOG_TAG = "WelcomeActivity";
    private String user_id;
    private LocationData locationData = LocationData.getLocationData();//store location to share between activities
    private Button recButton;
    private Location mCurrentLocation;
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        appInfo = AppInfo.getInstance(this);

        recButton = (Button) findViewById(R.id.RecordButton);
        spinner = (ProgressBar) findViewById(R.id.progBar);
        spinner.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000, 0x373E50));

        // creating a user id and storing it to System Preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        user_id = settings.getString("user_id", null);
        AppInfo.user_id = user_id;
        if (user_id == null) {
            // Creates a random one, and sets it.
            SecureRandomString srs = new SecureRandomString();
            user_id = srs.nextString();
            SharedPreferences.Editor e = settings.edit();
            e.putString("user_id", user_id);
            e.commit();
            AppInfo.user_id = user_id;
            Log.i(LOG_TAG, "user_id in main from AppInfo: " + AppInfo.user_id);
            Log.i(LOG_TAG, "user_id in Main: " + user_id );
        }

//        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        setSupportActionBar(myToolbar);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }

    // when button is pressed, we go to record a trip to MainActivity via an intent
    public void goToRecord(View v){
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
        // clear the list that holds the location data + the speed
        AppInfo.showlist.clear();
    }

    @Override
    public void onResume(){

        Log.i(LOG_TAG, "In welcome activity, size of aList is: " + appInfo.alist.size());


        for(int i=0; i<appInfo.alist.size();i++){
            Log.i(LOG_TAG, "In welcome activity, Log and lat of alist of element: " + i + " is: " +
                    appInfo.alist.get(i).longitude + ", " + appInfo.alist.get(i).latitude);
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // get the location of the user in the WelcomeActivity
        requestLocationUpdate();

        super.onResume();
    }

    /*
	Request location update. This must be called in onResume
	 */
    private void requestLocationUpdate(){
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Log.i("LocationData2", "requesting location update");
                //Log.i("LocationData", "Long in welcome when requesting is: " + locationData.getLocation().getLongitude());

            }
        }
    }

    /**
     * Listens to the location, and gets the most precise recent location.
     * Copied from Prof. Luca class code
     */
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            Location lastLocation = locationData.getLocation();
            mCurrentLocation = location;

            if(mCurrentLocation != null) {
                //if (mCurrentLocation.getLongitude() == 0 && mCurrentLocation.getLatitude() == 0) {
                recButton.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.GONE);
                //}
            } else{
                recButton.setVisibility(View.INVISIBLE);
            }

            // Do something with the location you receive.
            double newAccuracy = location.getAccuracy();

            long newTime = location.getTime();
            // Is this better than what we had?  We allow a bit of degradation in time.
            boolean isBetter = ((lastLocation == null) ||
                    newAccuracy < lastLocation.getAccuracy() + (newTime - lastLocation.getTime()));
            if (isBetter) {
                // We replace the old estimate by this one.
                locationData.setLocation(location);
                Log.i("LocationData", "Long in welcome is: " + locationData.getLocation().getLongitude()
                 + "and the latitude is: " + locationData.getLocation().getLatitude());

            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {
//            recButton = (Button) findViewById(R.id.RecordButton);
//            if(recButton.getVisibility() == View.VISIBLE)
//                recButton.setVisibility(View.INVISIBLE);
        }
    };

    /*
Remove location update. This must be called in onPause if the user has allowed location sharing
 */
    private void removeLocationUpdate() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {

                locationManager.removeUpdates(locationListener);
                Log.i(LOG_TAG, "removing location update");
            }
        }
    }

    @Override
    public void onPause(){

        removeLocationUpdate();// must disable location updates now
        Log.i(LOG_TAG, "We're on pause");
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AppInfo app) {
        Log.i(LOG_TAG, "Displaying: " + app.alist.size());

    }
}
