package com.xiao.embeddedcar.Utils.PublicMethods;

public class BaseConversion {

    /**
     * 二进制转十进制
     *
     * @param str -
     * @return -
     */
    public static byte[] getBytes(String str) {
        // TODO 循环，每次处理4位
        byte[] bytes = new byte[8];
        for (int i = 0; i < str.length() / 4; i++) {
            //每次截取4位计算
            bytes[i] = (byte) (Integer.parseInt(str.substring(4 * i, 4 * (i + 1)), 2) & 0xFF);
        }
        return bytes;
    }

    /**
     * 十进制转十六进制
     *
     * @param dec -
     * @return -
     */
    public static String decToHex(int dec) {
        final int sizeOfIntInHalfBytes = 8;
        final int numberOfBitsInAHalfByte = 4;
        final int halfByte = 0x0F;
        final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',};
        StringBuilder hexBuilder = new StringBuilder(sizeOfIntInHalfBytes);
        hexBuilder.setLength(sizeOfIntInHalfBytes);
        for (int i = sizeOfIntInHalfBytes - 1; i >= 0; --i) {
            int j = dec & halfByte;
            hexBuilder.setCharAt(i, hexDigits[j]);
            dec >>= numberOfBitsInAHalfByte;
        }
        return hexBuilder.toString().toLowerCase();
    }
}
