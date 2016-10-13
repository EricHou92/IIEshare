package com.hou.p2pmanager.p2putils;

/**
 * Created by ciciya on 2016/8/11.
 */
public class P2PConstant {
    public static final int BUFFER_LENGTH = 8192;
    public static final int PORT = 10000;//UDP通信端口

    public static final String FORMAT = "gbk";

    public static final String MSG_SEPARATOR = "\0";

    public static final String FILE_SHARE_SAVE_PATH = "信安快传";
    public static final String FILE_SECRET_SEND_PATH ="机密发送" ;
    public static final String FILE_SECRET_RECEIVE_PATH ="机密接收" ;

    public static final int MAXIMUM_POOL_SIZE = 4;

    public interface UI_MSG
    {
        int ADD_NEIGHBOR = 1000;
        int REMOVE_NEIGHBOR = 10001;
    }

    public interface CommandNum
    {
        int ON_LINE = 0;
        int OFF_LINE = 1;
        int ON_LINE_ANS = 2;

        int SEND_FILE_REQ = 3;
        int RECEIVE_FILE_ACK = 4;
        int SEND_FILE_START = 5;

        int SEND_TCP_ESTABLISHED = 6;
        int SEND_LINK_ERROR = 7;
        int SEND_PERCENTS = 8;
        int SEND_OVER = 9;

        int RECEIVE_TCP_ESTABLISHED = 10;
        int RECEIVE_PERCENT = 11;
        int RECEIVE_OVER = 12;

        int RECEIVE_ABORT_SELF = 13;
        int SEND_ABORT_SELF = 14;

    }

    public interface Src
    {
        int MANAGER = 90;
        int COMMUNICATE = 91;
        int SEND_TCP_THREAD = 92;
        int RECEIVE_TCP_THREAD = 92;
    }

    public interface Dst
    {
        int NEIGHBOR = 100;
        int FILE_SEND = 101;
        int FILE_RECEIVE = 102;
    }
}
