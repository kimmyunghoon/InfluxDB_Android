package com.example.admin.influxd_android_project.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import com.example.admin.influxd_android_project.Activity.MainActivity;
import com.example.admin.influxd_android_project.R;
import com.kyleduo.switchbutton.SwitchButton;

public class MainFragment extends Fragment {
    private View rootView = null;
    private Context context;
    private SwitchButton mapSw ;
    private SwitchButton sensor1Sw ;
    private SwitchButton sensor2Sw ;
    private SwitchButton sensor3Sw ;


    private boolean mapsw_isSelected=false;
    private MapFragment mapFragment;
    private Sensor1GraphFragment sensor1GraphFragment;
    private Sensor2GraphFragment sensor2GraphFragment;
    private Sensor3GraphFragment sensor3GraphFragment;
    public MainFragment(){
    }

    public MainFragment(Context context) {

    }
    public MainFragment setFragment(MapFragment fragment){
        mapFragment = fragment;
        return this;
    }
    public MainFragment setFragment(Sensor1GraphFragment fragment){
        sensor1GraphFragment = fragment;
        return this;
    }
    public MainFragment setFragment(Sensor2GraphFragment fragment){
        sensor2GraphFragment = fragment;
        return this;
    }
    public MainFragment setFragment(Sensor3GraphFragment fragment){
        sensor3GraphFragment = fragment;
        return this;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView==null)
            rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mapSw = rootView.findViewById(R.id.map_sw);
        mapSw = rootView.findViewById(R.id.map_sw);
        if (mapSw.isChecked()) {
            mapSw.setBackColorRes(R.color.sw_on_b);
            mapSw.setThumbColorRes(R.color.sw_on_t);
        } else {
            mapSw.setBackColorRes(R.color.sw_off_b);
            mapSw.setThumbColorRes(R.color.sw_off_t);
        }
        sensor1Sw = rootView.findViewById(R.id.sensor1_sw);
        if (sensor1Sw.isChecked()) {
            sensor1Sw.setBackColorRes(R.color.sw_on_b);
            sensor1Sw.setThumbColorRes(R.color.sw_on_t);
        } else {
            sensor1Sw.setBackColorRes(R.color.sw_off_b);
            sensor1Sw.setThumbColorRes(R.color.sw_off_t);
        }


        mapSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    mapFragment.setStatus_map(true);
                    mapFragment.StartDataThread();
                    mapSw.setBackColorRes(R.color.sw_on_b);
                    mapSw.setThumbColorRes(R.color.sw_on_t);
                } else {
                    mapFragment.setStatus_map(false);
                    mapFragment.EndDataThread();
                    mapSw.setBackColorRes(R.color.sw_off_b);
                    mapSw.setThumbColorRes(R.color.sw_off_t);
                }
            }
        });
        sensor1Sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sensor1GraphFragment.setStatus(true);
                    sensor1GraphFragment.StartDataThread();
                    sensor1Sw.setBackColorRes(R.color.sw_on_b);
                    sensor1Sw.setThumbColorRes(R.color.sw_on_t);
                } else {
                    sensor1GraphFragment.setStatus(false);
                    sensor1GraphFragment.EndDataThread();
                    sensor1Sw.setBackColorRes(R.color.sw_off_b);
                    sensor1Sw.setThumbColorRes(R.color.sw_off_t);
                }
            }
        });
        return rootView;
    }
}
