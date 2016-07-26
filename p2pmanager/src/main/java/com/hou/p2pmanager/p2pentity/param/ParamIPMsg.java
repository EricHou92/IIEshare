package com.hou.p2pmanager.p2pentity.param;

import com.hou.p2pmanager.p2pentity.SigMessage;

import java.net.InetAddress;

/**
 * Created by ciciya on 2016/7/26.
 */
public class ParamIPMsg {

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
