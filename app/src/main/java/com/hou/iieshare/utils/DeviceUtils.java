package com.hou.iieshare.utils;


import android.media.ExifInterface;
import android.os.Build;

import com.hou.p2pmanager.p2pentity.P2PFileInfo;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.List;


/**
 * Created by ciciya on 2016/8/5.
 */
public class DeviceUtils
{

    public static boolean isHTC()
    {
        boolean ishtc = false;
        if (Build.MODEL.contains("htc") || Build.MODEL.contains("HTC"))
        {
            ishtc = true;
        }
        return ishtc;
    }

    public static String convertByte(long size)
    {
        DecimalFormat df = new DecimalFormat("###.##");
        float f;
        if (size < 1024 * 1024)
        {
            f = ((float) size / (float) 1024);
            return (df.format(new Float(f).doubleValue()) + "KB");
        }
        else
        {
            f = ((float) size / (float) (1024 * 1024));
            return (df.format(new Float(f).doubleValue()) + "MB");
        }
    }

    public static String getFileName(String path)
    {
        return new File(path).getName();
    }

    /** get file size M K B G etc... */
    public static String getFileSize(long fileS)
    {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024)
        {
            fileSizeString = df.format((double) fileS) + "B";
        }
        else if (fileS < 1048576)
        {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        }
        else if (fileS < 1073741824)
        {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        }
        else
        {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public static String removeSpecial(String before)
    {
        String after = null;
        after = before.replace(" ", "").replace("\u003f", "").replace("\u002F", "")
                .replace("\u003A", "").replace("\u002A", "").replace("\u007C", "")
                .replace("\u003E", "").replace("\u003C", "").replace("\\", "")
                .replace("\"", "");
        return after;
    }

    /**
     * 对字符串进行md5加密
     *
     */
    public static String md5Encrypt(String srcStr)
    {
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bt = srcStr.getBytes();
            md5.update(bt);
            String destStr = toHex(md5.digest());
            return destStr;
        }
        catch (NoSuchAlgorithmException e)
        {
            return null;
        }
    }

    public static String toHex(byte[] bytes)
    {
        StringBuffer buf = new StringBuffer("");
        String tmp = null;

        for (int i = 0; i < bytes.length; i++)
        {
            tmp = (Integer.toHexString(bytes[i] & 0xFF));
            if (tmp.length() == 1)
            {
                buf.append("0");
            }
            buf.append(tmp);
        }
        return buf.toString();
    }

    public static int getExifOrientation(String filepath)
    {
        int degree = 0;
        ExifInterface exif = null;
        try
        {
            exif = new ExifInterface(filepath);
        }
        catch (IOException ex)
        {
        }
        if (exif != null)
        {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1)
            {
                switch (orientation)
                {
                    case ExifInterface.ORIENTATION_ROTATE_90 :
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180 :
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270 :
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }

    /**
     * 递归删除文件和文件夹
     * @param file    要删除的根目录
     */
    public static void deleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            //空文件夹情况
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                deleteFile(f);
            }
            file.delete();
        }
    }

    /**自定义文件夹扫描发送
     * @param fileList
     * @param path
     */
    public static void getFiles(List<P2PFileInfo> fileList, String path) {
        File desDir = new File(path);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }

        if (desDir.isFile()) {
            P2PFileInfo msg = new P2PFileInfo();
            msg.name = desDir.getName();
            msg.size = desDir.length();
            msg.path = desDir.getPath();
            fileList.add(msg);
            return;
        }
        if (desDir.isDirectory()) {
            File[] childFile = desDir.listFiles();
            for (File f : childFile) {
                getFiles(fileList, f.getAbsolutePath());
            }
        }
    }

}
