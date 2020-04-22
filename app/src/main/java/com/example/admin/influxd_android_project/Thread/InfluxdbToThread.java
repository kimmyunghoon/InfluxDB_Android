package com.example.admin.influxd_android_project.Thread;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.influxd_android_project.Data.Influx_Java;
import com.example.admin.influxd_android_project.Data.SensorDataBean;
import com.example.admin.influxd_android_project.R;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.maps.model.LatLng;

import org.influxdb.dto.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InfluxdbToThread extends Thread{
    String TAG = "InfluxdbToThread";
    private long startDate = 0;
    private long currentDate = 0;
    private long time_period = 5000;
    private String database ="test";
    private  Looper looper;
    private Context context = null;
    private String type ;
    private Influx_Java influxJava;
    HashMap<Long,LatLng>  lnfluxLocations ;
    HashMap<Long,LatLng> lnfluxLocationsTmp;
    HashMap<Long,SensorDataBean> lnfluxSDBTmp;
    HashMap<Long,SensorDataBean> lnfluxSDBs;
    boolean breakStaus = false;
    View uiView;
    TextView progressText;
    private String sensorDB_ = "";
    private  String sensorTable_ = "";
    private  int sensorIndex_=0;
    public InfluxdbToThread setContext(Context context){
      this.context = context;
        return this;
    }
    public InfluxdbToThread setView(View uiView,int id){
        this.uiView = uiView;
        progressText=(TextView)uiView.findViewById(id);
        return this;
    }
    public InfluxdbToThread setSensorDB_Info(String sensorDB_,String sensorTable_,int index){
        this.sensorDB_ = sensorDB_;
        this.sensorTable_=sensorTable_;
        this.sensorIndex_=index;
        return this;
    }
    public InfluxdbToThread setDate(long startDate,long currentDate){
        this.startDate = startDate;
        this.currentDate=currentDate;
        return this;
    }
    public InfluxdbToThread setInfluxApi(Influx_Java influxJava){
        this.influxJava = influxJava;

        return this;
    }
    public InfluxdbToThread setDataType(String type){
        this.type = type;
        return this;
    }
    public InfluxdbToThread setPeriod(long time_period){
        this.time_period = time_period;
        return this;
    }
    public InfluxdbToThread setDatabase(String database){
        this.database = database;
        return this;
    }
    boolean time_delay_status = true;

    public boolean isStatus_map_ui() {
        return status_map_ui;
    }

    public void setStatus_map_ui(boolean status_map_ui) {
        this.status_map_ui = status_map_ui;
    }

    private boolean status_map_ui = true;

    public boolean isStatus_graph_ui() {
        return status_graph_ui;
    }

    public void setStatus_graph_ui(boolean status_graph_ui) {
        this.status_graph_ui = status_graph_ui;
    }

    private boolean status_graph_ui = true;
    public Handler timeHandeler() {
        final Handler handler = new Handler(looper) {

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                while(true){
                    try {
                        if(status_map_ui||status_graph_ui) {
                            if (time_delay_status) {

                                type_settiong(type);
                                time_delay_status = false;
                            }

                            try {
                               // Log.d(TAG,"time_period :"+time_period);
                                Thread.sleep(time_period);
                                time_delay_status=true;
                            } catch (InterruptedException e) {

                                e.printStackTrace();

                            }
                        }
                        if(!breakStaus)
                            break;

                    } catch (Exception e) {

                      //  e.printStackTrace();
                    }
                }


            }

        };
        return handler;
    }

    private void type_settiong(String type) {
        switch (type){
            case "map":
                if((lnfluxLocationsTmp = influxJava.getData("test"))==null){

                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 내용
                            if(progressText!=null)
                            progressText.setText("데이터 갱신중...");
                            //Toast.makeText(context,"데이터 갱신중...",Toast.LENGTH_SHORT).show();


                        }

                    }, time_period);
                }
                else {
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 내용
                            if(progressText!=null)
                            progressText.setText("데이터 연결 확인");
                            //Toast.makeText(context,"데이터 갱신중...",Toast.LENGTH_SHORT).show();

                        }

                    }, time_period);
                    lnfluxLocations.putAll(lnfluxLocationsTmp);
                }
                break;

            case "sensor1":
                try {
                    lnfluxSDBTmp = influxJava.getSensorData(sensorDB_, sensorTable_, sensorIndex_);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                if(lnfluxSDBTmp ==null){
                    Log.d(TAG,"handleMessage2");
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 내용
                            if(progressText!=null)
                            progressText.setText("데이터 갱신중...");
                            //Toast.makeText(context,"데이터 갱신중...",Toast.LENGTH_SHORT).show();

                        }

                    }, time_period);
                }
                else {
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 내용
                            if(progressText!=null)
                            progressText.setText("데이터 연결 확인");
                            //Toast.makeText(context,"데이터 갱신중...",Toast.LENGTH_SHORT).show();

                        }

                    }, time_period);
                    lnfluxSDBs.putAll(lnfluxSDBTmp);
                }
                break;

            case "sensor2":

                break;
            case "sensor3":

                break;

        }
    }

    HandlerThread handlerThread;
    public void init(){

    }
    @Override
    public void run() {
        super.run();

        breakStaus=true;
        handlerThread = new HandlerThread("ht");
        handlerThread.start();
        looper = handlerThread.getLooper();
        timeHandeler().sendEmptyMessage(0);
        if(type.equals("map")) {
            lnfluxLocations= new HashMap<>();
            lnfluxLocationsTmp= new HashMap<>();
        }
        if(type.equals("sensor1")) {
            lnfluxSDBs = new HashMap<>();
            lnfluxSDBTmp= new HashMap<>();

        }
    }
    public void End(){
        timeHandeler().removeMessages(0);

        handlerThread.quitSafely();
        breakStaus=false;
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 내용

                progressText.setText("연결 안됨");
                //Toast.makeText(context,"데이터 갱신중...",Toast.LENGTH_SHORT).show();


            }

        }, time_period);
    }
    public void getUpDataPeriod(String type){
        switch (type){
            case "5s":
                time_period=5000;
                break;
            case "10s":
                time_period=10000;
                break;
            case "30s":
                time_period=30000;
                break;
            case "1m":
                time_period=60000;
                break;
            default:
                time_period=5000;
             break;

        }
    }
    public HashMap<Long,LatLng> getLocations() {

        return lnfluxLocations;
    }
    public HashMap<Long,SensorDataBean> getSensorDatas() {

        return lnfluxSDBs;
    }
    public void clearSensorDatas() {
        lnfluxSDBs.clear();
    }
    public void clearLocations() {
        lnfluxLocations.clear();
    }


}
