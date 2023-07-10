package com.xiao.embeddedcar.ViewModel;

import android.graphics.Bitmap;
import android.os.Handler;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiao.embeddedcar.Activity.MainActivity;
import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.Entity.LoginInfo;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.Utils.PublicMethods.FastDo;
import com.xiao.embeddedcar.Utils.QRcode.QRBitmapCutter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    /* 网络操作ViewModel */
    //登录信息
    private final MutableLiveData<LoginInfo> loginInfo = new MutableLiveData<>(new LoginInfo());
    //登录状态
    private final MutableLiveData<String> loginState = new MutableLiveData<>();
    //连接状态
    private final MutableLiveData<Integer> connectState = new MutableLiveData<>();
    //主从状态
    private final MutableLiveData<Boolean> chief_state_flag = new MutableLiveData<>(true);
    /* 模块Fragment的ViewModel数据 */
    //设备图片传入
    private final MutableLiveData<Bitmap> showImg = new MutableLiveData<>();
    //模块图片显示
    private final MutableLiveData<Bitmap> moduleImgShow = new MutableLiveData<>();
    //信息数据显示
    private final MutableLiveData<String> moduleInfoTV = new MutableLiveData<>();
    /* 配置文件ViewModel数据 */
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

    public MutableLiveData<LoginInfo> getLoginInfo() {
        return loginInfo;
    }

    public MutableLiveData<String> getLoginState() {
        return loginState;
    }

    public MutableLiveData<Integer> getConnectState() {
        return connectState;
    }

    public MutableLiveData<Boolean> getChief_state_flag() {
        return chief_state_flag;
    }

    public MutableLiveData<Bitmap> getShowImg() {
        return showImg;
    }

    public MutableLiveData<Bitmap> getModuleImgShow() {
        return moduleImgShow;
    }

    public MutableLiveData<String> getModuleInfoTV() {
        return moduleInfoTV;
    }

    public Handler getGetModuleInfoHandle() {
        return getModuleInfoHandle;
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

    private static boolean ready = true;

    public static void setReady(boolean ready) {
        MainViewModel.ready = ready;
    }

    /**
     * 刷新操作
     */
    public void refreshConnect() {
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
            //开启网络连接线程
            connect_thread();
        } else if (XcApplication.isSerial == XcApplication.Mode.SERIAL) {
            //使用纯串口uart4
            serial_thread();
        }
    }

    /**
     * WiFi模式下的线程通讯
     */
    private void connect_thread() {
        XcApplication.cachedThreadPool.execute(() -> ConnectTransport.getInstance().connect( MainActivity.getLoginInfo().getIP()));
    }

    /**
     * 串口模式下的通讯
     */
    private void serial_thread() {
        XcApplication.cachedThreadPool.execute(() -> ConnectTransport.getInstance().serial_connect());
    }

    /**
     * 获取摄像头回传图片Handle
     */
    private final Handler getBitmapHandle = new Handler(new WeakReference<Handler.Callback>(msg -> {
        if (msg.what == 1) {
            showImg.setValue((Bitmap) msg.obj);
            return true;
        } else if (msg.what == 0) {
            if (FastDo.isFastClick()) refreshConnect();
        } else connectState.setValue(msg.what);

        return false;
    }).get());

    /**
     * 获取摄像头回传的图片
     */
    public void getCameraPicture() {
        if (ready) {
            moduleInfoTV.setValue("正在开启线程尝试获取摄像头传入图片...");
            ready = !ready;
            //单线程池
            XcApplication.singleThreadExecutor.execute(() -> ConnectTransport.getInstance().getPicture(getBitmapHandle));
        }
    }

    /**
     * 获取模块回传信息Handler
     */
    private final Handler getModuleInfoHandle = new Handler(new WeakReference<Handler.Callback>(msg -> {
        if (msg.what == 1) moduleInfoTV.setValue((String) msg.obj);
        if (msg.what == 2) moduleImgShow.setValue((Bitmap) msg.obj);
        return true;
    }).get());
}
