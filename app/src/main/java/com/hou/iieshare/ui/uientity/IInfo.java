package com.hou.iieshare.ui.uientity;

import android.graphics.drawable.Drawable;

/**
 * Created by ciciya on 2016/7/26.
 */
public interface IInfo {

    public String getFilePath();

    public String getFileSize();

    public int getFileType();

    public Drawable getFileIcon();

    public String getFileName();
}
