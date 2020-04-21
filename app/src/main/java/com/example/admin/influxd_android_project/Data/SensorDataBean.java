package com.example.admin.influxd_android_project.Data;

import com.github.mikephil.charting.data.Entry;

import java.util.Date;

public class SensorDataBean extends Entry {



    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date date;

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }
    public void setData(double data,int index) {
        this.data[index] = data;
    }

    public double[] data = new double[3];
}
