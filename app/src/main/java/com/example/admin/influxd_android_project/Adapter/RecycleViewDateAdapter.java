package com.example.admin.influxd_android_project.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.admin.influxd_android_project.R;

public class RecycleViewDateAdapter extends RecyclerView.Adapter<RecycleViewDateAdapter.MyViewHolder> {
    private String[] mDataset;
    private Context context;
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public MyViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }
    @NonNull
    @Override
    public RecycleViewDateAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        TextView v = new TextView(context);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    public RecycleViewDateAdapter(String[] myDataset, Context context) {
        this.context=context;
        mDataset = myDataset;
    }
    @Override
    public void onBindViewHolder(@NonNull RecycleViewDateAdapter.MyViewHolder holder, int position) {
        holder.textView.setText(mDataset[position]);

    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
