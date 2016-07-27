package com.hou.iieshare.ui.uientity;

import android.graphics.drawable.Drawable;

import com.hou.p2pmanager.p2pconstant.P2PConstant;

/**
 * Created by ciciya on 2016/7/27.
 */
public class AppInfo implements IInfo{

    public int type = P2PConstant.TYPE.APP;
    public Drawable appIcon;
    public String appLabel;
    public String pkgName;
    public String appSize;
    public String appFilePath;

    @Override
    public String getFilePath()
    {
        return appFilePath;
    }

    @Override
    public String getFileSize()
    {
        return appSize;
    }

    @Override
    public int getFileType()
    {
        return type;
    }

    @Override
    public Drawable getFileIcon()
    {
        return appIcon;
    }

    @Override
    public String getFileName()
    {
        return appLabel;
    }

    @Override
    public boolean equals(Object o)
    {
        if (getFilePath() != null && ((AppInfo) o).getFilePath() != null)
            return getFilePath().equals(((AppInfo) o).getFilePath());
        else
            return false;
    }

}