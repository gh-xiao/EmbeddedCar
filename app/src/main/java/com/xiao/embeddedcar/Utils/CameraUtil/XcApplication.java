package com.xiao.embeddedcar.Utils.CameraUtil;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bkrc_logobkrc on 2018/1/10.
 */

public class XcApplication extends Application {
    //连接模式
    public enum Mode {
        SOCKET, SERIAL, USB_SERIAL
    }

    //模式选择
    public static Mode isSerial = Mode.SOCKET;

    private static XcApplication app;

    private static final String cameraIp = "192.168.1.101:81";
    //创建一个无限扩大的线程池
    public static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    //创建一个单一线程池 - 图片接收线程
    public static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    //创建一个单一线程池 - 串口数据接收线程
    public static final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public static XcApplication getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        Intent ipIntent = new Intent();
        //ComponentName的参数1:目标app的包名,参数2:目标app的Service完整类名
        ipIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.ethernet.CameraInitService"));
        //设置要传送的数据
        ipIntent.putExtra("pureCameraIP", "0.0.0.0");
        startService(ipIntent);   //摄像头设为静态192.168.16.20时，可以不用发送
    }
}
