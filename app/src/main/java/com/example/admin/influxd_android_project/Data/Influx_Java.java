package com.example.admin.influxd_android_project.Data;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class Influx_Java {
     private InfluxDB influxDB = InfluxDBFactory.connect(InfluxConstants.serverURL);
     private String TAG = "Influx_Java";
     private long setMapTime = 0;
     private long setSensorTime1 = 0;
     private long setSensorTime2 = 0;
     private long setSensorTime3 = 0;
     private  int time_period = 5;
     private  int notDataCount = 1;
     private  int notSensor1DataCount = 1;
     SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm.ss");
     private boolean isNotData = false;
     private boolean isSetData = true;
//     static Influx_Java influx_Java;
//    static Influx_Java getInstanse(){
//        if(influx_Java==null)
//            influx_Java = new Influx_Java();
//        return influx_Java;
//    }

    public long getMapTime(){
        if(setMapTime==0)
            return (long)((Calendar.getInstance().getTime().getTime() * Math.pow(10, 6) - period)/Math.pow(10, 6));
            else
        return (long)((setMapTime - period)/Math.pow(10, 6));
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    SimpleDateFormat formatS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS.ssssss'Z'");
     public HashMap<Long,LatLng> getData(final String databaseName){
        if (isSetData) {
            isSetData=false;
        try {
            ArrayList<LatLng> tmp = new ArrayList<>();
            @SuppressLint("UseSparseArrays")
            HashMap<Long,LatLng> tmpHashmap = new HashMap<>();

            influxDB.setDatabase(databaseName);

                AsyncTask<Void, Void, List<QueryResult.Result>> InfluxTaask = new AsyncTask<Void, Void, List<QueryResult.Result>>() {
                    @Override
                    protected List<QueryResult.Result> doInBackground(Void... voids) {
                        QueryResult queryResult;
                        try {
                            if (setMapTime == 0) {
                                Log.d(TAG, "탐색 실행");
                                setMapTime = (long) (Calendar.getInstance().getTime().getTime() * Math.pow(10, 6));
                                Log.d(TAG, "SELECT * FROM geo where time>=" + (long) (setMapTime - period)+" / "+setMapTime+" / "+period);
                                queryResult = influxDB.query(new Query("SELECT * FROM geo WHERE time>=" + (setMapTime - period), databaseName, true));
                            } else {
                                Log.d(TAG, "탐색 중");
                                setMapTime = (long) (Calendar.getInstance().getTime().getTime() * Math.pow(10, 6));
                                Log.d(TAG, "SELECT * FROM geo where time>=" + (long) (setMapTime - (long) 5 * 1E9));
                                queryResult = influxDB.query(new Query("SELECT * FROM geo WHERE time>=" + (setMapTime - (long) (notDataCount * time_period * Math.pow(10, 9))), databaseName, true));
                            }
                            Log.d(TAG, df.format(new Date(Calendar.getInstance().getTime().getTime())) + " / " + notDataCount);

                            influxDB.close();
                        } catch (Exception e) {
                            isSetData=true;
                            return null;
                        }
                        isSetData=true;
                        return queryResult.getResults();
                    }
                };

                List<QueryResult.Result> tmpList = InfluxTaask.execute().get();

//            ArrayList<QueryResult> arrayList = new ArrayList<QueryResult>();
//            arrayList.addAll(tmpList);
                if (tmpList == null) {
                    isSetData=true;
                    return null;
                }
                if (tmpList.get(0).getSeries() == null) {
                    isNotData = false;
                    notDataCount++;
                    isSetData=true;
                    return null;
                } else {
                    notDataCount = 0;
                    isNotData = true;
                }
              //  Log.d(TAG,tmpList.get(0).getSeries().get(0).getValues().toString());

            format.setTimeZone(TimeZone.getTimeZone("UTC"));
                for (int i = 0; i < tmpList.get(0).getSeries().get(0).getValues().size(); i++) {
                    tmp.add(new LatLng(Double.valueOf(tmpList.get(0).getSeries().get(0).getValues().get(i).get(2).toString()),
                            Double.valueOf(tmpList.get(0).getSeries().get(0).getValues().get(i).get(3).toString())));
                    Date   date       = format.parse ( tmpList.get(0).getSeries().get(0).getValues().get(i).get(0).toString() );

                    tmpHashmap.put(date.getTime(),
                            new LatLng(Double.valueOf(tmpList.get(0).getSeries().get(0).getValues().get(i).get(2).toString()),
                            Double.valueOf(tmpList.get(0).getSeries().get(0).getValues().get(i).get(3).toString())));
                }

            isSetData=true;
                return tmpHashmap;
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        isSetData=true;
        return null;
    }

     public void setMapTime(long setMapTime2){
        setMapTime=setMapTime2;
    }

     public String dateToDate(){
        if(setMapTime==0){
            return df.format(new Date((long)((Calendar.getInstance().getTime().getTime() * Math.pow(10, 6) - period)/Math.pow(10, 6))))+
                    " ~ "+df.format(new Date((long)(Calendar.getInstance().getTime().getTime() * Math.pow(10, 6)/Math.pow(10, 6))));
        }
        return df.format(new Date((long)((setMapTime - period)/Math.pow(10, 6))))+" ~ "+df.format(new Date((long)(setMapTime/Math.pow(10, 6))));
    }
     private long period = (long)(3600*12*1E9);

     public void getDataPeriod(String type){
        switch (type){
            case "12H":
                period = (long) (3600*12*1E9);
                break;
            case "6H":
                period = (long) (3600*1E9*6);
                break;
            case "3H":
                period = (long) (3600*3*1E9);
                break;
            case "1H":
                period = (long) (3600*1E9);
                break;
            case "30m":
                period = (long) (1800*1E9);
                break;
            case "15m":
                period = (long) (900*1E9);
                break;
            case "5m":
                period = (long) (300*1E9);
                break;

        }
    }
     public void getUpDataPeriod(String type){
        switch (type){
            case "5s":
                time_period=5;
                break;
            case "10s":
                time_period=10;
                break;
            case "30s":
                time_period=30;
                break;
            case "1m":
                time_period=60;
                break;
            default:
                time_period=5;
                break;

        }
    }
     private long getDataPeriod(){
        if(period==0)
        return (long) (3600*1E9*12);
        else
            return period;
    }

    public  HashMap<Long,SensorDataBean> getSensorData(String databasename, String sensor_table,int index) {

        if (isSetData) {
            isSetData=false;
            try {
                ArrayList<SensorDataBean> tmp = new ArrayList<>();
                HashMap<Long,SensorDataBean> tmpHash = new HashMap<>();
                influxDB.setDatabase(databasename);


                Date tmpDate =new Date();
                long test = Long.valueOf("1585708794531667000");

                for(int indexS=0;indexS<3;indexS++) {//
                    InfluxTaask influxTaask = new InfluxTaask(databasename,sensor_table,index);
                    List<QueryResult.Result> tmpList = influxTaask.execute((long)indexS + 1,test).get();//(long)(Calendar.getInstance().getTime().getTime() * Math.pow(10, 6))

                    setSensorTime1=0;
//            ArrayList<QueryResult> arrayList = new ArrayList<QueryResult>();
//            arrayList.addAll(tmpList);
                    if (tmpList == null) {
                        isSetData = true;
                        Log.d(TAG,"test1"+indexS);
                        return null;
                    }
                    /*
                    * 센서 데이터 테스트 부분 문제 발생. 이부분에서 넘어가지 않고있음.
                    * */
                    if (tmpList.get(0).getSeries() == null) {
                        isNotData = false;
                        notDataCount++;
                        isSetData = true;
                        Log.d(TAG,"test2"+indexS);
                        return null;
                    } else {
                        notDataCount = 0;
                        isNotData = true;
                    }
                    //Math.pow(10, 6)
                    formatS.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Log.d(TAG,tmpList.get(0).getSeries().get(0).getValues().toString()+"");
                    for (int i = 0; i < tmpList.get(0).getSeries().get(0).getValues().size(); i++) {

                        Date   date       = formatS.parse ( tmpList.get(0).getSeries().get(0).getValues().get(i).get(0).toString() );

                        if(tmpHash.containsKey(date.getTime())){
                            SensorDataBean sdb = tmpHash.get(date.getTime());
                            sdb.setDate(date);
                            sdb.setData( Double.valueOf(tmpList.get(0).getSeries().get(0).getValues().get(i).get(1).toString()),indexS);
                            tmpHash.put(date.getTime(),sdb);
                        }
                        else{
                            SensorDataBean sdb = new SensorDataBean();
                            sdb.setDate(date);
                            sdb.setData( Double.valueOf(tmpList.get(0).getSeries().get(0).getValues().get(i).get(1).toString()),indexS);
                            tmpHash.put(date.getTime(),sdb);
                        }


                    }
                    isSetData = true;
                }

                return tmpHash;
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        isSetData=true;
        return null;
    }
    class InfluxTaask extends AsyncTask<Long, Void, List<QueryResult.Result>> {
         String sensor_table;
         int index;
        String databasename;
        InfluxTaask(){

        }
        InfluxTaask(   String databasename,String sensor_table,int index){
            this.databasename=databasename;
            this.sensor_table = sensor_table;
            this.index =index;
        }

        @Override
        protected List<QueryResult.Result> doInBackground(Long... voids) {
            QueryResult queryResult =null;
            try {

                if(voids[0]==1) {//x
                    if (setSensorTime1 == 0) {
                        Log.d(TAG, "탐색 실행");
                        setSensorTime1 = (long) (voids[1]);
                        Log.d(TAG, "SELECT time, value FROM device_frmpayload_data_" + sensor_table + "_" + index + "_x where time>=" + (long) (setSensorTime1 - period) + " / " + setSensorTime1 + " / " + period);
                        queryResult = influxDB.query(new Query("SELECT time, value FROM device_frmpayload_data_" + sensor_table + "_" + index + "_x  where time>=" + (setSensorTime1 - period), databasename, true));
                    } else {
                        Log.d(TAG, "탐색 중");
                        setSensorTime1 = (long) (voids[1]);
                        Log.d(TAG, "SELECT time, value FROM geo where time>=" + (long) (setSensorTime1 - (long) 5 * 1E9));
                        queryResult = influxDB.query(new Query("SELECT time, value FROM device_frmpayload_data_" + sensor_table + "_" + index + "_x  WHERE time>=" + (setSensorTime1 - (long) (notSensor1DataCount * time_period * Math.pow(10, 9))), databasename, true));
                    }
                    Log.d(TAG, df.format(new Date(Calendar.getInstance().getTime().getTime())) + " / " + notSensor1DataCount);

                }
                else
                if(voids[0]==2) {//y
                    if (setSensorTime2 == 0) {
                        Log.d(TAG, "탐색 실행");
                        setSensorTime2 = (long) (voids[1]);
                        Log.d(TAG, "SELECT time, value FROM device_frmpayload_data_" + sensor_table + "_" + index + "_y where time>=" + (long) (setSensorTime2 - period) + " / " + setSensorTime2 + " / " + period);
                        queryResult = influxDB.query(new Query("SELECT time, value FROM device_frmpayload_data_" + sensor_table + "_" + index + "_y  where time>=" + (setSensorTime2 - period), databasename, true));
                    } else {
                        Log.d(TAG, "탐색 중");
                        setSensorTime2 = (long) (voids[1]);
                        queryResult = influxDB.query(new Query("SELECT time, value FROM device_frmpayload_data_" + sensor_table + "_" + index + "_y  WHERE time>=" + (setSensorTime2 - (long) (notSensor1DataCount * time_period * Math.pow(10, 9))), databasename, true));
                    }
                    Log.d(TAG, df.format(new Date(Calendar.getInstance().getTime().getTime())) + " / " + notSensor1DataCount);

                }
                else
                if(voids[0]==3) {//y
                    if (setSensorTime3 == 0) {
                        Log.d(TAG, "탐색 실행");
                        setSensorTime3 = (long) (voids[1]);
                        Log.d(TAG, "SELECT time, value FROM device_frmpayload_data_" + sensor_table + "_" + index + "_y where time>=" + (long) (setSensorTime3 - period) + " / " + setSensorTime3 + " / " + period);
                        queryResult = influxDB.query(new Query("SELECT time, value FROM device_frmpayload_data_" + sensor_table + "_" + index + "_z  where time>=" + (setSensorTime3 - period), databasename, true));
                    } else {
                        Log.d(TAG, "탐색 중");
                        setSensorTime3 = (long) (voids[1]);
                        queryResult = influxDB.query(new Query("SELECT time, value FROM device_frmpayload_data_" + sensor_table + "_" + index + "_z  WHERE time>=" + (setSensorTime3 - (long) (notSensor1DataCount * time_period * Math.pow(10, 9))), databasename, true));
                    }
                    Log.d(TAG, df.format(new Date(Calendar.getInstance().getTime().getTime())) + " / " + notSensor1DataCount);

                }
                influxDB.close();
                isSetData=true;
                if(queryResult==null)
                    return null;
                else
                    return queryResult.getResults();
            } catch (Exception e) {
                e.printStackTrace();
                isSetData=true;
                return null;
            }


        }

    }

    public long geSensorTime(){
        if(setSensorTime1==0)
            return (long)((Calendar.getInstance().getTime().getTime() * Math.pow(10, 6) - period)/Math.pow(10, 6));
        else
            return (long)((setSensorTime1 - period)/Math.pow(10, 6));
    }
    public void setSensorTime(long sensorTime){
        this.setSensorTime1=this.setSensorTime2= this.setSensorTime3=sensorTime;

    }
}
