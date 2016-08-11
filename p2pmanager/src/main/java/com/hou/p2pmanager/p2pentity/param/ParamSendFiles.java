package com.hou.p2pmanager.p2pentity.param;


import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by ciciya on 2015/9/20.
 */
public class ParamSendFiles
{
    public P2PFileInfo[] files;
    public P2PNeighbor[] neighbors;

    public ParamSendFiles(P2PNeighbor[] neighbors, P2PFileInfo[] files)
    {
        this.neighbors = neighbors;
        this.files = files;
    }
}
