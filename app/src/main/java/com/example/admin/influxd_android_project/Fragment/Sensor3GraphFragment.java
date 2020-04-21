package com.example.admin.influxd_android_project.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.admin.influxd_android_project.R;

public class Sensor3GraphFragment extends Fragment {
    private Context context;
    private View rootView;
    public Sensor3GraphFragment(){
    }
    public Sensor3GraphFragment(Context context) {
        this.context = context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_sensor3, container, false);


        return rootView;
    }
}
