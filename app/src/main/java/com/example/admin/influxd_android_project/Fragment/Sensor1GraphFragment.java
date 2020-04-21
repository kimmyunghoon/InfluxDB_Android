package com.example.admin.influxd_android_project.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
                                setDateItem(dateStr[dataIndex-1]);
                                dataChart.clear();
                                dataChart.notifyDataSetChanged(); // 차트에 데이터가 바뀌었다고 notify
                                dataChart.invalidate(); // refresh
                                saveIndex[0] =dataIndex;
                            }
                            if(saveIndex[1]!=updataIndex) {
                                setUpDateItem(updateStr[updataIndex-1]);
                                setTime_period(updateTime[updataIndex-1]);
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
    private void chartUpdate(HashMap<Long,SensorDataBean> tmp){
        Set key = tmp.keySet();
        chartDataArrX.clear();
        chartDataArrY.clear();
        chartDataArrZ.clear();
        for (Object value : key) {
            Long keyName = (Long) value;
            SensorDataBean sdb  = tmp.get(keyName);
            double []data = sdb.getData();
            chartDataArrX.add(new Entry(index,(float)data[0]));
            chartDataArrY.add(new Entry(index,(float)data[1]));
            chartDataArrZ.add(new Entry(index,(float)data[2]));
            index++;
        }
        if(chartDataArrX.size()==0)
            return;
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

        dataSets[0] = new LineDataSet(chartDataArrX,"X"); // 데어트, 범례
        dataSets[0].setAxisDependency(YAxis.AxisDependency.LEFT);
        //List<Integer> colors = new ArrayList<>(); // 색상지정
        /* 칼라 데이터 로직 */
        // dataSet.setColors(colors);
        dataSets[0].setValueTextColor(Color.BLACK);
        dataSets[0].setColors(Color.RED);
        dataSets[1] = new LineDataSet(chartDataArrY,"Y"); // 데어트, 범례
        dataSets[1].setAxisDependency(YAxis.AxisDependency.LEFT);
        //List<Integer> colors = new ArrayList<>(); // 색상지정
        /* 칼라 데이터 로직 */
        // dataSet.setColors(colors);
        dataSets[1].setValueTextColor(Color.BLACK);
        dataSets[1].setColors(Color.BLUE);
        dataSets[2] = new LineDataSet(chartDataArrZ,"Z"); // 데어트, 범례
        dataSets[2].setAxisDependency(YAxis.AxisDependency.LEFT);
        //List<Integer> colors = new ArrayList<>(); // 색상지정
        /* 칼라 데이터 로직 */
        // dataSet.setColors(colors);
        dataSets[2].setValueTextColor(Color.BLACK);
        dataSets[2].setColors(Color.GRAY);
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(this.dataSets[0]);
        dataSets.add(this.dataSets[1]);
        dataSets.add(this.dataSets[2]);
        LineData lineData = new LineData(dataSets);
        dataChart.setData(lineData);
        this.dataSets[0].notifyDataSetChanged(); // 데이터가 바뀌었다고 notify
        this.dataSets[1].notifyDataSetChanged(); // 데이터가 바뀌었다고 notify
        this.dataSets[2].notifyDataSetChanged(); // 데이터가 바뀌었다고 notify

        dataChart.notifyDataSetChanged(); // 차트에 데이터가 바뀌었다고 notify
        dataChart.invalidate(); // refresh

    }
    SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm.ss");
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
                    x=""+(int)value;
                return x;
            }
        };
        XAxis xAxis = dataChart.getXAxis(); // x축 스타일링시작
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // x축 위치 지정
        xAxis.setTextSize(10f); // 크기 지정
        xAxis.setTextColor(Color.RED); // 색 지정
        xAxis.setDrawLabels(true); // 라벨(x축 좌표)를 그릴지 결정
        xAxis.setDrawAxisLine(false); // x축 라인을 그림 (라벨이 없을때 잘 됨)
        xAxis.setDrawGridLines(false); // 내부 선 그을지 결정
        xAxis.setLabelCount(3); // 라벨의 개수를 결정 => 나누어 떨어지는 개수로 지정
        xAxis.setGranularity(100f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        dataChart.setBackgroundColor(Color.WHITE); // 배경색 지정
        Description description = new Description();
        description.setText("");
        dataChart.setDescription(description);// 설명 정의(오른쪽 아래)
        dataChart.setBorderWidth(100);
//        lineChart.setMaxVisibleValueCount(8);

// touch
        dataChart.setTouchEnabled(false);
// drag
        dataChart.setDragEnabled(false);
// scale
        dataChart.setScaleEnabled(false);
        dataChart.setScaleXEnabled(false);
        dataChart.setScaleYEnabled(false);
// pinchZoom
        dataChart.setPinchZoom(false);
// double tap
        dataChart.setDoubleTapToZoomEnabled(false);
        dataChart.animateX(3000, Easing.EasingOption.Linear); // 속도, 애니메이션
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
