package com.hou.p2pmanager.p2pcore;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hou.p2pmanager.p2pconstant.P2PConstant;
import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;
import com.hou.p2pmanager.p2pentity.param.ParamIPMsg;
import com.hou.p2pmanager.p2pentity.param.ParamReceiveFiles;
import com.hou.p2pmanager.p2pentity.param.ParamSendFiles;
import com.hou.p2pmanager.p2pentity.param.ParamTCPNotify;
import com.hou.p2pmanager.p2pinterface.Melon_Callback;
import com.hou.p2pmanager.p2pinterface.ReceiveFile_Callback;
import com.hou.p2pmanager.p2pinterface.SendFile_Callback;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Created by ciciya on 2015/9/17.
 */
public class P2PManager
{
    private static final String tag = P2PManager.class.getSimpleName();

    //文件保存根地址
    private static String ROOT_SAVE_DIR = Environment.getExternalStorageDirectory().getPath()
        + File.separator + P2PConstant.FILE_SHARE_SAVE_PATH ;

    private P2PNeighbor meInfo;
    private Melon_Callback melon_callback;
    private CustomHandlerThread p2pThread;
    private P2PHandler p2PHandler;
    private P2PManagerHandler mHandler;

    private ReceiveFile_Callback receiveFile_callback;
    private SendFile_Callback sendFile_callback;

    private Context mContext;
    private ParamSendFiles paramSendFiles;

    public P2PManager(Context context)
    {
        mContext = context;
        mHandler = new P2PManagerHandler(this);
    }

    public void start(P2PNeighbor neighbor, Melon_Callback neighbor_callback)
    {
        this.meInfo = neighbor;
        this.melon_callback = neighbor_callback;

        p2pThread = new CustomHandlerThread("P2PThread", P2PHandler.class);
        p2pThread.start();
        p2pThread.isReady();

        p2PHandler = (P2PHandler) p2pThread.getLooperHandler();
        p2PHandler.init(this, mContext);
    }

    //定义发送文件的方法
    public void sendFile(P2PNeighbor[] dsts, P2PFileInfo[] files,
            SendFile_Callback callback)
    {
        this.sendFile_callback = callback;
        p2PHandler.initSend();//发送前初始化

        paramSendFiles = new ParamSendFiles(dsts, files);
        p2PHandler.send2Handler(P2PConstant.CommandNum.SEND_FILE_REQ,
            P2PConstant.Src.MANAGER, P2PConstant.Dst.FILE_SEND, paramSendFiles);
    }

    //响应发送端请求
    public void ackReceive()
    {
        p2PHandler.send2Handler(P2PConstant.CommandNum.RECEIVE_FILE_ACK,
                P2PConstant.Src.MANAGER, P2PConstant.Dst.FILE_RECEIVE, null);
    }

    //定义接收文件的方法
    public void receiveFile(ReceiveFile_Callback callback)
    {
        receiveFile_callback = callback;
        p2PHandler.initReceive();//接收前初始化
    }


    public P2PNeighbor getSelfMeMelonInfo()
    {
        return meInfo;
    }

    public Handler getHandler()
    {
        return mHandler;
    }

