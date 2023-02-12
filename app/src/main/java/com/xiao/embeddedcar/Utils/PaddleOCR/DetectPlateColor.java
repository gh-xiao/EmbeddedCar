package com.xiao.embeddedcar.Utils.PaddleOCR;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.baidu.ai.edge.core.ocr.OcrResultModel;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class DetectPlateColor {

    public static String getColor(Bitmap inputBitmap, OcrResultModel result) {
        /* 获取左上角最小的点 */
        int MIN_X = Integer.MAX_VALUE;
        int MIN_Y = Integer.MAX_VALUE;
        /* 获取右下角最大的点 */
        int MAX_X = Integer.MIN_VALUE;
        int MAX_Y = Integer.MIN_VALUE;

        if (result.getPoints().size() > 0) {
            for (Point p : result.getPoints()) {
                MIN_X = Math.min(MIN_X, p.x);
                MAX_X = Math.max(MAX_X, p.x);
                MIN_Y = Math.min(MIN_Y, p.y);
                MAX_Y = Math.max(MAX_Y, p.y);
            }
        }
        int x = MIN_X;
        int y = MIN_Y;
        int width = MAX_X - MIN_X;
        int height = MAX_Y - MIN_Y;

        Bitmap ROI = Bitmap.createBitmap(inputBitmap, x, y, width, height);

        return detectColor(ROI);
    }

    private static String detectColor(Bitmap inputBitmap) {
        /* openCV创建用来存储图像信息的内存对象 */
        Mat srcMat = new Mat();
        Mat blueMat = new Mat();
        Mat greenMat = new Mat();

        /* 转化为Mat对象 */
        Utils.bitmapToMat(inputBitmap, srcMat);

        /* 转换为HSV */
        Imgproc.cvtColor(srcMat, blueMat, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(srcMat, greenMat, Imgproc.COLOR_RGB2HSV);

        /* 二值化 */
        Core.inRange(blueMat, new Scalar(95, 200, 180), new Scalar(130, 255, 255), blueMat);
        Core.inRange(greenMat, new Scalar(60, 100, 145), new Scalar(100, 200, 255), greenMat);

        /* 形态学处理 */
        //确定运算核
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        /* 开运算(除去白色噪点) */
        Imgproc.morphologyEx(blueMat, blueMat, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(greenMat, greenMat, Imgproc.MORPH_OPEN, kernel);
        /* 闭运算(除去黑色噪点) */
        Imgproc.morphologyEx(blueMat, blueMat, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(greenMat, greenMat, Imgproc.MORPH_CLOSE, kernel);
        /* 膨胀操作(扩大白色联通区域) */
        Imgproc.morphologyEx(blueMat, blueMat, Imgproc.MORPH_DILATE, kernel);
        Imgproc.morphologyEx(greenMat, greenMat, Imgproc.MORPH_DILATE, kernel);

        int green = 0;
        int blue = 0;

        for (int rows = 0; rows < blueMat.rows(); rows++) {
            for (int cols = 0; cols < blueMat.cols(); cols++) {
                double[] scalarVal = blueMat.get(rows, cols);
                if (scalarVal[0] > 1) blue++;
            }
        }

        for (int rows = 0; rows < greenMat.rows(); rows++) {
            for (int cols = 0; cols < greenMat.cols(); cols++) {
                double[] scalarVal = greenMat.get(rows, cols);
                if (scalarVal[0] > 1) green++;
            }
        }
//        Log.i(TAG, "Green: " + green + "\n" + "Blue" + blue);

        if (green < 100 && blue < 100) return "No Plate";
        return green >= blue ? "green" : "blue";
    }
}