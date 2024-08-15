package com.xiao.embeddedcar.utils.QRcode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * 彩色二维码截取
 */
public class QRBitmapCutter {


    private static final String TAG = QRBitmapCutter.class.getSimpleName();
    //裁剪参数
    private static final int[] RedQR = {0, 100, 150, 40, 255, 40, 255};
    private static final int[] GreenQR = {0, 35, 50, 75, 255, 100, 255};
    private static final int[] BlueQR = {0, 100, 150, 80, 255, 80, 255};
    //轮廓统计
    private static final List<MatOfPoint> contours = new ArrayList<>();
    //检测颜色选择
    private static QRColor color = QRColor.RED;

    public enum QRColor {RED, GREEN, BLUE}

    public static QRColor getColor() {
        return color;
    }

    public static void setColor(QRColor color) {
        QRBitmapCutter.color = color;
    }

    /**
     * @param inputQRBitmap 待解析裁剪的二维码原图
     * @return 对应颜色的二维码Bitmap
     */
    public static Bitmap QRCutter(Bitmap inputQRBitmap) {
        if (inputQRBitmap == null) return null;
        /* 转换为mat对象 */
        Mat mat = new Mat();
        Utils.bitmapToMat(inputQRBitmap, mat);
        /* 红色二维码反色处理 */
        if (color.equals(QRColor.RED)) {
            //RGB转换为BGR - 红蓝色互换
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        }
        /* 设置感兴趣区域,减少误裁剪概率 */
//        Rect ROI = new Rect(100, 10, mat.width() - 190, mat.height() - 35);
        Rect ROI = new Rect((mat.width() / 100 * 40), (mat.height() / 100 * 10), (mat.width() / 100 * 28), (mat.height() / 100 * 75));
        Mat ROIMat = new Mat(mat, ROI);
        /* 转换为包含hsv参数的mat对象 */
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(ROIMat, hsvMat, Imgproc.COLOR_RGB2HSV);
        /* 将图像根据指定参数转换为黑白mat对象,在选定范围内的像素转换为白色 */
        switch (color) {
            case GREEN:
                Core.inRange(hsvMat, new Scalar(GreenQR[1], GreenQR[3], GreenQR[5]),
                        new Scalar(GreenQR[2], GreenQR[4], GreenQR[6]), hsvMat);
                break;
            case RED:
                Core.inRange(hsvMat, new Scalar(RedQR[1], RedQR[3], RedQR[5]),
                        new Scalar(RedQR[2], RedQR[4], RedQR[6]), hsvMat);
                break;
            /* 默认处理蓝色二维码 */
            default:
                Core.inRange(hsvMat, new Scalar(BlueQR[1], BlueQR[3], BlueQR[5]),
                        new Scalar(BlueQR[2], BlueQR[4], BlueQR[6]), hsvMat);
                break;
        }

        /* 确定运算核，类似于卷积核 */
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        //https://www.jianshu.com/p/ee72f5215e07
        /* 膨胀操作(扩大白色联通区域) */
        for (int i = 1; i <= 6; i++)
            Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_DILATE, kernel);
        contours.clear();
        /* 获取轮廓 */
        //无用但必要的Mat对象
        Mat hierarchy = new Mat();
        /*  Imgproc.findContours()方法解析:
          Mat image - 输⼊的8位单通道"二值"图像
          List<MatOfPoint> contours - 包含MatOfPoint对象的List
          Mat hierarchy - (可选)拓扑信息
          int mode  轮廓检索模式
          int method 近似方法 */
        /* int mode - 提取参数:
        RETR_EXTERNAL: 表示只提取最外面的轮廓
        RETR_LIST: 表示提取所有轮廓并将其放⼊列表
        RETR_CCOMP: 表示提取所有轮廓并将组织成⼀个两层结构，其中顶层轮廓是外部轮廓，第⼆层轮廓是“洞”的轮廓
        RETR_TREE: 表示提取所有轮廓并组织成轮廓嵌套的完整层级结构 */
        Imgproc.findContours(hsvMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        /* (保险起见,加入判断)如果存在轮廓 */
        if (!contours.isEmpty()) {
            MatOfPoint contour = null;
            //最大面积
            double maxArea = 0;
            //所有轮廓的迭代器
            /* 获取最大轮廓 */
            for (MatOfPoint wrapper : contours) {
                //当前迭代的MatOfPoint对象
                //获取面积大小
                double area = Imgproc.contourArea(wrapper);
                if (area > maxArea) {
                    maxArea = area;
                    contour = wrapper;
                }
            }

            if (contour != null) {
                /* 最小外接矩形 */
                Rect rect = Imgproc.boundingRect(contour);
                /* 绘图 */
                Mat imgSource = ROIMat.clone();
                /* 微调裁剪矩形大小 */
                try {
                    Mat result = new Mat(imgSource, rect);
                    //裁剪后的图片
                    Bitmap rectBitmap = Bitmap.createBitmap(result.width(), result.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(result, rectBitmap);
                    Log.e(TAG, "二维码裁剪完毕");
                    return rectBitmap;
                } catch (Exception e) {
                    Log.e(TAG, "二维码裁剪错误");
                    e.printStackTrace();
                    return inputQRBitmap;
                }
            }
        }
        Log.e(TAG, "没有查找到轮廓");
        return inputQRBitmap;
    }

    /**
     * 图像灰度化(方便二维码识别)
     *
     * @param bmSrc 需要灰度化的Bitmap
     * @return 灰度化的bmSrc
     */
    public static Bitmap bitmap2Gray(Bitmap bmSrc) {
        // 得到图片的长和宽
        if (bmSrc == null) return null;
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        // 创建目标灰度图像
        Bitmap bmpGray;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        // 创建画布
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;
    }
}
