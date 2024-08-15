package com.xiao.embeddedcar.utils.PublicMethods;

/**
 * 进制转换
 */
public class BaseConversion {
    /**
     * 十六进制字符串转换成int型数据
     *
     * @param str "0x00" - "0xFF"
     * @return int
     */
    public static int String2Integer(String str) {
        return Integer.parseInt(str.substring(2), 16);
    }

    /**
     * 小端序转换成大端序
     *
     * @param str "0x00FC5F2E"
     * @return "0x002E5FFC"
     */
    public static String little2bigEndian(String str) {
//        String string = "0x00FC5F2E"; // 小端序
        StringBuilder sb = new StringBuilder();
        for (int i = str.length() - 2; i > 0; i -= 2) {
            sb.append(str.substring(i, i + 2)); // 每两个字符为一组，从后往前拼接
        }
        String reversed = "0x" + sb; // 大端序
        System.out.println(reversed); // 输出0x002E5FFC
        return reversed;
    }

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
