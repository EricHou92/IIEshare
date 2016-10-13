package com.hou.p2pmanager.p2pinterface;


import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by ciciya on 2015/9/20.
 * 我要接受实现的接收回调
 */
public interface ReceiveFile_Callback
{
    boolean QueryReceiving(P2PNeighbor src, P2PFileInfo files[]);

    void BeforeReceiving(P2PNeighbor src, P2PFileInfo files[]);

    void OnReceiving(P2PFileInfo files);

    void AfterReceiving();

    void AbortReceiving(int error, String alias);
}
