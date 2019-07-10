package com.example.drowzy;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 30 * 1000;  /* 30 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */
    private static final String APIKEY = "AIzaSyBNqQLhxm-jPLFOP5n6wZJBH4Uj48TPMlg";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
            Log.d(TAG, "onCreate: version");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
    }

    private void getLocation() {
        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        Log.d(TAG, "onLocationResult: got location result.");

                        Location location = locationResult.getLastLocation();

                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.d(TAG, "onLocationResult: Count");
                            Log.d(TAG, "onSuccess: Cordinates" + latitude + "," + longitude);
                            getJson(latitude,longitude);
                        }
                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    private void getJson(double cLatitude, double cLongitude){
        // Request a string response from the provided URL.
        // final TextView post_name = (TextView) findViewById(R.id.post_name);
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

//        String url ="https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="+cLatitude+","+cLongitude+"&destinations="+dLatitude+","+dLongitude+"&key="+APIKEY+"\n";
        String url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+cLatitude+","+cLongitude+"2&radius=1500&type=restaurant&key="+APIKEY+"\n";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                //Display the first 500 characters of the response string.
                // Convert String to json object
                try {
                    JSONObject json = new JSONObject(response);
                    JSONArray results_arr = json.getJSONArray("results");
                    JSONObject geometry_obj = results_arr.getJSONObject(0);
                    JSONObject location_obj = geometry_obj.getJSONObject("location");
                    JSONObject lat_obj = location_obj.getJSONObject("lat");
                    JSONObject long_obj = location_obj.getJSONObject("lng");

                    String name = geometry_obj.getString("name");
                    String lat = lat_obj.getString("lat");
                    String lng = long_obj.getString("lng");
//                    String distance_new = distance.substring(0, distance.length() -2);
                    Log.d(TAG, "onResponse: " + name);
//                            holder.post_distance.setText(String.valueOf(distance_new));
//                            Log.d(TAG, "onResponse: distance"+ distance);
//                          Log.d(TAG, "onResponse: distance " + distance);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                        Log.d(TAG, "onResponse: " + response.substring(0,250));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                post_name.setText("That didn't work!");
            }
        });

        Log.d(TAG, "getJson: " + url);
        queue.add(stringRequest);
    }

}


