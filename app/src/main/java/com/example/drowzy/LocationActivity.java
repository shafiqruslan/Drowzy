package com.example.drowzy;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class LocationActivity extends AppCompatActivity  {

    private static final String TAG = "LocationActivitynull";
    //private static final int PERMISSION_REQUESTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private RecyclerView.LayoutManager layoutManager;
    private Query query;
    private RecyclerView recyclerView;
    private FusedLocationProviderClient mfusedLocationClient;
    private static final String APIKEY = "AIzaSyBNqQLhxm-jPLFOP5n6wZJBH4Uj48TPMlg";
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private LocationRequest mLocationRequestHighAccuracy;
    private double cLatitude,cLongitude;

    public FirebaseRecyclerAdapter<LocationContent, LocationViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mfusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (isServicesOK()) {
            Log.d(TAG, "onCreate: Services" + isServicesOK());
            if (isMapsEnabled()) {
                Log.d(TAG, "onCreate: Maps "+ isMapsEnabled());
                getLastKnownLocation();
                getListPlace();
            }
        }
    }

    public interface VolleyCallback{
        void onSuccess(String result);
    }

    public void getListPlace() {

        // Write a message to the database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        query = databaseReference.child("Locations/South Bound").limitToFirst(5);

        query.keepSynced(true);

        // set up the RecyclerView
        recyclerView = findViewById(R.id.myrecyclerview);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        FirebaseRecyclerOptions<LocationContent> options =
                new FirebaseRecyclerOptions.Builder<LocationContent>()
                        .setQuery(query, LocationContent.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<LocationContent, LocationViewHolder>(options) {

        @Override
        protected void onBindViewHolder(@NonNull final LocationViewHolder holder, int position, @NonNull final LocationContent model) {
            double latitude = model.getLatitude();
            double longitude = model.getLongitude();

            getJson(new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    double latitude = model.getLatitude();
                    double longitude = model.getLongitude();
                    String name = model.getName();
                    sortLocation(holder,result,name,latitude,longitude);
                }
            },latitude,longitude);
            Log.d(TAG, "onBindViewHolder: " + latitude);
//                ArrayList<LocationData> al=new ArrayList<LocationData>();
//                al.add(new LocationData(name,dLatitude,dLongitude,Double.valueOf(mResult)));
//                Collections.sort(al,new DistanceComparator());
//                for(LocationData st: al) {
//                  System.out.println(st.name + " " + st.latitude + " " + st.longitude + "" + st.distance);
//                    holder.post_name.setText(st.name);
//                    holder.post_latitude.setText(String.valueOf(st.latitude));
//                    holder.post_longitude.setText(String.valueOf(st.longitude));
//                    holder.post_distance.setText(String.valueOf(st.distance));
//                }
//                holder.post_name.setText(model.getName());
//                holder.post_latitude.setText(String.valueOf(model.getLatitude()));
//                holder.post_longitude.setText(String.valueOf(model.getLongitude()));
//                System.out.println("name" +model.getName());
//                String temp = model.getName();
//                String[] split = temp.split("/n");
//
//            Arrays.sort(split);
//            for(int i = 0; i < split.length; i++){
//                System.out.println("tst " + split[i]);
//            }
            }


            @NonNull
            @Override
            public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup viewgroup, int i) {
                View view = LayoutInflater.from(viewgroup.getContext()).inflate(R.layout.location_row, viewgroup, false);

                return new LocationViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        Log.d(TAG, "getListPlace: Lalu" + firebaseRecyclerAdapter);
    }

    private void sortLocation(LocationViewHolder holder,String result,String name,double latitude,double longitude){

        ArrayList<LocationData> al=new ArrayList<LocationData>();
        al.add(new LocationData(name,latitude,longitude,Double.valueOf(result)));
        System.out.println(name);
//        al.add(new LocationData("shafiq",12,23,10));
//        al.add(new LocationData("alia",12,23,21));
//        al.add(new LocationData("abu",12,23,5));
//        al.add(new LocationData("atan",12,23,48));

        Collections.sort(al, new Comparator<LocationData>() {
            @Override
            public int compare(LocationData L1,LocationData L2){
                if(L1.distance==L2.distance)
                    return 0;
                else if(L1.distance>L2.distance)
                    return 1;
                else
                    return -1;
            }
        });
        for(LocationData st: al) {
            System.out.println("sort" + st.name + " " + st.latitude + " " + st.longitude + " " + st.distance);
            holder.post_name.setText(st.name);
            holder.post_latitude.setText(String.valueOf(st.latitude));
            holder.post_longitude.setText(String.valueOf(st.longitude));
            holder.post_distance.setText(String.valueOf(st.distance));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            // int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "getLastKnownLocation: takde permission location");
            getLocationPermission();

        }
        else {
            mfusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d(TAG, "onSuccess: lalu");
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                cLatitude = location.getLatitude();
                                cLongitude = location.getLongitude();
                                Log.d(TAG, "onSuccess: " + cLatitude + "," + cLongitude);
                            }
                        }
                    });
        }
    }

    private void getJson(@NonNull final VolleyCallback callback,double dLatitude, double dLongitude){
        // Request a string response from the provided URL.
        // final TextView post_name = (TextView) findViewById(R.id.post_name);
        // Instantiate the RequestQueue

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="+cLatitude+","+cLongitude+"&destinations="+dLatitude+","+dLongitude+"&key="+APIKEY+"\n";
        Log.d(TAG, "getJson: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

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
                            String distance_new = distance.substring(0, distance.length() - 2);
                            callback.onSuccess(distance_new);
//                          holder.post_distance.setText(String.valueOf(distance));
                            Log.d(TAG, "onResponse: distance"+ distance);
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

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        }

//    private void buildAlertMessageNoGps() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
//                .setCancelable(false)
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
//                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
//                    }
//                });
//        AlertDialog alert = builder.create();
//        alert.show();
//    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );

        if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) || manager.isProviderEnabled( LocationManager.NETWORK_PROVIDER ) ) {
            return true;
        }
        else {
            requestGps();
            return false;
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        else {
            Toast.makeText(this, "Access Location is Denied. Restart your application", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isServicesOK(){

        Log.d(TAG, "isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getListPlace();
                    getLastKnownLocation();
                    callFirebaseRecycler();
                    Log.d(TAG, "onRequestPermissionsResult: True");
                }
            }
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG, "onActivityResult: called.");
//        switch (requestCode) {
//            case PERMISSIONS_REQUEST_ENABLE_GPS: {
//                if(mLocationPermissionGranted){
//                    getLocationPermission();
//
//                }
//            }
//        }
//    }

    private void requestGps(){
        LocationRequest locationRequest = new LocationRequest();
        mLocationRequestHighAccuracy = locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy);

        builder.setNeedBle(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        Log.d(TAG, "onCreate: " + result);

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Log.d(TAG, "onComplete: response"+response);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        LocationActivity.this,
                                        PERMISSIONS_REQUEST_ENABLE_GPS);
                                Log.d(TAG, "onComplete: task complete");
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        Log.d(TAG, "onActivityResult: Lalu");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
                Log.d(TAG, "onActivityResult: request code "+ requestCode);
                switch (resultCode) {
                    case LocationActivity.RESULT_OK:
                        // All required changes were successfully made
                        Log.d(TAG, "onActivityResult: Result ok");
                        getListPlace();
                        callFirebaseRecycler();
                        getLastKnownLocation();
                        break;
                    case LocationActivity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(LocationActivity.this, "Gps is Denied. Restart your application.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

        private void callFirebaseRecycler(){
            if(firebaseRecyclerAdapter != null) {
                firebaseRecyclerAdapter.startListening();
                Log.d(TAG, "onStart: ");
            }
        }

        @Override
        protected void onStart(){
            super.onStart();
            if(firebaseRecyclerAdapter != null) {
                callFirebaseRecycler();
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
            if(firebaseRecyclerAdapter != null) {
                firebaseRecyclerAdapter.stopListening();
                Log.d(TAG, "onStop: ");
            }
        }
    }
