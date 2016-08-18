package com.hou.p2pmanager.p2pcore.receive;


import android.util.Log;

import com.hou.p2pmanager.p2pconstant.P2PConstant;
import com.hou.p2pmanager.p2pcore.P2PHandler;
import com.hou.p2pmanager.p2pcore.P2PManager;
import com.hou.p2pmanager.p2pentity.P2PFileInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by ciciya on 2016/8/1.
 * 接收端的线程实现
 */
public class ReceiveTask extends Thread
{
    private static final String tag = ReceiveTask.class.getSimpleName();

    private P2PHandler p2PHandler;
    private Receiver receiver;
    String sendIp;
    Socket socket;
    boolean finished = false;
    File receiveFile;
    BufferedOutputStream bufferedOutputStream;
    BufferedInputStream bufferedInputStream;
    byte[] readBuffer = new byte[512];
    private long startTime;
    private long overTime;
    private long totalSize;

    public ReceiveTask(P2PHandler handler, Receiver receiver)
    {
        this.p2PHandler = handler;
        this.receiver = receiver;
        this.sendIp = receiver.neighbor.ip;
    }
    @Override
    public void run()
    {
        loop : for (int i = 0; i < receiver.files.length; i++)
        {
            if (isInterrupted())
                break;
            try
            {
                socket = new Socket(sendIp, P2PConstant.PORT);
                //接收socket通道建立
                notifyReceiver(P2PConstant.CommandNum.RECEIVE_TCP_ESTABLISHED, null);

                P2PFileInfo fileInfo = receiver.files[i];

                Log.d(tag, "prepare to receive file:" + fileInfo.name + "; files size = "
                    + receiver.files.length);
                //记录线程开启时间
                if( i == 0)
                {
                    Date time1 = new Date();
                    startTime = time1.getTime();
                    System.out.println("接收文件线程开始时间:" + startTime);
                }

                String path = P2PManager.getSavePath(fileInfo.type, receiver.neighbor);
                File fileDir = new File(path);
                if (!fileDir.exists())
                    fileDir.mkdirs();

                receiveFile = new File(fileDir, fileInfo.name);
                if (receiveFile.exists())
                    receiveFile.delete();

                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(
                    receiveFile));

                long total = 0L;//正在接收文件的大小
                int len = 0;
                int lastPercent = 0, percent = 0;
                bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                while ((len = bufferedInputStream.read(readBuffer)) != -1)
                {
                    if (isInterrupted())
                    {
                        receiveFile.delete();
                        break loop;
                    }
                    bufferedOutputStream.write(readBuffer, 0, len);

                    total += len;
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

                Log.d(tag, "receive file " + fileInfo.name + " success");

                socket.close();

                if (i == receiver.files.length - 1)
                {
                    Log.d(tag, "receive file over");
                    //记录线程结束时间
                    Date time2 = new Date();
                    overTime = time2.getTime();
                    System.out.println("接收文件线程结束时间:" + overTime);
                    notifyReceiver(P2PConstant.CommandNum.RECEIVE_OVER, null);
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
                release();
            }
        }// end of loop

        long interval = overTime - startTime;
        float iSecond = (float) (interval/1000);
        System.out.println("传输时间" + iSecond + "S");
        String aSpeed = averageSpeed();
        System.out.println("传输平均速度" + aSpeed);
        String tSize = flux();
        System.out.println("传输消耗流量" + tSize);

        release();
    }

    /**传输平均速度
     * @return
     */
    public String averageSpeed()
    {
        long interval = overTime - startTime;
        float iSecond = (float) (interval/1000);
        for (int i = 0; i < receiver.files.length; i++)
        {
            P2PFileInfo fileInfo = receiver.files[i];
            totalSize += fileInfo.size;
        }
        float aveSpeed = ( totalSize / iSecond) / 1024;
        String aSpeed = aveSpeed + "KB/S";
        return aSpeed;
    }


    /**传输消耗流量
     * @return
     */
    public String flux()
    {
        String tSize = getFileSize(totalSize);
        return tSize;
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

    private void notifyReceiver(int cmd, Object obj)
    {
        if (!finished)
        {
            if (p2PHandler != null)
                p2PHandler.send2Handler(cmd, P2PConstant.Src.RECEIVE_TCP_THREAD,
                    P2PConstant.Dst.FILE_RECEIVE, obj);
        }
    }
}
