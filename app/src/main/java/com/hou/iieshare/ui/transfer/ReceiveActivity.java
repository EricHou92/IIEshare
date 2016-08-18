package com.hou.iieshare.ui.transfer;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hou.iieshare.MyApplication;
import com.hou.iieshare.R;
import com.hou.iieshare.constant.Constant;
import com.hou.iieshare.sdk.accesspoint.AccessPointManager;
import com.hou.iieshare.sdk.cache.Cache;
import com.hou.iieshare.ui.common.BaseActivity;
import com.hou.iieshare.utils.NetworkUtils;
import com.hou.iieshare.utils.ToastUtils;
import com.hou.p2pmanager.p2pconstant.P2PConstant;
import com.hou.p2pmanager.p2pcore.P2PManager;
import com.hou.p2pmanager.p2pentity.P2PFileInfo;
import com.hou.p2pmanager.p2pentity.P2PNeighbor;
import com.hou.p2pmanager.p2pinterface.Melon_Callback;
import com.hou.p2pmanager.p2pinterface.ReceiveFile_Callback;

import java.net.UnknownHostException;
import java.util.Random;

/**
 * Created by ciciya on 2016/8/11.
 */
public class ReceiveActivity extends BaseActivity
    implements AccessPointManager.OnWifiApStateChangeListener
{

    private static final String tag = ReceiveActivity.class.getSimpleName();
    private static final String SSID = "WifiHotSpot";
    private static final String PWD = "wen911021";

    private AccessPointManager mWifiApManager = null;
    private Random random = new Random();
    private TextView wifiName;
    private P2PManager p2PManager;
    private String receive_alias;
    public RelativeLayout receiveLayout;
    private ListView receiveListView;
    private FileTransferAdapter transferAdapter;
    private Context context = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_receive_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_receive_fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view,
                        getResources().getString(R.string.file_transfering_exit),
                        Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.ok),
                                new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        finish();
                                    }
                                }).show();
            }
        });

        Intent intent = getIntent();
        if (intent != null)
        {
            receive_alias = intent.getStringExtra("name");
        }
        else
            receive_alias = Build.DEVICE;

        //我要接收界面，下面的wifi的名字
        wifiName = (TextView) findViewById(R.id.activity_receive_radar_wifi);

        //未点击时，波纹显示，随机发送方名字
        receiveLayout = (RelativeLayout) findViewById(R.id.activity_receive_layout);
        receiveLayout.setVisibility(View.VISIBLE);
        //未点击时，正在接收，进度列表
        receiveListView = (ListView) findViewById(R.id.activity_receive_listview);
        receiveListView.setVisibility(View.GONE);

        Button testbutton = (Button) findViewById(R.id.activity_receive_testbutton);
        testbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiveLayout.setVisibility(View.GONE);
                receiveListView.setVisibility(View.VISIBLE);
                p2PManager.ackReceive();
            }
        });
        //testbutton.performClick();
        initP2P();

      /*  WifiApAdmin wifiAp = new WifiApAdmin(context);
        wifiAp.startWifiAp(SSID, PWD);*/

        //如果没有连接上wifi，建立wifi热点
        if (!NetworkUtils.isWifiConnected(MyApplication.getInstance()))
        {
            Log.d(tag, "no WiFi init wifi hotspot");
            intWifiHotSpot();
        }
        else
        {
            Log.d(tag, "useWiFi");
            wifiName.setText(String.format(getString(R.string.send_connect_to),
                    NetworkUtils.getCurrentSSID(ReceiveActivity.this)));
        }

    }

    private void initP2P()
    {
        p2PManager = new P2PManager(getApplicationContext());
        P2PNeighbor receive_melon = new P2PNeighbor();//接收方
        receive_melon.alias = receive_alias;//中间接收方的名字
        String ip = null;
        try
        {
            ip = AccessPointManager.getLocalIpAddress();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(ip))
            ip = NetworkUtils.getLocalIp(getApplicationContext());
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
        closeAccessPoint();
        //WifiApAdmin.closeWifiAp(context);

        if (p2PManager != null)
        {
            p2PManager.cancelReceive();
            p2PManager.stop();
        }

        Cache.selectedList.clear();
    }

    private void intWifiHotSpot()
    {
        mWifiApManager = new AccessPointManager(MyApplication.getInstance());
        mWifiApManager.setOnWifiApStateChangeListener(this);
        createAccessPoint();
    }

    private void createAccessPoint()
    {
        //创建wifi热点名字
        mWifiApManager.createWifiApSSID(Constant.WIFI_HOT_SPOT_SSID_PREFIX
                + Build.MODEL + "-" + random.nextInt(1000));
        //如果wifi热点未成功开启，弹出创建失败
        if (!mWifiApManager.startWifiAp())
        {
            ToastUtils.showTextToast(MyApplication.getInstance(),
                    getString(R.string.wifi_hotspot_fail));
            //后退
            onBackPressed();
        }
    }

    private void closeAccessPoint()
    {
        //关闭wifi热点
        try
        {
            if (mWifiApManager != null && mWifiApManager.isWifiApEnabled())
            {
                mWifiApManager.stopWifiAp(false);
                mWifiApManager.destroy(this);
            }
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onWifiStateChanged(int state)
    {
        //wifi状态改变
        if (state == AccessPointManager.WIFI_AP_STATE_ENABLED)
        {
            //onBuildWifiApSuccess();成功建立wifi热点，显示连接到。。。
            wifiName.setText(String.format(getString(R.string.send_connect_to),
                    mWifiApManager.getWifiApSSID()));
        }
        else if (state == AccessPointManager.WIFI_AP_STATE_FAILED)
        {
            //onBuildWifiApFailed();建立wifi热点失败，显示
            ToastUtils.showTextToast(MyApplication.getInstance(),
                    getString(R.string.wifi_hotspot_fail));
            onBackPressed();
        }
    }

}
