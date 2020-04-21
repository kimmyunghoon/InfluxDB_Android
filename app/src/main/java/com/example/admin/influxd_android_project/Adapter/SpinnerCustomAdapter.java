package com.example.admin.influxd_android_project.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.admin.influxd_android_project.R;

import java.util.ArrayList;
import java.util.List;

public class SpinnerCustomAdapter extends ArrayAdapter{
    private Context context;
    private List<String> items;
    LayoutInflater inflater;
    public SpinnerCustomAdapter(@NonNull Context context, int resource, @NonNull List<String>  objects) {
        super(context, resource, objects);
        this.context = context;
        items=objects;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            /*
             * 항목을 선택했을때 나오게되는 드롭 다운 항목들에 대한 레이아웃 구성
             *
             * */
            convertView = inflater.inflate(R.layout.datetext, parent, false);
        }

        //데이터세팅
        String text = items.get(position);
        ((TextView)convertView.findViewById(R.id.date_textview)).setText(text);

        return convertView;

    }
    @Override
    public int getCount() {
        if(items!=null) return items.size();
        else return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null) {
            /*
            * 보여지는 콤보박스 모습 : 선택된 항목의 레이아웃 구성
            * */
            convertView = inflater.inflate(R.layout.datetext, parent, false);
        }

        if(items!=null){
            //데이터세팅
            String text = items.get(position);
            ((TextView)convertView.findViewById(R.id.date_textview)).setText(text);
        }

        return convertView;

    }
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }




}
