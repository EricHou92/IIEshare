package com.hou.p2pmanager.p2pinterface;


import java.io.IOException;
import java.nio.channels.SelectionKey;


/**
 * Created by ciciya on 2015/9/22.
 * 处理NIO client与server交互
 */
public interface NIOHandler
{
    /**
     * 处理客户端请求连接
     */
    void handleAccept(SelectionKey key) throws IOException;

    void handleRead(SelectionKey key) throws IOException;

    /**
     * 处理发送端写文件
     */
    void handleWrite(SelectionKey key) throws IOException;
}
