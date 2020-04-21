package com.example.admin.influxd_android_project.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.admin.influxd_android_project.R;

public class Sensor2GraphFragment extends Fragment {
    private Context context;
    private View rootView;
    public Sensor2GraphFragment(){
    }
    public Sensor2GraphFragment(Context context) {
        this.context = context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_sensor2, container, false);


        return rootView;
    }
}
