package com.xiao.embeddedcar.utils.PaddleOCR;

import static com.xiao.embeddedcar.utils.PublicMethods.ColorHSV.PlateDetector_HSV_VALUE_HIGH;
import static com.xiao.embeddedcar.utils.PublicMethods.ColorHSV.PlateDetector_HSV_VALUE_LOW;

import android.graphics.Bitmap;
import android.util.Log;

import com.xiao.baiduocr.TestInferOcrTask;
import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.utils.PublicMethods.BitmapProcess;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * 修改自: <a href="https://blog.csdn.net/chenyouledashen/article/details/118067702">这里</a>.
 * 仅供参考
 */
public class PlateDetector {

    private static final String TAG = PlateDetector.class.getSimpleName();
    //裁剪后识别使用的图片
    private Bitmap RectBitmap;

    public Bitmap getRectBitmap() {
        return RectBitmap;
    }

    /**
     * 检测车牌并识别
     *
     * @param inputBitmap TFT图片
     * @return 车牌识别结果
     */
    public String plateDetector(Bitmap inputBitmap) {

        String plateStr = null;

        Mat mRgba = Bitmap2Mat(inputBitmap);
        /*  ********************车牌识别******************** */
        //实现步骤1、直接HSV颜色空间裁剪出来车牌,计算长宽比来过滤掉
        //实现步骤2、阈值分割,边缘检测,检测完之后绘制填充
        //实现步骤3、填充之后二值化,二值化之后保存下来训练
//        show_bitmap(mRgba);//显示图片到View
        /* 创建用来存储图像信息的内存对象 */
        Mat gray = new Mat();
        Mat binary = new Mat();
        /* 灰度化图片 */
        Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_BGR2GRAY);
        /* 二值化  边缘检测 */
        Imgproc.Canny(gray, binary, 50, 150);
        /* 指定腐蚀膨胀核 */
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        /* 膨胀 */
        Imgproc.dilate(binary, binary, kernel);
        /* 查找轮廓 */
        List<MatOfPoint> contours = new ArrayList<>();
        /* 储存包含拓扑信息的Mat对象 */
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        //最大面积
        double maxArea = 0;
        //迭代器对象
        Iterator<MatOfPoint> each = contours.iterator();
        /* 查找最大轮廓的面积 */
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea) maxArea = area;
        }
        //结果Mat
        Mat result = null;
        //重置迭代器
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            //获取面积
            double area = Imgproc.contourArea(contour);
            if (area > 0.01 * maxArea) {
                /* 多边形逼近,会使原图放大4倍 */
                Core.multiply(contour, new Scalar(4, 4), contour);
                MatOfPoint2f newcoutour = new MatOfPoint2f(contour.toArray());
                MatOfPoint2f resultcoutour = new MatOfPoint2f();
                double length = Imgproc.arcLength(newcoutour, true);
                double epsilon = 0.01 * length;
                Imgproc.approxPolyDP(newcoutour, resultcoutour, epsilon, true);
                contour = new MatOfPoint(resultcoutour.toArray());
                /* 进行修正,缩小4倍改变联通区域大小 */
                @SuppressWarnings("UnusedAssignment") MatOfPoint new_contour = new MatOfPoint();
                new_contour = ChangeSize(contour);
                /* 轮廓的面积 */
                double new_area = Imgproc.contourArea(new_contour);
                /* 求取中心点 */
                Moments mm = Imgproc.moments(contour);
                int center_x = (int) (mm.get_m10() / (mm.get_m00()));
                int center_y = (int) (mm.get_m01() / (mm.get_m00()));
                Point center = new Point(center_x, center_y);
                /* 最小外接矩形 */
                Rect rect = Imgproc.boundingRect(new_contour);
                /* 最小外接矩形面积 */
                double rectArea = rect.area();
                if (Math.abs((new_area / rectArea) - 1) < 0.2) {
                    /* 宽高比值 */
                    double wh = rect.size().width / rect.size().height;
                    if (Math.abs(wh - 1.7) < 0.7 && rect.width > 250) {
                        /* 绘图 */
                        Mat imgSource = mRgba.clone();
                        /* 绘制外接矩形 */
                        Imgproc.rectangle(imgSource, rect.tl(), rect.br(), new Scalar(255, 0, 0), 2);
                        /* *****图片裁剪*****可以封装成函数***** */
                        rect.x += 5;
                        rect.width -= 10;
                        rect.y += 45;
                        rect.height -= 65;
                        result = new Mat(imgSource, rect);
                        /* *****图片裁剪***可以封装成函数***** */
                        /* 向上采样,放大图片 */
                        Imgproc.pyrUp(result, result);
                    }
                }
            }
        }
        if (result != null) {
            Log.i(TAG, "TFT屏幕裁剪成功");
            /* *****使用HSV阈值分割***** */
            Mat hsv_img = result.clone();
            /* 保存图片 */
//                save_pic(result,true);
            /* Hsv颜色空间转换 */
            Imgproc.cvtColor(hsv_img, hsv_img, Imgproc.COLOR_BGR2HSV);

            /* 车牌阈值分割 */
            Mat plate_blue = new Mat();
            /* 车牌蓝色底8 - 标准车牌底色 */
            int blue_pixel_num = 0;
            Core.inRange(hsv_img, new Scalar(PlateDetector_HSV_VALUE_LOW[8]), new Scalar(PlateDetector_HSV_VALUE_HIGH[8]), plate_blue);
            for (int x = 0; x < plate_blue.width(); x++) {
                for (int y = 0; y < plate_blue.height(); y++) {
                    double[] pixel = plate_blue.get(y, x);
                    if (pixel[0] == 255.0) {
                        // 如果是白色
                        blue_pixel_num++;
                    }
                }
            }
            Log.i(TAG, "蓝色车牌像素面积: " + blue_pixel_num);//42873
            if (blue_pixel_num > 20000 && blue_pixel_num < 90000) {
                Log.i(TAG, "进入蓝色车牌识别");
                plateStr = rect(plate_blue, result, 1);
            }

            /* 车牌阈值分割 */
            Mat plate_green = new Mat();
            /* 车牌绿色底10 - 新能源汽车 */
            Core.inRange(hsv_img, new Scalar(PlateDetector_HSV_VALUE_LOW[11]), new Scalar(PlateDetector_HSV_VALUE_HIGH[11]), plate_green);
            int green_pixel_num = 0;
            for (int x = 0; x < plate_green.width(); x++) {
                for (int y = 0; y < plate_green.height(); y++) {
                    double[] pixel = plate_green.get(y, x);
                    if (pixel[0] == 255.0) {
                        // 如果是白色
                        green_pixel_num++;
                    }
                }
            }
            Log.i(TAG, "绿色车牌像素面积: " + green_pixel_num);//42873
            if (green_pixel_num > 10000 && green_pixel_num < 90000) {
                Log.i(TAG, "进入绿色车牌识别");
                plateStr = rect(plate_green, result, 2);
            }
        }
        return plateStr;
    }

    /**
     * 仅检测车牌种类
     *
     * @param inputBitmap TFT图片
     * @return 车牌种类识别结果
     */
    public String plateOnlyDetector(Bitmap inputBitmap) {

        String plateTypeStr = null;
        Mat mRgba = Bitmap2Mat(inputBitmap);
        /*  ********************车牌种类识别******************** */
        //实现步骤1、直接HSV颜色空间裁剪出来车牌,计算长宽比来过滤掉
        //实现步骤2、阈值分割,边缘检测,检测完之后绘制填充
        //实现步骤3、填充之后二值化,二值化之后保存下来训练
//        show_bitmap(mRgba);//显示图片到View
        /* 创建用来存储图像信息的内存对象 */
        Mat gray = new Mat();
        Mat binary = new Mat();
        /* 灰度化图片 */
        Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_BGR2GRAY);
        /* 二值化  边缘检测 */
        Imgproc.Canny(gray, binary, 50, 150);
        /* 指定腐蚀膨胀核 */
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(binary, binary, kernel);
        /* 查找轮廓 */
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        //最大面积
        double maxArea = 0;
        //所有轮廓的迭代器
        Iterator<MatOfPoint> each = contours.iterator();
        /* 获取最大轮廓 */
        while (each.hasNext()) {
            //当前迭代的MatOfPoint对象
            MatOfPoint wrapper = each.next();
            //获取面积大小
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea) maxArea = area;
        }
        Mat result = null;
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            double area = Imgproc.contourArea(contour);
            if (area > 0.01 * maxArea) {
                /* 多边形逼近,会使原图放大4倍 */
                Core.multiply(contour, new Scalar(4, 4), contour);
                MatOfPoint2f newcoutour = new MatOfPoint2f(contour.toArray());
                MatOfPoint2f resultcoutour = new MatOfPoint2f();
                double length = Imgproc.arcLength(newcoutour, true);
                double epsilon = 0.01 * length;
                Imgproc.approxPolyDP(newcoutour, resultcoutour, epsilon, true);
                contour = new MatOfPoint(resultcoutour.toArray());
                /* 进行修正,缩小4倍改变联通区域大小 */
                @SuppressWarnings("UnusedAssignment") MatOfPoint new_contour = new MatOfPoint();
                new_contour = ChangeSize(contour);
                /* 轮廓的面积 */
                double new_area = Imgproc.contourArea(new_contour);
                /* 求取中心点 */
                Moments mm = Imgproc.moments(contour);
                int center_x = (int) (mm.get_m10() / (mm.get_m00()));
                int center_y = (int) (mm.get_m01() / (mm.get_m00()));
                Point center = new Point(center_x, center_y);
                /* 最小外接矩形 */
                Rect rect = Imgproc.boundingRect(new_contour);
                /* 最小外接矩形面积 */
                double rectArea = rect.area();
                if (Math.abs((new_area / rectArea) - 1) < 0.2) {
                    /* 宽高比值 */
                    double wh = rect.size().width / rect.size().height;
                    if (Math.abs(wh - 1.7) < 0.7 && rect.width > 250) {
                        /* 绘图 */
                        Mat imgSource = mRgba.clone();
                        /* 绘制外接矩形 */
                        Imgproc.rectangle(imgSource, rect.tl(), rect.br(), new Scalar(255, 0, 0), 2);
                        /* *****图片裁剪*****可以封装成函数***** */
                        //初始数据
//                        rect.x += 5;
//                        rect.width -= 10;
//                        rect.y += 45;
//                        rect.height -= 65;
                        //蓝色
                        rect.x += 5;
                        rect.width -= 10;
                        rect.y += 30;
                        rect.height -= 50;
                        //绿色
//                        rect.x += 75;
//                        rect.width -= 150;
//                        rect.y += 110;
//                        rect.height -= 150;
                        result = new Mat(imgSource, rect);
                        /* *****图片裁剪***可以封装成函数***** */
                        /* 向上采样,放大图片 */
                        Imgproc.pyrUp(result, result);
                    }
                }
            }
        }
        if (result != null) {
            Log.i(TAG, "TFT屏幕裁剪成功");
            /* *****使用HSV阈值分割***** */
            Mat hsv_img = result.clone();
            /* 创建裁剪后的图片的bitmap对象 */
            RectBitmap = Bitmap.createBitmap(hsv_img.width(), hsv_img.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(hsv_img, RectBitmap);
            /* Hsv颜色空间转换 */
            Imgproc.cvtColor(hsv_img, hsv_img, Imgproc.COLOR_BGR2HSV);

            /* ----------车牌阈值分割---------- */
            Mat plate_blue = new Mat();
            /* 车牌蓝色底8 - 标准车牌底色 */
            int blue_pixel_num = 0;
            Core.inRange(hsv_img, new Scalar(PlateDetector_HSV_VALUE_LOW[8]), new Scalar(PlateDetector_HSV_VALUE_HIGH[8]), plate_blue);
            for (int x = 0; x < plate_blue.width(); x++) {
                for (int y = 0; y < plate_blue.height(); y++) {
                    double[] pixel = plate_blue.get(y, x);
                    if (pixel[0] == 255.0) {
                        // 如果是白色
                        blue_pixel_num++;
                    }
                }
            }
            Log.i(TAG, "蓝色车牌像素面积: " + blue_pixel_num);//42873
            if (blue_pixel_num > 11000 && blue_pixel_num < 150000) {
                Log.i(TAG, "识别为蓝色车牌");
                plateTypeStr = "蓝";
            }

            /* ----------车牌阈值分割---------- */
            Mat plate_green = new Mat();
            /* 车牌绿色底10 - 新能源汽车 */
            Core.inRange(hsv_img, new Scalar(PlateDetector_HSV_VALUE_LOW[11]), new Scalar(PlateDetector_HSV_VALUE_HIGH[11]), plate_green);
            int green_pixel_num = 0;
            for (int x = 0; x < plate_green.width(); x++) {
                for (int y = 0; y < plate_green.height(); y++) {
                    double[] pixel = plate_green.get(y, x);
                    if (pixel[0] == 255.0) {
                        // 如果是白色
                        green_pixel_num++;
                    }
                }
            }
            Log.i(TAG, "绿色车牌像素面积: " + green_pixel_num);//42873
            if (green_pixel_num > 10000 && green_pixel_num < 90000) {
                Log.i(TAG, "识别为绿色车牌");
                plateTypeStr = "绿";
            }
            /*TODO
             * 可往下添加需要识别的车牌种类
             */
        }
        return plateTypeStr;
    }

    /**
     * <p>通过HSV阈值得到的new_mask车牌的外矩形,new_src为了切割传进来的值</p>
     * <p>需要调整分割出来的矩形宽的长度和宽度,adaptiveThreshold要调节自适应阈值</p>
     * <p>plateColor: 1 - 识别蓝色车牌</p>
     * <p>plateColor: 2 - 识别绿色车牌</p>
     *
     * @param new_mask   车牌的外矩形
     * @param new_src    切割传入的值
     * @param plateColor 车牌颜色
     * @return 车牌号
     */
    public String rect(Mat new_mask, Mat new_src, int plateColor) {
        String result_str = null;
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        /* 查找轮廓 */
        Imgproc.findContours(new_mask, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea) maxArea = area;
        }
        Mat result;
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            double area = Imgproc.contourArea(contour);
            if (area > 0.01 * maxArea) {
                /* 多边形逼近,会使原图放大4倍 */
                Core.multiply(contour, new Scalar(4, 4), contour);
                MatOfPoint2f newContour = new MatOfPoint2f(contour.toArray());
                MatOfPoint2f resultContour = new MatOfPoint2f();
                double length = Imgproc.arcLength(newContour, true);
                double epsilon = 0.01 * length;
                Imgproc.approxPolyDP(newContour, resultContour, epsilon, true);
                contour = new MatOfPoint(resultContour.toArray());
                /* 进行修正,缩小4倍改变联通区域大小 */
                @SuppressWarnings("UnusedAssignment") MatOfPoint new_contour = new MatOfPoint();
                new_contour = ChangeSize(contour);
                /* 轮廓的面积 */
                double new_area = Imgproc.contourArea(new_contour);
                /* 最小外接矩形 */
                Rect rect = Imgproc.boundingRect(new_contour);
                /* 最小外接矩形面积 */
                double rectArea = rect.area();
                Log.i(TAG, "轮廓的面积: " + new_area);
                Log.i(TAG, "最小外接矩形面积: " + rectArea);
                Log.i(TAG, String.valueOf(Math.abs((new_area / rectArea) - 1)));
                if (Math.abs((new_area / rectArea) - 1) < 0.3) {
                    Log.i(TAG, "车牌宽度 " + rect.width);
                    if (rect.width > 300) {
                        Mat imgSource = new_src.clone();
                        Imgproc.rectangle(imgSource, rect.tl(), rect.br(), new Scalar(255, 0, 0), 2);
                        if (plateColor == 1) {
                            /* **********蓝色车牌裁剪范围********** */
//                            rect.x += 8;
                            rect.x += 65;
//                            rect.width -= 15;
                            rect.width -= 69;
                            rect.y += 8;
                            rect.height -= 15;
                            /* **********蓝色车牌裁剪范围********** */
                        }
                        if (plateColor == 2) {
                            /* **********绿色车牌裁剪范围********** */
                            rect.x += 97;
                            rect.width -= 109;
                            rect.y += 8;
                            rect.height -= 15;
                            /* **********绿色车牌裁剪范围********** */
                        }
                        result = new Mat(imgSource, rect);
                        Mat gray = new Mat();
                        /* 灰度化 */
                        Imgproc.cvtColor(result, gray, Imgproc.COLOR_BGR2GRAY);
                        /* 字体黑色时,要反色 */
                        if (plateColor == 2) {
                            /* 绿色的,要取反(因为绿色中间的字是黑色的)   蓝色的不用(因为蓝色中间的字是白色的) */
                            Core.bitwise_not(gray, gray);
                        }
                        Mat threshold = new Mat();
                        /* 蓝色自适应阈值 */
                        if (plateColor == 1) {
                            Imgproc.adaptiveThreshold(gray, threshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 111, -7);
                            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_ERODE, kernel);
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_ERODE, kernel);
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_DILATE, kernel);
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_DILATE, kernel);
                        }
                        /* 绿色自适应阈值 */
                        if (plateColor == 2) {
                            Imgproc.adaptiveThreshold(gray, threshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 111, -7);
                            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_ERODE, kernel);
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_ERODE, kernel);
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_ERODE, kernel);
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_ERODE, kernel);
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_DILATE, kernel);
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_DILATE, kernel);
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_DILATE, kernel);
                            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_DILATE, kernel);
                        }
                        Bitmap threshold_bitmap = Mat2Bitmap(threshold);
                        /* num4 和num6比较准确 */
