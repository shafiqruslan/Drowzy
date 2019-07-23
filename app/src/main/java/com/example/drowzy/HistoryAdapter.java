package com.example.drowzy;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context mCtx;
    private List<HistoryData> historyList;

    public HistoryAdapter(Context mCtx, List<HistoryData> historyList) {
        this.mCtx = mCtx;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryData history = historyList.get(position);
        holder.textViewTime.setText(history.time);
        holder.textViewDate.setText(history.date);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTime, textViewDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTime = itemView.findViewById(R.id.post_time);
            textViewDate = itemView.findViewById(R.id.post_date);
        }
    }
}
