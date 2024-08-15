package com.xiao.embeddedcar.utils.PublicMethods;

import android.graphics.Bitmap;
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
 * TFT标志物智能裁剪
 */
public class TFTAutoCutter {

    private static final String TAG = TFTAutoCutter.class.getSimpleName();
    /* HSV裁剪参数 */
    //原始参数(不建议调整)
    private static final int[] OriginPara = {0, 0, 180, 46, 175, 115, 255};
    //测试
//    private static final int[] CutterPara = {0, 180, 0, 255, 75, 255, 100};
//    private static final int[] CutterPara = {0, 180, 0, 179, 46, 255, 86};
//    public static final int[] CutterPara = {0, 180, 0, 175, 46, 255, 115};
//    private static int[] CutterPara = {0, 0, 180, 46, 175, 115, 255};
    // 可用
//    private static int[] CutterPara = {0, 0, 180, 10, 255, 200, 255};
//    private static int[] CutterPara = {0, 0, 180, 10, 255, 160, 255};
//    private static int[] CutterPara = {0, 0, 180, 46, 255, 115, 255};
    private static int[] CutterPara = {0, 0, 180, 46, 255, 155, 255};

    public static int[] getOriginPara() {
        return OriginPara;
    }

    public static int[] getCutterPara() {
        return CutterPara;
    }

    public static void setCutterPara(int[] parameter) {
        CutterPara = parameter;
    }

    /**
     * 裁剪与定位TFT屏幕
     *
     * @param inputTFTBitmap 传入待裁剪的已截取的TFT图片
     * @return 裁剪成功的TFT图片
     */
    public static Bitmap TFTCutter(Bitmap inputTFTBitmap) {
        if (inputTFTBitmap == null) return null;
        /* 转换为mat对象 */
        Mat mat = new Mat();
        Utils.bitmapToMat(inputTFTBitmap, mat);
        /* 转换为包含hsv参数的mat对象 */
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGB2HSV);
        /* 将图像根据指定参数转换为黑白mat对象,在选定范围内的像素转换为白色 */
        Core.inRange(hsvMat, new Scalar(CutterPara[1], CutterPara[3], CutterPara[5]), new Scalar(CutterPara[2], CutterPara[4], CutterPara[6]), hsvMat);
        /* 确定运算核，类似于卷积核 */
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        /* 开运算(除去白色噪点) */
        Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_OPEN, kernel);
        /* 膨胀操作(扩大白色联通区域) */
        Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_DILATE, kernel);
        Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_DILATE, kernel);
        Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_DILATE, kernel);
        /* 腐蚀操作(减少白色联通区域) */
        Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_ERODE, kernel);
        Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_ERODE, kernel);
        Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_ERODE, kernel);
        //轮廓统计
        List<MatOfPoint> contours = new ArrayList<>();
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
        RETR_CCOMP: 表示提取所有轮廓并将组织成一个两层结构，其中顶层轮廓是外部轮廓，第二层轮廓是“洞”的轮廓
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
                Mat imgSource = mat.clone();
                /* 微调裁剪矩形大小 */
                Mat result = new Mat(imgSource, rect);
                //裁剪后的图片
                Bitmap rectBitmap = Bitmap.createBitmap(result.width(), result.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(result, rectBitmap);
                Log.e(TAG, "TFT智能裁剪完毕");
                return rectBitmap;
            }
        }
        Log.e(TAG, "没有查找到轮廓");
        return null;
    }
}