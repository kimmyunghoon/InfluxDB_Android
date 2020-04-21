package com.example.admin.influxd_android_project.Adapter;


import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.admin.influxd_android_project.Fragment.MainFragment;
import com.example.admin.influxd_android_project.Fragment.MapFragment;
import com.example.admin.influxd_android_project.Fragment.Sensor1GraphFragment;
import com.example.admin.influxd_android_project.Fragment.Sensor2GraphFragment;
import com.example.admin.influxd_android_project.Fragment.Sensor3GraphFragment;


public class ContentsPagerAdapter extends FragmentStatePagerAdapter {

    private int mPageCount;
    private MainFragment main;
    private MapFragment map;
    private Sensor1GraphFragment s1gf;
    private Sensor2GraphFragment s2gf;
    private Sensor3GraphFragment s3gf;
    Context context;
    public ContentsPagerAdapter(FragmentManager fm, int pageCount, Context context) {

        super(fm);
        this.context =context;
        this.mPageCount = pageCount;
        if(map==null)
        map =  new MapFragment(context);

        if(s1gf==null) {
            s1gf = new Sensor1GraphFragment(context);

        }
        if(s2gf==null)
        s2gf =  new Sensor2GraphFragment(context);
        if(s3gf==null)
        s3gf =  new Sensor3GraphFragment(context);
        if(main==null) {
            main = new MainFragment(context).
                    setFragment(map).
                    setFragment(s1gf).
                    setFragment(s2gf).
                    setFragment(s3gf);

        }
    }



    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return main;
            case 1:
                return map;
            case 2:
                return s1gf;
            case 3:
                return s2gf;
            case 4:
                return s3gf;
            default:
                return null;

        }

    }

    public void influxMap(boolean status){
        if(map!=null) {
            if(status)
            map.StartDataThread();
            else
                map.StartDataThread();
        }
    }

    @Override

    public int getCount() {

        return mPageCount;

    }

}