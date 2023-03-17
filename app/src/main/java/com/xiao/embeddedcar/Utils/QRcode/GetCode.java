package com.xiao.embeddedcar.Utils.QRcode;

import android.util.Log;

public class GetCode {
    /**
     * 解析二维码字符串
     *
     * @param str 二维码字符串
     */
    public static String parsing(String str) {
        StringBuilder sb = new StringBuilder();
        for (char ch : str.toCharArray()) {
//            if (Character.isDigit(ch) || Character.isUpperCase(ch) || Character.isLowerCase(ch))
//            if (Character.isDigit(ch) || Character.isUpperCase(ch))
            if (Character.isDigit(ch)) sb.append(ch);
        }
        String code = sb.toString();
//        System.out.println(code);
        Log.i("code", code);
        return code;
    }

    /**
     * 二维码数据算法
     *
     * @param str 二维码字符串
     * @return result
     */
    public static String detectQRCode(String str) {
        return null;
    }
}
