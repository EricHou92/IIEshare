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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Created by ciciya on 2016/8/1.
 * 接收端的线程实现
 */
public class ReceiveTask extends Thread
{
    private static final String tag = ReceiveTask.class.getSimpleName();

    private P2PHandler p2PHandler;
    private Receiver receiver;
    private String sendIp;
    private Socket socket;
    boolean finished = false;
    private File receiveFile;
    private BufferedOutputStream bufferedOutputStream;
    private BufferedInputStream bufferedInputStream;
    private String startTime;
    private String overTime;
    private long totalSize;
    private File fileDir;
    private long startTime1;
    private long overTime1;
    private TreeSet<Integer> treeSet  = new TreeSet<>();

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
                notifyReceiver(P2PConstant.CommandNum.RECEIVE_TCP_ESTABLISHED, null);

                P2PFileInfo fileInfo = receiver.files[i];
                Log.d(tag, "prepare to receive file:" + fileInfo.name + "; files size = "
                    + receiver.files.length + "; 接收的第几个文件：" + i);

                //创建本地保存接收文件夹
                String path = P2PManager.getSavePath(receiver.neighbor);
                fileDir = new File(path);
                if (!fileDir.exists())
                    fileDir.mkdirs();
                //如果已经存在，删除原有的重复接收文件
                receiveFile = new File(fileDir, fileInfo.name);
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

                treeSet.add(i);
                receiveFile = null;
                fileInfo.setPercent(100);
                notifyReceiver(P2PConstant.CommandNum.RECEIVE_PERCENT, fileInfo);
                Log.d(tag, "receive file " + fileInfo.name + " success");
                socket.close();

                //记录线程开启时间
                if( i == 0)
                {
                    Date time1 = new Date();
                    startTime1 = time1.getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    startTime = simpleDateFormat.format(time1);
                    System.out.println("接收文件线程开始时间:" + startTime);
                }
                //记录线程结束时间
                if (i == receiver.files.length - 1)
                {
                    //记录线程结束时间
                    Date time2 = new Date();
                    overTime1 = time2.getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    overTime = simpleDateFormat.format(time2);
                    System.out.println("接收文件线程结束时间:" + overTime);
                    notifyReceiver(P2PConstant.CommandNum.RECEIVE_OVER, null);
                    Log.d(tag, "receive file over");
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

        //传输速度接口
        long interval = overTime1 - startTime1;
        float iSecond = (float) (interval/1000);
        System.out.println("传输时间" + iSecond + "S");
        String aSpeed = averageSpeed();
        System.out.println("传输平均速度" + aSpeed);
        String tSize = flux();
        System.out.println("传输消耗流量" + tSize);
        Integer already = treeSet.last() + 1;

        //接收结束后创建日志文件
        File fileLog = new File(P2PManager.ROOT_SAVE_DIR, "receiveLog@"+ System.currentTimeMillis()+".txt");
        if(!fileLog.exists()){
            try {
                fileLog.createNewFile();
                FileWriter fileWriter =new FileWriter(fileLog);
                Properties properties =new Properties();
                //properties.put("currentTime",System.currentTimeMillis());
                properties.put("startTime",startTime);
                //properties.put("overTime",overTime);
                properties.put("sendAlias",receiver.neighbor.alias +"@"+ receiver.neighbor.imei);
                properties.put("sendNum", String.valueOf(receiver.files.length));
                properties.put("receiveAlready", String.valueOf(already));

                properties.store(fileWriter,null);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        release();
    }

    /**传输平均速度
     * @return
     */
    public String averageSpeed()
    {
        long interval = overTime1 - startTime1;
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
