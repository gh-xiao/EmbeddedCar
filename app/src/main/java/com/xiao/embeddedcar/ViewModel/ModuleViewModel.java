package com.xiao.embeddedcar.ViewModel;

import android.graphics.Bitmap;
import android.os.Handler;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xiao.baiduocr.OCRResult;
import com.xiao.embeddedcar.Activity.MainActivity;
import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.Utils.PaddleOCR.DetectPlateColor;
import com.xiao.embeddedcar.Utils.PublicMethods.BitmapProcess;
import com.xiao.embeddedcar.Utils.PublicMethods.TFTAutoCutter;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class ModuleViewModel extends ViewModel {
    /* 视图ViewModel */
    //图片显示
    private final MutableLiveData<Bitmap> moduleImgShow = new MutableLiveData<>();
    //信息数据显示
    private final MutableLiveData<String> moduleInfoTV = new MutableLiveData<>();

    /* 数据与配置ViewModel */
    //检测模式
    private final MutableLiveData<Boolean> detectMode = new MutableLiveData<>(true);
    //实时接收设备传入图片
    private final MutableLiveData<Boolean> getImgMode = new MutableLiveData<>(false);
    //待检测图片
    private final MutableLiveData<Bitmap> detectPicture = new MutableLiveData<>();

    /* getter */
    public MutableLiveData<Bitmap> getModuleImgShow() {
        return moduleImgShow;
    }

    public MutableLiveData<String> getModuleInfoTV() {
        return moduleInfoTV;
    }

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
     * 获取模块回传信息Handler
     */
    private final Handler getModuleInfoHandle = new Handler(new WeakReference<Handler.Callback>(msg -> {
        if (msg.what == 1) moduleInfoTV.setValue((String) msg.obj);
        if (msg.what == 2) moduleImgShow.setValue((Bitmap) msg.obj);
        return true;
    }).get());

    /**
     * 消息回传解析线程启动
     */
    public void getThreadReturnMsg() {
        XcApplication.cachedThreadPool.execute(() -> ConnectTransport.getInstance().setReMsgHandler(getModuleInfoHandle));
    }

    /**
     * 模块测试控制
     *
     * @param i -
     */
    public void module(int i) {
        ConnectTransport ct = ConnectTransport.getInstance();
        if (Boolean.FALSE.equals(detectMode.getValue())) {
            Bitmap detect = detectPicture.getValue();
            if (detect != null) switch (i) {
                //红绿灯
                case 1:
                    new Thread(() -> ct.trafficLight(detect)).start();
                    return;
                //车牌
                case 2:
                    new Thread(() -> {
                        /* 裁剪 */
                        Bitmap bitmap = TFTAutoCutter.TFTCutter(detect);
                        /* 获得序列化的结果 */
                        String serialize = ct.DetectPlate(bitmap);
                        /* 反序列化 */
                        Type typeMap = new TypeToken<List<OCRResult>>() {}.getType();
                        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
                        List<OCRResult> results = gson.fromJson(serialize, typeMap);
                        if (results.size() > 0) {
                            for (OCRResult result : results) {
                                /* 色彩判断 */
                                String color = DetectPlateColor.getColor(bitmap, result);
                                if ("green".equals(color))
                                    ct.sendUIMassage(1, "新能源车牌: " + result.getLabelName());
                                if ("blue".equals(color))
                                    ct.sendUIMassage(1, "蓝底车牌: " + result.getLabelName());
                            }
                        } else ct.sendUIMassage(1, "No result!");
                    }).start();
                    return;
                //形状
                case 3:
                    new Thread(() -> ct.Shape(TFTAutoCutter.TFTCutter(detect))).start();
                    break;
                //交通标志物
                case 4:
                    new Thread(() -> {
                        Bitmap b = TFTAutoCutter.TFTCutter(detect);
                        ct.sendUIMassage(2, b);
                        ct.sendUIMassage(1, MainActivity.getYolov5_tflite_tsDetector().processImage(b));
                    }).start();
                    break;
                //二维码
                case 5:
                    new Thread(() -> ct.WeChatQR(detect)).start();
                    break;
                //图片保存
                case 6:
                    new Thread(() -> ct.sendUIMassage(1, BitmapProcess.saveBitmap("MFP", detect))).start();
                    break;
                //全安卓控制4
                case 0xB4:
                    new Thread(ct::Q4).start();
                    break;
            }
            else moduleInfoTV.setValue("传入图片为空!");
        } else {
            if (ct.getStream() != null) switch (i) {
                //红绿灯
                case 1:
                    Objects.requireNonNull(ct);
                    new Thread(ct::trafficLight_mod).start();
                    break;
                //车牌
                case 2:
                    Objects.requireNonNull(ct);
                    new Thread(ct::plate_DetectByColor).start();
                    break;
                //形状
                case 3:
                    Objects.requireNonNull(ct);
                    new Thread(ct::Shape_mod).start();
                    break;
                //交通标志物
                case 4:
                    Objects.requireNonNull(ct);
                    new Thread(ct::trafficSign_mod).start();
                    break;
                //二维码
                case 5:
                    Objects.requireNonNull(ct);
                    new Thread(ct::WeChatQR_mod).start();
                    break;
                //图片保存
                case 6:
                    new Thread(() -> ct.sendUIMassage(1, BitmapProcess.saveBitmap("Driver", ct.getStream()))).start();
                    break;
                //全安卓控制4
                case 0xB4:
                    Objects.requireNonNull(ct);
                    new Thread(ct::Q4).start();
                    break;
            }
            else moduleInfoTV.setValue("摄像头未发送图片!");
        }
    }
}
