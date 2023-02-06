package com.xiao.embeddedcar.Utils.TrafficSigns;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.DetectorFactory;
import org.tensorflow.lite.examples.detection.tflite.YoloV5Classifier;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 使用基于Yolov5的tflite模型的交通标志物识别
 */
public class Yolov5_tflite_TSDetector {

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    enum DetectorMode {TF_OD_API}

    //日志对象
    private static final Logger LOGGER = new Logger();
    //枚举常量 - 检测模式
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    //最小置信度
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    //核心检测对象
    private YoloV5Classifier detector;

    private long timestamp = 0;


    /**
     * 加载模型配置
     *
     * @param device       使用何种硬件加载
     * @param numThreads   使用多少线程加载
     * @param assetManager AssetManager管理对象
     */
    public boolean LoadModel(String device, int numThreads, AssetManager assetManager) {

        //模型文件
        String modelString = "TSyolov5s-fp16.tflite";
        /* 线程数(不推荐超过9线程数) */
        if (numThreads > 9) numThreads = 4;
        LOGGER.i("Changing model to ***" + modelString + "*** device ***" + device + "***");

        /* Try to load model. */
        /* 尝试加载模型 */
        try {
            detector = DetectorFactory.getDetector(assetManager, modelString);
            // Customize the interpreter to the type of device we want to use.
            if (detector == null) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception in updateActiveModel()");
//            Toast toast = Toast.makeText(FirstActivity.getContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
//            toast.show();
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

    public String processImage(Bitmap inputBitmap) {
        if (inputBitmap == null) return "ERROR";
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
//        Bitmap croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);
        Bitmap croppedBitmap = Bitmap.createBitmap(inputBitmap, 0, 0, width, height, matrix, true);

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
        /* 设置默认最低置信度阈值 */
//        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
//
//        switch (MODE) {
//            case TF_OD_API:
//                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
//                break;
//        }

        /* 筛选通过最低置信度阈值的识别结果 */
        final List<Classifier.Recognition> mappedRecognitions = new LinkedList<>();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {

                result.setLocation(location);
                /* 将通过最低置信度的结果添加到新List */
                mappedRecognitions.add(result);

                //识别结果
                Log.e("result.title:", result.getTitle());
            }
        }
        //最终结果
        if (mappedRecognitions.size() != 0) {
            /* 排列出最高置信度的结果 */
            Collections.sort(mappedRecognitions, (o1, o2) -> (int) (o1.getConfidence() - o2.getConfidence()));
            Log.e("SUCCESS", String.valueOf(mappedRecognitions.get(0).getConfidence()));
            return mappedRecognitions.get(0).getTitle();
        } else {
            Log.e("ERROR", "识别错误");
            return "turn_right";
        }
    }
}
