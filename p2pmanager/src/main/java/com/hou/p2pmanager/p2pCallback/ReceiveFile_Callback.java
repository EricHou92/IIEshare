package com.hou.p2pmanager.p2pCallback;


import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by ciciya on 2016/7/29.
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
