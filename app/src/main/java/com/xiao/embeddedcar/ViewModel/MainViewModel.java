package com.xiao.embeddedcar.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiao.embeddedcar.Utils.QRcode.QRBitmapCutter;

public class MainViewModel extends ViewModel {
    //主从状态
    private final MutableLiveData<Boolean> chief_state_flag = new MutableLiveData<>(true);
    //二维码检测色彩
    private final MutableLiveData<QRBitmapCutter.QRColor> QR_color = new MutableLiveData<>(QRBitmapCutter.color);
    //智能交通灯设备发送选择
    private final MutableLiveData<Integer> send_trafficLight = new MutableLiveData<>(2);
    //图形统计 - 颜色选择
    private final MutableLiveData<String> shape_color = new MutableLiveData<>("红色");
    //图形统计 - 形状类别
    private final MutableLiveData<String> shape_type = new MutableLiveData<>("总计");
    //TODO HSV色彩选择...
    //TODO 车牌种类选择...
    //TODO 识别(Yolo...)置信度设置...

    public MutableLiveData<Boolean> getChief_state_flag() {
        return chief_state_flag;
    }

    public MutableLiveData<QRBitmapCutter.QRColor> getQR_color() {
        return QR_color;
    }

    public MutableLiveData<Integer> getSend_trafficLight() {
        return send_trafficLight;
    }

    public MutableLiveData<String> getShape_color() {
        return shape_color;
    }

    public MutableLiveData<String> getShape_type() {
        return shape_type;
    }
}
