package com.hou.iieshare.utils;

import android.view.View;

/**
 * Created by ciciya on 2016/7/27.
 */
public class viewUtils {

    private final static String tag = viewUtils.class.getSimpleName();

    /** 获取每一个item在屏幕上的位置 */
    public static int[] getViewItemLocation(View view)
    {
        int[] location = new int[2]; //each item location
        view.getLocationInWindow(location);

        return location;
    }
}
