package com.project.a4ourcity;

import android.location.Location;

/**
 * Created by georgekaragiannis on 27/01/16.
 * Taken from T.A shobhit from findRestaurant
 */
public class LocationData {

    private static LocationData instance = null;

    private LocationData(){}

    private Location location;


    public Location getLocation(){
        return location;
    }

    public void setLocation(Location _location){
        location = _location;
    }

    public static LocationData getLocationData(){
        if(instance == null){
            instance = new LocationData();
        }
        return instance;
    }
}
