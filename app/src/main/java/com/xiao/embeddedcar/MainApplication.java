package com.xiao.embeddedcar;

import android.app.Application;

import com.xiao.embeddedcar.DataProcessingModule.CrashHandler;
import com.xiao.embeddedcar.utils.Network.WiFiStateUtil;
import com.xiao.embeddedcar.utils.PublicMethods.BitmapProcess;
import com.xiao.embeddedcar.utils.PublicMethods.ToastUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainApplication extends Application {
    private static MainApplication app;

    //连接模式
    public enum Mode {SOCKET, SERIAL, USB_SERIAL}

    //模式选择
    public static Mode isSerial = Mode.SOCKET;

    private static final String cameraIp = "192.168.1.101:81";
    //创建一个无限扩大的线程池
    public static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    //创建一个单一线程池 - 图片接收线程
    public static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    //创建一个单一线程池 - 串口数据接收线程
    public static final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public static MainApplication getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        /* 初始化全局异常捕获对象 */
        CrashHandler.getInstance().init(this);
        /* Toast工具类(底部弹出提示框)初始化 */
        ToastUtil.getInstance().init(this);
        /* 初始化Bitmap处理对象类 */
        BitmapProcess.getInstance().init(this);
        /* 初始化WiFi状态工具类 */
        WiFiStateUtil.getInstance().init(this);
    }
}
