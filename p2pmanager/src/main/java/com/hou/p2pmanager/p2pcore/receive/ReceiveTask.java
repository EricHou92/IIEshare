package com.hou.p2pmanager.p2pcore.receive;


import android.util.Log;

import com.hou.p2pmanager.p2putils.P2PConstant;
import com.hou.p2pmanager.p2pcore.P2PHandler;
import com.hou.p2pmanager.p2pcore.P2PManager;
import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2putils.TestUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by ciciya on 2016/8/1.
 * 接收端的线程实现
 */
public class ReceiveTask extends Thread
{
    private static final String tag =  ReceiveTask.class.getSimpleName();

    private P2PHandler p2PHandler;
    private Receiver receiver;
    private String sendIp;
    private Socket socket;
    boolean finished = false;
    private File receiveFile;
    private BufferedOutputStream bufferedOutputStream;
    private BufferedInputStream bufferedInputStream;
    private String startTime;
    private long totalSize = 0;
    private File fileDir;
    private long startTime1;
    private long overTime1;

    public ReceiveTask(P2PHandler handler, Receiver receiver)
    {
        this.p2PHandler = handler;
        this.receiver = receiver;
        this.sendIp = receiver.neighbor.ip;
    }
    @Override
    public void run()
    {
        Date time1 = new Date();
        startTime1 = time1.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        startTime = simpleDateFormat.format(time1);

        //接收文件前创建日志文件，保存接收时间
        File log = new File(P2PManager.ROOT_SAVE_DIR, "config.txt");
        if(!log.exists()) {
            try {
                log.createNewFile();
                FileWriter fileWriter = new FileWriter(log);
                Properties properties = new Properties();
                properties.put("breakTime", startTime);

                properties.store(fileWriter, null);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //读取配置文件，更新时间
        else{
            try {
                Properties properties = new Properties();
                FileReader fileReader = new FileReader(log);
                properties.load(fileReader);
                String oldTime = properties.getProperty("breakTime");
                if(!oldTime.equals(startTime)){
                    startTime = oldTime;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String address = P2PManager.getSavePath(receiver.neighbor) + File.separator + startTime;
        TestUtil.addLog(address, tag + " 接收文件线程开始时间: " + startTime);
        TestUtil.addLog(address, tag + " 发送方手机型号" + receiver.neighbor.alias);
        TestUtil.addLog(address, tag + " 发送方IMEI：" + receiver.neighbor.imei);
        TestUtil.addLog(address, tag + " 发送方IP：" + receiver.neighbor.ip);
        loop : for (int i = 0; i < receiver.files.length; i++)
        {
            if (isInterrupted())
                break;
            try
            {
                socket = new Socket(sendIp, P2PConstant.PORT);
                notifyReceiver(P2PConstant.CommandNum.RECEIVE_TCP_ESTABLISHED, null);

                P2PFileInfo fileInfo = receiver.files[i];
                int i1 = i + 1;
                TestUtil.addLog(address, tag +  " 开始接收文件 = " + fileInfo.name
                        + "; 接收的第" +  i1 +"个文件"
                        + "; 文件大小 = " + getFileSize(fileInfo.size)
                        + "; 文件来源地址 = " + fileInfo.path );

                String[] strings = fileInfo.path.split("/");
                String newPath = "";
                for(int x =6; x<strings.length-1; x++){
                    String newString = "/" + strings[x];
                    newPath += newString;
                }

                String path = P2PManager.getSavePath(receiver.neighbor) + File.separator + startTime + newPath;
                fileDir = new File(path);
                if (!fileDir.exists())
                    fileDir.mkdirs();
                 receiveFile = new File(fileDir, fileInfo.name);

                //如果已经存在，删除原有的重复接收文件
                if (receiveFile.exists())
                    receiveFile.delete();
                //本地存储文件的数据流
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(
                    receiveFile));
                long total = 0L;//正在接收文件的大小
                int len = 0;
                byte[] readBuffer = new byte[512];
                int lastPercent = 0, percent = 0;
                bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                //从输入流中读取数据写入到文件中
                while ((len = bufferedInputStream.read(readBuffer)) != -1)
                {
                    if (isInterrupted())
                    {
                        receiveFile.delete();
                        break loop;
                    }
                    bufferedOutputStream.write(readBuffer, 0, len);
                    total += len;

                    //接收百分比相关
                    percent = (int) (((float) total / fileInfo.size) * 100);
                    if (percent - lastPercent > 1 || percent == 100)
                    {
                        lastPercent = percent;
                        fileInfo.setPercent(percent);
                        notifyReceiver(P2PConstant.CommandNum.RECEIVE_PERCENT, fileInfo);
                    }
                    if (total >= fileInfo.size)
                    {
                        Log.d(tag, "total > file info size");
                        break;
                    }
                } // end of while

                receiveFile = null;
                fileInfo.setPercent(100);
                notifyReceiver(P2PConstant.CommandNum.RECEIVE_PERCENT, fileInfo);
                TestUtil.addLog(address, tag + " 接收文件 " + fileInfo.name + " 成功");
                socket.close();

                if(i == receiver.files.length - 1){
                    log.delete();
                    notifyReceiver(P2PConstant.CommandNum.RECEIVE_OVER, null);
                    TestUtil.addLog(address, tag + " 本次接收任务结束");
                    finished = true;
                }

            }
            catch (InterruptedIOException e)
            {
                e.printStackTrace();
                finished = true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                finished = true;
            }
            finally
            {
                //传输速度接口
                Date time2 = new Date();
                overTime1 = time2.getTime();
                long interval = overTime1 - startTime1;
                float iSecond = (float) (interval/1000);
                TestUtil.addLog(address, tag + " 传输已耗时间" + iSecond + "S");
                totalSize += receiver.files[i].size;
                TestUtil.addLog(address, tag + " 传输已耗流量" + getFileSize(totalSize));
                String aSpeed = (totalSize / iSecond) / 1024 + "KB/S";
                TestUtil.addLog(address, tag +  " 此文件传输速度" + aSpeed);
                release();
            }
        }// end of loop
        release();
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

    private void notifyReceiver(int cmd, Object obj)
    {
        if (!finished)
        {
            if (p2PHandler != null)
                p2PHandler.send2Handler(cmd, P2PConstant.Src.RECEIVE_TCP_THREAD,
                        P2PConstant.Dst.FILE_RECEIVE, obj);
        }
    }

    private void release()
    {
        if (bufferedOutputStream != null)
        {
            try
            {
                bufferedOutputStream.close();
                bufferedOutputStream = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if (bufferedInputStream != null)
        {
            try
            {
                bufferedInputStream.close();
                bufferedInputStream = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if (socket != null)
        {
            try
            {
                socket.close();
                socket = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
