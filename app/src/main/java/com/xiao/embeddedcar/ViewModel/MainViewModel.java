package com.xiao.embeddedcar.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiao.embeddedcar.Utils.QRcode.QRBitmapCutter;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    //主从状态
    private final MutableLiveData<Boolean> chief_state_flag = new MutableLiveData<>(true);
    //二维码检测色彩
    private final MutableLiveData<ArrayList<QRBitmapCutter.QRColor>> QR_color = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> tv_color = new MutableLiveData<>("");
    //红色
    private final MutableLiveData<Boolean> red = new MutableLiveData<>(true);
    //绿色
    private final MutableLiveData<Boolean> green = new MutableLiveData<>(false);
    //蓝色
    private final MutableLiveData<Boolean> blue = new MutableLiveData<>(true);
    //智能交通灯设备发送选择: 1/2 - A/B
    private final MutableLiveData<Integer> send_trafficLight = new MutableLiveData<>(1);
    //智能交通灯设备位置检测选择: 1/2 - 长线/短线
    private final MutableLiveData<Integer> detect_trafficLight = new MutableLiveData<>(2);
    //智能交通灯设备位置阈值设置
    private final MutableLiveData<Integer> light_location_confidence = new MutableLiveData<>(65);
    //图形统计 - 颜色选择
    private final MutableLiveData<String> shape_color = new MutableLiveData<>("红色");
    //图形统计 - 形状类别
    private final MutableLiveData<String> shape_type = new MutableLiveData<>("总计");
    //车牌种类
    private final MutableLiveData<String> plate_color = new MutableLiveData<>("green");
    //车型检测种类
    private final MutableLiveData<String> detect_car_type = new MutableLiveData<>("car");
    //车型所需种类
    private final MutableLiveData<String> car_type = new MutableLiveData<>("all");
    //交通标志物识别最低置信度
    private final MutableLiveData<Float> traffic_sign_minimumConfidence = new MutableLiveData<>(0.35f);
    //车型识别最低置信度
    private final MutableLiveData<Float> VID_minimumConfidence = new MutableLiveData<>(0.3f);
    /* 实验性 */
    //自定义的车牌
    private final MutableLiveData<String> plate_data = new MutableLiveData<>("A123B4");
    //自定义的车型
    private final MutableLiveData<String> car_type_data = new MutableLiveData<>("car");
    //自定义的交通标志物
    private final MutableLiveData<String> traffic_sign_data = new MutableLiveData<>("go_straight");
    //选择车牌发送方式
    private final MutableLiveData<Boolean> send_plate_mode = new MutableLiveData<>(false);
    //选择车型发送方式
    private final MutableLiveData<Boolean> send_car_type_mode = new MutableLiveData<>(false);
    //选择交通标志物发送方式
    private final MutableLiveData<Boolean> send_TS_mode = new MutableLiveData<>(false);
    //自定义车牌识别方法设置
    private final MutableLiveData<Boolean> detect_methods_choose = new MutableLiveData<>(false);

    /* getter */
    public MutableLiveData<Boolean> getChief_state_flag() {
        return chief_state_flag;
    }

    public MutableLiveData<ArrayList<QRBitmapCutter.QRColor>> getQR_color() {
        return QR_color;
    }

    public MutableLiveData<String> getTv_color() {
        return tv_color;
    }

    public MutableLiveData<Boolean> getRed() {
        return red;
    }

    public MutableLiveData<Boolean> getBlue() {
        return blue;
    }

    public MutableLiveData<Boolean> getGreen() {
        return green;
    }

    public MutableLiveData<Integer> getSend_trafficLight() {
        return send_trafficLight;
    }

    public MutableLiveData<Integer> getDetect_trafficLight() {
        return detect_trafficLight;
    }

    public MutableLiveData<Integer> getLight_location_confidence() {
        return light_location_confidence;
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

    /* 自定义getter */
    public MutableLiveData<String> getPlate_data() {
        return plate_data;
    }

    public MutableLiveData<String> getCar_type_data() {
        return car_type_data;
    }

    public MutableLiveData<String> getTraffic_sign_data() {
        return traffic_sign_data;
    }

    public MutableLiveData<Boolean> getSend_plate_mode() {
        return send_plate_mode;
    }

    public MutableLiveData<Boolean> getSend_car_type_mode() {
        return send_car_type_mode;
    }

    public MutableLiveData<Boolean> getSend_TS_mode() {
        return send_TS_mode;
    }

    public MutableLiveData<Boolean> getDetect_methods_choose() {
        return detect_methods_choose;
    }
}
