package com.hou.p2pmanager.p2pcore;


import android.util.Log;

import com.hou.p2pmanager.p2pconstant.P2PConstant;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;
import com.hou.p2pmanager.p2pentity.SigMessage;
import com.hou.p2pmanager.p2pentity.param.ParamIPMsg;
import com.hou.p2pmanager.p2ptimer.OSTimer;
import com.hou.p2pmanager.p2ptimer.Timeout;

import java.net.InetAddress;
import java.util.HashMap;


/**
 * Created by ciciya on 2015/9/19.
 */
public class NeighborManager
{

    private static final String tag = NeighborManager.class.getSimpleName();

    private P2PManager p2PManager;
    private P2PHandler p2PHandler;
    private P2PCommunicate sigCommunicate;

    private HashMap<String, P2PNeighbor> mNeighbors;

    public NeighborManager(P2PManager manager, P2PHandler handler, P2PCommunicate communicate)
    {
        p2PHandler = handler;
        p2PManager = manager;
        sigCommunicate = communicate;

        mNeighbors = new HashMap<String, P2PNeighbor>();
    }

    public void sendBroadcast()
    {
        Timeout timeout = new Timeout()
        {
            @Override
            public void onTimeOut()
            {
                Log.d(tag, "broadcast 广播 msg");
                sigCommunicate.BroadcastMSG(P2PConstant.CommandNum.ON_LINE,
                    P2PConstant.Dst.NEIGHBOR);
            }
        };
        //发送两个广播消息
        new OSTimer(p2PHandler, timeout, 250).start();
        new OSTimer(p2PHandler, timeout, 500).start();
    }

    public void disPatchMsg(ParamIPMsg ipmsg)
    {
        switch (ipmsg.peerMSG.commandNum)
        {
            case P2PConstant.CommandNum.ON_LINE : //收到上线广播
                Log.d(tag, "receive on_line and send on_line_ans message");
                addNeighbor(ipmsg.peerMSG, ipmsg.peerIAddr);
                //回复我上线
                p2PHandler.send2Neighbor(ipmsg.peerIAddr,
                    P2PConstant.CommandNum.ON_LINE_ANS, null);
                break;
            case P2PConstant.CommandNum.ON_LINE_ANS : //收到对方上线的回复
                Log.d(tag, "received on_line_ans message");
                addNeighbor(ipmsg.peerMSG, ipmsg.peerIAddr);
                break;
            case P2PConstant.CommandNum.OFF_LINE :
                delNeighbor(ipmsg.peerIAddr.getHostAddress());
                break;

        }
    }

    public void offLine()
    {
        Timeout timeOut = new Timeout()
        {
            @Override
            public void onTimeOut()
            {
                sigCommunicate.BroadcastMSG(P2PConstant.CommandNum.OFF_LINE,
                    P2PConstant.Dst.NEIGHBOR);
            }
        };
        timeOut.onTimeOut();
        new OSTimer(p2PHandler, timeOut, 250);
        new OSTimer(p2PHandler, timeOut, 500);
    }

    public HashMap<String, P2PNeighbor> getNeighbors()
    {
        return mNeighbors;
    }

    private void addNeighbor(SigMessage sigMessage, InetAddress address)
    {
        String ip = address.getHostAddress();
        P2PNeighbor neighbor = mNeighbors.get(ip);
        if (neighbor == null)
        {
            neighbor = new P2PNeighbor();
            neighbor.alias = sigMessage.senderAlias;
            neighbor.imei = sigMessage.senderImei;
            neighbor.ip = ip;
            neighbor.inetAddress = address;
            mNeighbors.put(ip, neighbor);

            p2PManager.getHandler().sendMessage(
                p2PManager.getHandler().obtainMessage(P2PConstant.UI_MSG.ADD_NEIGHBOR,
                    neighbor));
        }
    }

    private void delNeighbor(String ip)
    {
        P2PNeighbor neighbor = mNeighbors.get(ip);
        if (neighbor != null)
        {
            mNeighbors.remove(ip);
            p2PManager.getHandler().sendMessage(
                p2PManager.getHandler().obtainMessage(P2PConstant.UI_MSG.REMOVE_NEIGHBOR,
                    neighbor));
        }
    }
}
