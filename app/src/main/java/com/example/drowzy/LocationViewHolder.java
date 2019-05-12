package com.example.drowzy;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

    public class LocationViewHolder extends RecyclerView.ViewHolder {

        public TextView post_name;
        public TextView post_latitude;
        public TextView post_longitude;
        public TextView post_distance;


        public LocationViewHolder(View itemView){
            super(itemView);

            post_name = itemView.findViewById(R.id.post_name);
            post_latitude = itemView.findViewById(R.id.post_latitude);
            post_longitude = itemView.findViewById(R.id.post_longitude);
            post_distance = itemView.findViewById(R.id.post_distance);

        }
    }
