package com.xiao.embeddedcar.Utils.RFID;

import java.util.Arrays;
import java.util.HashSet;

public class Tools {

    /**
     * ----------------------------------判断调用哪个编码------------------------------------------
     */
    public static Character[] data = null;
    public static String[] outResult;//编码结果集

    public static void choose() {
        HashSet<Character> hashSet = new HashSet<>(Arrays.asList(data));
//        if (hashSet.size() == 3 && hashSet.contains('A') && hashSet.contains('B') && hashSet.contains('C')) {
//            System.out.println("---------------------识别到卡2--------------------------");
//            RFID2 rfid2 = new RFID2();
//            outResult = rfid2.RFID2();
//        } else {
//            System.out.println("---------------------识别到卡1--------------------------");
//            RFID1 rfid1 = new RFID1();
//            outResult = rfid1.RFID1();
//        }
        //确保hashSet为空
        System.out.println("---------------------识别到卡1--------------------------");
        RFID1 rfid1 = new RFID1();
//        outResult = rfid1.RFID1();
        hashSet.removeAll(Arrays.asList(data));
        System.out.println("///////////////////////////////////////////////");
        System.out.println(hashSet);
    }

}