    public void stop()
    {
        if (p2pThread != null)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.d(tag, "p2pManager stop");
                    ((P2PHandler) p2pThread.getLooperHandler()).release();
                    p2pThread.quit();
                    p2pThread = null;
                    p2PHandler = null;
                }
            }).start();
        }
    }

    public void cancelReceive()
    {
        p2PHandler.send2Handler(P2PConstant.CommandNum.RECEIVE_ABORT_SELF,
            P2PConstant.Src.MANAGER, P2PConstant.Dst.FILE_RECEIVE, null);
    }

    public void cancelSend(P2PNeighbor neighbor)
    {
        p2PHandler.send2Handler(P2PConstant.CommandNum.SEND_ABORT_SELF,
            P2PConstant.Src.MANAGER, P2PConstant.Dst.FILE_SEND, neighbor);
    }

    public static String getSendPath()
    {
        return ROOT_SAVE_DIR
                + File.separator  + P2PConstant.FILE_SECRET_SEND_PATH;
    }

    public static String getSavePath(int type,P2PNeighbor neighbor)
    {
        String[] typeStr = {P2PConstant.FILE_SECRET_RECEIVE_PATH};
        return ROOT_SAVE_DIR + File.separator + neighbor.alias + "@" + neighbor.ip + File.separator + typeStr[type];
    }


    /**
     * 获取广播地址
     */
    public static InetAddress getBroadcastAddress(Context context)
            throws UnknownHostException
    {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null)
        {
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    public static void setSaveDir(String dir)
    {
        ROOT_SAVE_DIR = dir;
    }

    public static String getSaveDir()
    {
        return ROOT_SAVE_DIR;
    }

    private static class P2PManagerHandler extends Handler
    {
        private WeakReference<P2PManager> weakReference;

        public P2PManagerHandler(P2PManager manager)
        {
            this.weakReference = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg)
        {
            P2PManager manager = weakReference.get();
            if (manager == null)
                return;

            switch (msg.what)
            {
                case P2PConstant.UI_MSG.ADD_NEIGHBOR :
                    if (manager.melon_callback != null)
                        manager.melon_callback.Melon_Found((P2PNeighbor) msg.obj);
                    break;
                case P2PConstant.UI_MSG.REMOVE_NEIGHBOR :
                    if (manager.melon_callback != null)
                        manager.melon_callback.Melon_Removed((P2PNeighbor) msg.obj);
                    break;
                case P2PConstant.CommandNum.SEND_FILE_REQ : //收到请求发送文件
                    if (manager.receiveFile_callback != null)
                    {
                        ParamReceiveFiles params = (ParamReceiveFiles) msg.obj;
                        manager.receiveFile_callback.QueryReceiving(params.Neighbor,
                            params.Files);
                    }
                    break;
                case P2PConstant.CommandNum.SEND_FILE_START : //发送端开始发送
                    if (manager.sendFile_callback != null)
                    {
                        manager.sendFile_callback.BeforeSending();
                    }
                    break;
                case P2PConstant.CommandNum.SEND_PERCENTS :
                    ParamTCPNotify notify = (ParamTCPNotify) msg.obj;
                    if (manager.sendFile_callback != null)
                        manager.sendFile_callback.OnSending((P2PFileInfo) notify.Obj,
                            notify.Neighbor);
                    break;
                case P2PConstant.CommandNum.SEND_OVER :
                    if (manager.sendFile_callback != null)
                        manager.sendFile_callback.AfterSending((P2PNeighbor) msg.obj);
                    break;
                case P2PConstant.CommandNum.SEND_ABORT_SELF : //通知接收者，发送者退出了
                    if (manager.receiveFile_callback != null)
                    {
                        ParamIPMsg paramIPMsg = (ParamIPMsg) msg.obj;
                        if (paramIPMsg != null)
                            manager.receiveFile_callback.AbortReceiving(
                                P2PConstant.CommandNum.SEND_ABORT_SELF,
                                paramIPMsg.peerMSG.senderAlias);
                    }
                    break;
                case P2PConstant.CommandNum.RECEIVE_ABORT_SELF : //通知发送者，接收者退出了
                    if (manager.sendFile_callback != null)
                        manager.sendFile_callback.AbortSending(msg.what,
                            (P2PNeighbor) msg.obj);
                    break;
                case P2PConstant.CommandNum.RECEIVE_OVER :
                    if (manager.receiveFile_callback != null)
                        manager.receiveFile_callback.AfterReceiving();
                    break;
                case P2PConstant.CommandNum.RECEIVE_PERCENT :
                    if (manager.receiveFile_callback != null)
                        manager.receiveFile_callback.OnReceiving((P2PFileInfo) msg.obj);
                    break;
            }
        }
    }

}
