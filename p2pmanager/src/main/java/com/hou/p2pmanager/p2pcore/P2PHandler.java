package com.hou.p2pmanager.p2pcore;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.hou.p2pmanager.p2putils.P2PConstant;
import com.hou.p2pmanager.p2pcore.receive.ReceiveManager;
import com.hou.p2pmanager.p2pcore.send.SendManager;
import com.hou.p2pmanager.p2pentity.param.ParamIPMsg;
import com.hou.p2pmanager.p2putils.OSTimer;
import com.hou.p2pmanager.p2putils.Timeout;

import java.net.InetAddress;


/**
 * Created by ciciya on 2016/8/1.
 * 所有的message中转的handler，可以接受来自UI或者thread的message，也可以转发message到UI
 */
public class P2PHandler extends Handler
{
    private static final String tag = P2PHandler.class.getSimpleName();

    private P2PManager p2PManager;
    private UDPCommunicate udpCommunicate;
    private NeighborManager neighborManager;
    private ReceiveManager receiveManager;
    private SendManager sendManager;

    public P2PHandler(Looper looper)
    {
        super(looper);
    }

    public NeighborManager getNeighborManager()
    {
        return neighborManager;
    }

    public void init(P2PManager manager, Context context)
    {
        this.p2PManager = manager;
        //新开启一个线程
        udpCommunicate = new UDPCommunicate(p2PManager, this, context);
        udpCommunicate.start();

        neighborManager = new NeighborManager(p2PManager, this, udpCommunicate);
        //neighborManager.sendBroadcast();
        //新开启一个线程，发送广播子线程，不和UI交互，不需要Handler，sendmessage
        new Thread()
        {
            public void run()
            {
                neighborManager.sendBroadcast();
            }
        }.start();
    }

    public void initSend()
    {
        sendManager = new SendManager(this);
    }

    public void initReceive()
    {
        receiveManager = new ReceiveManager(this);
    }

    public void releaseReceive()
    {
        neighborManager.offLine();
        receiveManager.quit();
    }

    @Override
    public void handleMessage(Message msg) //进行网络相关操作
    {
        int src = msg.arg1;
        int dst = msg.arg2;
        switch (dst)
        {
            //总的分为三大类，邻居，发送，接收
            case P2PConstant.Dst.NEIGHBOR : //好友状态上线或者离线
                Log.d(tag, "received neighbor message");
                if (neighborManager != null)
                    neighborManager.disPatchMsg((ParamIPMsg) msg.obj);
                break;
            case P2PConstant.Dst.FILE_SEND : //发送文件
                if (sendManager != null)
                    sendManager.disPatchMsg(msg.what, src, msg.obj);
                break;
            case P2PConstant.Dst.FILE_RECEIVE : //接收文件
                if (receiveManager != null)
                    receiveManager.disPatchMsg(msg.what, src, msg.obj);
                break;
        }
    }
    public void release()
    {
        Log.d(tag, "p2pHandler release");

        if (receiveManager != null)
            releaseReceive();

        if (sendManager != null)
        {
            sendManager.quit();
        }

        Timeout timeout = new Timeout()
        {
            @Override
            public void onTimeOut()
            {
                if (udpCommunicate != null)
                {
                    udpCommunicate.quit();
                    udpCommunicate = null;
                }
            }
        };
        new OSTimer(this, timeout, 250);

        neighborManager = null;
    }

    public void releaseSend()
    {
        sendManager.quit();
        sendManager = null;
    }

    public void send2Handler(int cmd, int src, int dst, Object obj)
                                // what,    arg1,     arg2,       obj
    {
        sendMessage(this.obtainMessage(cmd, src, dst, obj));
    }

    public void send2UI(int cmd, Object obj)
    {
        if (p2PManager != null)
            p2PManager.getHandler().sendMessage(
                    p2PManager.getHandler().obtainMessage(cmd, obj));
    }

    public void send2Neighbor(InetAddress peer, int cmd, String add)
    {
        if (udpCommunicate != null)
            udpCommunicate.sendMsg2Peer(peer, cmd, P2PConstant.Dst.NEIGHBOR, add);
    }

    public void send2Sender(InetAddress peer, int cmd, String add)
    {
        if (udpCommunicate != null)
            udpCommunicate.sendMsg2Peer(peer, cmd, P2PConstant.Dst.FILE_SEND, add);
    }

    public void send2Receiver(InetAddress peer, int cmd, String add)
    {
        if (udpCommunicate != null)
            udpCommunicate.sendMsg2Peer(peer, cmd, P2PConstant.Dst.FILE_RECEIVE,
                add);
    }

}
