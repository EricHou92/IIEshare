package com.hou.p2pmanager.p2pinterface;


import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by ciciya on 2015/9/20.
 * 我要发送实现的发送回调
 */
public interface SendFile_Callback
{
    void BeforeSending();

    void OnSending(P2PFileInfo file, P2PNeighbor dest);

    void AfterSending(P2PNeighbor dest);

    void AbortSending(int error, P2PNeighbor dest);
}
