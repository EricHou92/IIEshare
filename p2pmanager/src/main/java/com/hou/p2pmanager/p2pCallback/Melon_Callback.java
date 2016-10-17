package com.hou.p2pmanager.p2pCallback;


import com.hou.p2pmanager.p2pentity.P2PNeighbor;

/**
 * Created by ciciya on 2016/7/29.
 */
public interface Melon_Callback
{
    void Melon_Found(P2PNeighbor melon);

    void Melon_Removed(P2PNeighbor melon);
}
