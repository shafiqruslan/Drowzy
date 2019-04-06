package com.example.drowzy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.Reference;
import java.util.ArrayList;

public class LocationActivity extends AppCompatActivity {

    private static final String TAG = "LocationActivitynull";
    private static final int PERMISSION_REQUESTS = 1;
    private RecyclerView.LayoutManager layoutManager;
    private Query query;
    private RecyclerView recyclerView;
    private FusedLocationProviderClient fusedLocationClient;
//    private double latitude,longitude;
    public FirebaseRecyclerAdapter<LocationContent, LocationViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Write a message to the database
         DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        query = databaseReference.child("Locations/South Bound").limitToLast(50);

//        GeoFire geoFire = new GeoFire(databaseReference);

        query.keepSynced(true);
//        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 0.6);

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
            protected void onBindViewHolder(@NonNull LocationViewHolder holder, int position, @NonNull LocationContent model) {
//                double locationLatitude = model.getLatitude();
//                double locationLongitude = model.getLongitude();

                    holder.post_name.setText(model.getName());
                    holder.post_latitude.setText(String.valueOf(model.getLatitude()));
                    holder.post_longitude.setText(String.valueOf(model.getLongitude()));

                //                Log.d(TAG, "onCreate: " + model.getLongitude());
            }

            @NonNull
            @Override
            public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup viewgroup, int i) {
                View view = LayoutInflater.from(viewgroup.getContext()).inflate(R.layout.location_row, viewgroup, false);

                return new LocationViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }


    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUESTS);
//            return;
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
//                                latitude = location.getLatitude();
//                                longitude = location.getLongitude();
                            }
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUESTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

        @Override
        protected void onStart(){
            super.onStart();
            firebaseRecyclerAdapter.startListening();
        }

        @Override
        protected void onStop() {
            super.onStop();
            firebaseRecyclerAdapter.stopListening();
        }
    }
