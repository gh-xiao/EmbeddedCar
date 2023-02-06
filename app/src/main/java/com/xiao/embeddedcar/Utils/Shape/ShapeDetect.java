//package com.xiao.embeddedcar.Utils.Shape;
//
//
//
//import static com.xiao.embeddedcar.Utils.PublicMethods.ColorHSV.HSV_VALUE_HIGH;
//import static com.xiao.embeddedcar.Utils.PublicMethods.ColorHSV.HSV_VALUE_LOW;
//
//import android.graphics.Bitmap;
//import android.util.Log;
//
//import org.opencv.android.Utils;
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfPoint;
//import org.opencv.core.MatOfPoint2f;
//import org.opencv.core.Point;
//import org.opencv.core.Rect;
//import org.opencv.core.RotatedRect;
//import org.opencv.core.Scalar;
//import org.opencv.core.Size;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.imgproc.Moments;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//
///**
// * 测试代码
// * 无法保证正常使用
// */
//@Deprecated
//public class ShapeDetect {
//
//    public static String TAG = "ShapeDetect";
//
//    public static final int FONT_HERSHEY_PLAIN = 1;
//    //颜色数量统计变量
//    public static int Cambridge_blue_Num = 0, yellow_Num = 0, blue_Num = 0, qing_Num = 0, red_Num = 0, mag_Num = 0, black_Num = 0;
//    //白底照片图形数量统计变量
//    public static int triangle_Num = 0, rectangle_Num = 0, rhombus_Num = 0, pentagon_Num = 0, circle_Num = 0;
//    //图形数量统计变量
//    public static int san_Num = 0, rect_Num = 0, lin_Num = 0, star_Num = 0, yuan_Num = 0;
//
//
//    // 转换工具
//    public static Mat BitmapToMat(Bitmap bmp) {
//        Mat mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
//        Utils.bitmapToMat(bmp, mat);
//
//        return mat;
//    }
//
//    public int[] shapeDetect(Bitmap bmp) {
//
//        Cambridge_blue_Num = 0;
//        yellow_Num = 0;
//        blue_Num = 0;
//        qing_Num = 0;
//        red_Num = 0;
//        mag_Num = 0;
//        black_Num = 0;
//        triangle_Num = 0;
//        rectangle_Num = 0;
//        rhombus_Num = 0;
//        pentagon_Num = 0;
//        circle_Num = 0;
//        san_Num = 0;
//        rect_Num = 0;
//        lin_Num = 0;
//        star_Num = 0;
//        yuan_Num = 0;
//        //最大面积查找之后要清零
//        Max_area = 0;
//        //参数复位
//        Max_area_Yanse = "品红色";
//        //参数复位
//        Max_area_shape = "triangle";
//        //false为黑底(默认黑底)  true为白底
//        boolean black_white_Flag = false;
//
//        Mat mRgba = BitmapToMat(bmp);
////        save_pic(mRgba,1);
////        Mat mRgba=read_pic(false,"plate1.jpg",1);
//        Mat gray = new Mat();
//        /* 灰度化 */
//        Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_BGR2GRAY);
//
//        Mat binary = new Mat();
//        /* 二值化  边缘检测 */
//        Imgproc.Canny(gray, binary, 50, 150);
//        /* 指定腐蚀膨胀核 */
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
//        Imgproc.dilate(binary, binary, kernel);
//
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        /* 查找轮廓 */
//        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        double maxArea = 0;
//        Iterator<MatOfPoint> each = contours.iterator();
//        while (each.hasNext()) {
//            MatOfPoint wrapper = each.next();
//            double area = Imgproc.contourArea(wrapper);
//            if (area > maxArea) maxArea = area;
//        }
//
//        Mat result = null;
//        each = contours.iterator();
//        while (each.hasNext()) {
//            MatOfPoint contour = each.next();
//            double area = Imgproc.contourArea(contour);
//            if (area > 0.01 * maxArea) {
//                /* 多边形逼近 会使原图放大4倍 */
//                Core.multiply(contour, new Scalar(4, 4), contour);
//                MatOfPoint2f newcoutour = new MatOfPoint2f(contour.toArray());
//                MatOfPoint2f resultcoutour = new MatOfPoint2f();
//                double length = Imgproc.arcLength(newcoutour, true);
//                double epsilon = 0.01 * length;
//                Imgproc.approxPolyDP(newcoutour, resultcoutour, epsilon, true);
//                contour = new MatOfPoint(resultcoutour.toArray());
//                /* 进行修正，缩小4倍改变联通区域大小 */
//                @SuppressWarnings("UnusedAssignment") MatOfPoint new_contour = new MatOfPoint();
//                new_contour = ChangeSize(contour);
//                /* 轮廓的面积 */
//                double new_area = Imgproc.contourArea(new_contour);
//                /* 求取中心点 */
//                Moments mm = Imgproc.moments(contour);
//                int center_x = (int) (mm.get_m10() / (mm.get_m00()));
//                int center_y = (int) (mm.get_m01() / (mm.get_m00()));
//                Point center = new Point(center_x, center_y);
//                /* 最小外接矩形 */
//                Rect rect = Imgproc.boundingRect(new_contour);
//                /* 最小外接矩形面积 */
//                double rectarea = rect.area();
//                /* 轮廓的面积/最小外接矩形面积(一个圆和一个圆的外接矩形)  一定小于1 一般为0.1 0.2 */
//                if (Math.abs((new_area / rectarea) - 1) < 0.2) {
//                    /* 宽高比值 */
//                    double wh = rect.size().width / rect.size().height;
//                    if (Math.abs(wh - 1.7) < 0.7 && rect.width > 250) {
//                        Mat imgSource = mRgba.clone();
//                        /* 绘制外接矩形 */
//                        Imgproc.rectangle(imgSource, rect.tl(), rect.br(),
//                                new Scalar(0, 0, 255), 2);
//                        /* *****图片裁剪*****可以封装成函数***** */
//                        rect.x += 5;
//                        rect.width -= 25;
//                        rect.y += 2;// 10
//                        rect.height -= 3; //12
//                        result = new Mat(imgSource, rect);
//                        /* 剪切后的图片复制一份 */
//                        Mat black_while = result.clone();
//                        /* 存储剪切后的图片灰度化 */
//                        Mat black_while_gray = new Mat();
//                        /* 灰度化图片 */
//                        Imgproc.cvtColor(black_while, black_while_gray, Imgproc.COLOR_BGR2GRAY);
//                        /* 存储二值化后的图片 */
//                        Mat hsv_gray_mask = new Mat();
//                        Imgproc.threshold(black_while_gray, hsv_gray_mask, 50, 255, Imgproc.THRESH_BINARY);
//                        /* 放大规定的大小 */
//                        Imgproc.resize(hsv_gray_mask, hsv_gray_mask, new Size(303, 183));
//                        /* 统计黑白底和白底的像素差，用于判断是黑白底还是白底 */
//                        int black_white_pixle_num = 0;
//                        for (int x = 0; x < hsv_gray_mask.width(); x++) {
//                            for (int y = 0; y < hsv_gray_mask.height(); y++) {
//                                double[] pixel = hsv_gray_mask.get(y, x);
//                                /* 如果是白色 */
//                                if (pixel[0] == 255.0) black_white_pixle_num++;
//                            }
//                        }
//                        /* 白底的白色像素比较多 */
//                        /* 白底 */
//                        if (black_white_pixle_num > 41000) black_white_Flag = true;
//                        /* *****图片裁剪*****可以封装成函数***** */
//                        /* 向上采样,放大图片 */
//                        Imgproc.pyrUp(result, result);
//                    }
//                }
//            }
//        }
//
//
//        if (result != null) {
//            /* *****使用HSV阈值分割***** */
//            Mat hsv_img = result.clone();
//            /* Hsv颜色空间转换 */
//            Imgproc.cvtColor(hsv_img, hsv_img, Imgproc.COLOR_BGR2HSV);
////            show_bitmap(hsv_img);
//            /* 浅蓝色0阈值分割 */
//            Mat Cambridge_blue = new Mat();
//            Core.inRange(hsv_img, new Scalar(HSV_VALUE_LOW[0]), new Scalar(HSV_VALUE_HIGH[0]), Cambridge_blue);
//            Imgproc.erode(Cambridge_blue, Cambridge_blue, kernel);
//            /* 浅蓝色0颜色数量和该颜色对应的图形 */
//            yanse(Cambridge_blue, 0);
//            /* 黄色1阈值分割 */
//            Mat yellow = new Mat();
//            Core.inRange(hsv_img, new Scalar(HSV_VALUE_LOW[1]), new Scalar(HSV_VALUE_HIGH[1]), yellow);
//            Imgproc.erode(yellow, yellow, kernel);
//            /* 黄色1颜色数量和该颜色对应的图形 */
//            yanse(yellow, 1);
//            /* 品红2阈值分割 */
//            Mat purple = new Mat();
//            Core.inRange(hsv_img, new Scalar(HSV_VALUE_LOW[2]), new Scalar(HSV_VALUE_HIGH[2]), purple);
//            Imgproc.erode(purple, purple, kernel);
//            Imgproc.erode(purple, purple, kernel);
//            /* 品红2颜色数量和该颜色对应的图形 */
//            yanse(purple, 2);
//            /* 浅红色3、深红色6阈值分割 */
//            Mat red = new Mat();
//            Mat dark_red = new Mat();
//            /* 浅红色阈值 */
//            Core.inRange(hsv_img, new Scalar(HSV_VALUE_LOW[3]), new Scalar(HSV_VALUE_HIGH[3]), red);
//            /* 深红色阈值 */
//            Core.inRange(hsv_img, new Scalar(HSV_VALUE_LOW[6]), new Scalar(HSV_VALUE_HIGH[6]), dark_red);
//            Core.bitwise_or(red, dark_red, red);
//            Imgproc.erode(red, red, kernel);
//            /* 红色6颜色数量和该颜色对应的图形 */
//            yanse(red, 6);
//            /* 蓝色4阈值分割 */
//            Mat blue = new Mat();
//            Core.inRange(hsv_img, new Scalar(HSV_VALUE_LOW[4]), new Scalar(HSV_VALUE_HIGH[4]), blue);
//            Imgproc.erode(blue, blue, kernel);
//            Imgproc.erode(blue, blue, kernel);
//            yanse(blue, 4);
//            /* 青色5阈值分割 */
//            Mat cyan = new Mat();
//            Core.inRange(hsv_img, new Scalar(HSV_VALUE_LOW[5]), new Scalar(HSV_VALUE_HIGH[5]), cyan);
//            Imgproc.erode(cyan, cyan, kernel);
//            yanse(cyan, 5);
//            /* 黑色7阈值分割   白底 黑色进行阈值分割 */
//            Mat black = new Mat();
//            if (black_white_Flag) {
//                Core.inRange(hsv_img, new Scalar(HSV_VALUE_LOW[7]), new Scalar(HSV_VALUE_HIGH[7]), black);
//                Imgproc.erode(black, black, kernel);
//                yanse(black, 7);
//            }
//
//            Log.e(TAG, "浅蓝色个数：" + Cambridge_blue_Num + "黄色个数：" + yellow_Num + "品红色个数:" + mag_Num + "蓝色个数：" + blue_Num + "青色个数:" + qing_Num + "红色个数:" + red_Num);
//
//            Mat hsv_mask = Mat.zeros(cyan.size(), cyan.type());
//            Core.bitwise_or(hsv_mask, Cambridge_blue, hsv_mask);
//            Core.bitwise_or(hsv_mask, yellow, hsv_mask);
//            Core.bitwise_or(hsv_mask, purple, hsv_mask);
//            Core.bitwise_or(hsv_mask, red, hsv_mask);
//            Core.bitwise_or(hsv_mask, blue, hsv_mask);
//            Core.bitwise_or(hsv_mask, cyan, hsv_mask);
//
//            /* 白底 */
//            if (black_white_Flag) Core.bitwise_or(hsv_mask, black, hsv_mask);
//
//            Imgproc.erode(hsv_mask, hsv_mask, kernel);
//            /* *****HSV阈值分割***** */
//
//
//            /* *****使用Canny阈值分割来弄***** */
//            Mat resutl_mask = null;
//            Mat canny_all_hierarchy = new Mat();
//            /* 复制截取上采样之后的图片 */
//            Mat canny_new_img = result.clone();
//            if (!black_white_Flag) {
//                /* 复制截取上采样之后的图片 */
//                Mat canny_img = result.clone();
//                /* 灰度化 */
//                Mat canny_gray = new Mat();
//                /* 灰度化处理 */
//                Imgproc.cvtColor(canny_img, canny_gray, Imgproc.COLOR_BGR2GRAY);
//
//                Mat canny = new Mat();
//                /* 边缘化二值化检测 */
//                Imgproc.Canny(canny_gray, canny, 50, 150);
//                /* 指定腐蚀膨胀核 */
//                Mat canny_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(1, 1));
//                /* 闭操作，先膨胀后腐蚀清楚小黑点，清楚连通区域 */
//                Imgproc.morphologyEx(canny, canny, Imgproc.MORPH_ERODE, canny_kernel, new Point(-1, -1), 3);
//                /* 指定腐蚀膨胀核 */
//                Mat canny_kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
//                /* 闭操作，先膨胀后腐蚀清楚小黑点，清楚连通区 */
//                Imgproc.morphologyEx(canny, canny, Imgproc.MORPH_CLOSE, canny_kernel2);
//                /* 闭操作，先膨胀后腐蚀清楚小黑点，清楚连通区 */
//                Imgproc.morphologyEx(canny, canny, Imgproc.MORPH_CLOSE, canny_kernel2);
//                Imgproc.morphologyEx(canny, canny, Imgproc.MORPH_DILATE, canny_kernel2);
//
//                List<MatOfPoint> canny_all_contours = new ArrayList<>();
//
//                /* 边缘化之后再查找所有的轮廓 */
//                Imgproc.findContours(canny, canny_all_contours, canny_all_hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//                Mat canny_new_img2 = new Mat(canny_new_img.size(), canny_new_img.type(), new Scalar(0, 0, 0));
//                for (int i = 0; i < canny_all_contours.size(); i++) {
//                    double area = Imgproc.contourArea(canny_all_contours.get(i));
//                    if (area > 300) {
//                        Imgproc.drawContours(canny_new_img2, canny_all_contours,
//                                i, new Scalar(0, 0, 255), -1, Imgproc.LINE_4, canny_all_hierarchy, 1, new Point(0, 0));
//                        Imgproc.morphologyEx(canny_new_img2, canny_new_img2, Imgproc.MORPH_OPEN, kernel);
//                    }
//                }
//
//                Mat mask_blue = new Mat();
//                /* 常规蓝色 */
//                Core.inRange(canny_new_img2, new Scalar(HSV_VALUE_LOW[8]), new Scalar(HSV_VALUE_HIGH[8]), mask_blue);
//                Imgproc.erode(mask_blue, mask_blue, kernel);
//                Imgproc.erode(mask_blue, mask_blue, kernel);
//
//                Core.bitwise_or(mask_blue, hsv_mask, mask_blue);
//
//                resutl_mask = mask_blue.clone();
//            }
//            /* *****使用Canny阈值分割来弄***** */
//
//
//            if (resutl_mask == null) resutl_mask = hsv_mask;
//
//            /* *****查找整张图片的轮廓，然后绘制出来***** */
//            /* 1、轮廓查找 */
//            List<MatOfPoint> resutl_contours = new ArrayList<>();
//            /* 边缘化之后再查找所有的轮廓 */
//            Imgproc.findContours(resutl_mask, resutl_contours, canny_all_hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//            Imgproc.drawContours(canny_new_img, resutl_contours, -1, new Scalar(255, 0, 0), 3);
//            san_Num = rect_Num = lin_Num = star_Num = yuan_Num = 0;
//            for (MatOfPoint c : resutl_contours) {
//                int area = (int) Imgproc.contourArea(c);
//                Log.i(TAG, "识别面积：" + area);
//                //300、800、500  if (area > 500&&800<area)
//                if (area > 500) {
//                    /* 统计形状个数 */
//                    DetectShape(canny_new_img, new MatOfPoint2f(c.toArray()));
//                }
//            }
//
//            save_pic(canny_new_img, 2);
//
//
//            if (!black_white_Flag)
//                Log.e("ShapeDetect黑底", "三角形个数：" + san_Num + "矩形个数：" + rect_Num + "菱形个数：" + lin_Num + "五角形个数：" + star_Num + "圆形个数：" + yuan_Num);
//            else
//                Log.e("ShapeDetect白底", "三角形个数：" + triangle_Num + "矩形个数：" + rectangle_Num + "菱形个数：" + rhombus_Num + "五角形个数：" + pentagon_Num + "圆形个数：" + circle_Num);
//            /* 颜色数据 */
//            shape_yanse();
//            Log.e(TAG, "最大面积为：" + Max_area + " " + Max_area_Yanse + " " + Max_area_shape);
//            result = null;
//        }
//        /* 白底 */
//        if (black_white_Flag)
//            return return_shape_num(triangle_Num, rectangle_Num, rhombus_Num, pentagon_Num, circle_Num);
//            /* 黑白底 */
//        else return return_shape_num(san_Num, rect_Num, lin_Num, star_Num, yuan_Num);
//        // black_white_Flag==false为真，为黑白底   black_white_Flag==false?return_shape_num(san_Num,rect_Num,lin_Num,sta  //黑白底r_Num,yuan_Num):
//        //                        return_shape_num(triangle_Num,rectangle_Num,rhombus_Num,pentagon_Num,circle_Num)
//    }
//
//    /**
//     * 按键拍照存图片,每按一次就存一张图片
//     * opencv中的图片格式是BGR,而手机中的图片是RGB
//     * <p>
//     * Mat photo:需要保存的图片，int plate=1:为保存车牌图片,2为保存形状图片,3为交通灯图片
//     */
//
//    private static String ZIKU_PATH_plate = "/storage/emulated/0/DCIM/Tess/" + java.io.File.separator + "plate";
//    private static String ZIKU_PATH_shape = "/storage/emulated/0/DCIM/Tess/" + java.io.File.separator + "shape";
//    private static String ZIKU_PATH_light = "/storage/emulated/0/DCIM/Tess/" + java.io.File.separator + "light";
//    static int save_picture = 0;
//
//    public static void save_pic(Mat photo, int plate) {
//        save_picture++;
//        Imgproc.cvtColor(photo, photo, Imgproc.COLOR_BGR2RGB);
//        String filename = null;
//        if (plate == 1) {
//            filename = ZIKU_PATH_plate + "/plate" + String.valueOf(save_picture) + ".jpg";
//        } else if (plate == 2) {
//            filename = ZIKU_PATH_shape + "/shape" + String.valueOf(save_picture) + ".jpg";
//        } else if (plate == 3) {
//            filename = ZIKU_PATH_light + "/light" + String.valueOf(save_picture) + ".jpg";
//        }
//        Imgcodecs.imwrite(filename, photo);
//    }
//
//    /**
//     * 读取手机中的图片进行识别，
//     * 但是要改变颜色空间，
//     * 因为手机中的照片是RGB格式，
//     * 而opencv显示的是BGR
//     * <p>
//     * boolean auto=true自动读取，false固定名字读取，
//     * String pic_name=plate1.jpg 固定图片名字
//     * int plate=1读取车牌的，2读取形状的
//     */
//    static int read_picture = 0;
//
//    public static Mat read_pic(boolean auto, String pic_name, int plate) {
//        read_picture++;
//        String filename = null;
//        if (!auto) {
//            if (plate == 1) {
//                filename = ZIKU_PATH_plate + "/" + pic_name;
//            } else if (plate == 2) {
//                filename = ZIKU_PATH_shape + "/" + pic_name;
//            } else if (plate == 3) {
//                filename = ZIKU_PATH_light + "/" + pic_name;
//            }
//        } else {
//            if (plate == 1) {
//                filename = ZIKU_PATH_plate + "/plate" + String.valueOf(read_picture) + ".jpg";
//            } else if (plate == 2) {
//                filename = ZIKU_PATH_shape + "/shape" + String.valueOf(read_picture) + ".jpg";
//            } else if (plate == 3) {
//                filename = ZIKU_PATH_light + "/light" + String.valueOf(read_picture) + ".jpg";
//            }
//        }
//        Log.i(TAG, "read_pic: " + filename);
//        Mat photo = Imgcodecs.imread(filename);
//        Imgproc.cvtColor(photo, photo, Imgproc.COLOR_RGB2BGR);
//        return photo;
//    }
//
//
//    public static Mat DetectShape(Mat canny_new_img, MatOfPoint2f c1) {
//
//        double area = Imgproc.contourArea(c1);
//        Moments moments = Imgproc.moments(c1);
//        /* 计算轮廓中心 */
//        int cx = (int) (moments.m10 / moments.m00);
//        int cy = (int) (moments.m01 / moments.m00);
//
//        Imgproc.circle(canny_new_img, new Point(cx, cy), 5, new Scalar(255, 0, 0), -1);
//
//        /* 计算轮廓的周长 */
//        double peri = Imgproc.arcLength(c1, true);
//        /* 计算出菱形或者正方形的边长，用于判断菱形与正方形和矩形的区别 */
//        //double side_lenght=peri/4;
//        MatOfPoint2f approx = new MatOfPoint2f();
//        //得到大概值
//        Imgproc.approxPolyDP(c1, approx, 0.028 * peri, true);
//
//        //如果是三角形形状，则有三个顶点
//        if (approx.toList().size() == 3) {
//            Imgproc.putText(canny_new_img, "san", new Point(cx, cy), FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 5);
//            san_Num++;
//        }
//
//        //如果有四个顶点，则是正方形或者长方形
//        else if (approx.toList().size() == 4) {
//
//            double new_area = 0, minArea = 0;
//
//            new_area = Imgproc.contourArea(c1);
//
//            RotatedRect rect1 = Imgproc.minAreaRect(c1);
//
//            minArea = rect1.size.area();
//
//            double rec = area / minArea;
//            if (rec >= 0.83 && rec < 1.15) {
//                rect_Num++;
//                Imgproc.putText(canny_new_img, "rect", new Point(cx, cy),
//                        FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 5);
//            } else {
//                lin_Num++;
//                Imgproc.putText(canny_new_img, "lin", new Point(cx, cy),
//                        FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 5);
//            }
//        }
//
//        //如果是五角形，则有五个顶点
//        else if (approx.toList().size() >= 10 && approx.toList().size() <= 13) {
//            star_Num++;
//            Imgproc.putText(canny_new_img, "star", new Point(cx, cy),
//                    FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 5);
//        } else if (approx.toList().size() > 4 && approx.toList().size() < 10) {
//            yuan_Num++;
//            Imgproc.putText(canny_new_img, "circle", new Point(cx, cy),
//                    FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 5);
//        }
//
//        //除了以上情况之外，我们假设为圆形
//        else if (approx.toList().size() > 13) {
//            yuan_Num++;
//            Imgproc.putText(canny_new_img, "circle", new Point(cx, cy),
//                    FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 5);
//        }
//        return canny_new_img;
//    }
//
//    public static int[] shape_yanse() {
//        Log.e(TAG, "浅蓝色个数：" + Cambridge_blue_Num + "黄色个数：" + yellow_Num + "品红色个数:" + mag_Num
//                + "蓝色个数：" + blue_Num + "青色个数:" + qing_Num + "红色个数:" + red_Num);
//        int shapenum[] = new int[6];
//        shapenum[0] = Cambridge_blue_Num;
//        shapenum[1] = yellow_Num;
//        shapenum[2] = mag_Num;
//        shapenum[3] = blue_Num;
//        shapenum[4] = qing_Num;
//        shapenum[5] = red_Num;
//        return shapenum;
//    }
//
//    public int[] return_shape_num(int san, int rect, int lin, int star, int yuan) {
//        int shapenum[] = new int[5];
//        shapenum[0] = san;
//        shapenum[1] = rect;
//        shapenum[2] = lin;
//        shapenum[3] = star;
//        shapenum[4] = yuan;
//        return shapenum;
//    }
//
//
//    /**
//     * 颜色识别
//     * mask->分割后的二值化图像
//     * i->0 浅蓝色、  i->1 黄色、  i->2品红、  i=>4 蓝色、i->5 青色(绿色)、i=>6 红色、 i->7黑色
//     * //浅蓝0、//黄色1、//品红2、//浅红色3、//蓝色4、//青色5、// 深红色6、//黑色7
//     */
//    static int Max_area = 0;//一张图片中那个形状最大面积
//    static String Max_area_Yanse = "品红色";//最大面积对应的形状颜色
//    static String Max_area_shape = "triangle";//最大面积对应的形状
//    static boolean Max_area_YanseFlag = false;
//
//    public Mat yanse(Mat mask, int i) {
//
//        Mat mask1 = mask.clone();
//        List<MatOfPoint> contour = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(mask1, contour, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        for (MatOfPoint c : contour) {
//            int area = 0;
//            area = (int) Imgproc.contourArea(c);
//            Log.i(TAG, "颜色形状识别各个轮廓的面积：" + area);
//            //300、800、
//            if (area > 250 && area < 2000) {
//
//                //查找一张图片里面的最大轮廓
//                if (area > Max_area) {
//                    Max_area = area;//查找最大面积
//                    Max_area_YanseFlag = true;
//                }
//
//                Moments moments = Imgproc.moments(c);
//                //计算轮廓中心
//                int cx = (int) (moments.m10 / moments.m00);
//                int cy = (int) (moments.m01 / moments.m00);
//                //浅蓝0、//黄色1、//品红2、//浅红色3、//蓝色4、//青色5、// 深红色6、//黑色7
//                //浅蓝色
//                if (i == 0) {
//                    Cambridge_blue_Num++;//计算浅蓝色图形个数
//                    String shape = hsv_shape_detect(new MatOfPoint2f(c.toArray()));
//                    Log.e(TAG, "0、颜色形状识别浅蓝色" + shape + "的面积：" + area);
//                    shape_Num(shape);
//                    if (Max_area_YanseFlag == true) {
//                        Max_area_Yanse = "浅蓝色";
//                        Max_area_shape = shape;
//                        Max_area_YanseFlag = false;
//                    }
//                }
//                //黄色
//                if (i == 1) {
//                    yellow_Num++;//计算黄色图形个数
//                    String shape = hsv_shape_detect(new MatOfPoint2f(c.toArray()));
//                    Log.e(TAG, "1、颜色形状识别黄色" + shape + "的面积：" + area);
//                    shape_Num(shape);
//                    if (Max_area_YanseFlag == true) {
//                        Max_area_Yanse = "黄色";
//                        Max_area_shape = shape;
//                        Max_area_YanseFlag = false;
//                    }
//                }
//                //品红色
//                if (i == 2) {
//                    mag_Num++;//计算品红色图形个数
//                    String shape = hsv_shape_detect(new MatOfPoint2f(c.toArray()));
//                    Log.e(TAG, "2、颜色形状识别品红色" + shape + "的面积：" + area);
//                    shape_Num(shape);
//                    if (Max_area_YanseFlag == true) {
//                        Max_area_Yanse = "品红色";
//                        Max_area_shape = shape;
//                        Max_area_YanseFlag = false;
//                    }
//                }
//                //蓝色
//                if (i == 4) {
//                    blue_Num++;//计算蓝色图形个数
//                    String shape = hsv_shape_detect(new MatOfPoint2f(c.toArray()));
//                    Log.e(TAG, "4、颜色形状识别蓝色" + shape + "的面积：" + area);
//                    shape_Num(shape);
//                    if (Max_area_YanseFlag == true) {
//                        Max_area_Yanse = "蓝色";
//                        Max_area_shape = shape;
//                        Max_area_YanseFlag = false;
//                    }
//                }
//                //青色
//                if (i == 5) {
//                    qing_Num++;//计算青色图形个数
//                    String shape = hsv_shape_detect(new MatOfPoint2f(c.toArray()));
//                    Log.e(TAG, "5、颜色形状识别青色" + shape + "的面积：" + area);
//                    shape_Num(shape);
//                    if (Max_area_YanseFlag == true) {
//                        Max_area_Yanse = "青色";
//                        Max_area_shape = shape;
//                        Max_area_YanseFlag = false;
//                    }
//                }
//                //红色
//                if (i == 6) {
//                    red_Num++;//计算红色图形个数
//                    String shape = hsv_shape_detect(new MatOfPoint2f(c.toArray()));
//                    Log.e(TAG, "5、颜色形状识别红色" + shape + "的面积：" + area);
//                    shape_Num(shape);
//                    if (Max_area_YanseFlag == true) {
//                        Max_area_Yanse = "红色";
//                        Max_area_shape = shape;
//                        Max_area_YanseFlag = false;
//                    }
//                }
//                //黑色
//                if (i == 7) {
//                    black_Num++;//计算黑色图形个数
//                    String shape = hsv_shape_detect(new MatOfPoint2f(c.toArray()));
//                    Log.e(TAG, "7、颜色形状识别黑色" + shape + "的面积：" + area);
//                    shape_Num(shape);
//                    if (Max_area_YanseFlag == true) {
//                        Max_area_Yanse = "黑色";
//                        Max_area_shape = shape;
//                        Max_area_YanseFlag = false;
//                    }
//                }
//            }
//        }
//
//        hierarchy.release();
//        return mask;
//    }
//
//
//    public static String hsv_shape_detect(MatOfPoint2f c) {
//
//        String shape = "unknown";
//        //计算轮廓的周长
//        double peri = Imgproc.arcLength(c, true);
//        //计算出菱形或者正方形的边长，用于判断菱形与正方形和矩形的区别
////        double side_lenght=peri/4;
//        MatOfPoint2f approx = new MatOfPoint2f();
//        //得到大概值
//        Imgproc.approxPolyDP(c, approx, 0.028 * peri, true);
//
//        //如果是三角形形状，则有三个顶点
//        if (approx.toList().size() == 3) {
//            shape = "triangle";
//        }
//
//        //如果有四个顶点，则是正方形或者长方形
//        else if (approx.toList().size() == 4) {
//
//            double area = 0, minArea = 0;
//
//            area = Imgproc.contourArea(c);
//
//            RotatedRect rect1 = Imgproc.minAreaRect(c);
//
//            minArea = rect1.size.area();
//
//            double rec = area / minArea;
//
//            if (rec >= 0.83 && rec < 1.15)
//                shape = "rectangle";
//            else
//                shape = "rhomb.0000  us";
//        }
//
//        //如果是五角形，则有五个顶点
//        else if (approx.toList().size() >= 10 && approx.toList().size() <= 13) {
//            shape = "pentagon";
//        }
//
//        //除了以上情况之外，我们假设为圆形
//        else if (approx.toList().size() > 13) {
//            shape = "circle";
//        }
//
//        return shape;
//    }
//
//
//    public static void shape_Num(String shape) {
//        //int[] shape_Num=new int[5];
//        String triangle = "triangle";
//        String rectangle = "rectangle";
//        String pentagon = "pentagon";
//        String circle = "circle";
//        String rhombus = "rhombus";//菱形
//        if (triangle.equals(shape)) {
//            //shape_Num[0]=triangle_Num++;
//            triangle_Num++;
//        }
//        if (rectangle.equals(shape)) {
////            shape_Num[1]=rectangle_Num++;
//            rectangle_Num++;
//        }
//        if (pentagon.equals(shape)) {
////            shape_Num[2]=pentagon_Num++;
//            pentagon_Num++;
//        }
//        if (circle.equals(shape)) {
//            //shape_Num[3]=circle_Num++;
//            circle_Num++;
//        }
//        if (rhombus.equals(shape)) {
//            //shape_Num[3]=circle_Num++;
//            rhombus_Num++;
//        }
//        //return shape_Num;
//    }
//
//    //发送颜色值给LED的第二排
//    public static void send_yanseToLED_two(int[] yanse_data) {
//        FirstActivity.Connect_Transport.digital(2, 0XF0 | yanse_data[0], 0XF0 | yanse_data[1], 0XF0 | yanse_data[2]);//发送颜色值给LED的第二排
//        FirstActivity.Connect_Transport.YanChi(100);
//        FirstActivity.Connect_Transport.digital(2, 0XF0 | yanse_data[0], 0XF0 | yanse_data[1], 0XF0 | yanse_data[2]);
//        FirstActivity.Connect_Transport.YanChi(100);
//        FirstActivity.Connect_Transport.digital(2, 0XF0 | yanse_data[0], 0XF0 | yanse_data[1], 0XF0 | yanse_data[2]);
//        FirstActivity.Connect_Transport.YanChi(100);
//    }
//
//    private static short[] data = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//
//    //立体标志位显示最大面积的颜色
//    public static int liti_yanse() {
////        data[0] = 0x13;
//        int which = liti_yanse_index(Max_area_Yanse);
////        data[1] = (short) (which + 0x01);
////        FirstActivity.Connect_Transport.YanChi(500);
////        FirstActivity.Connect_Transport.infrared_stereo(data);
////        FirstActivity.Connect_Transport.YanChi(500);
////        FirstActivity.Connect_Transport.infrared_stereo(data);
//        return which;
//    }
//
//    //发送颜色信息给立体
//    public static void send_litiyanse(int which) {
//        data[0] = 0x13;
//        data[1] = (short) (which + 0x01);
//        FirstActivity.Connect_Transport.YanChi(500);
//        FirstActivity.Connect_Transport.infrared_stereo(data);
//        FirstActivity.Connect_Transport.YanChi(500);
//        FirstActivity.Connect_Transport.infrared_stereo(data);
//        FirstActivity.Connect_Transport.YanChi(500);
//        FirstActivity.Connect_Transport.infrared_stereo(data);
//    }
//
//    //立体标志位显示最大面积的形状
//    public static int liti_shape() {
////        data[0] = 0x12;
//        int which = liti_shape_index(Max_area_shape);
////        data[1] = (short) (which + 0x01);
////        FirstActivity.Connect_Transport.YanChi(500);
////        FirstActivity.Connect_Transport.infrared_stereo(data);
////        FirstActivity.Connect_Transport.YanChi(500);
////        FirstActivity.Connect_Transport.infrared_stereo(data);
////        FirstActivity.Connect_Transport.YanChi(500);
//        return which;
//    }
//
//    //发送形状信息给立体
//    public static void send_litishape(int which) {
//        data[0] = 0x12;
//        data[1] = (short) (which + 0x01);
//        FirstActivity.Connect_Transport.YanChi(500);
//        FirstActivity.Connect_Transport.infrared_stereo(data);
//        FirstActivity.Connect_Transport.YanChi(500);
//        FirstActivity.Connect_Transport.infrared_stereo(data);
//        FirstActivity.Connect_Transport.YanChi(500);
//        FirstActivity.Connect_Transport.infrared_stereo(data);
//    }
//
//
//    private static int liti_yanse_index(String yanse) {
//        int which = 0;
//        if (yanse.equals("红色")) {
//            which = 0;
//        } else if (yanse.equals("绿色")) {
//            which = 1;
//        } else if (yanse.equals("蓝色")) {
//            which = 2;
//        } else if (yanse.equals("黄色")) {
//            which = 3;
//        } else if (yanse.equals("品红色")) {
//            which = 4;
//        } else if (yanse.equals("青色")) {
//            which = 5;
//        } else if (yanse.equals("黑色")) {
//            which = 6;
//        } else if (yanse.equals("白色")) {
//            which = 7;
//        }
//        return which;
//    }
//
//    private static int liti_shape_index(String shape) {
//        int which = 0;
//        if (shape.equals("rectangle")) {
//            which = 0;
//        } else if (shape.equals("circle")) {
//            which = 1;
//        } else if (shape.equals("triangle")) {
//            which = 2;
//        } else if (shape.equals("rhombus")) {
//            which = 3;
//        } else if (shape.equals("pentagon")) {
//            which = 4;
//        }
//        return which;
//    }
//
//
//    public static void main(String[] argv) {
////        System.out.print(shape_Index("A122B4"));
//    }
//
//    //车牌的 XY3YXY  有效图形为三角形=（3%2）+1=2
//    public static int shape_Index(String plate_Flag) {
//        int num = 2;
//        if (plate_Flag != null && plate_Flag.length() == 6) {
//            num = (plate_Flag.charAt(2) - '0');
//            num = (num % 2) + 1;
//        }
//        return num;
//    }
//
//    public static int shape_Index2(int light_Flag) {
//        int num = 2;
//        num = light_Flag;
//        num = (num % 2) + 1;
//        return num;
//    }
//
//    // 把坐标降低到4分之一
//    MatOfPoint ChangeSize(MatOfPoint contour) {
//        for (int i = 0; i < contour.height(); i++) {
//            double[] p = contour.get(i, 0);
//            p[0] = p[0] / 4;
//            p[1] = p[1] / 4;
//            contour.put(i, 0, p);
//        }
//        return contour;
//    }
//}
//
