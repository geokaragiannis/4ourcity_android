package com.project.a4ourcity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by georgekaragiannis on 22/02/16.
 */

public class MyService extends Service {

    public final String LOG_TAG = "My service";

    private NotificationManager notificationManager;
    private int ONGOING_NOTIFICATION_ID = 1; // This cannot be 0. So 1 is a good candidate.

    // Thread and runnable.
    private Thread myThread;
    private MyServiceTask myTask;


    // Binder given to clients
    private final IBinder myBinder = new MyBinder();
    // Binder class.
    public class MyBinder extends Binder {
        MyService getService() {
            // Returns the underlying service.
            return MyService.this;
        }
    }

    public MyService() {
    }

    @Override
    public void onCreate() {

        Log.i(LOG_TAG, "Service is being created");

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showMyNotification();

        // Creates the thread
        myTask = new MyServiceTask(getApplicationContext());
        myThread = new Thread(myTask);
        myThread.setPriority(10);
        myThread.start();
        // initialize the location of the phone
        myTask.updateLocation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "Service is being bound");
        // Returns the binder to this service.
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
        // We start the task thread.
        if (!myThread.isAlive()) {
            myThread.start();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        Log.i(LOG_TAG, "Stopping.");
        // Stops the Thread.
        myTask.stopProcessing();
        Log.i(LOG_TAG, "Stopped.");

        notificationManager.cancel(ONGOING_NOTIFICATION_ID);
    }


    @SuppressWarnings("deprecation")
    private void showMyNotification() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification  = new Notification.Builder(this)
                .setContentTitle("Starting service")
                .setContentText("Service Started")
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true).build();

        notificationManager.notify(0,notification);
    }

}
