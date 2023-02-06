package com.xiao.embeddedcar.Utils.NetworkAndUIUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private Context mContext;
    //单例对象
    @SuppressLint("StaticFieldLeak")
    private static ToastUtil mInstance;


    private ToastUtil() {
    }

    public static synchronized ToastUtil getInstance() {
        if (null == mInstance) {
            mInstance = new ToastUtil();
        }
        return mInstance;
    }

    public void init(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void ShowToast(String msg) {
        Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public void ShowToast(String msg, int duration) {
        Toast toast = Toast.makeText(mContext, msg, duration);
        toast.show();
    }
}
