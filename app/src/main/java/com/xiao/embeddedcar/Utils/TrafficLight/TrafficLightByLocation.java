package com.xiao.embeddedcar.Utils.TrafficLight;

import android.graphics.Bitmap;
import android.util.Log;

import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.Utils.PublicMethods.BitmapProcess;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightByLocation {

    private static final String TAG = TrafficLightByLocation.class.getSimpleName();
    //原始位置阈值
    public static final int ORIGIN_LOCATION = 65;
    //红绿灯位置阈值调整
    private static int LIGHT_LOCATION = 65;
    private static Mat rAyMat, greenMat;
    //已经处理好的图像
    private static Bitmap detectROI;

    public static Bitmap getDetectROI() {
        return detectROI;
    }

    public static void setLightLocation(int lightLocation) {
        LIGHT_LOCATION = lightLocation;
    }

    /**
     * 识别传入的红绿灯图片
     *
     * @param inputBitmap 传入需要识别的红绿灯图片
     * @param location    交通灯位置
     * @return String – 红绿灯识别结果
     */
    public static String Identify(Bitmap inputBitmap, int location) {
        if (inputBitmap == null) return "ERROR";
        Mat srcMat = new Mat();
        Utils.bitmapToMat(inputBitmap, srcMat);
        return Identify(srcMat, location);
    }

    /**
     * 识别传入的红绿灯图片
     *
     * @param srcMat   传入需要识别的Mat
     * @param location 交通灯位置
     * @return String – 红绿灯识别结果
     */
    public static String Identify(Mat srcMat, int location) {

        if (srcMat == null) return "ERROR";
        Rect rect;
        /* 长线 */
        if (location == 1)
            rect = new Rect((srcMat.width() / 100 * 25), (srcMat.height() / 100 * 10), (srcMat.width() / 100 * 60), (srcMat.height() / 100 * 45));
            /* 短线 */
        else
            rect = new Rect((srcMat.width() / 100 * 20), (srcMat.height() / 100 * 2), (srcMat.width() / 100 * 60), (srcMat.height() / 100 * 45));
        /* 截取感兴趣区域 */
        Mat ROI = new Mat(srcMat, rect);
        /* 保存用 */
        detectROI = Bitmap.createBitmap(ROI.width(), ROI.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(ROI, detectROI);
        BitmapProcess.saveBitmap("红绿灯ROI区域", detectROI);
        /* 创建用来存储图像信息的内存对象 */
        rAyMat = new Mat();
        greenMat = new Mat();
        /* 转换为带有HSV数据的红,黄,绿Mat对象 */
        Imgproc.cvtColor(ROI, rAyMat, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(ROI, greenMat, Imgproc.COLOR_RGB2HSV);

        //颜色分割
        Log.i("start Core.inRange()", "开始颜色分割");
        /* 第一版测试 */
//            Core.inRange(rAyMat, new Scalar(0, 80, 255), new Scalar(50, 255, 255), rAyMat);
//            Core.inRange(yellowMat, new Scalar(0, 0, 255), new Scalar(0, 50, 255), yellowMat);
//            Core.inRange(greenMat, new Scalar(70, 0, 255), new Scalar(90, 255, 255), greenMat);

        /* 赛场上专用 */
//        Core.inRange(rAyMat, new Scalar(0, 80, 230), new Scalar(15, 255, 255), rAyMat);
//        Core.inRange(yellowMat, new Scalar(25, 0, 230), new Scalar(70, 255, 255), yellowMat);
//        Core.inRange(greenMat, new Scalar(70, 0, 230), new Scalar(100, 255, 255), greenMat);

        Core.inRange(rAyMat, new Scalar(0, 50, 230), new Scalar(180, 255, 255), rAyMat);
        Core.inRange(greenMat, new Scalar(60, 0, 255), new Scalar(90, 255, 255), greenMat);

        /* 强光下使用 */
//            Core.inRange(rAyMat, new Scalar(0, 80, 255), new Scalar(50, 255, 255), rAyMat);
//            Core.inRange(yellowMat, new Scalar(0, 0, 255), new Scalar(0, 50, 255), greenMat);
//            Core.inRange(greenMat, new Scalar(70, 0, 255), new Scalar(90, 255, 255), yellowMat);

        /* 形态学处理 */
        //确定运算核
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        /* 开,闭运算 */
        Imgproc.morphologyEx(rAyMat, rAyMat, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(rAyMat, rAyMat, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(greenMat, greenMat, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(greenMat, greenMat, Imgproc.MORPH_CLOSE, kernel);

        BitmapProcess.saveBitmap("rAy", rAyMat);
        BitmapProcess.saveBitmap("g", greenMat);

        int rAy_X = 0;
        int Ig = 0;

        /* 二值化HSV数组的每个像素值不是0(黑)就是255(白) */
        for (int rows = 0; rows < greenMat.rows(); rows++) {
            for (int cols = 0; cols < greenMat.cols(); cols++) {
                double[] scalarVal = greenMat.get(rows, cols);
                if (scalarVal[0] > 1) Ig++;
            }
        }

        /* 处理红灯和黄灯(获取X坐标点) */
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(rAyMat, contours, rAyMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (MatOfPoint point : contours) {
            if (Imgproc.contourArea(point) > 2000) {
                ConnectTransport.getInstance().sendUIMassage(1, "查找到轮廓面积: " + Imgproc.contourArea(point));
                MatOfPoint2f matOfPoint2f = new MatOfPoint2f(point.toArray());
                Rect boundingRect = Imgproc.boundingRect(matOfPoint2f);
                rAy_X = (int) boundingRect.tl().x;
            }
        }

        ConnectTransport.getInstance().sendUIMassage(1, "x顶点: " + rAy_X + "\ngreenPixel: " + Ig);
        destroy();

        /* 赛场上专用配套 */
        if (Ig > 750) return "绿灯";
        return rAy_X <= LIGHT_LOCATION ? "红灯" : "黄灯";
    }

    /**
     * 释放资源
     */
    private static void destroy() {
        rAyMat.release();
        greenMat.release();
    }
}
