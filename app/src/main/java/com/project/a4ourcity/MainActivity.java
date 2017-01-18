package com.project.a4ourcity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.Manifest;
import android.content.Intent;
import android.content.ServiceConnection;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public class MainActivity extends AppCompatActivity {
    private LocationData locationData = LocationData.getLocationData();//store location to share between activities
    public static String LOG_TAG = "Main Activity";

    Button stopService;

    public static final String SUCCESS = "ok";
    String user_id;

    double longitude = -9.9,latitude = 10.10,speed = 12.221;
    AppInfo appInfo;
    ArrayList<BumpElement> alist;

    private MyService myService;
    private boolean serviceBound;
    private boolean serviceHasBeenStopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appInfo = AppInfo.getInstance(this);
        alist  = new ArrayList<BumpElement>();

        stopService = (Button) findViewById(R.id.stopService);

        // getting the user_id created in Main activity.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = settings.edit();
        user_id = settings.getString("user_id", null);
        Log.i(LOG_TAG, "user_id in ChatActivity from Preferences: " + user_id);
        Log.i(LOG_TAG, "user_id in ChatActivity from Appinfo: " + AppInfo.user_id);

        //mUiHandler = new Handler(getMainLooper(), new UiCallback());

    }

    @Override
    public void onResume(){

        // bind to the service only one time when onResume is called
        // if the user has stopped the service once, then the service
        // should not run when onResume is called
        if(!serviceHasBeenStopped){
            Intent intent = new Intent(this, MyService.class);
            startService(intent);
            bindMyService();
        }


        // EventBus is used as a shotcut to talk to my service
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        super.onResume();
    }

    // when we bind to myService
    private void bindMyService() {
        // Binds to the service.
        Log.i(LOG_TAG, "Starting the service");
        Intent intent = new Intent(this, MyService.class);
        Log.i("LOG_TAG", "Trying to bind");
        // send an intent with a callback
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // displays data on a GoogleMap and also sends the data to the cloud
    // using my API
    public void showOnMap(View v){

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.168.223:7101/OurCityWebService-ViewController-context-root/resources/")
                .addConverterFactory(GsonConverterFactory.create())    //parse Gson string
                .client(httpClient)    //add logging
                .build();

        SendResults service = retrofit.create(SendResults.class);

        for(int i = 0; i< AppInfo.showlist.size();i++) {

            latitude = AppInfo.showlist.get(i).latitude;
            longitude = AppInfo.showlist.get(i).longitude;
            speed = AppInfo.showlist.get(i).speed;
            String timeStamp = new SimpleDateFormat("yyyyMMdd:HHmmss").format(new Date());

            //make the call
            Call<QueryResponse> queryResponseCall = service.write(latitude, longitude, speed, 551,timeStamp);

            //Call retrofit asynchronously
            queryResponseCall.enqueue(new Callback<QueryResponse>() {

                @Override
                public void onResponse(Response<QueryResponse> response) {

                    // check is response is ok
                    try {
                        if (response.body().result.equals(SUCCESS)) {
                            Log.i(LOG_TAG, "Response is ok ");
                        } else {
                            Log.i(LOG_TAG, "message = my error");
                        }
                    } catch (NullPointerException e) {
                        Log.i(LOG_TAG, "Exception Thrown");
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    // Log error here since request failed
                }

            });

        }

        if (serviceBound) {
            Log.i("MyService", "Unbinding via Show on Map");
            unbindService(serviceConnection);
            serviceBound = false;
            // If we like, stops the service.
            if (true) {
                Log.i(LOG_TAG, "Stopping.");
                Intent intent = new Intent(this, MyService.class);
                stopService(intent);
                Log.i(LOG_TAG, "Stopped.");
            }

            // display a toast when service stops
            if (!serviceBound){
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(this, "service stopped", duration);
                toast.show();
                serviceHasBeenStopped = true;
            }

            Log.i(LOG_TAG, "appinfo size is: " + appInfo.alist.size());
        }

        // go to maps activity
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }

    // when pressing Stop Service Button
    public void stopService(View v){

        Intent myintent = new Intent(this,WelcomeActivity.class);
        // go back to WelcomeActivity, but do not launch a new activity
        // use the privious one on the stack (save memory and being neat)
        myintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myintent);
        Log.i(LOG_TAG, "appinfo size is: " + appInfo.alist.size());
        // clear the list that holds the location + speed when button
        // is pressed and reset the text
        AppInfo.showlist.clear();
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(Integer.toString(AppInfo.showlist.size()));

        if (serviceBound) {
            Log.i("MyService", "Unbinding");
            unbindService(serviceConnection);
            serviceBound = false;
            // If we like, stops the service.
            if (true) {
                Log.i(LOG_TAG, "Stopping.");
                Intent intent = new Intent(this, MyService.class);
                stopService(intent);
                Log.i(LOG_TAG, "Stopped.");
            }

            if (!serviceBound){
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(this, "service stopped", duration);
                toast.show();
                serviceHasBeenStopped = true;
            }


        }
    }

    // override the onBackPressed method, so that when the
    // user presses the back button, I do more than just
    // going back
    @Override
    public void onBackPressed() {

        if (serviceBound) {
            Log.i("MyService", "Unbinding using back button");
            unbindService(serviceConnection);
            serviceBound = false;
            // If we like, stops the service.
            if (true) {
                Log.i(LOG_TAG, "Stopping.");
                Intent intent = new Intent(this, MyService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                stopService(intent);
                Log.i(LOG_TAG, "Stopped.");
            }

            // inform the user that the srvice has stopped via a Toast
            // (when the service is no longer bound)
            if (!serviceBound){
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(this, "service stopped", duration);
                toast.show();
                serviceHasBeenStopped = true;
            }

            Log.i(LOG_TAG, "appinfo size is: " + appInfo.alist.size());
        }
        //call super
        super.onBackPressed();
    }


    @Override
    public void onPause(){

        super.onPause();
    }

    // When the user stops the app, without having stopped the service,
    // then we unbind from it.
    @Override
    protected void onDestroy(){
        if (serviceBound) {
            Log.i("MyService", "Unbinding via onDestroy");
            unbindService(serviceConnection);
            serviceBound = false;
            // If we like, stops the service.
            if (true) {
                Log.i(LOG_TAG, "Stopping.");
                Intent intent = new Intent(this, MyService.class);
                stopService(intent);
                Log.i(LOG_TAG, "Stopped.");
            }

            for(int i = 0; i<appInfo.alist.size();i++){
                Log.i(LOG_TAG, "AppInfo longitude of element: " + i + " is" + appInfo.alist.get(i).getLongitude() );
            }
        }

        super.onDestroy();
    }




    // Service connection code.
    // callback that says whether the binding is successful or not
    // 2 methods
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            // We have bound to the service.
            MyService.MyBinder binder = (MyService.MyBinder) serviceBinder;
            // get a reference of the service from serviceBinder parameter
            myService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;

        }
    };

    // eventBus. get message from MyServiceTask and show # of bumps/potholes
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AppInfo app) {

        Log.i(LOG_TAG, "Displaying: " +app.alist.size());
        TextView tv = (TextView) findViewById(R.id.textView);
        // display the size in the textview
        tv.setText(Integer.toString(app.alist.size()));


    }

    // interface to make the call
    public interface SendResults{
        @POST("data/write")
        Call<QueryResponse> write(@Query("latitude") double latitude,
                                            @Query("longitude") double longitude,
                                            @Query("speed") double speed,
                                            @Query("user_id") int user_id,
                                            @Query("timestamp") String timestamp);
    }


}