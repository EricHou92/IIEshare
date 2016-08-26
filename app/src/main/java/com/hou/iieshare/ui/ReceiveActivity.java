package com.hou.iieshare.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hou.iieshare.R;
import com.hou.iieshare.utils.Cache;
import com.hou.iieshare.utils.NetworkUtils;
import com.hou.iieshare.utils.ToastUtils;
import com.hou.iieshare.utils.WifiAp;
import com.hou.p2pmanager.p2pconstant.P2PConstant;
import com.hou.p2pmanager.p2pcore.P2PManager;
import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;
import com.hou.p2pmanager.p2pinterface.Melon_Callback;
import com.hou.p2pmanager.p2pinterface.ReceiveFile_Callback;

import java.net.UnknownHostException;

/**
 * Created by ciciya on 2016/8/11.
 */
public class ReceiveActivity extends AppCompatActivity
{

    private static final String tag = ReceiveActivity.class.getSimpleName();
    private static final String SSID = "WifiHotSpot";
    private static final String PWD = "root1234";

    private TextView wifiName;
    private P2PManager p2PManager;
    private String receive_alias;
    private ListView receiveListView;
    private FileTransferAdapter transferAdapter;
    private Context context = null;
    private String receive_Imei;
    private Button receiveButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        context = this;

        receive_alias = Build.DEVICE;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        receive_Imei = tm.getDeviceId();

        //下面的wifi的名字
        wifiName = (TextView) findViewById(R.id.activity_receive_radar_wifi);

        //进度列表
        receiveListView = (ListView) findViewById(R.id.activity_receive_listview);
        if (receiveListView != null) {
            receiveListView.setVisibility(View.GONE);
        }

        initP2P();

        Log.d(tag, "init wifi hotspot");
        WifiAp wifiAp = new WifiAp(context);
        wifiAp.startWifiAp(SSID, PWD);
        /*wifiName.setText(String.format(getString(R.string.send_connect_to),
                NetworkUtils.getCurrentSSID(context)));*/

        receiveButton = (Button) findViewById(R.id.activity_receive_button);
        if (receiveButton != null) {
            receiveButton.setVisibility(View.VISIBLE);
        }
        receiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    receiveButton.setVisibility(View.GONE);
                    receiveListView.setVisibility(View.VISIBLE);
                    p2PManager.ackReceive();
                }
            });
        //testbutton.performClick();
    }

    private void initP2P()
    {
        p2PManager = new P2PManager(getApplicationContext());
        P2PNeighbor receive_melon = new P2PNeighbor();//接收方
        receive_melon.alias = receive_alias;//中间接收方的名字
        receive_melon.imei = receive_Imei;
        System.out.println("接收方Imei值" + receive_Imei);
        String ip = null;
        try
        {
            ip = NetworkUtils.getLocalIpAddress();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        /*if (TextUtils.isEmpty(ip))
            ip = NetworkUtils.getLocalIp(getApplicationContext());*/
        receive_melon.ip = ip;

        p2PManager.start(receive_melon, new Melon_Callback()
        {
            @Override
            public void Melon_Found(P2PNeighbor melon)
            {
            }

            @Override
            public void Melon_Removed(P2PNeighbor melon)
            {
            }
        });



        p2PManager.receiveFile(new ReceiveFile_Callback()
        {
            @Override
            public boolean QueryReceiving(P2PNeighbor src, P2PFileInfo[] files)
            {
                //如果发送方不为空
                if (src != null)
                {
                    //将要接收的文件加入到Cache中
                    if (files != null)
                    {
                        for (P2PFileInfo file : files)
                        {
                            if (!Cache.selectedList.contains(file))
                                Cache.selectedList.add(file);
                        }
                        transferAdapter = new FileTransferAdapter(getApplicationContext());
                        receiveListView.setAdapter(transferAdapter);
                    }
                }
                return false;
            }

            @Override
            public void BeforeReceiving(P2PNeighbor src, P2PFileInfo[] files)
            {

            }

            @Override
            public void OnReceiving(P2PFileInfo file)
            {
                //默认Cache里没有file
                int index = -1;
                //若有file，返回第一次出现的index
                if (Cache.selectedList.contains(file))
                {
                    index = Cache.selectedList.indexOf(file);
                }
                //若有file,返回其传输百分比
                if (index != -1)
                {
                    P2PFileInfo fileInfo = Cache.selectedList.get(index);
                    fileInfo.percent = file.percent;
                    transferAdapter.notifyDataSetChanged();
                }
                else
                {
                    Log.d(tag, "onReceiving index error");
                }
            }

            @Override
            public void AfterReceiving()
            {
                ToastUtils.showTextToast(getApplicationContext(),
                        getString(R.string.file_receive_completed));
                finish();
            }

            @Override
            public void AbortReceiving(int error, String name)
            {
                //中断接收文件
                switch (error)
                {
                    case P2PConstant.CommandNum.SEND_ABORT_SELF :
                        ToastUtils.showTextToast(getApplicationContext(),
                                String.format(getString(R.string.send_abort_self), name));
                        finish();
                        break;
                }
            }
        });
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        WifiAp.closeWifiAp(context);

        if (p2PManager != null)
        {
            p2PManager.cancelReceive();
            p2PManager.stop();
        }
        Cache.selectedList.clear();
    }

}
