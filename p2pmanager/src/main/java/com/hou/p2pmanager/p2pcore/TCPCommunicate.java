package com.hou.p2pmanager.p2pcore;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hou.p2pmanager.p2pconstant.P2PConstant;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;
import com.hou.p2pmanager.p2pentity.SigMessage;
import com.hou.p2pmanager.p2pentity.param.ParamIPMsg;
import com.hou.p2pmanager.p2putils.RSAUtil;
import com.hou.p2pmanager.test.StreamTool;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by ciciya on 2016/8/27.
 * 接收端和发送端的tcp交互
 */
public class TCPCommunicate extends Thread {

    private static final String tag = TCPCommunicate.class.getSimpleName();

    private P2PHandler p2PHandler;
    private P2PManager p2PManager;
    private String[] mLocalIPs;
    private boolean isStopped = false;

    private Context mContext;
    private RSAUtil rsa = new RSAUtil();
    private ServerSocket serverSocket;
    private Socket receiveSocket;
    private Socket sendSocket;
    private String strReceive;

    public TCPCommunicate(P2PManager manager, P2PHandler handler, Context context) {
        mContext = context;
        this.p2PHandler = handler;
        this.p2PManager = manager;
        setPriority(MAX_PRIORITY);
        init();
    }

    private void init() {
        mLocalIPs = getLocalAllIP();
        try {
            serverSocket = new ServerSocket(P2PConstant.PORT);
            Log.d(tag,"接收方serverSocket建立");
            /*serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(P2PConstant.PORT));//将端口号和socket绑定*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        isStopped = false;
    }

    @Override
    public void run() {
        while (!isStopped) {
            try {
                //开始接收数据包,响应客户端
                receiveSocket = serverSocket.accept();//此语句未运行
                Log.d(tag,"接收方等待连接");
                PushbackInputStream inStream = new PushbackInputStream(
                        receiveSocket.getInputStream());
                strReceive = StreamTool.readLine(inStream);
                System.out.println("客户端消息：" + strReceive);
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                isStopped = true;
                break;
            }

            String ip = receiveSocket.getInetAddress().getHostAddress();
            if (!TextUtils.isEmpty(ip)) {
                if (!isLocal(ip)) //自己会收到自己的广播消息，进行过滤
                {
                    if (!isStopped) {
                        Log.d(tag, "sig communicate process received udp message = "
                                + strReceive);
                        ParamIPMsg msg = new ParamIPMsg(
                                strReceive, receiveSocket.getInetAddress(), receiveSocket.getPort());

                        p2PHandler.send2Handler(
                                msg.peerMSG.commandNum, P2PConstant.Src.COMMUNICATE,
                                msg.peerMSG.recipient, msg);
                    } else {
                        break;
                    }
                }
            } else {
                isStopped = true;
                break;
            }
        }
        release();
    }


    //将自己的信息广播出去
    public void BroadcastMSG(int cmd, int recipient) {
        try {
            /* InetAddress.getByName(P2PConstant.MULTI_ADDRESS) */
            sendMsg2Peer(P2PManager.getBroadcastAddress(mContext), cmd, recipient, null);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg2Peer(InetAddress sendTo, int cmd, int recipient, String add) {
        SigMessage sigMessage = getSelfMsg(cmd);
        if (add == null)
            sigMessage.addition = "null";
        else
            sigMessage.addition = add;
        sigMessage.recipient = recipient;

        sendUdpData(sigMessage.toProtocolString(), sendTo);
    }

   /* Socket tcp发送函数
    必须先运行服务器再运行客户端，次序不能颠倒，
    也就是必须先有接收的，再有发送的，
    不然发送的地方没有接收的，肯定会出Connection refused: connect 之类的错误*/
    private synchronized void sendUdpData( String sendStr, InetAddress sendTo) {
                try {
                    Log.d(tag, "send udp data = " + sendStr + "; send to = " + sendTo.getHostAddress());
                    sendSocket = new Socket(sendTo.getHostAddress(),P2PConstant.PORT);//此语句未执行
                    Log.d(tag,"发送socket建立");
                    //输出到服务端
                    OutputStream outStream = sendSocket.getOutputStream();
                    outStream.write(sendStr.getBytes());

                } catch (IOException e) {
                    e.printStackTrace();
                }
    }


    public void quit() {
        isStopped = true;
        release();
    }

    private void release() {
        Log.d(tag, "sigCommunicate release");
            try {
                if (receiveSocket != null)
                receiveSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private SigMessage getSelfMsg(int cmd) {
        SigMessage msg = new SigMessage();
        msg.commandNum = cmd;
        P2PNeighbor melonInfo = p2PManager.getSelfMeMelonInfo();
        if (melonInfo != null) {
            msg.senderAlias = melonInfo.alias;
            msg.senderImei = melonInfo.imei;
            msg.senderIp = melonInfo.ip;
        }
        return msg;
    }

    private boolean isLocal(String ip) {
        for (int i = 0; i < mLocalIPs.length; i++) {
            if (ip.equals(mLocalIPs[i]))
                return true;
        }
        return false;
    }

    private String[] getLocalAllIP() {
        ArrayList<String> IPs = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()
                            && ip instanceof Inet4Address) {
                        IPs.add(ip.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return IPs.toArray(new String[]{});
    }

}