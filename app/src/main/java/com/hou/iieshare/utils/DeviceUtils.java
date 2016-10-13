package com.hou.iieshare.utils;


import com.hou.p2pmanager.p2pentity.P2PFileInfo;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.List;


/**
 * Created by ciciya on 2016/8/5.
 */
public class DeviceUtils
{
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
