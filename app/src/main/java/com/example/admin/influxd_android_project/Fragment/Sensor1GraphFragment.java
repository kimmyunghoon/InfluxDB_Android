package com.example.admin.influxd_android_project.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.admin.influxd_android_project.Adapter.SpinnerCustomAdapter;
import com.example.admin.influxd_android_project.Data.Influx_Java;
import com.example.admin.influxd_android_project.Data.SensorDataBean;
import com.example.admin.influxd_android_project.R;
import com.example.admin.influxd_android_project.Thread.InfluxdbToThread;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Sensor1GraphFragment extends Fragment {
    private Context context;
    private View rootView;
    private Timer graphStatusTimer;
    private boolean start_graph=false;
    private  InfluxdbToThread influxdbThread;
    private Influx_Java influxJava;
    private String TAG = "Sensor1GraphFragment";
    private long time_period = 5000;
    private LineChart dataChart;
    private ArrayList<SensorDataBean> sdbs;

    TextView date_refresh_text;

    AlertDialog.Builder alt_bld;
    AlertDialog dialog;
    LinearLayout layout;
    TextView date_text;
    private String[] dateStr={"12H","6H","3H","1H","30m","15m","5m"};
    private String[] updateStr={"5s","10s","30s","1m"};
    private int[] updateTime={5000,10000,30000,60000};
    private int dataIndex = 0;
    private int updataIndex = 0;
    private int[] saveIndex = {0,0};
    private String sensorDB_ = "";
    private  String sensorTable_ = "";
    private  int sensorIndex_=0;
    public Sensor1GraphFragment(){
    }
    public Sensor1GraphFragment(Context context) {
        this.context = context;
        graphStatusTimer = new Timer();
        chartDataArr= new ArrayList<>();
        if(chartDataArrX==null)
        chartDataArrX= new ArrayList<>();
        if(chartDataArrY==null)
        chartDataArrY= new ArrayList<>();
        if(chartDataArrZ==null)
        chartDataArrZ= new ArrayList<>();
        sdbs = new ArrayList<>();
         influxJava = new Influx_Java();
        setTimer();
    }
    public Sensor1GraphFragment setSensorDB_Info(String sensorDB_,String sensorTable_,int index){
        this.sensorDB_ = sensorDB_;
        this.sensorTable_=sensorTable_;
        this.sensorIndex_=index;
        return this;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (influxdbThread!=null) {
            influxdbThread.setStatus_graph_ui(true);
        }
        start_graph=true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (influxdbThread!=null) {
            influxdbThread.setStatus_graph_ui(false);
        }
        start_graph=false;
    }

    @Override
    public void onStop() {
        super.onStop();
        start_graph=false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView==null)
         rootView = inflater.inflate(R.layout.fragment_sensor1, container, false);

        dataChart  = rootView.findViewById(R.id.sensor_chart1);
        date_refresh_text = rootView.findViewById(R.id.date_text_refresh);
        if(influxdbThread!=null)
        influxdbThread.setView(rootView,R.id.data_status_tx);
        chartSetting();
        settingUI();
        Button time_set = (Button) rootView.findViewById(R.id.time_set_bt);
        time_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date_text.setText(influxJava.dateToDate());


                dialog.show();


            }
        });

        if(alt_bld==null) {
            alt_bld = new AlertDialog.Builder(context);
            alt_bld.setTitle("설정")
                    .setCancelable(false)
                    .setView(layout)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            if(saveIndex[0]!=dataIndex) {
                                setDateItem(dateStr[dataIndex]);
                                dataChart.clear();
                                dataChart.notifyDataSetChanged(); // 차트에 데이터가 바뀌었다고 notify
                                dataChart.invalidate(); // refresh
                                saveIndex[0] =dataIndex;
                            }
                            if(saveIndex[1]!=updataIndex) {
                                setUpDateItem(updateStr[updataIndex]);
                                Log.d(TAG,updateTime[updataIndex]+"");
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

    public void setTimer(){

        graphStatusTimer.scheduleAtFixedRate(
                new TimerTask() {
                    Runnable updateStatusRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if(start_graph&&status)
                            {

                                if(influxdbThread!=null&&influxdbThread.getSensorDatas()!=null) {
                                    chartUpdate(influxdbThread.getSensorDatas());
                                    influxdbThread.clearSensorDatas();
                                    date_refresh_text.setText(influxJava.dateToDate());
                                  //  Log.d(TAG,"time_period : "+time_period);
                                }
                                else{
                                    date_refresh_text.setText(influxJava.dateToDate());
                                    Log.d(TAG,"실행 안됨");
                                }
                               // chartUpdateTest();
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
        if(influxdbThread==null){
        influxdbThread  = new InfluxdbToThread()
                .setContext(context)
                .setSensorDB_Info(sensorDB_, sensorTable_, sensorIndex_)
                .setDataType("sensor1")
                .setInfluxApi(influxJava);
        influxdbThread.init();
        }
        if(rootView!=null)
            influxdbThread.setView(rootView,R.id.data_status_tx);
        influxdbThread.run();
        start_graph = true;
    }
    int index = 1;
    private List<Entry> chartDataArr ;
    private List<Entry> chartDataArrX ;
    private List<Entry> chartDataArrY ;
    private List<Entry> chartDataArrZ ;
    LineDataSet dataSet;
    LineDataSet[] dataSets =new LineDataSet[3];

    private void chartUpdateTest(){
        chartDataArr.add(new Entry(index++,index*2));
        if(chartDataArr.size()>10)
            chartDataArr.remove(0);
        dataSet = new LineDataSet(chartDataArr,"Sensor 1"); // 데어트, 범례
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        //List<Integer> colors = new ArrayList<>(); // 색상지정
        /* 칼라 데이터 로직 */
        // dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLUE);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        dataChart.setData(lineData);
        dataSet.notifyDataSetChanged(); // 데이터가 바뀌었다고 notify
        dataChart.notifyDataSetChanged(); // 차트에 데이터가 바뀌었다고 notify
        dataChart.invalidate(); // refresh

    }

    int zoom_index = 100;
    private void chartUpdate(HashMap<Long,SensorDataBean> tmp){
        Set key = tmp.keySet();

//        chartDataArrX.clear();
//        chartDataArrY.clear();
//        chartDataArrZ.clear();
        ArrayList<Long> tmpKeys = new ArrayList<>();
        for (Object value : key) {
            Long keyName = (Long) value;
            tmpKeys.add(keyName);
        }
        //long [] tmpKeyss = new long[tmpKeys.size()];
        Long[] tmpKeyss = tmpKeys.toArray(new Long[tmpKeys.size()]);
        Arrays.sort(tmpKeyss);

        for (Object value : tmpKeyss) {
            Long keyName = (Long) value;
            SensorDataBean sdb  = tmp.get(keyName);
            //Log.d(TAG,keyName+"");
            double []data = sdb.getData();
            chartDataArrX.add(new Entry(index,(float)data[0]));
            chartDataArrY.add(new Entry(index,(float)data[1]));
            chartDataArrZ.add(new Entry(index,(float)data[2]));
            index++;
        }
//        while(chartDataArrX.size()>50)
//            chartDataArrX.remove(0);
//        while(chartDataArrY.size()>50)
//            chartDataArrY.remove(0);
//        while(chartDataArrZ.size()>50)
//            chartDataArrZ.remove(0);
       /*
       * 날짜에 다른 데이터 제거하는 부분 추가.
       *
       * */
//        for(int cdIndex=0;cdIndex<chartDataArrX.size();cdIndex++) {
//            if (chartDataArrX.get(cdIndex).getX() < influxJava.geSensorTime()) {
//                chartDataArrX.remove(cdIndex);
//            }
//            if (chartDataArrY.get(cdIndex).getX() < influxJava.geSensorTime()) {
//                chartDataArrY.remove(cdIndex);
//            }
//            if (chartDataArrZ.get(cdIndex).getX() < influxJava.geSensorTime()) {
//                chartDataArrZ.remove(cdIndex);
//            }
//        }
      //  if(dataSets[0]==null)
         dataSets[0] = new LineDataSet(chartDataArrX,"X"); // 데어트, 범례
//        else{
//            for(int i=0;i<chartDataArrX.size();i++)
//            dataSets[0].addEntry(chartDataArrX.get(i));
//        }
        dataSets[0].setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSets[0].setDrawCircles(false);
        //List<Integer> colors = new ArrayList<>(); // 색상지정
        /* 칼라 데이터 로직 */
        // dataSet.setColors(colors);
        dataSets[0].setValueTextColor(Color.BLACK);
        dataSets[0].setColors(Color.RED);
        dataSets[1] = new LineDataSet(chartDataArrY,"Y"); // 데어트, 범례

//        if(dataSets[1]==null)
//            dataSets[1] = new LineDataSet(chartDataArrY,"Y"); // 데어트, 범례
//        else{
//            for(int i=0;i<chartDataArrY.size();i++)
//                dataSets[1].addEntry(chartDataArrY.get(i));
//        }
        dataSets[1].setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSets[1].setDrawCircles(false);
        //List<Integer> colors = new ArrayList<>(); // 색상지정
        /* 칼라 데이터 로직 */
        // dataSet.setColors(colors);
        dataSets[1].setValueTextColor(Color.BLACK);
        dataSets[1].setColors(Color.BLUE);
        dataSets[2] = new LineDataSet(chartDataArrZ,"Z"); // 데어트, 범례

//        if(dataSets[2]==null)
//            dataSets[2] = new LineDataSet(chartDataArrZ,"Y"); // 데어트, 범례
//        else{
//            for(int i=0;i<chartDataArrZ.size();i++)
//                dataSets[2].addEntry(chartDataArrZ.get(i));
//        }
        dataSets[2].setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSets[2].setDrawCircles(false);
        //List<Integer> colors = new ArrayList<>(); // 색상지정
        /* 칼라 데이터 로직 */
        // dataSet.setColors(colors);
        dataSets[2].setValueTextColor(Color.BLACK);
        dataSets[2].setColors(Color.GRAY);

        //if(dataChart.getLineData()==null) {
            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(this.dataSets[0]);
            dataSets.add(this.dataSets[1]);
            dataSets.add(this.dataSets[2]);
            LineData lineData = new LineData(dataSets);

            dataChart.setData(lineData);
       // }


        this.dataSets[0].notifyDataSetChanged(); // 데이터가 바뀌었다고 notify
        this.dataSets[1].notifyDataSetChanged(); // 데이터가 바뀌었다고 notify
        this.dataSets[2].notifyDataSetChanged(); // 데이터가 바뀌었다고 notify
        dataChart.setVisibleXRangeMaximum(zoom_index); // allow 20 values to be displayed at once on the x-axis, not more

        dataChart.moveViewToX(dataChart.getData().getXMax());
       // dataChart.enableScroll();
       // dataChart.setDragDecelerationEnabled(true);
        dataChart.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        dataChart.setScrollBarSize(100);
//        dataChart.animateX(3000, Easing.EasingOption.Linear);
//        dataChart.getAnimation().
        dataChart.setHorizontalScrollBarEnabled(true);
       dataChart.notifyDataSetChanged(); // 차트에 데이터가 바뀌었다고 notify
        dataChart.invalidate(); // refresh

    }
    SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd");
    private void chartSetting(){
//        for(int i=1 ; i<11 ; i++){
//            if(i%2==0) chartDataArr.add(new Entry(i, i * 4));
//            else chartDataArr.add(new Entry(i, i));
//        }
//        dataSet = new LineDataSet(chartDataArr,"Sensor 1"); // 데어트, 범례
//        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
//        //List<Integer> colors = new ArrayList<>(); // 색상지정
//        /* 칼라 데이터 로직 */
//       // dataSet.setColors(colors);
//        dataSet.setValueTextColor(Color.BLUE);
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            // x축에 있는 value들을 가져오는 콜백함수
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String x;
                    if(value>100000)
                x = df.format(new Date((long)value));
                else
                    x="Q"+(int)value;
                return x;
            }
        };
        XAxis xAxis = dataChart.getXAxis(); // x축 스타일링시작
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // x축 위치 지정
        xAxis.setTextSize(10f); // 크기 지정
        xAxis.setTextColor(Color.RED); // 색 지정

        xAxis.setDrawLabels(true); // 라벨(x축 좌표)를 그릴지 결정
        xAxis.setDrawAxisLine(true); // x축 라인을 그림 (라벨이 없을때 잘 됨)
        xAxis.setDrawGridLines(false); // 내부 선 그을지 결정
        xAxis.setLabelCount(10); // 라벨의 개수를 결정 => 나누어 떨어지는 개수로 지정
        //xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        dataChart.setBackgroundColor(Color.WHITE); // 배경색 지정

        Description description = new Description();
        description.setText("");
        dataChart.setDescription(description);// 설명 정의(오른쪽 아래)
        dataChart.setBorderWidth(100);
        dataChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {
               // Log.d(TAG,"onChartGestureStart");
            }

            @Override
            public void onChartGestureEnd(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {
              //  Log.d(TAG,"onChartGestureEnd");
            }

            @Override
            public void onChartLongPressed(MotionEvent motionEvent) {
                //Log.d(TAG,"onChartLongPressed");
            }

            @Override
            public void onChartDoubleTapped(MotionEvent motionEvent) {
                Log.d(TAG,"onChartDoubleTapped");
//                zoom_index = zoom_index/2;
//                if(zoom_index<10)
//                    zoom_index=10;
//                dataChart.setVisibleXRangeMaximum(zoom_index);
//                dataChart.invalidate();
            }

            @Override
            public void onChartSingleTapped(MotionEvent motionEvent) {
               Log.d(TAG,"onChartSingleTapped");
//                zoom_index = zoom_index*2;
//                if(zoom_index>80)
//                    zoom_index=80;
//                dataChart.setVisibleXRangeMaximum(zoom_index);
//                dataChart.invalidate();
            }

            @Override
            public void onChartFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
              //  Log.d(TAG,"onChartFling");
            }

            @Override
            public void onChartScale(MotionEvent motionEvent, float v, float v1) {
               // Log.d(TAG,"onChartScale");
            }

            @Override
            public void onChartTranslate(MotionEvent motionEvent, float v, float v1) {

            }
        });
//        lineChart.setMaxVisibleValueCount(8);

// touch
        dataChart.setTouchEnabled(true);
// drag
        dataChart.setDragEnabled(true);
// scale
        dataChart.setScaleEnabled(true);
        dataChart.setScaleXEnabled(true);
        dataChart.setScaleYEnabled(false);
// pinchZoom
        dataChart.setPinchZoom(false);
     //   dataChart.getViewPortHandler().setMaximumScaleX(2f);
// double tap
        dataChart.setDoubleTapToZoomEnabled(true);

//        List<ILineDataSet> dataSets = new ArrayList<>();
//        dataSets.add(dataSet);
//
//        LineData lineData = new LineData(dataSets);
//        dataChart.setData(lineData);
        dataChart.invalidate(); // refresh


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
    public void setTime_period(int time_period) {
        this.time_period = time_period;
    }
    boolean status =false;
    public void setStatus(boolean status) {
        this.status = status;
    }
    public void EndDataThread(){
        status = false;
        if(dataChart!=null) {
            dataChart.clear();
            dataChart.notifyDataSetChanged(); // 차트에 데이터가 바뀌었다고 notify
            dataChart.invalidate(); // refresh
        }
        influxdbThread.End();
    }
}
