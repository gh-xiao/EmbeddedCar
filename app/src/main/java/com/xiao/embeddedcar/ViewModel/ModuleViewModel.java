package com.xiao.embeddedcar.ViewModel;

import static com.xiao.embeddedcar.Utils.CameraUtil.XcApplication.cachedThreadPool;
import static com.xiao.embeddedcar.Utils.PaddleOCR.PlateDetector.completion;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.baidu.ai.edge.core.ocr.OcrResultModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xiao.embeddedcar.Activity.MainActivity;
import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.Utils.PaddleOCR.DetectPlateColor;
import com.xiao.embeddedcar.Utils.PublicMethods.BitmapProcess;
import com.xiao.embeddedcar.Utils.PublicMethods.TFTAutoCutter;

import org.tensorflow.lite.examples.detection.tflite.Classifier;

import java.lang.reflect.Type;
import java.util.List;

public class ModuleViewModel extends ViewModel {
    /* 数据与配置ViewModel */
    //检测模式
    private final MutableLiveData<Boolean> detectMode = new MutableLiveData<>(true);
    //实时接收设备传入图片
    private final MutableLiveData<Boolean> getImgMode = new MutableLiveData<>(false);
    //待检测图片
    private final MutableLiveData<Bitmap> detectPicture = new MutableLiveData<>();

    /* getter */
    public MutableLiveData<Boolean> getDetectMode() {
        return detectMode;
    }

    public MutableLiveData<Boolean> getGetImgMode() {
        return getImgMode;
    }

    public MutableLiveData<Bitmap> getDetectPicture() {
        return detectPicture;
    }

    /**
     * 模块测试控制
     *
     * @param i -
     */
    public void module(int i, MainViewModel mainViewModel) {
        ConnectTransport ct = ConnectTransport.getInstance();
        if (Boolean.FALSE.equals(detectMode.getValue())) {
            Bitmap detect = detectPicture.getValue();
            if (detect != null) switch (i) {
                //红绿灯
                case 1:
                    cachedThreadPool.execute(() -> ct.trafficLight(detect));
                    break;
                //车牌
                case 2:
                    cachedThreadPool.execute(() -> {
                        /* 裁剪 */
                        Bitmap bitmap = TFTAutoCutter.TFTCutter(detect);
                        /* 获得序列化的结果 */
                        String serialize = ct.DetectPlate(bitmap);
                        /* 反序列化 */
                        Type typeMap = new TypeToken<List<OcrResultModel>>() {}.getType();
                        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
                        /* 反序列化结果 */
                        List<OcrResultModel> results = gson.fromJson(serialize, typeMap);
                        /* 最终结果 */
                        String finalResult;
                        ct.getPlate_list().clear();
                        if (results.size() > 0) for (OcrResultModel result : results) {
                            ct.getPlate_list().add(result.getLabel());
                            /* 色彩判断 */
                            String color = DetectPlateColor.getColor(bitmap, result);
                            if ("green".equals(color))
                                ct.sendUIMassage(1, "新能源车牌: " + result.getLabel());
                            if ("blue".equals(color))
                                ct.sendUIMassage(1, "蓝底车牌: " + result.getLabel());
                            if (mainViewModel.getPlate_color().getValue() != null && mainViewModel.getPlate_color().getValue().equals(color)) {
                                finalResult = result.getLabel();
                                finalResult = completion(finalResult);
                                ct.sendUIMassage(1, "当前所需车牌: ■■■" + finalResult + "■■■");
                            }
                        }
                        else ct.sendUIMassage(1, "No result!");
                    });
                    break;
                //形状
                case 3:
                    cachedThreadPool.execute(() -> ct.Shape(TFTAutoCutter.TFTCutter(detect)));
                    break;
                //交通标志物
                case 4:
                    cachedThreadPool.execute(() -> {
                        Bitmap b = TFTAutoCutter.TFTCutter(detect);
                        ct.sendUIMassage(2, b);
                        String TSResult = MainActivity.getTS_Detector().processImage(b);
                        /* 反序列化 */
                        Type typeMap = new TypeToken<List<Classifier.Recognition>>() {}.getType();
                        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
                        /* 反序列化结果 */
                        List<Classifier.Recognition> TSResults = gson.fromJson(TSResult, typeMap);
                        /* 最终结果 */
                        if (TSResults.size() > 0) for (Classifier.Recognition result : TSResults) {
                            ct.sendUIMassage(1, result.getTitle() + ": " + result.getConfidence());
                            /* 将结果返回至模块ImageView */
                            ct.sendUIMassage(2, MainActivity.getTS_Detector().getSaveBitmap());
                        }
                        else ct.sendUIMassage(1, "No result!");
                    });
                    break;
                //车型识别
                case 5:
                    cachedThreadPool.execute(() -> {
                        Bitmap b = TFTAutoCutter.TFTCutter(detect);
                        ct.sendUIMassage(2, b);
                        String VIDResult = MainActivity.getVID_Detector().processImage(b);
                        /* 反序列化 */
                        Type typeMap = new TypeToken<List<Classifier.Recognition>>() {}.getType();
                        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
                        /* 反序列化结果 */
                        List<Classifier.Recognition> VIDResults = gson.fromJson(VIDResult, typeMap);
                        /* 最终结果 */
                        if (VIDResults.size() > 0)
                            for (Classifier.Recognition result : VIDResults) {
                                ct.sendUIMassage(1, result.getTitle() + ": " + result.getConfidence());
                                /* 将结果返回至模块ImageView */
                                ct.sendUIMassage(2, MainActivity.getVID_Detector().getSaveBitmap());
                            }
                        else ct.sendUIMassage(1, "No result!");
                    });
                    break;
                //二维码
                case 6:
                    cachedThreadPool.execute(() -> ct.WeChatQR(detect));
                    break;
                //图片保存
                case 7:
                    cachedThreadPool.execute(() -> ct.sendUIMassage(1, BitmapProcess.saveBitmap("MFP", mainViewModel.getModuleImgShow().getValue())));
                    break;
                //全安卓控制4
                case 0xB4:
                    cachedThreadPool.execute(ct::Q4);
                    break;
                case 0xFF:
                    cachedThreadPool.execute(() -> {
                    });
                    break;
            }
            else mainViewModel.getModuleInfoTV().setValue("传入图片为空!");
        } else {
            if (ct != null && ct.getStream() != null) switch (i) {
                //红绿灯
                case 1:
                    cachedThreadPool.execute(ct::trafficLight_mod);
                    break;
                //车牌(色彩)
                case 2:
                    cachedThreadPool.execute(ct::plate_DetectByColor);
                    break;
                //形状
                case 3:
                    cachedThreadPool.execute(ct::Shape_mod);
                    break;
                //交通标志物
                case 4:
                    cachedThreadPool.execute(ct::trafficSign_mod);
                    break;
                //车牌(车型)
                case 5:
                    cachedThreadPool.execute(ct::VID_mod);
                    break;
                //二维码
                case 6:
                    cachedThreadPool.execute(ct::WeChatQR_mod);
                    break;
                //图片保存
                case 7:
                    cachedThreadPool.execute(() -> ct.sendUIMassage(1, BitmapProcess.saveBitmap("Driver", ct.getStream())));
                    break;
                //全安卓控制4
                case 0xB4:
                    cachedThreadPool.execute(ct::Q4);
                    break;
                case 0xFF:
                    cachedThreadPool.execute(() -> {});
                    break;
            }
            else mainViewModel.getModuleInfoTV().setValue("摄像头未发送图片!");
        }
    }
}
