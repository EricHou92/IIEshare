package com.hou.iieshare.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * Created by dong on 2016/8/19.
 */
public class Wifiadmin {
    private final static String TAG = "Wifiadmin";
    // 定义WifiManager对象
    private static WifiManager mWifiManager;
    // 定义WifiInfo对象
    private WifiInfo mWifiInfo;
    // 扫描出的网络连接列表
    private static List<ScanResult> mWifiList;
    // 网络连接列表
    private static List<WifiConfiguration> mWifiConfiguration;
    // 定义一个WifiLock,能够阻止wifi进入睡眠状态，使wifi一直处于活跃状态
    WifiManager.WifiLock mWifiLock;
    private StringBuilder stringBuilder = new StringBuilder();
    private int paramInt;
    public static String[] str;
    private static ProgressDialog progressDialog;
    private static Context context;
    private String SSID;
    private WifiConfiguration mConfig;
    private List<String> Hotpot;
    public static final int WIFI_AP_STATE_DISABLING = 0;
    public static final int WIFI_AP_STATE_DISABLED = 1;
    public static final int WIFI_AP_STATE_ENABLING = 2;
    public static final int WIFI_AP_STATE_ENABLED = 3;
    public static final int WIFI_AP_STATE_FAILED = 4;

    // 构造器
    public Wifiadmin(Context context) {
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    /**
     * 是否存在网络信息
     *
     * @return
     */

    // 打开WIFI
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    // 关闭WIFI
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    // 检查当前WIFI状态
    public void checkState() {

        if (mWifiManager.getWifiState() == 0) {
           Toast.makeText(context, "网卡正在关闭", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == 1) {
            Toast.makeText(context, "网卡已经关闭", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == 2) {
            Toast.makeText(context, "网卡正在打开", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == 3) {
            Toast.makeText(context, "网卡已经打开", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "没有获得状态", Toast.LENGTH_SHORT).show();;
        }

    }
    //判断网络连接状态
   public boolean checkNetworkState() {
        boolean flag = false;
        //得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            flag = manager.getActiveNetworkInfo().isAvailable();
        }
        if (!flag) {
            Toast.makeText(context,"重连接网络", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context,"判断网络类型", Toast.LENGTH_SHORT).show();
        }

        return flag;
    }

public int connectwifi(){
    WifiInfo info = mWifiManager.getConnectionInfo();
    if (info.getBSSID() != null) {
        int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);

			int speed = info.getLinkSpeed();
		    String units = WifiInfo.LINK_SPEED_UNITS;
			String ssid = info.getSSID();
           return strength;

    }
    return 0;
}

    // 锁定WifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 解锁WifiLock
    public void releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    // 创建一个WifiLock
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("");
    }

    // 得到配置好的网络
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    // 指定配置好的网络进行连接
    public void connectConfiguration(int index, String wen911021) {
        // 索引大于配置好的网络索引返回
        if (index >= mWifiConfiguration.size()) {
            return;
        }
        // 连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
                true);
    }


    public  static String getSSID() {

        Date date=new Date();
        String a= String.format("%te", date);

        String b= String.format("%tY", date);

        String c= String.format("%tj", date);
        String d= String.format("%tm", date);

        String e= String.format("%td", date);
        String f= String.format("%ty", date);
        String h= String.format("%tH", date);

        String ssid;
        return ssid=a+b+c+d+e+f+h;
    }


    // 得到网络列表
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }

    // 查看扫描结果
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder
                    .append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }



        // 添加一个网络并连接
        public void addNetwork(WifiConfiguration wcg) {
            int wcgID = mWifiManager.addNetwork(wcg);
            mWifiManager.enableNetwork(wcgID, true);
        }


        // 得到MAC地址
        public String getMacAddress() {
            return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
        }

        // 得到接入点的BSSID
        public String getBSSID() {
            return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
        }

        // 得到IP地址
        public Serializable getIPAddress() {
            return (mWifiInfo == null) ? 0 : intToIp(mWifiInfo.getIpAddress());
        }

        public String intToIp(int i) {
            return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                    + "." + ((i >> 24) & 0xFF);
        }

        // 得到连接的ID
        public int getNetworkId() {
            return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
        }

        // 得到WifiInfo的所有信息包
        public String getWifiInfo() {
            return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
        }
       //返回热点是否可用
    public boolean isApEnabled(Context mContext) {
        int state = getWifiApState(mContext);
        return WIFI_AP_STATE_ENABLING == state || WIFI_AP_STATE_ENABLED == state;
    }

    public int getWifiApState(Context mContext) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            int i = (Integer) method.invoke(wifiManager);
            return i;
        } catch (Exception e) {
            return WIFI_AP_STATE_FAILED;
        }
    }
    // 断开指定ID的网络
        public void disconnectWifi(int netId) {
            mWifiManager.disableNetwork(netId);
            mWifiManager.disconnect();
            mWifiInfo = null;
        }

        public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
            WifiConfiguration config = new WifiConfiguration();
            config.allowedAuthAlgorithms.clear();
            config.allowedGroupCiphers.clear();
            config.allowedKeyManagement.clear();
            config.allowedPairwiseCiphers.clear();
            config.allowedProtocols.clear();
            config.SSID = "\"" + SSID + "\"";
            WifiConfiguration tempConfig = this.IsExsits(SSID);
            if (tempConfig != null) {
                mWifiManager.removeNetwork(tempConfig.networkId);
            }

            if (Type == 1) //WIFICIPHER_NOPASS
            {
                config.wepKeys[0] = "";
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
            }
            if (Type == 2) //WIFICIPHER_WEP
            {
                config.hiddenSSID = true;
                config.wepKeys[0] = "\"" + Password + "\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
            }
            if (Type == 3) //WIFICIPHER_WPA
            {
                config.preSharedKey = "\"" + "root1234" + "\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;
            }

            return config;
        }


        private WifiConfiguration IsExsits(String ssid) {
            List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + ssid + "\"")) {
                    return existingConfig;
                }
            }
            return null;
        }

    }
