package com.hou.p2pmanager.p2pentity;

import java.net.InetAddress;

/**
 * Created by ciciya on 2016/7/26.
 * 局域网用户
 */
public class P2PNeighbor {

    public String alias;
    public String ip;
    public InetAddress inetAddress;

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        P2PNeighbor s = (P2PNeighbor) obj;

        if ((s.ip == null))
            return false;

        return (this.ip.equals(s.ip));
    }
}
