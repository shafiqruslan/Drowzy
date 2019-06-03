package com.example.drowzy;

import android.Manifest;
import android.app.ActivityManager;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationService2 extends Service {

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
                            callData(latitude,longitude);
                            Log.d(TAG, "onLocationResult: Count");
                            Log.d(TAG, "onSuccess: Cordinates" + latitude + "," + longitude);
                        }
                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    private void callData(final double cLatitude, final double cLongitude){
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Locations/South Bound");

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // TODO: handle the post

                    String name = postSnapshot.child("name").getValue().toString();
                    String data = postSnapshot.child("latitude").getValue().toString();
                    String data2 = postSnapshot.child("longitude").getValue().toString();

                    double dLatitude = Double.valueOf(data);
                    double dLongitude = Double.valueOf(data2);

                    Log.d(TAG, "onDataChange: latitude" );
                    getJson(cLatitude,cLongitude,dLatitude,dLongitude);

                }
//                Log.d(TAG, "onDataChange: list" + list);;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void getJson(double cLatitude, double cLongitude, final double dLatitude, final double dLongitude){
        // Request a string response from the provided URL.
        // final TextView post_name = (TextView) findViewById(R.id.post_name);
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        String url ="https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="+cLatitude+","+cLongitude+"&destinations="+dLatitude+","+dLongitude+"&key="+APIKEY+"\n";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                //Display the first 500 characters of the response string.
                // Convert String to json object
                try {
                    JSONObject json = new JSONObject(response);
                    JSONArray rows_arr = json.getJSONArray("rows");
                    JSONObject rows_obj = rows_arr.getJSONObject(0);
                    JSONArray elements_arr = rows_obj.getJSONArray("elements");
                    JSONObject elements_obj = elements_arr.getJSONObject(0);
                    JSONObject distance_obj = elements_obj.getJSONObject("distance");

                    String distance = distance_obj.getString("text");
                    String distance_new = distance.substring(0, distance.length() -2);
                    Log.d(TAG, "onResponse: " + distance_new);
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

//    public static int getMinValue(int[] numbers){
//        int minValue = numbers[0];
//        for(int i=1;i<numbers.length;i++){
//            if(numbers[i] < minValue){
//                minValue = numbers[i];
//            }
//        }
//        return minValue;
//    }
}


