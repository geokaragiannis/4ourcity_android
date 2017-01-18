package com.project.a4ourcity;

/**
 * Created by georgekaragiannis on 04/02/16.
 * The array list will hold bumpElements
 * which have the location and the speed of
 * the car
 */
public class BumpElement {

    BumpElement(){}

    public double longitude,latitude,speed;

    BumpElement(double lon, double lat, double spd ){

        longitude = lon;
        latitude = lat;
        speed = spd;
    }

    public double getLongitude(){return longitude;}
    public double getLatitude(){return latitude;}
    public double getSpeed(){return speed;}

    public void setLongitude(double x){longitude = x;}
    public void setLatitude(double x){latitude = x;}
    public void setSpeed(double x){speed = x;}
}
