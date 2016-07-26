package com.hou.p2pmanager.p2pinterface;

import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by ciciya on 2016/7/26.
 * 我要发送实现的发送回调
 */
public interface SendFile_Callback {

    public void BeforeSending();

    public void OnSending(P2PFileInfo file, P2PNeighbor dest);

    public void AfterSending(P2PNeighbor dest);

    public void AfterAllSending();

    public void AbortSending(int error, P2PNeighbor dest);
}