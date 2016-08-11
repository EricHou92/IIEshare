package com.hou.iieshare;


import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;


/**
 * Created by ciciya on 2016/8/11.
 */
public class MyApplication extends Application
{

    private static MyApplication instance;

    public static int SCREEN_WIDTH;


    @Override
    public void onCreate()
    {
        super.onCreate();

        instance = this;
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        Point screen = new Point();
        display.getSize(screen);
        SCREEN_WIDTH = Math.min(screen.x, screen.y);

        initImageLoader();
    }

    private void initImageLoader()
    {
    }

    public static MyApplication getInstance()
    {
        return instance;
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
    }
}
