package com.hou.p2pmanager.p2putils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ciciya on 2016/9/26.
 */
public class LogUtil {
    public static void addLog(String address, String line) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);//日期格式化对象
        File fileDir = new File(address);
        if (!fileDir.exists())
            fileDir.mkdirs();
        File file = new File(fileDir, "receivelog.txt");
        try{
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.append(sdf.format(new Date(System.currentTimeMillis())) + "  "
                    + line + "\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
