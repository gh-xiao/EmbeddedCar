package com.xiao.embeddedcar.Utils.Shape;

import android.graphics.Bitmap;
import android.util.Log;

import com.xiao.embeddedcar.Utils.PublicMethods.ColorHSV;
import com.xiao.embeddedcar.Utils.TrafficLight.TrafficLight;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShapeDetector {
    //目标类的简写名称
    private static final String TAG = ShapeDetector.class.getSimpleName();
    //轮廓绘制/轮廓统计
    private static final List<MatOfPoint> contours = new ArrayList<>();
    //HashMap<颜色,对应颜色HashMap<形状,数量>>
    private final HashMap<String, ShapeStatistics> Shape = new HashMap<>();
    //检测出的所有图形数量
    private int totals = 0;

    /**
     * 获取该图片中所有的图形数量
     */
    public int getTotals() {
        for (Map.Entry<String, ShapeStatistics> map : Shape.entrySet()) {
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
    public HashMap<String, ShapeStatistics> getShape() {
        return Shape;
    }

    /**
     * 形状识别 - Bitmap图片处理
     *
     * @param inputBitmap 需要处理的图片
     */
    public void shapePicProcess(Bitmap inputBitmap) {

        if (inputBitmap == null) return;

        /* 图片截取方式1 */
//        Bitmap Btmp = Bitmap.createBitmap(inputBitmap,
//                //开始的x轴
//                (inputBitmap.getWidth() / 100) * 25,
//                //开始的y轴
//                (inputBitmap.getHeight() / 100) * 50,
//                //从开始的x轴截取到当前位置的宽度
//                (inputBitmap.getWidth() / 100) * 35,
//                //从开始的y轴截取到当前位置的高度
//                (inputBitmap.getHeight() / 100) * 50);

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
        Shape.clear();
        /* 调整截图位置(图片截取方式2) */
        //测试图片用
//        Rect rect = new Rect(188, 81, 322, 192);
        //???
//        Rect rect = new Rect(188, 145, 290, 195);
        //主车用
//        Rect rect = new Rect(188, 175, 290, 175);
//        Rect rect = new Rect(200, 160, 290, 175);

//        Mat dstmat = new Mat(srcMat, rect);

        /* 如果使用Utils.loadResource()加载图片资源,则需要转换为RGB */
//        Imgproc.cvtColor(dstmat, dstmat, Imgproc.COLOR_BGR2RGB);

        /* 保存用 */
        Bitmap save = Bitmap.createBitmap(srcMat.width(), srcMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(srcMat, save);
        TrafficLight.saveBitmap("shape_裁剪.jpg", save);

        /* 颜色形状分析 */
        Identify(srcMat, ColorHSV.yellowHSV1, "黄色");
        Identify(srcMat, ColorHSV.greenHSV1, "绿色");
        Identify(srcMat, ColorHSV.cyanHSV, "青色");
        Identify(srcMat, ColorHSV.blueHSV2, "蓝色");
        Identify(srcMat, ColorHSV.purpleHSV1, "紫色");
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
        Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_CLOSE, kernel);
        /* 轮廓提取,用于提取图像的轮廓 */
        contours.clear();
        Imgproc.findContours(hsvMat, contours, outMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        /* 轮廓数量统计 */
        int contoursCounts = contours.size();
        /* 绘制轮廓,用于绘制找到的图像轮廓 */
        /*
        函数参数详解:
        第一个参数image表示目标图像
        第二个参数contours表示输⼊的轮廓组，每⼀组轮廓由点vector构成
        第三个参数contourIdx指明画第⼏个轮廓，如果该参数为负值，则画全部轮廓
        第四个参数color为轮廓的颜色
        第五个参数thickness为轮廓的线宽，如果为负值或CV_FILLED表⽰填充轮廓内部
        */
        Imgproc.drawContours(mat, contours, -1, new Scalar(0, 255, 0), 4);

        /* 形状统计 */
        /* 核心统计代码,参数已调整 */
        //轮廓
        MatOfPoint2f contour2f;
        //近似曲线(多边形拟合)
        MatOfPoint2f approxCurve;
        double epsilon;
        int tri, rect, circle, star, rhombus;
        tri = rect = circle = star = rhombus = 0;
        /* 遍历轮廓 */
        for (int i = 0; i < contoursCounts; i++) {
            /* 判断面积是否大于阈值 */
            Log.i(TAG, "这是轮廓面积: " + Imgproc.contourArea(contours.get(i)));
            if (Imgproc.contourArea(contours.get(i)) > 250) {
                /* 某一个点的集合(当前对象的轮廓) */
                contour2f = new MatOfPoint2f(contours.get(i).toArray());
                /* 计算轮廓的周长 */
                epsilon = 0.035 * Imgproc.arcLength(contour2f, true);
                /* 多边形拟合 */
                approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);
//                System.out.println("数量: " + approxCurve.rows());
                /* 获取包含旋转角度的最小外接矩形 */
                Rect minRect = Imgproc.boundingRect(approxCurve);
                /* 计算外接矩形的轮廓中心 */
                Imgproc.rectangle(mat, new Point(minRect.x, minRect.y), new Point(minRect.x + minRect.width, minRect.y + minRect.height), new Scalar(255, 255, 0), 4);
                if (approxCurve.rows() == 3) tri++;
                    /* 判断矩形和菱形 */
                else if (approxCurve.rows() == 4) {
                    double area, minArea;
                    /* 该图形(四边形)拟合的面积 */
                    area = Imgproc.contourArea(contour2f);
                    /* 包含旋转角度的最小外接矩形的面积 */
                    RotatedRect minAreaRect = Imgproc.minAreaRect(contour2f);
                    minArea = minAreaRect.size.area();
                    /* 图形面积/外接矩形面积 */
                    double rec = area / minArea;
                    Log.i(TAG, "这是area / minArea得到的阈值: " + rec);
                    if (rec >= 0.75 && rec < 1.15) rect++;
                    else rhombus++;
                }
                /* 判断五角星和圆形 */
                else if (approxCurve.rows() > 4) {
                    /* 最小外接矩形的面积 */
                    int minAreaRect = minRect.height * minRect.width;
                    /* 该图形面积 */
                    double area = Imgproc.contourArea(contours.get(i));
                    if ((area / minAreaRect) > 0.5) circle++;
                    else star++;
                }
            }
        }

        /* 引用ShapeCount对象存放识别数据 */
        SaveResult(colorName, circle, tri, rect, star, rhombus);

        /* 输出结果 */
        String msg = colorName + "轮廓: " + contoursCounts + "\n圆形: " + circle + " 三角形: " + tri + " 矩形: " + rect + " 菱形: " + rhombus + " 五角星: " + star;
        Log.i(TAG, msg);

        /* 保存图片 */
        Bitmap save = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, save);
        TrafficLight.saveBitmap(colorName + ".jpg", save);
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
        HashMap<String, Integer> shapeStatistics = new HashMap<>();
        shapeStatistics.put("三角形", tri);
        shapeStatistics.put("矩形", rect);
        shapeStatistics.put("菱形", rhombus);
        shapeStatistics.put("五角星", star);
        shapeStatistics.put("圆形", circle);
        shapeStatistics.put("总计", tri + rect + rhombus + star + circle);
        /* 形状计数对象 */
        ShapeStatistics statistics = new ShapeStatistics();
        /* 保存在该对象上 */
        statistics.setShapeStatistics(shapeStatistics);
        Shape.put(colorName, statistics);
    }
}