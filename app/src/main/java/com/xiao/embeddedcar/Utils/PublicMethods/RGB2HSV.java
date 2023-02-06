package com.xiao.embeddedcar.Utils.PublicMethods;

public class RGB2HSV {

    /**
     * 色彩算法RGB转HSV
     *
     * @param rgb 传入int[3] RGB
     * @return float[3] HSV
     */
    public static float[] rgbToHsv(int[] rgb) {
        //切割rgb数组
        int R = rgb[0];
        int G = rgb[1];
        int B = rgb[2];
        //公式运算 /255
        float R_1 = R / 255f;
        float G_1 = G / 255f;
        float B_1 = B / 255f;
        //重新拼接运算用数组
        float[] all = {R_1, G_1, B_1};
        float max = all[0];
        float min = all[0];
        //循环查找最大值和最小值
        for (float v : all) {
            if (max <= v) {
                max = v;
            }
            if (min >= v) {
                min = v;
            }
        }
        float C_max = max;
        float C_min = min;
        //计算差值
        float diff = C_max - C_min;
        float hue = 0f;
        //判断情况计算色调H
        if (diff == 0f) hue = 0f;
        else {
            if (C_max == R_1) hue = (((G_1 - B_1) / diff) % 6) * 60f;
            if (C_max == G_1) hue = (((B_1 - R_1) / diff) + 2f) * 60f;
            if (C_max == B_1) hue = (((R_1 - G_1) / diff) + 4f) * 60f;
        }
        //计算饱和度S
        float saturation;

        if (C_max == 0f) saturation = 0f;
        else saturation = diff / C_max;
        //明度V - C_max
        return new float[]{hue, saturation, C_max};
    }

}
