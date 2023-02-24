package com.xiao.embeddedcar.Utils.Shape;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * 此内容由ChatGPT生成
 */
public class ShapeDetectionByChatGPT {
    public static void main(String[] args) {
        //加载本地OpenCV库文件
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //提供原图像
        Mat source = new Mat();
//        source = Imgproc.imread("input.jpg");

        Bitmap inputBitmap = BitmapFactory.decodeFile("");
        Utils.bitmapToMat(inputBitmap, source);

        //
        Mat destination = new Mat(source.rows(), source.cols(), source.type());
        //灰度化图像
        Imgproc.cvtColor(source, destination, Imgproc.COLOR_BGR2GRAY);
        //高斯模糊处理
        Imgproc.GaussianBlur(destination, destination, new Size(3, 3), 0);
        //
        Mat circles = new Mat();
        //执行霍夫圆变换
        //https://www.opencv.org.cn/opencvdoc/2.3.2/html/doc/tutorials/imgproc/imgtrans/hough_circle/hough_circle.html
        Imgproc.HoughCircles(destination, circles, Imgproc.CV_HOUGH_GRADIENT, 1, destination.rows() / 8.0, 200, 100, 0, 0);

        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            Imgproc.circle(source, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(source, center, radius, new Scalar(255, 0, 255), 3, 8, 0);
        }

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        //检测所有轮廓，所有轮廓建立一个等级树结构。外层轮廓包含内层轮廓，内层轮廓还可以继续包含内嵌轮廓
        Imgproc.findContours(destination, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.toArray().length; i++) {
            Imgproc.drawContours(source, contours, i, new Scalar(0, 0, 255), 2);
            double area = Imgproc.contourArea(contours.get(i));
            if (area > 30) {
                Rect rect = Imgproc.boundingRect(contours.get(i));
                if (rect.height == rect.width) {
                    Imgproc.putText(source, "Square", new Point(rect.x, rect.y), 1, 2, new Scalar(0, 255, 0));
                } else if (rect.height > rect.width) {
                    Imgproc.putText(source, "Rectangle", new Point(rect.x, rect.y), 1, 2, new Scalar(0, 255, 0));
                } else {
                    Imgproc.putText(source, "Triangle", new Point(rect.x, rect.y), 1, 2, new Scalar(0, 255, 0));
                }
            }
        }
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        for (int i = 0; i < contours.toArray().length; i++) {
            /* 获取包含旋转角度的最小外接矩形 */
            Rect minRect = Imgproc.boundingRect(contours.get(i));
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
            /* 计算轮廓的周长 */
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
            /* 多边形拟合 */
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
            if (approxCurve.total() == 5) {
                Imgproc.putText(source, "Pentagon", new Point(minRect.x, minRect.y), 1, 2, new Scalar(0, 255, 0));
            } else if (approxCurve.total() == 4) {
                Rect rect = Imgproc.boundingRect(contours.get(i));
                double ar = rect.width / (double) rect.height;
                if (ar >= 0.95 && ar <= 1.05) {
                    Imgproc.putText(source, "Diamond", new Point(rect.x, rect.y), 1, 2, new Scalar(0, 255, 0));
                } else {
                    Imgproc.putText(source, "Rectangle", new Point(rect.x, rect.y), 1, 2, new Scalar(0, 255, 0));
                }
            }
        }
//        Imgproc.imwrite("output.jpg", source);

    }
}