package com.xiao.embeddedcar.Utils.Shape;

import android.graphics.Bitmap;
import android.util.Log;

import com.xiao.embeddedcar.Entity.ShapeStatistics;
import com.xiao.embeddedcar.Utils.PublicMethods.BitmapProcess;
import com.xiao.embeddedcar.Utils.PublicMethods.ColorHSV;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeDetector {
    //目标类的简写名称
    private static final String TAG = ShapeDetector.class.getSimpleName();
    //轮廓绘制/轮廓统计
    private static final List<MatOfPoint> contours = new ArrayList<>();
    //HashMap<颜色,HashMap<形状,数量>>
    private final HashMap<String, ShapeStatistics> ColorCounts = new HashMap<>();
    //检测出的所有图形数量
    private int totals = 0;

    /**
     * 获取该图片中所有的图形数量
     */
    public int getTotals() {
        for (Map.Entry<String, ShapeStatistics> map : ColorCounts.entrySet()) {
            totals += map.getValue().getCounts("总计");
        }
        return totals;
    }

    /**
     * 重置统计
     *
     * @param totals 0
     */
    public void setTotals(int totals) {
        this.totals = totals;
    }

    /**
     * 获取指定颜色的统计对象
     *
     * @return 该颜色的统计对象<形状, 数量>
     */
    public HashMap<String, ShapeStatistics> getColorCounts() {
        return ColorCounts;
    }

    /**
     * 获取指定图形的数量
     */
    public int getShapeCounts(String shapeName) {
        int counts = 0;
        for (Map.Entry<String, ShapeStatistics> map : ColorCounts.entrySet()) {
            counts += map.getValue().getCounts(shapeName);
        }
        return counts;
    }

    /**
     * 形状识别 - Bitmap图片处理
     *
     * @param inputBitmap 需要处理的图片
     */
    public void shapePicProcess(Bitmap inputBitmap) {
        if (inputBitmap == null) return;
        /* openCV创建用来存储图像信息的内存对象 */
        Mat srcMat = new Mat();
        /* 转化为Mat对象 */
        Utils.bitmapToMat(inputBitmap, srcMat);
        shapePicProcess(srcMat);
    }

    /**
     * 形状识别 - Mat图片处理
     *
     * @param srcMat 需要识别的图片
     */
    public void shapePicProcess(Mat srcMat) {
        if (srcMat == null) return;
        ColorCounts.clear();
        /* 保存用 */
        BitmapProcess.saveBitmap("TFTAutoCutter", srcMat);
        /* 颜色形状分析 */
        Identify(srcMat, ColorHSV.yellowHSV1, "黄色");
        Identify(srcMat, ColorHSV.greenHSV1, "绿色");
        Identify(srcMat, ColorHSV.cyanHSV, "青色");
        Identify(srcMat, ColorHSV.blueHSV3, "蓝色");
        Identify(srcMat, ColorHSV.purpleHSV2, "紫色");
        /* 红色颜色取反,方便处理 */
        Identify(srcMat, "红色");
    }

    /**
     * <p>形状识别 - 反色处理</p>
     * <p>因红色阈值问题,建议将图片进行反色处理</p>
     *
     * @param inputMat 已经处理的Mat对象
     */
    private void Identify(Mat inputMat, @SuppressWarnings("SameParameterValue") String colorName) {
        Mat dstMat = new Mat();
        //RGB转换为BGR - 红蓝色互换
        Imgproc.cvtColor(inputMat, dstMat, Imgproc.COLOR_BGR2RGB);
        Identify(dstMat, ColorHSV.red2blueHSV, colorName);
    }

    /**
     * 形状识别
     *
     * @param Mtmp      已经处理的Mat对象
     * @param r         色彩数据
     * @param colorName 色彩名
     */
    private void Identify(Mat Mtmp, int[] r, String colorName) {
        /* openCV创建用来存储图像信息的内存对象 */
        Mat hsvMat = new Mat();
        Mat outMat = new Mat();
        Mat mat = Mtmp.clone();
        /* 转换为HSV */
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGB2HSV);
        /* 颜色分割 */
        Core.inRange(hsvMat, new Scalar(r[2], r[4], r[6]), new Scalar(r[1], r[3], r[5]), hsvMat);
        /* 确定运算核，类似于卷积核 */
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        /* 开运算(除去白噪点) */
        Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_OPEN, kernel);
        /* 闭运算(除去黑噪点) */
