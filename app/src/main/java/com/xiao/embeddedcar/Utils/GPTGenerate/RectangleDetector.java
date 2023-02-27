package com.xiao.embeddedcar.Utils.GPTGenerate;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class RectangleDetector {

    // 加载openCV库
//    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }

    public static void main(String[] args) {
        // 加载图像，并转换为灰度图
        Mat image = Imgcodecs.imread("rectangle.jpg");
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // 检测边缘，并得到一个二值化的图像
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 100, 200);

        // 寻找轮廓，并得到一个轮廓列表
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);

        // 遍历轮廓列表，对每个轮廓近似一个多边形，并判断它是否是一个矩形
        for (MatOfPoint contour : contours) {
            // 近似一个多边形
            double epsilon = 0.01 * Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, epsilon, true);

            // 判断是否是一个矩形
            if (approx.total() == 4 && Math.abs(Imgproc.contourArea(approx)) > 1000 &&
                    Imgproc.isContourConvex(new MatOfPoint(approx.toArray()))) {
                // 获取顶点坐标
                Point[] points = approx.toArray();
                Point p1 = points[0];
                Point p2 = points[1];
                Point p3 = points[2];
                Point p4 = points[3];

                // 计算两个顶点之间的距离，也就是边长
//                double length1 = Core.norm(new Scalar(p1.x - p2.x, p1.y - p2.y));
//                double length2 = Core.norm(new Scalar(p2.x - p3.x, p2.y - p3.y));
//                double length3 = Core.norm(new Scalar(p3.x - p4.x, p3.y - p4.y));
//                double length4 = Core.norm(new Scalar(p4.x - p1.x, p4.y - p1.y));

                double length1 = getDistance(p1, p2);
                double length2 = getDistance(p2, p3);
                double length3 = getDistance(p3, p4);
                double length4 = getDistance(p4, p1);

                // 计算两条相邻边之间的夹角，使用弧度制表示
                double angle1 =
                        Math.atan2(Math.abs(p1.y - p2.y), Math.abs(p1.x - p2.x));
                double angle2 =
                        Math.atan2(Math.abs(p2.y - p3.y), Math.abs(p2.x - p3.x));
                double angle3 =
                        Math.atan2(Math.abs(p3.y - p4.y), Math.abs(p3.x - p4.x));
                double angle4 =
                        Math.atan2(Math.abs(p4.y - p1.y), Math.abs(p4.x - p1.x));

                // 打印结果
                System.out.println("Found a rectangle with:");
                System.out.println("Lengths: " + length1 + ", " + length2 + ", "
                        + length3 + ", " + length4);
                System.out.println("Angles: " + angle1 + ", " + angle2 + ", "
                        + angle3 + ", " + angle4);
            }
        }
    }


    /* ***********************************************************************
     *函数名：        getDistance
     *
     *函数作用：      获取两点之间的距离
     *
     *函数参数：
     *CvPoint2D32f pointO  - 起点
     *CvPoint2D32f pointA  - 终点
     *
     *函数返回值：
     *double           两点之间的距离
     ************************************************************************* */
    /**
     * 计算两点之间的距离
     *
     * @param point1 -
     * @param point2 -
     * @return 两点距离
     */
    public static double getDistance(Point point1, Point point2) {
//        double distance;
//        distance = Math.pow((pointO.x - pointA.x), 2) + Math.pow((pointO.y - pointA.y), 2);
//        distance = Math.sqrt(distance);
//        return distance;
        return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }
}