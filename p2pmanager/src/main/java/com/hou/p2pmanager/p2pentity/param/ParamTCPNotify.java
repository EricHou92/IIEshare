package com.hou.p2pmanager.p2pentity.param;

import com.hou.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by ciciya on 2016/7/26.
 */
public class ParamTCPNotify {

    public P2PNeighbor Neighbor;
    public Object Obj;

    public ParamTCPNotify(P2PNeighbor dest, Object obj)
    {
        Neighbor = dest;
        Obj = obj;
    }
}
