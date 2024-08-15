package com.xiao.embeddedcar.utils.CameraUtil;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.xiao.embeddedcar.data.Entity.LoginInfo;
import com.xiao.embeddedcar.MainApplication;
import com.xiao.embeddedcar.data.ViewModel.MainViewModel;

public class CameraConnectUtil {
    private static final String A_S = "com.a_s";
    //单例对象
    @SuppressLint("StaticFieldLeak")
    private static volatile CameraConnectUtil mInstance;
    //上下文
    private Context mContext;
    private MainViewModel mainViewModel;
    private LoginInfo l;
    //自定义广播接收器 - 来自CameraSearchService的广播
    public BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            l.setIPCamera(intent.getStringExtra("IP"));
            l.pureCameraIP = intent.getStringExtra("pureIP");
            mainViewModel.getLoginInfo().setValue(l);
            mainViewModel.getLoginState().setValue(intent.getStringExtra("loginState"));
            Log.e("camera ip::", "***" + l.getIPCamera() + "***");

            // 如果是串口配置在这里提前启动摄像头驱动，否则是WiFi的话到下个界面再连接
            if (MainApplication.isSerial != MainApplication.Mode.SOCKET) useUartCamera();

            mContext.unregisterReceiver(this);
        }
    };

    /**
     * 私有无参构造器
     */
    private CameraConnectUtil() {
    }

    /**
     * 获取CrashHandler实例
     */
    public static CameraConnectUtil getInstance() {
        if (null == mInstance) synchronized (CameraConnectUtil.class) {
            if (null == mInstance) mInstance = new CameraConnectUtil();
        }
        return mInstance;
    }

    /**
     * 初始化函数
     *
     * @param context 上下文信息
     */
    public void init(Context context, MainViewModel vm) {
        mContext = context.getApplicationContext();
        mainViewModel = vm;
        l = vm.getLoginInfo().getValue();
    }

    /**
     * getter
     */
    public static String getaS() {
        return A_S;
    }

    /**
     * 摄像头初始化
     */
    public void cameraInit() {
        //广播接收器注册
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(A_S);
        mContext.registerReceiver(myBroadcastReceiver, intentFilter);
    }

    /**
     * 启动摄像头
     */
    public void useUartCamera() {
        Intent ipIntent = new Intent();
        //ComponentName的参数1:目标app的包名,参数2:目标app的Service完整类名
        ipIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.ethernet.CameraInitService"));
        //设置要传送的数据
        ipIntent.putExtra("pureCameraIP", l.pureCameraIP);
        mContext.startService(ipIntent);   //摄像头设为静态192.168.16.20时，可以不用发送
    }

    /**
     * 搜索摄像cameraIP
     */
    public void search() {
        Intent intent = new Intent(mContext, CameraSearchService.class);
        mContext.startService(intent);
    }

    /**
     * 摄像头停止服务
     */
    public void cameraStopService() {
        Intent intent = new Intent(mContext, CameraSearchService.class);
        mContext.stopService(intent);
    }

    public void destroy() {
        try {
            mContext.unregisterReceiver(myBroadcastReceiver);
        } catch (Exception e) {
            Log.e(A_S, "unregisterReceiver fail!");
        }
    }
}
