package com.xiao.embeddedcar.Utils.PublicMethods;

public class FastDo {
    // 两次点击按钮之间的点击间隔不能少于300毫秒(3秒)
    private static final int MIN_CLICK_DELAY_TIME = 3000;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) flag = true;
        lastClickTime = curClickTime;
        return flag;
    }

    // 两次点击按钮之间的点击间隔不能少于300毫秒(3秒)
    private static final int MIN_SEND_DELAY_TIME = 1000;
    private static long lastSendTime;

    public static boolean isFastSend() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastSendTime) >= MIN_SEND_DELAY_TIME) flag = true;
        lastSendTime = curClickTime;
        return flag;
    }
}
