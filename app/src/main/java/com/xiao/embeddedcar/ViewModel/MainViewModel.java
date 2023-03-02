package com.xiao.embeddedcar.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiao.embeddedcar.Utils.QRcode.QRBitmapCutter;

public class MainViewModel extends ViewModel {
    //主从状态
    private final MutableLiveData<Boolean> chief_state_flag = new MutableLiveData<>(true);
    //二维码检测色彩
    private final MutableLiveData<QRBitmapCutter.QRColor> QR_color = new MutableLiveData<>(QRBitmapCutter.color);
    //智能交通灯设备发送选择: 1/2 - A/B
    private final MutableLiveData<Integer> send_trafficLight = new MutableLiveData<>(1);
    //智能交通灯设备位置检测选择: 1/2 - 长线/短线
    private final MutableLiveData<Integer> detect_trafficLight = new MutableLiveData<>(2);
    //图形统计 - 颜色选择
    private final MutableLiveData<String> shape_color = new MutableLiveData<>("红色");
    //图形统计 - 形状类别
    private final MutableLiveData<String> shape_type = new MutableLiveData<>("总计");
    //车牌种类
    private final MutableLiveData<String> plate_color = new MutableLiveData<>("green");
    //车型检测种类
    private final MutableLiveData<String> detect_car_type = new MutableLiveData<>("bike");
    //车型所需种类
    private final MutableLiveData<String> car_type = new MutableLiveData<>("all");
    //交通标志物识别最低置信度
    private final MutableLiveData<Float> traffic_sign_minimumConfidence = new MutableLiveData<>(0.5f);
    //车型识别最低置信度
    private final MutableLiveData<Float> VID_minimumConfidence = new MutableLiveData<>(0.3f);

    public MutableLiveData<Boolean> getChief_state_flag() {
        return chief_state_flag;
    }

    public MutableLiveData<QRBitmapCutter.QRColor> getQR_color() {
        return QR_color;
    }

    public MutableLiveData<Integer> getSend_trafficLight() {
        return send_trafficLight;
    }

    public MutableLiveData<Integer> getDetect_trafficLight() {
        return detect_trafficLight;
    }

    public MutableLiveData<String> getShape_color() {
        return shape_color;
    }

    public MutableLiveData<String> getShape_type() {
        return shape_type;
    }

    public MutableLiveData<String> getPlate_color() {
        return plate_color;
    }

    public MutableLiveData<String> getDetect_car_type() {
        return detect_car_type;
    }

    public MutableLiveData<String> getCar_type() {
        return car_type;
    }

    /* 设置交通标志物识别默认最低置信度阈值 */
    public MutableLiveData<Float> getTraffic_sign_minimumConfidence() {
        return traffic_sign_minimumConfidence;
    }

    /* 设置车型识别默认最低置信度阈值 */
    public MutableLiveData<Float> getVID_minimumConfidence() {
        return VID_minimumConfidence;
    }
}
