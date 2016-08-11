package com.hou.p2pmanager.p2pentity.param;


import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by ciciya on 2015/9/20.
 */
public class ParamReceiveFiles
{
    public P2PNeighbor Neighbor;
    public P2PFileInfo[] Files;

    public ParamReceiveFiles(P2PNeighbor dest, P2PFileInfo[] files)
    {
        Neighbor = dest;
        Files = files;
    }
}
