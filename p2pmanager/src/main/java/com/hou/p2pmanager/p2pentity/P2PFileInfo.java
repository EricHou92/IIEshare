package com.hou.p2pmanager.p2pentity;


import java.io.File;


/**
 * Created by ciciya on 2016/7/29.
 * android设备中的文件
 */
public class P2PFileInfo
{
    public File file;
    public String path;
    public String name;
    public long size;
    public int type;
    public int percent;
    public boolean success;
    public long LengthNeeded = 0;


    public P2PFileInfo() {
    }

    public P2PFileInfo(String string,P2PNeighbor neighbor)
    {
        String str[] = string.split(":");
        name = str[0];
        size = Long.parseLong(str[1]);
        type = Integer.parseInt(str[2]);
        path = str[3];
        //path = P2PManager.getSavePath( neighbor) + File.separator + name;
        //File.separator是用来分隔同一个路径字符串中的目录的,如C:\Program Files\Common Files
    }

    public int getPercent()
    {
        return percent;
    }

    public void setPercent(int percent)
    {
        this.percent = percent;
        if (percent == 100)
        {
            success = true;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        return (((P2PFileInfo) (o)).name.equals(name))
            && (((P2PFileInfo) (o)).size == size) && (((P2PFileInfo) (o)).type == type)
            && (((P2PFileInfo) (o)).path.equals(path));
    }


    @Override
    //要发送的文件字段信息
    public String toString()
    {
        return name + ":" + size + ":" + type + ":" + path  +"\0";
    }

    /**增加文件副本
     * @return
     */
    public P2PFileInfo duplicate()
    {
        P2PFileInfo file = new P2PFileInfo();

        file.name = this.name;
        file.size = this.size;
        file.path = this.path;
        file.type = this.type;
        file.percent = this.percent;
        file.success = this.success;
        file.LengthNeeded = this.LengthNeeded;

        return file;
    }
}
