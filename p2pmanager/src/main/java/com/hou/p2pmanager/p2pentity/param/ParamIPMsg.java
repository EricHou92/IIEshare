package com.hou.p2pmanager.p2pentity.param;


import com.hou.p2pmanager.p2pentity.SigMessage;

import java.net.InetAddress;


/**
 * Created by ciciya on 2016/7/29.
 * 加上接收者ip地址和端口号的数据包
 */
public class ParamIPMsg
{
    public SigMessage peerMSG;
    public InetAddress peerIAddr;
    public int peerPort;

    public ParamIPMsg(String msg, InetAddress addr, int port)
    {
        peerMSG = new SigMessage(msg);
        peerIAddr = addr;
        peerPort = port;
    }
}
