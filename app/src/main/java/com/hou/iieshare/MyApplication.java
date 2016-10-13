package com.hou.iieshare;


import android.app.Application;


/**
 * Created by ciciya on 2016/9/28.
 */
public class MyApplication extends Application
{
    private static MyApplication instance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
    }

    public static MyApplication getInstance()
    {
        return instance;
    }
}
