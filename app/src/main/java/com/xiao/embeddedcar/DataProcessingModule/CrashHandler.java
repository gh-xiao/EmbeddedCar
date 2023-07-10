package com.xiao.embeddedcar.DataProcessingModule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

//https://www.jianshu.com/p/9b2f43d87c9f
//https://blog.csdn.net/shankezh/article/details/79332004

/**
 * <p>全局异常捕获</p>
 * <p>发生Uncaught异常时，由该类来接管程序</p>
 *
 * @author Created by zly on 2016/7/3.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final String TAG = this.getClass().getSimpleName();
    //系统默认UncaughtExceptionHandler
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //context
    private Context mContext;
    //存储异常和参数信息
    private final Map<String, String> paramsMap = new HashMap<>();
    //格式化时间
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
    //单例对象
    @SuppressLint("StaticFieldLeak")
    private static volatile CrashHandler mInstance;

    /**
     * 私有构造器
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例
     */
    public static CrashHandler getInstance() {
        if (null == mInstance) synchronized (CrashHandler.class) {
            if (null == mInstance) mInstance = new CrashHandler();
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为系统默认的
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * uncaughtException 回调函数
     */
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果自己没处理交给系统处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            //自己处理
            try {
                //延迟3秒杀进程
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            //退出程序
//            ex.getAppManager().AppExit(mContext);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

    }

    /**
     * 收集错误信息.发送到服务器
     *
     * @return 处理了该异常返回true, 否则false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) return false;
        //收集设备参数信息
        collectDeviceInfo(mContext);
        //添加自定义信息
        addCustomInfo();
        //使用Toast来显示异常信息
        new Thread(() -> {
            Looper.prepare();
            Toast.makeText(mContext, "程序开小差了呢..", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }).start();
        //保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }


    /**
     * 收集设备参数信息
     *
     * @param ctx CrashHandler持有的Context
     */
    private void collectDeviceInfo(Context ctx) {
        //获取versionName,versionCode
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                paramsMap.put("versionName", pi.versionName == null ? "null" : pi.versionName);
                paramsMap.put("versionCode", String.valueOf(pi.versionCode));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occurred when collect package info", e);
        }
        //获取所有系统信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                paramsMap.put(field.getName(), Objects.requireNonNull(field.get(null)).toString());
            } catch (Exception e) {
                Log.e(TAG, "an error occurred when collect crash info", e);
            }
        }
    }

    /**
     * 添加自定义参数
     */
    private void addCustomInfo() {

    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex 抛出的异常
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = format.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crash/";
                File dir = new File(path);
                if (!dir.exists()) if (dir.mkdirs()) Log.i(TAG, "目录创建成功");
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occurred while writing file...", e);
        }
        return null;
    }
}