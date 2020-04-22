package com.example.admin.influxd_android_project.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.admin.influxd_android_project.Adapter.ContentsPagerAdapter;
import com.example.admin.influxd_android_project.Adapter.CustomViewPager;
import com.example.admin.influxd_android_project.Data.Influx_Java;
import com.example.admin.influxd_android_project.R;
import com.google.android.material.tabs.TabLayout;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    private ViewPager mViewPager;
    private ContentsPagerAdapter mContentsPagerAdapter;
    private  int tabNum;
    CustomViewPager cvp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //etRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        //mViewPager = (ViewPager) findViewById(R.id.pager_content);

        tabNum=0;
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout) ;
        tabLayout.addTab(tabLayout.newTab().setCustomView(createTabView("Setting"))) ;
        tabLayout.addTab(tabLayout.newTab().setCustomView(createTabView("Map"))) ;
        tabLayout.addTab(tabLayout.newTab().setCustomView(createTabView("Sensor1"))) ;
        tabLayout.addTab(tabLayout.newTab().setCustomView(createTabView("Sensor2"))) ;
        tabLayout.addTab(tabLayout.newTab().setCustomView(createTabView("Sensor3"))) ;

        mContentsPagerAdapter = new ContentsPagerAdapter(
                getSupportFragmentManager(), tabLayout.getTabCount(),this);

//        mViewPager.setAdapter(mContentsPagerAdapter);
//        mViewPager.addOnPageChangeListener(
//                new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        cvp  =  findViewById(R.id.pager_content);

        cvp.setAdapter(mContentsPagerAdapter);
        cvp.setPagingEnabled(false);
        cvp.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("test",tab.getPosition()+"");
                tabNum = tab.getPosition();


                cvp.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cvp.setCurrentItem(tabNum);
//                        if(tabNum==1)
//                        mContentsPagerAdapter.influxMap(true);
                    }
                }, 100);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });


    }
    @Override
    public void onResume() {
        super.onResume();

        cvp.postDelayed(new Runnable() {
            @Override
            public void run() {
                cvp.setCurrentItem(tabNum);

            }
        }, 100);
    }
    private View createTabView(String tabName) {
        View tabView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView txt_name = (TextView) tabView.findViewById(R.id.txt_name);
        txt_name.setText(tabName);
        return tabView;

    }
}
