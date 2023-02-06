package com.xiao.embeddedcar.Utils.RFID;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * -------------------------------RFID卡2-?--------------------------------------------
 */

public class RFID2 {
    public static String[] outResult;//编码结果集

    private static String Binary = "";
    private static final String[] Code = {"00", "10", "11"};
    private static final HashMap<Character, Double> hashMap = new HashMap<>();
    private static final Character[] data = Tools.data;

    //二进制转十进制
    private static byte[] getBytes(String str) {
        // TODO 循环，每次处理4位
        byte[] bytes = new byte[8];
        for (int i = 0; i < str.length() / 4; i++) {
            //每次截取4位计算
            bytes[i] = (byte) (Integer.parseInt(str.substring(4 * i, 4 * (i + 1)), 2) & 0xFF);
        }
        return bytes;
    }

    @SuppressLint("NewApi")
    private static HashMap<Character, String> SortAndGroup() {
        /*
          将hashMap中的每一个键值对作为一副关系图放入list中
          List<Map对象>=new ArrayList<~>(键值对)
         */
        List<Map.Entry<Character, Double>> list = new ArrayList<>(hashMap.entrySet());
        //value不同时，按value排序,否则按key排序
        list.sort((o1, o2) -> !o2.getValue().equals(o1.getValue()) ? o2.getValue().compareTo(o1.getValue()) : o1.getKey().compareTo(o2.getKey()));
        //对应字母的二进制
        HashMap<Character, String> index = new HashMap<>();
        System.out.println(index);
        int i = 0;
        for (Map.Entry<Character, Double> entry : list) {
            index.put(entry.getKey(), Code[i++]);
        }
        System.out.println("此处为SortAndGroup输出: " + index);
        return index;
    }

    //十进制转十六进制
    private static String[] GroupAndChange() {
        int tagger = 0;
        String[] str = new String[8];
        byte[] bytes = getBytes(Binary);
        System.out.println("此处输出bytes[]: " + Arrays.toString(bytes));
        for (byte b : bytes) {
            str[tagger++] = "0x0" + (Integer.toHexString(b)).toUpperCase();
        }
//        for (int i = 0; i < str.length; i++) {
//            str[i] = "0x0" + (Integer.toHexString(bytes[i])).toUpperCase();
//        }
        System.out.println("此处为十进制转十六进制输出: " + Arrays.toString(str));
        return str;
    }

    @SuppressLint("NotConstructor")
    public String[] RFID2() {
//        data = new Character[]{'A', 'A', 'B', 'C', 'B', 'B', 'C', 'B', 'B', 'B', 'A', 'C', 'A', 'C', 'B', 'B'};
        System.out.println("此处为获取到data: " + Arrays.toString(Tools.data));
        HashSet<Character> set = new HashSet<>();
        //统计元素
        for (Character c : data) {
            if (!hashMap.containsKey(c)) {
                hashMap.put(c, 1.0);
            } else {
                hashMap.put(c, hashMap.get(c) + 1.0);
            }
            set.add(c);
        }
        //计算概率分布
        for (Character c : set) {
            hashMap.put(c, hashMap.get(c) / 16);
        }
        //降序排序并进行分组编码
        HashMap<Character, String> index = SortAndGroup();
        //转换为二进制字符串
        for (Character c : data) {
            String aCatch = index.get(c);
            assert aCatch != null;
            Binary = Binary.concat(aCatch);
        }
        Binary = Binary.trim();
        System.out.println("此处为Binary输出: " + Binary);
        //将二进制数处理成十六进制指令
        return outResult = GroupAndChange();
    }

}
