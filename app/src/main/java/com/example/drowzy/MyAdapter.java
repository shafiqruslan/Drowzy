package com.example.drowzy;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Activity mActvity;
    private Context mContext;
    private ArrayList<LocationContent> mList;
    private LocationList.OnItemClickListener mClickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Activity activity, ArrayList<LocationContent> list, LocationList.OnItemClickListener onItemClickListener) {
        mActvity = activity;
        mContext = mActvity.getApplicationContext();
        mList = list;
        mClickListener = onItemClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_row, parent, false);
        return new MyViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.bind(mList.get(position),mClickListener);
        holder.post_name.setText(mList.get(position).getName());
        holder.post_latitude.setText(Double.toString(mList.get(position).getLatitude()));
        holder.post_longitude.setText(Double.toString(mList.get(position).getLongitude()));
        holder.post_distance.setText(mList.get(position).getDistance()+" Km");

    }

    public void setDataset(ArrayList<LocationContent> locationList){
        mList.clear();
        mList.addAll(locationList);
        notifyDataSetChanged();
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView post_name;
        public TextView post_latitude;
        public TextView post_longitude;
        public TextView post_distance;

        public MyViewHolder(View itemView) {

            super(itemView);
            post_name = itemView.findViewById(R.id.post_name);
            post_latitude = itemView.findViewById(R.id.post_latitude);
            post_longitude = itemView.findViewById(R.id.post_longitude);
            post_distance = itemView.findViewById(R.id.post_distance);

        }

        //Hack to implement clickListener in RecyclerView
        public void bind(final LocationContent list, final LocationList.OnItemClickListener listener){
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    listener.onItemClick(list);
                }
            });
        }
    }
}
