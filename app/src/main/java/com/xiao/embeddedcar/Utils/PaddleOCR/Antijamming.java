package com.xiao.embeddedcar.Utils.PaddleOCR;

import android.graphics.Bitmap;

import com.xiao.embeddedcar.Utils.PublicMethods.RGB2HSV;


/**
 * 简易车牌反干扰模块
 */
public class Antijamming {
    //红、绿、蓝、黄、品、青、黑色个数
    private static final int[] colorNum = new int[8];
    //黑色最大RGB值和
    private static final int blackMax = 255;
    //红绿蓝最大RGB值和
    private static final int RGBMax = 365;
    //黄品青最大RGB值和
    private static final int noiseMax = 510;
    //HSV数组
    private static float[] Hsv;

    public static boolean ColorTask(Bitmap inputBitmap) {

        int[] rgb = new int[3];

        Bitmap bitmap = Bitmap.createBitmap(inputBitmap,
                (inputBitmap.getWidth() / 100) * 30,
                (inputBitmap.getHeight() / 100) * 69,
                (inputBitmap.getWidth() / 100) * 45,
                (inputBitmap.getHeight() / 100) * 37);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int minx = 0;
        //int miny = bi.getMinY();

        int[] pixels = new int[width * height];
//        int[] pl = new int[bitmap.getWidth() * bitmap.getHeight()];

        /* 把二维图片的每一行像素颜色值读取到一个一维数组中*/
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = minx; i < width; i += 5) {
            for (int j = height / 3; j < height / 1.5; j += 5) {
                int pixel = bitmap.getPixel(i, j);
                rgb[0] = (pixel & 0xff0000) >> 16;
                rgb[1] = (pixel & 0xff00) >> 8;
                rgb[2] = (pixel & 0xff);
                Hsv = RGB2HSV.rgbToHsv(rgb);
                //蓝色
                if (170 <= Hsv[0] && Hsv[0] <= 250) colorNum[3]++;
                //黄色
                if (45 <= Hsv[0] && Hsv[0] <= 75) colorNum[4]++;
                //红色
                if ((0 <= Hsv[0] && Hsv[0] <= 15) || (340 <= Hsv[0])) colorNum[1]++;
            }
        }

//        int blue = colorNum[6] + colorNum[3];
        int blue = colorNum[3];
        System.out.println("蓝色" + blue);
        System.out.println("红色" + colorNum[1]);
//        System.out.println("绿色 " + colorNum[2]);
        System.out.println("黄色" + colorNum[4]);

        return blue <= colorNum[1] + colorNum[4];
//        return blue <= colorNum[4] || blue <= colorNum[1];
//        return blue <= colorNum[4];
//        return blue <= colorNum[1];
    }

    /**
     * 此方法仅供参考
     * 像素处理背景变为白色，红、绿、蓝、黄、品、青、黑色，白色不变
     */
    @Deprecated
    private Bitmap ColorTask_test(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        // 把二维图片的每一行像素颜色值读取到一个一维数组中
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] pl = new int[bitmap.getWidth() * bitmap.getHeight()];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                int pixel = pixels[offset + x];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                int rgb = r + g + b;
                //黑色
                if (rgb < blackMax) {
                    pl[offset + x] = pixel;
                    colorNum[7]++;
                }
                // 红绿蓝
                else if (rgb < RGBMax) {
                    pl[offset + x] = pixel;
                    if (r > g && r > b)
                        //红色
                        colorNum[1]++;
                    else if (g > b)
                        //绿色
                        colorNum[2]++;
                    else
                        //蓝色
                        colorNum[3]++;
                }
                //黄、品和青
                else if (rgb < noiseMax) {
                    pl[offset + x] = pixel;
                    if (b < r && b < g)
                        //黄色
                        colorNum[4]++;
                    else if (g < r)
                        //品色
                        colorNum[5]++;
                    else
                        //青色
                        colorNum[6]++;
                } else {
                    // 白色
                    pl[offset + x] = 0xffffffff;
                }
            }
        }
        //把颜色值重新赋给新建的图片 图片的宽高为以前图片的值
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pl, 0, width, 0, 0, width, height);
        return result;
    }
}
