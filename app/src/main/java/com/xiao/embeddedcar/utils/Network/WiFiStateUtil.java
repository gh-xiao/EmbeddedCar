package com.xiao.embeddedcar.utils.Network;

import static android.net.wifi.WifiManager.EXTRA_WIFI_STATE;
import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.xiao.embeddedcar.data.Entity.LoginInfo;
import com.xiao.embeddedcar.utils.PublicMethods.ToastUtil;
import com.xiao.embeddedcar.data.ViewModel.MainViewModel;

import java.util.Objects;

public class WiFiStateUtil {

    private static final String TAG = "Main";
    @SuppressLint("StaticFieldLeak")
    private static volatile WiFiStateUtil mInstance;
    private Context mContext;
    private WifiManager wifiManager;
    private ToastUtil toastUtil;
    private WifiStateBroadcastReceive mReceive;

    /**
     * 私有构造器
     */
    private WiFiStateUtil() {
    }

    /**
     * 获取ConnectTransport单例对象
     */
    public static WiFiStateUtil getInstance() {
        if (null == mInstance) synchronized (WiFiStateUtil.class) {
            if (null == mInstance) mInstance = new WiFiStateUtil();
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        wifiManager = (WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean getWifiState() {
        // 获取WiFi状态
        return wifiManager.isWifiEnabled();
    }

    /**
     * 打开WiFi
     *
     * @return 操作状态
     */
    public boolean openWifi() {
        if (getWifiState()) return true;
        return wifiManager.setWifiEnabled(true);
    }

    /**
     * 关闭WiFi
     *
     * @return 操作状态
     */
    public boolean closeWifi() {
        if (!getWifiState()) return true;
        return wifiManager.setWifiEnabled(false);
    }

    public boolean wifiInit(MainViewModel vm) {
        if (mContext == null || vm == null) return false;
        /* 得到服务器的IP地址 */
        assert wifiManager != null;
        /* 获得DHCP信息 */
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        /* 获得LoginInfo对象 */
        LoginInfo l = vm.getLoginInfo().getValue();
        if (l != null) {
            /* 设置IPCar */
            l.setIP(Formatter.formatIpAddress(dhcpInfo.gateway));
            /* 设置vm */
            vm.getLoginInfo().setValue(l);
            String ip = l.getIP();
            return !Objects.equals(ip, "0.0.0.0") && !Objects.equals(ip, "127.0.0.1");
        } else return false;
    }

    /**
     * 注册WiFi接收器(未经使用)
     */
    @Deprecated
    public void registerWifiReceiver() {
        if (mReceive == null) mReceive = new WifiStateBroadcastReceive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WIFI_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mReceive, intentFilter);
    }

    /**
     * 注销WiFi接收器(未经使用)
     */
    @Deprecated
    public void unregisterWifiReceiver() {
        if (mReceive != null) {
            mContext.unregisterReceiver(mReceive);
            mReceive = null;
        }
    }

    class WifiStateBroadcastReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiState = intent.getIntExtra(EXTRA_WIFI_STATE, 0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    toastUtil.ShowToast("WiFi已关闭,请检查设备连接状态");
                    Log.i(TAG, "onReceive: " + "Wifi已关闭");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    toastUtil.ShowToast("WiFi正在关闭...");
                    Log.i(TAG, "onReceive: " + "WiFi正在关闭...");
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.i(TAG, "onReceive: " + "WiFi已打开");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Log.i(TAG, "onReceive: " + "WiFi正在打开...");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:

                    break;
                default:
                    break;
            }
        }
    }
}