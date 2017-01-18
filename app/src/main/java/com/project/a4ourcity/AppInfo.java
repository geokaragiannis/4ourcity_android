package com.project.a4ourcity;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by georgekaragiannis on 04/02/16.
 * pass info around activities
 */
public class AppInfo {

    public double longitude, latitude, speed;

    public static String user_id;

    // both lists are used but for different purposes. alist
    // is used to pass info around MyServiceTask and MainActivity.
    // showlist is used to pass data to the map and make the call
    public ArrayList<BumpElement> alist = new ArrayList<>();
    public static ArrayList<BumpElement> showlist = new ArrayList<>();

    private static AppInfo instance = null;


    public static AppInfo getInstance(Context context) {
        if(instance == null) {
            instance = new AppInfo();
            instance.longitude = 0;
            instance.latitude = 0;
            instance.speed = 0;
        }
        return instance;
    }

}