//                        String baiduOCR_str = doOcr(threshold_bitmap, "eng");
                        BitmapProcess.saveBitmap("车牌", threshold_bitmap);
                        String baiduOCR_str = TestInferOcrTask.getInstance().detector(threshold_bitmap);
                        Log.i(TAG, "*****这里是车牌号*****" + baiduOCR_str);
                        if (baiduOCR_str != null) {
                            if (baiduOCR_str.length() >= 6 && baiduOCR_str.length() <= 10) {
//                                result_str = plateString(baiduOCR_str);
                                result_str = completion(baiduOCR_str);
                                Log.i(TAG, "车牌处理后结果: " + result_str);
                                return result_str;
                            }
                        }
                    }
                }
            }
        }
        return result_str;
    }

    //车牌发送给道闸
    public static void plateToGate(String plateResult) {
        if (plateResult != null && plateResult.length() == 6) {
            Log.i(TAG, "正在发送车牌识别结果1：" + plateResult);
            ConnectTransport ct = ConnectTransport.getInstance();
            ct.YanChi(100);
            ct.gate(0x10, plateResult.charAt(0), plateResult.charAt(1), plateResult.charAt(2));
            ct.YanChi(100);//多发几次防止数据丢失
            ct.gate(0x10, plateResult.charAt(0), plateResult.charAt(1), plateResult.charAt(2));
            ct.YanChi(100);//多发几次防止数据丢失
            ct.gate(0x10, plateResult.charAt(0), plateResult.charAt(1), plateResult.charAt(2));
            ct.YanChi(100);
            ct.gate(0x11, plateResult.charAt(3), plateResult.charAt(4), plateResult.charAt(5));
            ct.YanChi(100);
            ct.gate(0x11, plateResult.charAt(3), plateResult.charAt(4), plateResult.charAt(5));
            ct.YanChi(100);
            ct.gate(0x11, plateResult.charAt(3), plateResult.charAt(4), plateResult.charAt(5));
            ct.YanChi(100);
            Log.i(TAG, "正在发送车牌识别结果2：" + plateResult);
        }
    }

    /**
     * 车牌字符串处理
     * 将传入的车牌字符串进行识别
     */
    @Deprecated
    private String plateString(String plateResult) {
        /* 后面的六位字符toLowerCase() - 大写转小写 */
        try {
            plateResult = plateResult.replaceAll(" ", "");
            /* 后面的六位字符toUpperCase()  小写转大写 */
            String platNumber = plateResult.substring(plateResult.length() - 6).toUpperCase();
            StringBuilder strBuilder = new StringBuilder(platNumber);
            /* 按照A123B4  的格式   进行相似匹配替换 */
            plateReplace(strBuilder);
            /* 输出结果 */
            platNumber = strBuilder.toString();
            plateResult = platNumber;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plateResult;
    }

    /**
     * 过滤与补全
     *
     * @param plateResult 车牌识别结果
     * @return 过滤后的车牌
     */
    public static String completion(String plateResult) {
        StringBuilder sb = new StringBuilder();
        for (char ch : plateResult.toCharArray()) {
            /* 如果为数字或字母则添加进sb中 */
            if (Character.isDigit(ch) || Character.isUpperCase(ch) || Character.isLowerCase(ch))
                sb.append(ch);
        }
        /* 不满6个数则补全到6 */
        while (sb.toString().length() < 6) sb.append(0);
        /* 按照A123B4的格式进行相似匹配替换 */
        plateReplace(sb);
        return sb.toString().toUpperCase(Locale.ROOT);
    }

    /**
     * 车牌数据替换,减少错误率
     */
    private static StringBuilder plateReplace(StringBuilder platNumber) {

        if (platNumber.charAt(1) >= 'A' && platNumber.charAt(1) <= 'Z')
            charToNum(platNumber, 1);//第1、2、3、5位本应该为数字,如果出现识别为字符就将其转换为数字
        if (platNumber.charAt(2) >= 'A' && platNumber.charAt(2) <= 'Z')
            charToNum(platNumber, 2);//第1、2、3、5位本应该为数字,如果出现识别为字符就将其转换为数字
        if (platNumber.charAt(3) >= 'A' && platNumber.charAt(3) <= 'Z')
            charToNum(platNumber, 3);//第1、2、3、5位本应该为数字,如果出现识别为字符就将其转换为数字
        if (platNumber.charAt(5) >= 'A' && platNumber.charAt(5) <= 'Z')
            charToNum(platNumber, 5);//第1、2、3、5位本应该为数字,如果出现识别为字符就将其转换为数字
        if (platNumber.charAt(0) >= '0' && platNumber.charAt(0) <= '9')
            numToChar(platNumber, 0);//第0、4位本应该为字符,如果出现识别为数字就将其转换为字符
        if (platNumber.charAt(4) >= '0' && platNumber.charAt(4) <= '9')
            numToChar(platNumber, 4);//第0、4位本应该为字符,如果出现识别为数字就将其转换为字符

//        platNumber=numToChar(platNumber,4);
//        sleep(10);

        return platNumber;
    }

    /**
     * 车牌中的数字转换为字符
     * 车牌:H833E8    位置：i=(0)、（4）
     */
    private static StringBuilder numToChar(StringBuilder platNumber, int i) {
        char a = platNumber.charAt(i);
        switch (a) {
            case '0':
                platNumber.setCharAt(i, 'D');
                break;
            case '1':
                platNumber.setCharAt(i, 'I');
                break;
            case '2':
                platNumber.setCharAt(i, 'Z');
                break;
            case '3':
                platNumber.setCharAt(i, 'B');
                break;
            case '4':
                platNumber.setCharAt(i, 'A');
                break;
            case '5':
                platNumber.setCharAt(i, 'S');
                break;
            case '6':
                platNumber.setCharAt(i, 'G');
                break;
            case '7':
                platNumber.setCharAt(i, 'T');
                break;
            case '8':
                platNumber.setCharAt(i, 'B');
                break;
            case '9':
                break;
        }
        return platNumber;
    }

    /**
     * 车牌中字符转换为数字
     * 车牌:H833E8    位置：i=(1 2 3)、（5）
     */
    private static StringBuilder charToNum(StringBuilder platNumber, int i) {
        char a = platNumber.charAt(i);
        switch (a) {
            case 'A':
                platNumber.setCharAt(i, '4');
                break;
            case 'B':
                platNumber.setCharAt(i, '8');
                break;
            case 'C':
                platNumber.setCharAt(i, '0');
                break;
            case 'D':
                platNumber.setCharAt(i, '4');
                break;
            case 'E':
            case 'F':
                break;
            case 'G':
                platNumber.setCharAt(i, '6');
                break;
            case 'H':
                break;
            case 'I':
                platNumber.setCharAt(i, '1');
                break;
            case 'J':
                break;
            case 'K':
                break;
            case 'L':
                platNumber.setCharAt(i, '1');
                break;
            case 'M':
                break;
            case 'N':
                break;
            case 'O':
                platNumber.setCharAt(i, '0');
                break;
            case 'P':
                break;
            case 'Q':
                break;
            case 'R':
                break;
            case 'S':
                platNumber.setCharAt(i, '5');
                break;
            case 'T':
                platNumber.setCharAt(i, '7');
                break;
            case 'U':
                break;
            case 'V':
                break;
            case 'W':
                break;
            case 'X':
                break;
            case 'Y':
                break;
            case 'Z':
                platNumber.setCharAt(i, '2');
                break;
            case '?':
                platNumber.setCharAt(i, '9');
                break;
        }

        return platNumber;
    }

    // 转换工具
    public static Bitmap Mat2Bitmap(Mat cannyMat) {
        Bitmap bmpCanny = Bitmap.createBitmap(cannyMat.cols(), cannyMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cannyMat, bmpCanny);
        return bmpCanny;
    }

    // 转换工具
    public static Mat Bitmap2Mat(Bitmap bmp) {
        Mat mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bmp, mat);
        return mat;
    }

    // 把坐标降低到4分之一
    private MatOfPoint ChangeSize(MatOfPoint contour) {
        for (int i = 0; i < contour.height(); i++) {
            double[] p = contour.get(i, 0);
            p[0] = p[0] / 4;
            p[1] = p[1] / 4;
            contour.put(i, 0, p);
        }
        return contour;
    }
}
