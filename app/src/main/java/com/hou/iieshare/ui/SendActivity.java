package com.hou.iieshare.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.hou.iieshare.R;
import com.hou.iieshare.utils.Cache;
import com.hou.iieshare.utils.NetworkUtils;
import com.hou.iieshare.utils.ToastUtils;
import com.hou.p2pmanager.p2pconstant.P2PConstant;
import com.hou.p2pmanager.p2pcore.P2PManager;
import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;
import com.hou.p2pmanager.p2pinterface.Melon_Callback;
import com.hou.p2pmanager.p2pinterface.SendFile_Callback;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SendActivity extends AppCompatActivity {

    private static final String tag = SendActivity.class.getSimpleName();

    private P2PManager p2PManager;
    private String send_alias;
    private ListView fileSendListView;
    private List<P2PNeighbor> neighbors = new ArrayList<>();//文件的接受者列表
    private P2PNeighbor curNeighbor;
    private FileTransferAdapter transferAdapter;
    private P2PNeighbor add_neighbor;
    private String send_Imei;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        send_alias = Build.DEVICE;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        send_Imei = tm.getDeviceId();

        //自定义扫描文件夹，并发送
        getFiles(Cache.selectedList,P2PManager.getSendPath());

        initP2P();

        //接收方点击后，正在发送，进度列表消失
        fileSendListView = (ListView) findViewById(R.id.activity_send_listview);
        if (fileSendListView != null) {
            fileSendListView.setVisibility(View.GONE);
        }

        sendButton = (Button) findViewById(R.id.activity_send_testbutton);
        if (sendButton != null) {
            sendButton.setVisibility(View.VISIBLE);
        }
        sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendButton.setVisibility(View.GONE);
                    for (int i = 0; i < neighbors.size(); i++)
                    {
                        if (neighbors.get(i).alias.equals(add_neighbor.alias))
                        {
                            curNeighbor = neighbors.get(i);
                            SendFile(curNeighbor);
                            break;
                        }
                    }
                }
            });
        //testbutton.performClick();
    }


    /**自定义文件夹扫描
     * @param fileList
     * @param path
     */
    private void getFiles(List<P2PFileInfo> fileList, String path)
    {
        Log.d(tag, "机密发送文件夹创建");
        File desDir = new File(path);
        if (!desDir.exists())
        {
            desDir.mkdirs();
        }
        File[] allFiles = desDir.listFiles();
        for (int i = 0; i < allFiles.length; i++)
        {
            if (!allFiles[i].isDirectory())
            {
                File file = allFiles[i];
                if (file.isFile())
                {
                    P2PFileInfo msg = new P2PFileInfo();
                    msg.name = file.getName();
                    msg.size = file.length();
                    msg.path = file.getPath();
                    fileList.add(msg);
                }
                else if (!file.getAbsolutePath().contains(".thumnail"))
                {
                    getFiles(fileList, file.getAbsolutePath());
                }
            }
            else
            {
                File[] allFiles2 = allFiles[i].listFiles();
                for (int j = 0; j < allFiles2.length; j++) {
                    File file = allFiles2[j];
                    if (file.isFile()) {
                        P2PFileInfo msg = new P2PFileInfo();
                        msg.name = file.getName();
                        msg.size = file.length();
                        msg.path = file.getPath();
                        fileList.add(msg);
                    }
                    else if (!file.getAbsolutePath().contains(".thumnail"))
                    {
                        getFiles(fileList, file.getAbsolutePath());
                    }
                }
            }
        }
    }


    //发送初始化，进行端到端连接的初始化工作
    private void initP2P()
    {
        p2PManager = new P2PManager(getApplicationContext());
        P2PNeighbor send_melon = new P2PNeighbor();//发送方
        send_melon.alias = send_alias;//发送方的别名
        send_melon.imei = send_Imei;//发送方Imei
        System.out.println("发送方Imei值" + send_Imei);
        String ip = null;
        try
        {
            ip = NetworkUtils.getLocalIpAddress();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        send_melon.ip = ip;//发送方IP

        //调用start方法
        p2PManager.start(send_melon, new Melon_Callback()
        {
            @Override
            public void Melon_Found(P2PNeighbor neighbor)
            {
                if (neighbor != null)
                {
                    if (!neighbors.contains(neighbor))
                        neighbors.add(neighbor);
                }
                add_neighbor = neighbor;
            }

            @Override
            public void Melon_Removed(P2PNeighbor neighbor)
            {
                if (neighbor != null)
                {
                    neighbors.remove(neighbor);
                }
            }
        });
    }

    /**调用发送文件的方法
     */
    private void SendFile(P2PNeighbor neighbor)
    {
        P2PNeighbor[] neighbors = new P2PNeighbor[]{neighbor};//文件接收者
        P2PFileInfo[] fileArray = new P2PFileInfo[Cache.selectedList.size()];
        //待发送的文件循环遍历
        for (int i = 0; i < Cache.selectedList.size(); i++)
        {
            fileArray[i] = Cache.selectedList.get(i);
        }

        //调用发送文件函数
        p2PManager.sendFile(neighbors, fileArray, new SendFile_Callback()
        {
            @Override
            public void BeforeSending()
            {
                //正在发送，进度列表显示
                fileSendListView.setVisibility(View.VISIBLE);
                transferAdapter = new FileTransferAdapter(getApplicationContext());
                fileSendListView.setAdapter(transferAdapter);

                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null)
                    actionBar.setTitle(getString(R.string.file_sending));
            }

            @Override
            public void OnSending(P2PFileInfo file, P2PNeighbor dest)
            {
                Log.d(tag, "onSending file percent = " + file.percent);

                int index = -1;
                if (Cache.selectedList.contains(file))
                {
                    index = Cache.selectedList.indexOf(file);
                }
                if (index != -1)
                {
                    P2PFileInfo fileInfo = Cache.selectedList.get(index);
                    fileInfo.percent = file.percent;
                    transferAdapter.notifyDataSetChanged();
                }
                else
                {
                    Log.d(tag, "onSending index error");
                }
            }

            @Override
            //发送结束
            public void AfterSending(P2PNeighbor dest)
            {
                ToastUtils.showTextToast(getApplicationContext(),
                        getString(R.string.file_send_complete));
                //增加发送完后自动删除
                File file = new File(P2PManager.getSendPath());
                //DeviceUtils.deleteFile(file);
                finish();
            }

            @Override
            //发送全部结束
            public void AfterAllSending()
            {
                ToastUtils.showTextToast(getApplicationContext(),
                        getString(R.string.file_send_complete));
                finish();
            }

            @Override
            //中断传输
            public void AbortSending(int error, P2PNeighbor dest)
            {
                String format = getString(R.string.send_abort_self);
                String toastMsg = "";
                switch (error)
                {
                    case P2PConstant.CommandNum.RECEIVE_ABORT_SELF :
                        toastMsg = String.format(format, dest.alias);
                        break;
                }

                ToastUtils.showTextToast(getApplicationContext(), toastMsg);

                finish();
            }
        });
    }



    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (p2PManager != null)
        {
            if (curNeighbor != null)
                p2PManager.cancelSend(curNeighbor);
            p2PManager.stop();
        }
        for (int i = 0; i < Cache.selectedList.size(); i++)
        {
            Cache.selectedList.get(i).percent = 0;
        }
    }

}
