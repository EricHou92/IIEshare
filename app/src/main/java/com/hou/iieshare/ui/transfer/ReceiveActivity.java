package com.hou.iieshare.ui.transfer;

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

import com.hou.iieshare.R;
import com.hou.iieshare.MyApplication;
import com.hou.iieshare.constant.Constant;
import com.hou.iieshare.sdk.accesspoint.AccessPointManager;
import com.hou.iieshare.sdk.cache.Cache;
import com.hou.iieshare.ui.common.BaseActivity;
import com.hou.iieshare.ui.view.CommonProgressDialog;
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

    private AccessPointManager mWifiApManager = null;
    private Random random = new Random();
    private CommonProgressDialog progressDialog;
    private TextView wifiName;
    private P2PManager p2PManager;
    private String receive_alias;
    public RelativeLayout receiveLayout;
    private ListView receiveListView;
    private FileTransferAdapter transferAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
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


        //如果没有wifi，建立热点
        if (!NetworkUtils.isWifiConnected(MyApplication.getInstance()))
        { //create wifi hot spot
            Log.d(tag, "no WiFi init wifi hotspot");
            intWifiHotSpot();
        }
        else
        {
            Log.d(tag, "useWiFi");
            wifiName.setText(String.format(getString(R.string.send_connect_to),
                    NetworkUtils.getCurrentSSID(ReceiveActivity.this)));
        }

        //initP2P();

        /*//修改
        initP2P();
        receiveLayout.setVisibility(View.GONE);
        receiveListView.setVisibility(View.VISIBLE);

        p2PManager.ackReceive();*/

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
        //stop wifi hot spot
        closeAccessPoint();
        //if (rippleOutLayout != null)
        //    rippleOutLayout.stopRippleAnimation();

        if (p2PManager != null)
        {
            p2PManager.cancelReceive();
            p2PManager.stop();
        }

        Cache.selectedList.clear();
    }


    private void intWifiHotSpot()
    {
        progressDialog = new CommonProgressDialog(ReceiveActivity.this);
        progressDialog.setMessage(getString(R.string.wifi_hotspot_creating));
        progressDialog.show();

        mWifiApManager = new AccessPointManager(MyApplication.getInstance());
        mWifiApManager.setWifiApStateChangeListener(this);
        createAccessPoint();
    }

    private void createAccessPoint()
    {
        mWifiApManager.createWifiApSSID(Constant.WIFI_HOT_SPOT_SSID_PREFIX
                + android.os.Build.MODEL + "-" + random.nextInt(1000));

        if (!mWifiApManager.startWifiAp())
        {
            if (progressDialog != null)
                progressDialog.dismiss();

            ToastUtils.showTextToast(MyApplication.getInstance(),
                    getString(R.string.wifi_hotspot_fail));
            onBackPressed();
        }
    }

    private void closeAccessPoint()
    {
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
        if (state == AccessPointManager.WIFI_AP_STATE_ENABLED)
        {
            onBuildWifiApSuccess();
        }
        else if (AccessPointManager.WIFI_AP_STATE_FAILED == state)
        {
            onBuildWifiApFailed();
        }
    }

    private void onBuildWifiApFailed()
    {
        ToastUtils.showTextToast(MyApplication.getInstance(),
                getString(R.string.wifi_hotspot_fail));

        if (progressDialog != null)
            progressDialog.dismiss();

        onBackPressed();
    }

    private void onBuildWifiApSuccess()
    {
        if (progressDialog != null)
            progressDialog.dismiss();

        wifiName.setText(String.format(getString(R.string.send_connect_to),
                mWifiApManager.getWifiApSSID()));
    }

}
