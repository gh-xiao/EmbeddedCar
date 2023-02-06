package com.xiao.embeddedcar.Utils.TrafficLight;

import android.graphics.Bitmap;
import android.util.Log;

import com.xiao.embeddedcar.Utils.PublicMethods.RGB2HSV;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class TrafficLight {

    private static Bitmap bm = null;
    private static String ColorPixel;

    /**
     * 识别传入的红绿灯图片
     *
     * @param inputBitmap 传入需要识别的红绿灯图片
     * @return String – 红绿灯识别结果
     */
    @Deprecated
    public static String getImageColorPixel(Bitmap inputBitmap) {
        if (inputBitmap == null) return "ERROR";
        /*在这里调整传入的图片以方便红绿灯的识别*/
        Bitmap bitmap = Bitmap.createBitmap(inputBitmap,
                (inputBitmap.getWidth() / 100) * 25,
                (inputBitmap.getHeight() / 100) * 2,
                (inputBitmap.getWidth() / 100) * 65,
                (inputBitmap.getHeight() / 100) * 45);
        //保存图片用
        bm = bitmap;
        int[] rgb = new int[3];
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int minx = 0;
        //int miny = bi.getMinY();
        int red = 0;
        int yellow = 0;
        int green = 0;
        for (int i = minx; i < width; i += 5) {
            for (int j = height / 3; j < height / 1.5; j += 5) {
                int pixel = bitmap.getPixel(i, j);
                rgb[0] = (pixel & 0xff0000) >> 16;
                rgb[1] = (pixel & 0xff00) >> 8;
                rgb[2] = (pixel & 0xff);
                //调整RGB数组的参数可以改变识别的颜色
                //可以将RGB数组改变为HSV数组方便选择需要识别的颜色
                float[] hsv = RGB2HSV.rgbToHsv(rgb);
                //RGB数组判断颜色
//                if (rgb[0] >= 210 && rgb[1] < 200 && rgb[2] < 200) red++;
//                if (rgb[0] >= 200 && rgb[1] >= 145 && rgb[2] < 200) yellow++;
//                if (rgb[0] <= 200 && rgb[1] >= 200 && rgb[2] < 200) green++;
                //HSV数组判断颜色
//                if (ColorConfirmation.isRed(hsv)) red++;
//                if (ColorConfirmation.isYellow(hsv)) yellow++;
//                if (ColorConfirmation.isGreen(hsv)) green++;
                if (hsv[0] <= 25 || (300 <= hsv[0])) red++;
                if (25 < hsv[0] && hsv[0] <= 65) yellow++;
                if (90 <= hsv[0] && hsv[0] <= 160) green++;
            }
        }
        //输出该图片包含指定颜色的像素个数
        System.out.println("红色" + red);
        System.out.println("黄色" + yellow);
        System.out.println("绿色" + green);
        if (red > yellow) {
            return ColorPixel = red > green ? "红灯" : "绿灯";
        } else {
            return ColorPixel = green > yellow ? "绿灯" : "黄灯";
        }
    }


    /**
     * 识别传入的图片
     *
     * @param inputBitmap 传入需要识别的红绿灯图片
     * @return String – 红绿灯识别结果
     */
    public static int getImagePixel(Bitmap inputBitmap) {
        /*在这里调整传入的图片以方便红绿灯的识别*/
        //保存图片用
        int[] rgb = new int[3];
        int width = inputBitmap.getWidth();
        int height = inputBitmap.getHeight();
        int minx = 0;
        int miny = 0;
        int white = 0;
        for (int i = minx; i < width; i += 5) {
            for (int j = miny; j < height; j += 5) {
                int pixel = inputBitmap.getPixel(i, j);
                rgb[0] = (pixel & 0xff0000) >> 16;
                rgb[1] = (pixel & 0xff00) >> 8;
                rgb[2] = (pixel & 0xff);
                //调整RGB数组的参数可以改变识别的颜色
                //可以将RGB数组改变为HSV数组方便选择需要识别的颜色
                float[] hsv = RGB2HSV.rgbToHsv(rgb);
                //HSV数组判断颜色
                if (hsv[0] == 0 && hsv[1] == 0 && hsv[2] == 1) white++;

            }
        }
        //输出该图片包含指定颜色的像素个数
//        System.out.println("白色: " + white);
        return white;
    }

    /**
     * 全局保存图片的方法
     *
     * @param name 自定义图片名
     * @param bm   需要保存的图片
     */
    public static void saveBitmap(String name, Bitmap bm) {
        Log.d("Save Bitmap", "Ready to save picture");
        // 指定我们想要存储文件的地址
        String TargetPath = "/storage/emulated/0/DCIM/Tess/";
        Log.d("Save Bitmap", "Save Path = " + TargetPath);
        // 判断指定文件夹的路径是否存在
        if (!fileIsExist(TargetPath)) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            // 如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, name);
            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                // 存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                Log.d("Save Bitmap", "The picture is save to your phone!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 只保存TrafficLight类使用的图片
     * 如需保存其他图片请添加bitmap参数
     */
    public static void saveBitmap() {
        Log.d("Save Bitmap", "Ready to save picture");
        // 指定我们想要存储文件的地址
        String TargetPath = "/storage/emulated/0/DCIM/Tess/";
        Log.d("Save Bitmap", "Save Path=" + TargetPath);
        // 判断指定文件夹的路径是否存在
        if (!fileIsExist(TargetPath)) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            // 如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, ColorPixel + ".jpg");
            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                // 存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                Log.d("Save Bitmap", "The picture is save to your phone!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    static boolean fileIsExist(String fileName) {
        //传入指定的路径，然后判断路径是否存在
        File file = new File(fileName);
        //file.mkdirs() 创建文件夹的意思
        return file.exists() || file.mkdirs();
    }
}
