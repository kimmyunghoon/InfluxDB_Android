package com.example.admin.influxd_android_project.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.admin.influxd_android_project.Activity.MainActivity;
import com.example.admin.influxd_android_project.Adapter.RecycleViewDateAdapter;
import com.example.admin.influxd_android_project.Adapter.SpinnerCustomAdapter;
import com.example.admin.influxd_android_project.Data.Influx_Java;
import com.example.admin.influxd_android_project.R;
import com.example.admin.influxd_android_project.Thread.InfluxdbToThread;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private View rootView = null;
    private Context context;
    private MapView mapView = null;
    private GoogleMap mMap;
    private Timer mapStatusTimer;


    private boolean status_map = false;
    private boolean start_map = true;
    private  HashMap<Long,Marker> markersname ;
    private int time_period = 5000;
    private  InfluxdbToThread  influxdbThread;
    private Influx_Java influxJava;
    private RecycleViewDateAdapter rvDateAdapter;
    private LinearLayoutManager  layoutManager;
    private String TAG = "MapFragment";
    LinearLayout layout;
    AlertDialog.Builder alt_bld;
    private String[] dateStr={"12H","6H","3H","1H","30m","15m","5m"};
    private String[] updateStr={"5s","10s","30s","1m"};
    private int[] updateTime={5000,10000,30000,60000};

    AlertDialog dialog;
    public MapFragment(){
    }
    public MapFragment(Context context) {
        this.context =context;
        mapStatusTimer = new Timer();
        markersname = new HashMap<>();
        influxJava = new Influx_Java();
        //setTimer();
    }
    TextView date_text;
    TextView date_refresh_text;
    private int dataIndex = 0;
    private int updataIndex = 0;
    private int[] saveIndex = {0,0};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView==null)
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        date_refresh_text = rootView.findViewById(R.id.date_text_refresh);
        if(influxdbThread!=null)
            influxdbThread.setView(rootView,R.id.data_status_tx);
        mapView = (MapView) rootView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onStart();
        mapView.onResume();
        mapView.getMapAsync(this);

        Button time_set = (Button) rootView.findViewById(R.id.time_set_bt);
        time_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                date_text.setText(influxJava.dateToDate());
                dialog.show();


            }
        });
        settingUI();
        if(alt_bld==null) {
            alt_bld = new AlertDialog.Builder(context);
            alt_bld.setTitle("설정")
                    .setCancelable(false)
                    .setView(layout)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            if(saveIndex[0]!=dataIndex) {
                                setDateItem(dateStr[dataIndex]);
                                mMap.clear();
                                influxJava.setMapTime(0);
                                saveIndex[0] =dataIndex;
                            }
                            if(saveIndex[1]!=updataIndex) {
                                setUpDateItem(updateStr[updataIndex]);
                                setTime_period(updateTime[updataIndex]);
                                saveIndex[1] =updataIndex;
                            }

                            dialog.dismiss();

                        }

                    });
            dialog = alt_bld.create();
        }
        return rootView;
    }
    LatLng setLocation = new LatLng(36.35714,127.3388788);
    Bitmap smallMarker;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(this.getActivity());
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(setLocation, 16));

        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(android.R.drawable.star_on);
        Bitmap b=bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);

    }
    int count = 0;
    SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm.ss");
    public void addLocation(HashMap<Long, LatLng> locations){

        int c=0;
        int c1=0;
        if(mMap!=null) {

            Set key = locations.keySet();

            for (Object value : key) {

                Long keyName = (Long) value;
                //Log.d(TAG,locations.get(keyName).toString());
                MarkerOptions marker = new MarkerOptions().position(locations.get(keyName)).title(df.format(new Date(keyName))).icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                Marker markerT = mMap.addMarker(marker);
                markerT.setSnippet("위치");

                markersname.put(keyName, markerT);
                c++;
            }
            key = markersname.keySet();
            for (Object o : key) {
                Long keyName = (Long) o;

                if (keyName < influxJava.getMapTime()) {//찾고자 하는 범위 시간 : 작으면(오래됬으면)
                   // Log.d(TAG,"시간 테스트1 "+keyName+"/"+influxJava.getMapTime());
                    Objects.requireNonNull(markersname.get(keyName)).setVisible(false);
                    Objects.requireNonNull(markersname.get(keyName)).remove();
                    c1++;
                }
                else{
                 //   Log.d(TAG,"시간 테스트2 "+keyName+"/"+influxJava.getMapTime());
                }
             //   Log.d(TAG,df.format(new Date(keyName))+"/"+df.format(new Date(influxJava.getMapTime())));
            }
           // Log.d(TAG,"marker 갯수 "+c+"-"+c1);
            mapView.postDelayed(() -> {
                if (mMap != null) {
                    if(locations.size()>0) {
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(locations.get(locations.size() - 1)).
                                zoom(mMap.getCameraPosition().zoom).
                                build()));
                    }
                }
            }, 100);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        setTimer();
       if(influxdbThread!=null){
           influxJava.setMapTime(0);
           influxdbThread.setStatus_map_ui(true);
       }

    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG,"onStop");
        if(influxdbThread!=null){
            influxdbThread.setStatus_map_ui(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
        if(influxdbThread!=null){
            influxdbThread.setStatus_map_ui(false);
        }
    }

    public void setTimer(){

        mapStatusTimer.scheduleAtFixedRate(
                new TimerTask() {
                    Runnable updateStatusRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if(start_map&&status_map)
                            {
                                if(influxdbThread!=null&&influxdbThread.getLocations()!=null) {
                                    addLocation(influxdbThread.getLocations());
                                    influxdbThread.clearLocations();
                                    date_refresh_text.setText(influxJava.dateToDate());
                                }
                                else{

                                    date_refresh_text.setText(influxJava.dateToDate());

                                    Log.d(TAG,"실행 안됨");
                                }

                            }
                        }
                    };
                    @Override
                    public void run() {
                        Activity a = getActivity();
                        if (a == null) return;
                        a.runOnUiThread(updateStatusRunnable);
                    }
                }, 200, time_period);
    }
    public void StartDataThread(){
        if(influxdbThread==null) {
            influxdbThread = new InfluxdbToThread().setContext(context).setDataType("map").setInfluxApi(influxJava);
            influxdbThread.init();
        }
        if(rootView!=null)
            influxdbThread.setView(rootView,R.id.data_status_tx);
        influxdbThread.run();
        start_map = true;
    }
    public void EndDataThread(){
        start_map = false;
        influxdbThread.End();

    }



    public int getTime_period() {
        return time_period;
    }

    public void setTime_period(int time_period) {
        this.time_period = time_period;
    }

    public void settingUI(){
        //보류
//        RecyclerView recyclerViewDate = new RecyclerView(context);
//        recyclerViewDate.setHasFixedSize(true);
//        layoutManager = new LinearLayoutManager(context);
//        recyclerViewDate.setLayoutManager(layoutManager);
//        rvDateAdapter = new RecycleViewDateAdapter(dateStr,context);
//        recyclerViewDate.setAdapter(rvDateAdapter);
//

        Spinner dateSpinner = new Spinner(context);
        SpinnerCustomAdapter dateSpinnerAdapter = new SpinnerCustomAdapter(context,R.layout.date_spinner_layout, new ArrayList<>(Arrays.asList(dateStr)));
        dateSpinner.setAdapter(dateSpinnerAdapter);
        dateSpinner .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        Spinner updateSpinner = new Spinner(context);
        SpinnerCustomAdapter updateSpinnerSpinnerAdapter = new SpinnerCustomAdapter(context,R.layout.date_spinner_layout, new ArrayList<>(Arrays.asList(updateStr)));
        updateSpinner.setAdapter(updateSpinnerSpinnerAdapter);

        updateSpinner .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView et_text = new TextView(context);
        et_text.setGravity(Gravity.CENTER);
        date_text = new TextView(context);
        date_text.setGravity(Gravity.CENTER);
        TextView et1_text = new TextView(context);
        et1_text.setGravity(Gravity.CENTER);
        TextView et2_text = new TextView(context);
        et2_text.setGravity(Gravity.CENTER);
        Space tmp = new Space(context);
        tmp.setMinimumHeight(50);
        Space tmp1 = new Space(context);
        tmp1.setMinimumHeight(50);
        Space tmp2 = new Space(context);
        tmp2.setMinimumHeight(50);
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        et_text.setText("현재 적용되고 있는 날짜 범위");
        //date_text.setText(Influx_Java.dateToDate());
        layout.addView(et_text,0);
        layout.addView(date_text,1);
        layout.addView(tmp,2);
        et1_text.setText("갱신 범위 설정");
        LinearLayout  spinnerDatelayout = new LinearLayout(context);
        spinnerDatelayout.setOrientation(LinearLayout.VERTICAL);
        spinnerDatelayout.addView(et1_text,0);
        spinnerDatelayout.addView(dateSpinner,1);
        layout.addView(spinnerDatelayout,3);
        layout.addView(tmp1,4);
        et2_text.setText("갱신 주기 설정");
        LinearLayout  spinnerUpdatelayout = new LinearLayout(context);
        spinnerUpdatelayout.setOrientation(LinearLayout.VERTICAL);

        spinnerUpdatelayout.addView(et2_text,0);
        spinnerUpdatelayout.addView(updateSpinner,1);
        layout.addView(spinnerUpdatelayout,5);
        layout.addView(tmp2,6);

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"dateSpinner "+position);
                dataIndex = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        updateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"updateSpinner "+position);
                updataIndex = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public boolean isStatus_map() {
        return status_map;
    }

    public void setStatus_map(boolean status_map) {
        this.status_map = status_map;
    }
    void setDateItem(String type){
        if(influxJava!=null)
        influxJava.getDataPeriod(type);
    }
    void setUpDateItem(String type){
        if(influxJava!=null)
        influxJava.getUpDataPeriod(type);
        if(influxdbThread!=null)
        influxdbThread.getUpDataPeriod(type);
    }

}
