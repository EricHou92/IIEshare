package com.hou.p2pmanager.p2pinterface;

import com.hou.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by ciciya on 2016/7/26.
 */
public interface iie_Callback {

    /**
     * 局域网发现好友
    */
    public void iie_Found(P2PNeighbor iie);

    /**
     * 局域网好友离开
     */
    public void iie_Removed(P2PNeighbor iie);
}