//        Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_CLOSE, kernel);
        /* 轮廓提取,用于提取图像的轮廓 */
        contours.clear();
        Imgproc.findContours(hsvMat, contours, outMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        /* 绘制轮廓,用于绘制找到的图像轮廓 */
        /*
         * 函数参数详解:
         * 第一个参数image表示目标图像
         * 第二个参数contours表示输⼊的轮廓组，每⼀组轮廓由点vector构成
         * 第三个参数contourIdx指明画第⼏个轮廓，如果该参数为负值，则画全部轮廓
         * 第四个参数color为轮廓的颜色
         * 第五个参数thickness为轮廓的线宽，如果为负值或CV_FILLED表⽰填充轮廓内部
         */
        Imgproc.drawContours(mat, contours, -1, new Scalar(0, 255, 0), 2);
        /* 形状统计 */
        /* 核心统计代码,参数已调整 */
        //轮廓
        MatOfPoint2f contour2f;
        //近似曲线(多边形拟合)
        MatOfPoint2f approxCurve;
        /* 逼近的精度(阈值),设定的原始曲线与近似曲线之间的最大距离 */
        double epsilon;
        int tri, rect, circle, star, rhombus;
        tri = rect = circle = star = rhombus = 0;
        Log.e(TAG, "----------" + colorName + "总计轮廓: " + contours.size() + "----------");
        /* 遍历轮廓 */
        for (int i = 0; i < contours.size(); i++) {
            /* 判断面积是否大于阈值(有效图形) */
            if (Imgproc.contourArea(contours.get(i)) > 200) {
                Log.i(TAG, "查找到有效轮廓,面积为: " + Imgproc.contourArea(contours.get(i)));
                /* 某一个点的集合(当前对象的轮廓) */
                contour2f = new MatOfPoint2f(contours.get(i).toArray());
                /*
                 * 计算轮廓的周长
                 * 0.035这个系数是一个精度因子，用来控制近似多边形的形状。
                 * 它越小，近似多边形就越接近原始轮廓。它越大，近似多边形就越简单，有更少的顶点。你可以根据你的需要调整这个系数。
                 * by New Bing
                 */
                epsilon = 0.045 * Imgproc.arcLength(contour2f, true);
                //多边形拟合后的轮廓
                approxCurve = new MatOfPoint2f();
                /* 多边形拟合 */
                Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);
                /* boundingRect获取不带旋转角度的最小外接矩形 */
//                Rect minRect = Imgproc.boundingRect(approxCurve);
                /* 绘制不带旋转角度的外接矩形,并计算外接矩形的轮廓中心 */
//                Imgproc.rectangle(mat, minRect, new Scalar(255, 255, 0), 1);
                /* minAreaRect获得带旋转角度的最小外接矩形 */
                RotatedRect minRotatedRect = Imgproc.minAreaRect(approxCurve);
                /* 获取顶点数据 */
                Point[] box = new Point[4];
                minRotatedRect.points(box);
                /* 将顶点转换为整数类型 */
                MatOfPoint boxInt = new MatOfPoint();
                boxInt.fromArray(box);
                /* 绘制带旋转角度的外接矩形,并计算外接矩形的轮廓中心 */
                Imgproc.polylines(mat, Collections.singletonList(boxInt), true, new Scalar(255, 255, 255));
                Log.i(TAG, "包含角点数: " + approxCurve.rows());
                if (approxCurve.rows() == 3) tri++;
                    /* 判断矩形和菱形 */
                    /* 面积判断法 - 旧 */
//                else if (approxCurve.rows() == 4) {
//                    double area, minArea;
//                    /* 该图形(四边形)拟合的面积 */
//                    area = Imgproc.contourArea(approxCurve);
//                    /* 包含旋转角度的最小外接矩形的面积 */
//                    RotatedRect minAreaRect = Imgproc.minAreaRect(approxCurve);
//                    minArea = minAreaRect.size.area();
//                    /* 图形面积/外接矩形面积 */
//                    double rec = area / minArea;
//                    Log.i(TAG, "这是area / minArea得到的阈值: " + rec);
//                    if (rec >= 0.80 && rec < 1.15) rect++;
//                    else rhombus++;
//                }
                    /* 判断菱形 - 边长 */
                else if (isRhombus(approxCurve)) rhombus++;
                    /* 判断矩形 - 对角线 */
                else if (isRectangle(approxCurve)) rect++;
                    /* 判断五角星和圆形 - 面积 */
//                else if (approxCurve.rows() > 4) {
//                    /* 最小外接矩形的面积 */
//                    int minAreaRect = minRect.height * minRect.width;
//                    /* 该图形面积 */
//                    double area = Imgproc.contourArea(contours.get(i));
//                    if ((area / minAreaRect) > 0.5) circle++;
//                    else star++;
//                }
                    /* 判断五角星和圆形 - 圆形度 */
                else if (approxCurve.rows() > 4) {
                    /* 该图形面积 */
                    double area = Imgproc.contourArea(contours.get(i));
                    /* 该图形周长 */
                    double len = Imgproc.arcLength(approxCurve, true);
                    /* 圆形度 */
                    double roundness = (4 * Math.PI * area) / (len * len);
                    Log.i(TAG, "该图形的圆形度: " + roundness);
                    if (roundness > 0.8) circle++;
                    else star++;
                }
            }
        }
        /* 引用ShapeCount对象存放识别数据 */
        SaveResult(colorName, circle, tri, rect, star, rhombus);
        /* 输出结果 */
        String msg = "圆形: " + circle + " 三角形: " + tri + " 矩形: " + rect + " 菱形: " + rhombus + " 五角星: " + star;
        Log.e(TAG, msg);
        /* 保存图片 */
        BitmapProcess.saveBitmap(colorName, mat);
        Log.e(TAG, "----------" + colorName + "识别完成----------");
    }

    /**
     * 判断一个多边形是否为菱形
     *
     * @param approxCurve MatOfPoint2f
     * @return boolean
     */
    private static boolean isRhombus(MatOfPoint2f approxCurve) {
        /* 如果顶点数为4，则可能为菱形 */
        if (approxCurve.toArray().length == 4) {
            /* 获取顶点坐标 */
            Point[] points = approxCurve.toArray();
            /* 获取边长长度 */
            double l1 = getDistance(points[0], points[1]);
            double l2 = getDistance(points[1], points[2]);
            double l3 = getDistance(points[2], points[3]);
            double l4 = getDistance(points[3], points[0]);
            Log.i(TAG, "该轮廓四边边长(顺时针):\n" + "■■■" + l1 + "■■■" + l2 + "■■■\n■■■" + l4 + "■■■" + l3 + "■■■");
            /* 轮廓的邻边边长在误差范围内相等则为菱形 */
            return Math.abs(l1 - l2) < 5 && Math.abs(l2 - l3) < 5 && Math.abs(l3 - l4) < 5 && Math.abs(l4 - l1) < 5;
        }
        // 不是菱形
        return false;
    }

    /**
     * 判断一个多边形是否为矩形
     *
     * @param approxCurve MatOfPoint2f
     * @return boolean
     */
    private static boolean isRectangle(MatOfPoint2f approxCurve) {
        /* 如果顶点数为4，则可能为矩形 */
        if (approxCurve.toArray().length == 4) {
            /* 计算四个顶点之间的距离 */
//            double d1 = getDistance(approxCurve.toArray()[0], approxCurve.toArray()[1]);
//            double d2 = getDistance(approxCurve.toArray()[1], approxCurve.toArray()[2]);
//            double d3 = getDistance(approxCurve.toArray()[2], approxCurve.toArray()[3]);
//            double d4 = getDistance(approxCurve.toArray()[3], approxCurve.toArray()[0]);
            /* 计算对角线之间的距离 */
            double d5 = getDistance(approxCurve.toArray()[0], approxCurve.toArray()[2]);
            double d6 = getDistance(approxCurve.toArray()[1], approxCurve.toArray()[3]);
            /* 判断对角线是否相等，并且相邻边是否垂直（即乘积为零） */
//            double vector = Math.abs(d1 * d2 + d2 * d3 + d3 * d4 + d4 * d1);
//            Log.e(TAG, "对角线长度比对: " + Math.abs(d5 - d6) + "邻边角度误差: " + vector);
//            return Math.abs(d5 - d6) < 1e-6 && vector < 1e-6; // 是矩形
            /* 判断对角线是否相等 */
            Log.e(TAG, "对角线长度比对: " + Math.abs(d5 - d6));
            return Math.abs(d5 - d6) < 3; // 是矩形
        }
        // 不是矩形
        return false;
    }

    /**
     * 计算两点之间的距离
     *
     * @param p1 -
     * @param p2 -
     * @return 两点距离
     */
    private static double getDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     * 形状数据保存
     *
     * @param colorName 形状颜色
     * @param circle    -
     * @param tri       -
     * @param rect      -
     * @param star      -
     * @param rhombus   -
     */
    private void SaveResult(String colorName, int circle, int tri, int rect, int star, int rhombus) {
        /* 保存该颜色包含的图形统计 */
        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("三角形", tri);
        hashMap.put("矩形", rect);
        hashMap.put("菱形", rhombus);
        hashMap.put("五角星", star);
        hashMap.put("圆形", circle);
        hashMap.put("总计", tri + rect + rhombus + star + circle);
        /* 形状计数对象 */
        ShapeStatistics statistics = new ShapeStatistics();
        /* 保存在该对象上 */
        statistics.setShapeStatistics(hashMap);
        ColorCounts.put(colorName, statistics);
    }
}