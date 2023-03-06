package com.xiao.embeddedcar.Utils.TrafficLight;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class TrafficLightByColor {

    private static final String TAG = TrafficLightByColor.class.getSimpleName();
    private static Mat redMat, greenMat, yellowMat;

    /**
     * 识别传入的红绿灯图片
     *
     * @param inputBitmap 传入需要识别的红绿灯图片
     * @param location    交通灯位置
     * @return String – 红绿灯识别结果
     */
    public static String Identify(Bitmap inputBitmap, int location) {
        if (inputBitmap == null) return "ERROR";
        /* 在这里调整传入的图片以方便红绿灯的识别(不建议) */
//        Btmp = Bitmap.createBitmap(inputBitmap,
//                //开始的x轴
//                (inputBitmap.getWidth() / 100) * 25,
//                //开始的y轴
//                (inputBitmap.getHeight() / 100) * 2,
//                //从开始的x轴截取到当前位置的宽度
//                (inputBitmap.getWidth() / 100) * 65,
//                //从开始的y轴截取到当前位置的高度
//                (inputBitmap.getHeight() / 100) * 45);
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
            rect = new Rect((srcMat.width() / 100 * 20), (srcMat.height() / 100 * 10), (srcMat.width() / 100 * 50), (srcMat.height() / 100 * 45));
            /* 短线 */
        else
            rect = new Rect((srcMat.width() / 100 * 20), (srcMat.height() / 100 * 2), (srcMat.width() / 100 * 60), (srcMat.height() / 100 * 45));
        Mat ROI = new Mat(srcMat, rect);
        /* 保存用 */
//        BitmapProcess.saveBitmap("红绿灯ROI区域", ROI);
        /* 创建用来存储图像信息的内存对象 */
        redMat = new Mat();
        greenMat = new Mat();
        yellowMat = new Mat();
        /* 转换为带有HSV数据的红,黄,绿Mat对象 */
        Imgproc.cvtColor(ROI, redMat, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(ROI, yellowMat, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(ROI, greenMat, Imgproc.COLOR_RGB2HSV);

        //颜色分割
        Log.i("start Core.inRange()", "开始颜色分割");
        /* 第一版测试 */
//            Core.inRange(redMat, new Scalar(0, 80, 255), new Scalar(50, 255, 255), redMat);
//            Core.inRange(yellowMat, new Scalar(0, 0, 255), new Scalar(0, 50, 255), yellowMat);
//            Core.inRange(greenMat, new Scalar(70, 0, 255), new Scalar(90, 255, 255), greenMat);

        /* 赛场上专用 */
        Core.inRange(redMat, new Scalar(0, 80, 230), new Scalar(15, 255, 255), redMat);
        Core.inRange(yellowMat, new Scalar(25, 0, 230), new Scalar(70, 255, 255), yellowMat);
        Core.inRange(greenMat, new Scalar(60, 0, 230), new Scalar(100, 255, 255), greenMat);

//        Core.inRange(redMat, new Scalar(0, 50, 230), new Scalar(180, 255, 255), redMat);
//        Core.inRange(yellowMat, new Scalar(0, 50, 230), new Scalar(180, 255, 255), yellowMat);
//        Core.inRange(greenMat, new Scalar(70, 0, 255), new Scalar(90, 255, 255), greenMat);

        /* 强光下使用 */
//            Core.inRange(redMat, new Scalar(0, 80, 255), new Scalar(50, 255, 255), redMat);
//            Core.inRange(yellowMat, new Scalar(0, 0, 255), new Scalar(0, 50, 255), greenMat);
//            Core.inRange(greenMat, new Scalar(70, 0, 255), new Scalar(90, 255, 255), yellowMat);

        /* 形态学处理 */
        //确定运算核
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        /* 开,闭运算 */
        Imgproc.morphologyEx(redMat, redMat, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(redMat, redMat, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(yellowMat, yellowMat, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(yellowMat, yellowMat, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(greenMat, greenMat, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(greenMat, greenMat, Imgproc.MORPH_CLOSE, kernel);

//        BitmapProcess.saveBitmap("r", redMat);
//        BitmapProcess.saveBitmap("y", yellowMat);
//        BitmapProcess.saveBitmap("g", greenMat);

        int Ir = 0;
        int Iy = 0;
        int Ig = 0;

        /* 二值化HSV数组的每个像素值不是0(黑)就是255(白) */
        for (int rows = 0; rows < redMat.rows(); rows++) {
            for (int cols = 0; cols < redMat.cols(); cols++) {
                double[] scalarVal = redMat.get(rows, cols);
                if (scalarVal[0] > 1) Ir++;
            }
        }
        for (int rows = 0; rows < yellowMat.rows(); rows++) {
            for (int cols = 0; cols < yellowMat.cols(); cols++) {
                double[] scalarVal = yellowMat.get(rows, cols);
                if (scalarVal[0] > 1) Iy++;
            }
        }
        for (int rows = 0; rows < greenMat.rows(); rows++) {
            for (int cols = 0; cols < greenMat.cols(); cols++) {
                double[] scalarVal = greenMat.get(rows, cols);
                if (scalarVal[0] > 1) Ig++;
            }
        }

        Log.i(TAG, "redPixel: " + Ir + "\nyellowPixel: " + Iy + "\ngreenPixel: " + Ig);
        destroy();

        /* 赛场上专用配套 */
        if (Ig > 1000) return "绿灯";
        return (Ir > 4000 || Iy < 500) ? "红灯" : "黄灯";
    }

    /**
     * 释放资源
     */
    private static void destroy() {
        redMat.release();
        greenMat.release();
        yellowMat.release();
    }
}
