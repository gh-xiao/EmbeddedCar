package com.xiao.embeddedcar.Utils.PublicMethods;

public class BaseConversion {
    //二进制转十进制
    public static byte[] getBytes(String str) {
        // TODO 循环，每次处理4位
        byte[] bytes = new byte[8];
        for (int i = 0; i < str.length() / 4; i++) {
            //每次截取4位计算
            bytes[i] = (byte) (Integer.parseInt(str.substring(4 * i, 4 * (i + 1)), 2) & 0xFF);
        }
        return bytes;
    }
}
