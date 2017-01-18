package com.project.a4ourcity;


import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.ClusterRenderer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    AppInfo appInfo;
    private ArrayList<BumpElement> showList = new ArrayList<>();
    private ArrayList<LatLng> locList = new ArrayList<>();

    // Declare a variable for the cluster manager.
    private ClusterManager<MyItem> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        appInfo = AppInfo.getInstance(this);
    }

    @Override
    protected void onResume(){
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        super.onResume();
    }

    // eventBus. get message from MyServiceTask
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AppInfo app) {
        Log.i("MyMap", "nlah");

        showList = app.alist;
        for(int i = 0; i<showList.size(); i++){
            Log.i("MyMap", "showList.size = " + showList.size());
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.i("MyMap", "size of appinfo.showlist = " + AppInfo.showlist.size());

        for (int i = 0; i < AppInfo.showlist.size(); i++) {
            Log.i("MyMap", "Inside onMapReady");
            double lon = AppInfo.showlist.get(i).longitude;
            double lat = AppInfo.showlist.get(i).latitude;
            double speed = AppInfo.showlist.get(i).speed;

            locList.add(i,new LatLng(lat,lon));

            LatLng bump = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions()
                    .position(bump)
                    .title("Bump" + (i+1) )
                    .snippet("Speed: " + speed));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bump, 14));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(bump)               // Sets the center of the map to bump
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }

//        PolylineOptions rectOptions = new PolylineOptions()
//                .add(new LatLng(38.047574,23.809956))
//                .add(new LatLng(38.147574,23.809956))
//                .add(new LatLng(38.147574,23.909956))
//                .add(new LatLng(38.047574,23.909956))
//                .add(new LatLng(38.057574,23.839956))
//                .width(15)
//                .color(Color.BLUE);
//
//                //.add(new LatLng(38.047574,23.809956));
//
//        Polyline polyline = mMap.addPolyline(rectOptions);

        //---------

        PolylineOptions Poly = new PolylineOptions()
                .addAll(locList)
                .width(5)
                .color(923539);

        Polyline myPolyLine = mMap.addPolyline(Poly);

        setUpClusterer();


    }

    // override the press of the back button, so that the user goes back directly
    // to the WelcomeActivity
    @Override
    public void onBackPressed() {

        // go back via an Intent, but use the activity below on the stack
        Intent myIntent = new Intent(this, WelcomeActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myIntent);

        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(this, "Press button to record again", duration);
        toast.show();

        super.onBackPressed();
    }

    private void setUpClusterer(){

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(mClusterManager);
        //mMap.setOnMarkerClickListener(mClusterManager);

        addItems();

    }

    private void addItems(){

        for(int i = 0; i<locList.size(); i++){

//            Log.i("Loco", "Long is: " + locList.get(i).longitude);
//            Log.i("Loco", "Lat is: " + locList.get(i).latitude);
            MyItem item = new MyItem(locList.get(i).latitude,locList.get(i).longitude);
            mClusterManager.addItem(item);
        }

        // Set some lat/lng coordinates to start with.
//        double lat = 38.047768;
//        double lng = 23.810642;
//
//        // Add ten cluster items in close proximity, for purposes of this example.
//        for (int i = 0; i < 10; i++) {
//            double offset = i / 60d;
//            lat = lat + offset;
//            lng = lng + offset;
//            MyItem offsetItem = new MyItem(lat, lng);
//            mClusterManager.addItem(offsetItem);
//        }
    }
}

