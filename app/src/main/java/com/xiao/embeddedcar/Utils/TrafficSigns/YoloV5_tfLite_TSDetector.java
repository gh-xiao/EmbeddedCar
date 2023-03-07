package com.xiao.embeddedcar.Utils.TrafficSigns;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.xiao.embeddedcar.Utils.PublicMethods.BitmapProcess;

import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.DetectorFactory;
import org.tensorflow.lite.examples.detection.tflite.YoloV5Classifier;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 使用基于YoloV5-tfLite模型的交通标志物识别
 */
public class YoloV5_tfLite_TSDetector {

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
//    enum DetectorMode {TF_OD_API}

    //日志对象
    private static final Logger LOGGER = new Logger();
    //枚举常量 - 检测模式
//    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.35f;
    //最小置信度
    public static float minimumConfidence;
    //核心检测对象
    private YoloV5Classifier detector;
    //模型列表
    private final String[] models = new String[]{"TSyolov5s-fp16.tflite", "TSyolov5s-fp16-3.tflite", "TSyolov5s-fp16-byGray.tflite"};
    //检测图片
    private Bitmap SaveBitmap;
    private long timestamp = 0;

    public Bitmap getSaveBitmap() {
        return SaveBitmap;
    }

    /**
     * 加载模型配置
     *
     * @param device       使用何种硬件加载
     * @param numThreads   使用多少线程加载
     * @param assetManager AssetManager管理对象
     */
    public boolean LoadModel(String device, int numThreads, AssetManager assetManager) {

        //模型文件
        String modelString = models[2];
        //检测类别(标签)
        String labelFilename = "TSclass.txt";
        /* 线程数(不推荐超过9线程数) */
        if (numThreads > 9) numThreads = 4;
        LOGGER.i("Changing model to ***" + modelString + "*** device ***" + device + "***");

        /* Try to load model. */
        /* 尝试加载模型 */
        try {
            detector = DetectorFactory.getDetector(assetManager, modelString, labelFilename);
            // Customize the interpreter to the type of device we want to use.
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception in updateActiveModel()");
//            Toast toast = Toast.makeText(FirstActivity.getContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            return false;
        }

        switch (device) {
            case "GPU":
                detector.useGpu();
                break;
            case "NNAPI":
                detector.useNNAPI();
                break;
            default:
                detector.useCPU();
                break;
        }
        /* 设置线程数 */
        detector.setNumThreads(numThreads);
        return true;
    }

    /**
     * 检测图片
     *
     * @param inputBitmap -
     * @return Gson字符串
     */
    public String processImage(Bitmap inputBitmap) {
        /* 结果列表对象 */
        List<Classifier.Recognition> recognitions = new LinkedList<>();
        /* 将结果转换成Gson */
        Gson gson = new Gson();

        if (inputBitmap == null) return gson.toJson(recognitions);
        //416*416
        int cropSize = detector.getInputSize();
        System.out.println(cropSize);

        int width = inputBitmap.getWidth();
        int height = inputBitmap.getHeight();
        float scaleWidth = ((float) cropSize) / width;
        float scaleHeight = ((float) cropSize) / height;
        //矩阵
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        /* 将输入图片通过矩阵变换得到416*416大小的新图片 */
        Bitmap croppedBitmap = Bitmap.createBitmap(inputBitmap, 0, 0, width, height, matrix, true);
        /* 灰度化图像 */
        croppedBitmap = BitmapProcess.GrayscaleImage(croppedBitmap);
        /* 设置输出结果图像(在该图像上绘制识别结果) */
        Bitmap draw = croppedBitmap.copy(Bitmap.Config.ARGB_8888, true);

        ++timestamp;
        final long currTimestamp = timestamp;

        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");
        /* 利用分类器classifier对图片进行预测分析，得到图片为每个分类的概率. 比较耗时 */
        LOGGER.i("Running detection on image " + currTimestamp);

        final long startTime = SystemClock.uptimeMillis();
        /* 核心检测 */
        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
        /* 计算检测时间 */
        long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
        /* 检测出多少对象 */
        Log.e("CHECK", "run: " + results.size());
        /* 检测时间 */
        Log.i("Time Spent: ", lastProcessingTimeMs + "ms");

        /* 筛选通过最低置信度阈值的识别结果 */
        final List<Classifier.Recognition> mappedRecognitions = new LinkedList<>();
        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= minimumConfidence) {
                result.setLocation(location);
                /* 将通过最低置信度的结果添加到新List */
                mappedRecognitions.add(result);
                //识别结果
                Log.e("result: ", result.getTitle() + result.getConfidence());
                drawBitmap(result, draw);
            }
        }

        return gson.toJson(mappedRecognitions.size() > 0 ? mappedRecognitions : recognitions);
    }

    private void drawBitmap(Classifier.Recognition result, Bitmap resultBitmap) {
        final Canvas canvas = new Canvas(resultBitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        canvas.drawRect(result.getLocation(), paint);
        SaveBitmap = resultBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }
}
