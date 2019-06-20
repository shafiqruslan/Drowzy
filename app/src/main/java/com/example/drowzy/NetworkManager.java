package com.example.drowzy;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkManager {

    private static final String TAG = "NetworkManager";
    private static final String PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";

    private Context mContext;
    private final MyAdapter mAdapter;
    private String API_KEY;
    private ArrayList<LocationContent> mList = new ArrayList<>();

    public NetworkManager(Context context, MyAdapter adapter){
        mContext = context.getApplicationContext();
        mAdapter = adapter;
        API_KEY = "AIzaSyBNqQLhxm-jPLFOP5n6wZJBH4Uj48TPMlg";
    }

    public void getNearbyPlaces(Location location, int radius, String keyword){
        String url = PLACES_BASE_URL +
                "location=" +location.getLatitude()+ ","+ location.getLongitude()+
                "&radius=" + radius +
                "&keyword=" + keyword +
                "&key=" + API_KEY;
        makeRequest(url,location);
    }

    public void getNearbyGasStations(Location location, int radius){
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Log.d(TAG, "lat: " + location.getLatitude() + ", long: " + location.getLongitude());
        String url = PLACES_BASE_URL  +
                "type=gas_station" +
                "&location=" +latitude+ ","+ longitude+
                "&radius=" + radius +
                "&key=" + API_KEY;
        makeRequest(url,location);
    }

    private void refreshPlaces(List<LocationContent> list){
        mList.clear();
        mList.addAll(list);
        mAdapter.setDataset(mList);
    }

    private void makeRequest(String url,final Location location){
        Log.d(TAG, "Url: " + url);
        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray results = json.getJSONArray("results");

                            List<LocationContent> list = new ArrayList<>();
                            Log.d(TAG, "no of results: " + results.length());
                            if(results.length() > 0) {
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject googlePlace = results.getJSONObject(i);

                                    Double distance = HelperClass.getDistanceFromLatLonInKm(
                                            location.getLatitude(),location.getLongitude(),
                                            googlePlace.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                                            ,googlePlace.getJSONObject("geometry").getJSONObject("location").getDouble("lng")) / 1000;

                                    String distanceBetweenTwoPlace = String.valueOf(distance);
                                    String distance_new = distanceBetweenTwoPlace.substring(0, 3);
                                    list.add(new LocationContent(googlePlace.getString("name"),
                                            googlePlace.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                                            googlePlace.getJSONObject("geometry").getJSONObject("location").getDouble("lng"),Double.valueOf(distance_new)));
                                }
                                //sort the distance
                                Collections.sort(list,new DistanceComparator());
                            }
                            refreshPlaces(list);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error occurred while getting places", e);
                        }
                        Log.d(TAG, "onResponse: " + response.substring(0,250));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}

