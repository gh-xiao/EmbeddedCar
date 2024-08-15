package com.xiao.embeddedcar.utils.PublicMethods;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则匹配
 */
public class Matching {

    private static void study() {
        //在 Java 中，\\ 表示：我要插入一个正则表达式的反斜线，所以其后的字符具有特殊的意义。
        System.out.print("\\");    // 输出为 \
        System.out.print("\\\\");  // 输出为 \\

    }

    /**
     * 有效信息为^与.之间数据所代表的数值
     *
     * @param str -
     * @return 有效信息
     */
    public static int methods1(String str) {
        int value = -1;
        //创建一个正则表达式对象，匹配^和.之间的内容
        Pattern p = Pattern.compile("\\^(.*?)\\.");
        //创建一个匹配器对象，用来在str中查找匹配
        Matcher m = p.matcher(str);
        //如果找到了匹配，就把它赋值给k
        if (m.find()) {
            //获取第一个括号里的内容，也就是有效信息
            String k = m.group(1);
            assert k != null;
            for (char c : k.toCharArray()) {
                if (Character.isDigit(c)) {
                    //把k转换成数值类型
                    value = Integer.parseInt(String.valueOf(c));
                    break;
                }
            }
            System.out.println("有效信息为数值 " + value);
        }
        return value;
    }

    /**
     * 从字符串str中提取数字部分的内容(匹配一次)
     */
    public static void methods2(String str) {
        Pattern p = Pattern.compile(" /[0-9]+/");
    }
}
