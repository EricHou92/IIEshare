package com.hou.p2pmanager.p2pcore.receive;


import com.hou.p2pmanager.p2putils.P2PConstant;
import com.hou.p2pmanager.p2pcore.P2PHandler;
import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;
import com.hou.p2pmanager.p2pentity.param.ParamIPMsg;
import com.hou.p2pmanager.p2pentity.param.ParamReceiveFiles;

/**
 * Created by ciciya on 2016/8/6.
 */
public class ReceiveManager
{

    protected P2PHandler p2PHandler;
    private Receiver receiver;

    public ReceiveManager(P2PHandler handler)
    {
        p2PHandler = handler;
    }

    public void init()
    {
        if (receiver != null)
            receiver = null;
    }

    public void disPatchMsg(int cmd, int src, Object obj)
    {
        switch (src)
        {
            case P2PConstant.Src.COMMUNICATE :
            {
                ParamIPMsg paramIPMsg = (ParamIPMsg) obj;
                if (cmd == P2PConstant.CommandNum.SEND_FILE_REQ)
                {
                    invoke(paramIPMsg);
                }
                else
                {
                    if (receiver != null)
                        receiver.dispatchCommMSG(cmd, paramIPMsg);
                }
                break;
            }
            case P2PConstant.Src.MANAGER :
                if (receiver != null)
                    receiver.dispatchUIMSG(cmd, obj);
                break;
            case P2PConstant.Src.RECEIVE_TCP_THREAD :
                if (cmd == P2PConstant.CommandNum.RECEIVE_PERCENT)
                    if (receiver != null)
                        receiver.flagPercent = false;
                if (receiver != null)
                    receiver.dispatchTCPMSG(cmd, obj);
                break;
        }
    }

    public void quit()
    {
        init();
    }

    private void invoke(ParamIPMsg paramIPMsg)
    {
        String peerIP = paramIPMsg.peerIAddr.getHostAddress();
        //P2PNeighbor neighbor = receiver.neighbor;
        P2PNeighbor neighbor = p2PHandler.getNeighborManager().getNeighbors().get(peerIP);

        String[] strArray = paramIPMsg.peerMSG.addition.split(P2PConstant.MSG_SEPARATOR);
        P2PFileInfo[] files = new P2PFileInfo[strArray.length];
        for (int i = 0; i < strArray.length; i++)
        {
            files[i] = new P2PFileInfo(strArray[i],neighbor);
        }

        receiver = new Receiver(this, neighbor, files);

        ParamReceiveFiles paramReceiveFiles = new ParamReceiveFiles(neighbor, files);
        if (p2PHandler != null)
            p2PHandler.send2UI(P2PConstant.CommandNum.SEND_FILE_REQ, paramReceiveFiles);
    }
}
