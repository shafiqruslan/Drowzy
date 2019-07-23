package com.example.drowzy;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class SleepContent {
    public String uid;
    public String name;
    public String time;
    public String date;

    public SleepContent() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public SleepContent(String uid, String name, String time, String date) {
        this.uid = uid;
        this.name = name;
        this.time = time;
        this.date = date;
    }

    // [START slee[_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("time", time);
        result.put("date", date);

        return result;
    }
    // [END post_to_map]



}
