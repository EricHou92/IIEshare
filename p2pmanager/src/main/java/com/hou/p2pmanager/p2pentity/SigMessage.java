package com.hou.p2pmanager.p2pentity;


import com.hou.p2pmanager.p2pconstant.P2PConstant;

import java.util.Date;


/**
 * Created by ciciya on 2015/9/17.
 * 局域网用户之间的upd消息
 * 自定义消息格式设置
 */
public class SigMessage
{
    /**
     * 发送包的编号 时间即编号
     */
    public String packetNum;

    /**
     * 发送者的昵称
     */
    public String senderAlias;

    /**
     * 发送者Imei
     */
    public String senderImei;

    public String senderPath;

    /**
     * 发送者的ip地址
     */
    public String senderIp;

    /**
     *发送者的消息处理方式编号
     */
    public int commandNum;

    /**
     * 接收者处理方式编号
     */
    public int recipient;

    /**
     * 发送的具体内容
     */
    public String addition;

    public SigMessage()
    {
        this.packetNum = getTime();
    }

    public SigMessage(String protocolString)
    {
        protocolString = protocolString.trim();
        String[] args = protocolString.split(":");

        packetNum = args[0];
        senderAlias = args[1];
        senderImei = args[2];
        senderIp = args[3];
        commandNum = Integer.parseInt(args[4]);
        recipient = Integer.parseInt(args[5]);
        if (args.length > 6)
            addition = args[6];
        else
            addition = null;

        for (int i = 7; i < args.length; i++)
        {
            addition += (":" + args[i]);
        }
    }

    public String toProtocolString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(packetNum);
        sb.append(":");
        sb.append(senderAlias);
        sb.append(":");
        sb.append(senderImei);
        sb.append(":");
        sb.append(senderIp);
        sb.append(":");
        sb.append(commandNum);
        sb.append(":");
        sb.append(recipient);
        sb.append(":");
        sb.append(addition);
        sb.append(P2PConstant.MSG_SEPARATOR);

        return sb.toString();
    }

    private String getTime()
    {
        Date nowDate = new Date();
        return Long.toString(nowDate.getTime());
    }
}
