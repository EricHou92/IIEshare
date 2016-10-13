package com.hou.p2pmanager.p2pcore.send;


import android.util.Log;

import com.hou.p2pmanager.p2putils.P2PConstant;
import com.hou.p2pmanager.p2pcore.P2PHandler;
import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;
import com.hou.p2pmanager.p2pentity.SocketTransInfo;
import com.hou.p2pmanager.p2pentity.param.ParamTCPNotify;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by ciciya on 2015/9/22.
 */
public class SendTask extends OneByOneRunnable
{
    private static final String tag = SendTask.class.getSimpleName();

    public final static int TRANS_START = 1;
    public final static int TRANS_OVER = 2;

    private Sender sender;
    private SocketChannel socketChannel; //与客户端通信的通道
    private P2PHandler p2PHandler;
    private P2PNeighbor neighbor;
    private SocketTransInfo socketTransInfo;
    private P2PFileInfo p2PFileInfo;
    private long lastTransferred;
    private int step;
    private RandomAccessFile randomAccessFile = null;
    private FileChannel fileChannel;
    private MappedByteBuffer mappedByteBuffer = null;
    private Thread thread;
    private boolean idle;//空闲
    protected boolean finished = false;

    public SendTask(Sender sender, SocketChannel socketChannel)
    {
        this.p2PHandler = sender.p2PHandler;
        this.sender = sender;
        this.neighbor = sender.neighbor;
        this.socketChannel = socketChannel;
    }

    @Override
    public void run()
    {
        Log.d(tag, "send task run function");
        super.pause();
        thread = Thread.currentThread();

        int len = 0;
        idle = false;

        while (!idle)
        {
            if (Thread.interrupted())
            {
                release();
                break;
            }
            try
            {
                len = socketChannel.write(mappedByteBuffer);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                notifySender(P2PConstant.CommandNum.SEND_LINK_ERROR, null);
                release();
            }

            socketTransInfo.Transferred += len;
            socketTransInfo.Length -= len;
            socketTransInfo.Offset += len;

            if (socketTransInfo.Length == 0)
            {
                idle = true;
                try
                {
                    randomAccessFile.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                randomAccessFile = null;
            }

            if ((socketTransInfo.Transferred - lastTransferred) > step || idle)
            {
                lastTransferred = socketTransInfo.Transferred;
                if (sender.flagPercents == false || idle)
                {
                    sender.flagPercents = true;
                    notifySender(P2PConstant.CommandNum.SEND_PERCENTS, socketTransInfo);
                }
            }

        }// end of while

        if (Thread.interrupted())
            release();

        super.resume();
    }

    public int prepare()
    {
        Log.d(tag, "send task prepare function");

        socketTransInfo = new SocketTransInfo(sender.index);
        p2PFileInfo = sender.files[sender.index];
        p2PFileInfo.LengthNeeded = p2PFileInfo.size;
        socketTransInfo.Length = p2PFileInfo.size;
        socketTransInfo.Offset = 0;
        lastTransferred = 0;

        try
        {
            randomAccessFile = new RandomAccessFile(new File(p2PFileInfo.path), "r");

           /* //移动文件指定的位置开始写入数据
            File fileLog = new File(P2PManager.ROOT_SAVE_DIR, "sendLog.txt");
            if(fileLog.exists()) {
                Properties properties = new Properties();
                FileReader fileReader = new FileReader(fileLog);
                properties.load(fileReader);
                Integer transferred = Integer.valueOf(properties.getProperty("transferred"));
                randomAccessFile.seek(transferred);
            }*/

            fileChannel = randomAccessFile.getChannel();
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,
                socketTransInfo.Offset, socketTransInfo.Length); //将文件映射到内存
            step = (int) ((float) p2PFileInfo.size / 100 + 0.5);

            return TRANS_START;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return TRANS_OVER;
        }
    }

    public void notifySender(int cmd, Object obj)
    {
        ParamTCPNotify notify = new ParamTCPNotify(neighbor, obj);
        if (!finished)
        {
            if (p2PHandler != null)
                p2PHandler.send2Handler(cmd, P2PConstant.Src.SEND_TCP_THREAD,
                    P2PConstant.Dst.FILE_SEND, notify);
        }
    }

    public void quit()
    {
        if (thread != null && thread.isAlive())
            thread.interrupt();
        else
            release();
    }

    private synchronized void release()
    {
        if (!finished)
        {
            if (socketChannel != null)
            {
                try
                {
                    socketChannel.socket().close();
                    socketChannel.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (randomAccessFile != null)
            {
                try
                {
                    randomAccessFile.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (fileChannel != null)
            {
                try
                {
                    fileChannel.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            finished = true;
        }
    }

}
