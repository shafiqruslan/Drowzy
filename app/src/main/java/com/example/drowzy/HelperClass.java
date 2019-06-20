package com.example.drowzy;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.android.volley.VolleyLog.TAG;

public class HelperClass{
//    private static final String APIKEY = "AIzaSyBNqQLhxm-jPLFOP5n6wZJBH4Uj48TPMlg";

    public static Double getDistanceFromLatLonInKm(Double firstPointLatitude,
                                                   Double firstPointLongitude,
                                                   Double secondPointLatitude,
                                                   Double secondPointLongitude) {
        // Radius of the earth in km
        final int radiusOfEarth = 6371;
        Double latitudeDistance = Math.toRadians(secondPointLatitude - firstPointLatitude);
        Double longitudeDistance = Math.toRadians(secondPointLongitude - firstPointLongitude);

        double a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(Math.toRadians(firstPointLatitude)) * Math.cos(Math.toRadians(secondPointLatitude))
                * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        // convert to meters
        double distance = radiusOfEarth * c * 1000;
        distance = Math.pow(distance, 2) + Math.pow(0.0, 2);

        return Math.sqrt(distance);
    }

    public static double Perclos(double time){
        double perclos = time/30000 * 100;
        return perclos;
    }

//    public static void getDistanceFromLatLonInKm(Double firstPointLatitude,
//                                                 Double firstPointLongitude,
//                                                 Double secondPointLatitude,
//                                                 Double secondPointLongitude){
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//
//        String url ="https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="+firstPointLatitude+","+firstPointLongitude+"&destinations="+secondPointLatitude+","+secondPointLongitude+"&key="+APIKEY+"\n";
//
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
//
//            @Override
//            public void onResponse(String response) {
//
//                //Display the first 500 characters of the response string.
//                // Convert String to json object
//                try {
//                    JSONObject json = new JSONObject(response);
//                    JSONArray rows_arr = json.getJSONArray("rows");
//                    JSONObject rows_obj = rows_arr.getJSONObject(0);
//                    JSONArray elements_arr = rows_obj.getJSONArray("elements");
//                    JSONObject elements_obj = elements_arr.getJSONObject(0);
//                    JSONObject distance_obj = elements_obj.getJSONObject("distance");
//
//                    String distance = distance_obj.getString("text");
//                    String distance_new = distance.substring(0, distance.length() -2);
//                    Log.d(TAG, "onResponse: " + distance_new);
////                            holder.post_distance.setText(String.valueOf(distance_new));
////                            Log.d(TAG, "onResponse: distance"+ distance);
////                          Log.d(TAG, "onResponse: distance " + distance);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
////                        Log.d(TAG, "onResponse: " + response.substring(0,250));
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
////                post_name.setText("That didn't work!");
//            }
//        });
//
//        Log.d(TAG, "getJson: " + url);
//        queue.add(stringRequest);
//    }
}

