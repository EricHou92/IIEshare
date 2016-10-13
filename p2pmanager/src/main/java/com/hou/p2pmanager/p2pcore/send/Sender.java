package com.hou.p2pmanager.p2pcore.send;


import com.hou.p2pmanager.p2putils.P2PConstant;
import com.hou.p2pmanager.p2pcore.P2PHandler;
import com.hou.p2pmanager.p2pcore.P2PManager;
import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;
import com.hou.p2pmanager.p2pentity.SocketTransInfo;
import com.hou.p2pmanager.p2pentity.param.ParamIPMsg;
import com.hou.p2pmanager.p2pentity.param.ParamTCPNotify;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by ciciya on 2016/8/2.
 */
public class Sender
{
    private static final String tag = Sender.class.getSimpleName();

    P2PHandler p2PHandler;
    P2PFileInfo[] files;
    SendManager sendManager;
    P2PNeighbor neighbor;
    ArrayList<SendTask> mSendTasks = new ArrayList<>();
    int index = 0;//发送文件编号
    boolean flagPercents = false;

    public Sender(P2PHandler handler, SendManager sendManager, P2PNeighbor neighbor, P2PFileInfo[] fs)
    {
        this.p2PHandler = handler;
        this.sendManager = sendManager;
        this.neighbor = neighbor;

        files = new P2PFileInfo[fs.length];
        for (int i = 0; i < files.length; i++)
        {
            files[i] = fs[i].duplicate();
            files[i].percent = 0;
        }
    }

    //命令调度
    public void dispatchCommMSG(int cmd, ParamIPMsg ipmsg)
    {
        switch (cmd)
        {
            case P2PConstant.CommandNum.RECEIVE_FILE_ACK :
                startSelf();
                //通知界面开始发送
                if (p2PHandler != null)
                    p2PHandler.send2UI(P2PConstant.CommandNum.SEND_FILE_START, null);
                //通知接收端 开始发送文件
                if (p2PHandler != null)
                    p2PHandler.send2Receiver(ipmsg.peerIAddr,
                        P2PConstant.CommandNum.SEND_FILE_START, null);
                break;
            case P2PConstant.CommandNum.RECEIVE_ABORT_SELF : //接收者退出
                clearSelf();
                System.out.println("此时发送到第几个文件：" + index);
                //通知UI
                if (p2PHandler != null)
                    p2PHandler.send2UI(cmd, neighbor);
                break;
        }
    }

    //IP数据包调度
    public void dispatchTCPMsg(int cmd, ParamTCPNotify notify)
    {
        switch (cmd)
        {
            case P2PConstant.CommandNum.SEND_PERCENTS :
            {
                SocketTransInfo socketTransInfo = (SocketTransInfo) notify.Obj;
                P2PFileInfo fileInfo = files[index];
                int lastPercent, percent;
                lastPercent = fileInfo.getPercent();
                percent = (int) (((float)socketTransInfo.Transferred / fileInfo.size) * 100);

               //发送结束后创建日志文件(修订版本在这里调用)

                ParamTCPNotify tcpNotify;
                if (percent < 100)
                {
                    if (percent != lastPercent)
                    {
                        fileInfo.setPercent(percent);
                        tcpNotify = new ParamTCPNotify(neighbor, fileInfo);
                        if (p2PHandler != null)
                            p2PHandler.send2UI(P2PConstant.CommandNum.SEND_PERCENTS,
                                tcpNotify);

                    }
                }
                else if (percent == 100)
                {
                    fileInfo.setPercent(percent);
                    tcpNotify = new ParamTCPNotify(neighbor, fileInfo);
                    p2PHandler.send2UI(P2PConstant.CommandNum.SEND_PERCENTS, tcpNotify);
                    index++;
                    clearTask();


                    //发送结束后创建日志文件
                    File fileLog = new File(P2PManager.ROOT_SAVE_DIR, "sendLog.txt");
                    if(!fileLog.exists()){
                        try {
                            fileLog.createNewFile();
                            FileWriter fileWriter =new FileWriter(fileLog);
                            Properties properties = new Properties();
                            properties.put("receiveAlias",this.neighbor.alias +"@"+ this.neighbor.imei);
                            properties.put("sendNum", String.valueOf(this.files.length));
                            properties.put("sendAlready", String.valueOf(index));
                            properties.put("fileInfo",fileInfo.name);
                            properties.put("percent",String.valueOf(percent));

                            properties.store(fileWriter,null);
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        fileLog.delete();
                        try {
                            fileLog.createNewFile();
                            FileWriter fileWriter =new FileWriter(fileLog);
                            Properties properties = new Properties();
                            properties.put("receiveAlias",this.neighbor.alias +"@"+ this.neighbor.imei);
                            properties.put("sendTotal", String.valueOf(this.files.length));
                            properties.put("sendAlready", String.valueOf(index));
                            properties.put("fileInfo",fileInfo.name);
                            properties.put("percent",String.valueOf(percent));

                            properties.store(fileWriter,null);
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (index == files.length)
                    {
                        fileLog.delete();
                        if (p2PHandler != null)
                            p2PHandler.send2UI(P2PConstant.CommandNum.SEND_OVER, neighbor);
                    }
                }
                break;
            }
            case P2PConstant.CommandNum.SEND_TCP_ESTABLISHED :
                break;
        }
    }

    //UI信息调度
    public void dispatchUIMSG(int cmd)
    {
        switch (cmd)
        {
            case P2PConstant.CommandNum.SEND_ABORT_SELF :
                clearSelf();
                p2PHandler.send2Receiver(neighbor.inetAddress,
                    P2PConstant.CommandNum.SEND_ABORT_SELF, null);
                break;
        }
    }

    private void clearTask()
    {
        if (mSendTasks.size() > 0)
        {
            SendTask task = mSendTasks.get(0);
            if (task != null && task.finished == false)
            {
                task.quit();
            }
            mSendTasks.remove(0);
        }
    }

    private void startSelf()
    {
        sendManager.startSend(neighbor.ip, this);
    }

    public void clearSelf()
    {
        sendManager.removeSender(neighbor.ip);
    }

}
