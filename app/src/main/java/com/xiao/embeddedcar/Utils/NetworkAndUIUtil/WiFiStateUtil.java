package com.xiao.embeddedcar.Utils.NetworkAndUIUtil;

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

import com.xiao.embeddedcar.Entity.LoginInfo;
import com.xiao.embeddedcar.ViewModel.ConnectViewModel;

import java.util.Objects;

public class WiFiStateUtil {

    private static final String TAG = "Main";
    @SuppressLint("StaticFieldLeak")
    private static WiFiStateUtil mInstance;
    private Context mContext;
    private WifiManager wifiManager;
    private ConnectViewModel vm;
    private LoginInfo l;
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
    public static synchronized WiFiStateUtil getInstance() {
        if (null == mInstance) {
            mInstance = new WiFiStateUtil();
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

    public boolean wifiInit(ConnectViewModel vm) {
        if (mContext == null) return false;
        this.vm = vm;
        /* 得到服务器的IP地址 */
        assert wifiManager != null;
        /* 获得DHCP信息 */
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        /* 获得LoginInfo对象 */
        l = vm.getLoginInfo().getValue();
        if (l != null) {
            /* 设置IPCar */
            l.setIP(Formatter.formatIpAddress(dhcpInfo.gateway));
            /* 设置vm */
            vm.getLoginInfo().setValue(l);
            return !Objects.equals(l.getIP(), "0.0.0.0") && !Objects.equals(l.getIP(), "127.0.0.1");
        } else return false;

//        FirstActivity.IPCar = Formatter.formatIpAddress(dhcpInfo.gateway);
//        return !FirstActivity.IPCar.equals("0.0.0.0") && !FirstActivity.IPCar.equals("127.0.0.1");
    }

    /**
     * 进行连接操作后调用的方法
     * @return 是否为正确的IP地址
     */
    public boolean wifiInit() {
        if (mContext == null || vm == null) return false;
        /* 得到服务器的IP地址 */
        assert wifiManager != null;
        /* 获得DHCP信息 */
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        /* 获得LoginInfo对象 */
        l = vm.getLoginInfo().getValue();
        if (l != null) {
            /* 设置IPCar */
            l.setIP(Formatter.formatIpAddress(dhcpInfo.gateway));
            /* 设置vm */
            vm.getLoginInfo().setValue(l);
            return !Objects.equals(l.getIP(), "0.0.0.0") && !Objects.equals(l.getIP(), "127.0.0.1");
        } else return false;
    }

}