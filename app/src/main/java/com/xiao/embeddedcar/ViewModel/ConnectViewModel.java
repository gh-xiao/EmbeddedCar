package com.xiao.embeddedcar.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiao.embeddedcar.Entity.LoginInfo;
import com.xiao.embeddedcar.Utils.CameraUtil.CameraConnectUtil;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.Utils.NetworkAndUIUtil.USBToSerialUtil;
import com.xiao.embeddedcar.Utils.NetworkAndUIUtil.WiFiStateUtil;

import java.util.Objects;

public class ConnectViewModel extends ViewModel {
    /* 控件ViewModel */
    //左侧TextView显示
    private final MutableLiveData<String> connectInfo = new MutableLiveData<>();
    //Switch控件值
    private final MutableLiveData<Boolean> connectMode = new MutableLiveData<>();

    public MutableLiveData<String> getConnectInfo() {
        return connectInfo;
    }

    public MutableLiveData<Boolean> getConnectMode() {
        return connectMode;
    }

    /* 网络操作ViewModel */
    //登录信息
    private final MutableLiveData<LoginInfo> loginInfo = new MutableLiveData<>(new LoginInfo());
    //登录状态
    private final MutableLiveData<String> loginState = new MutableLiveData<>();

    public MutableLiveData<LoginInfo> getLoginInfo() {
        return loginInfo;
    }

    public MutableLiveData<String> getLoginState() {
        return loginState;
    }

    /**
     * 请求连接
     */
    public void requestConnect() {
        CameraConnectUtil.getInstance().cameraInit();
        connectInfo.setValue("检查通讯方式...\n");
        //网络通讯
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET) useNetwork();
        else {
            //搜索摄像头然后启动摄像头
            connectInfo.setValue("使用串口通讯\n");
            USBToSerialUtil.getInstance().connectUSBSerial();
            CameraConnectUtil.getInstance().search();
            return;
        }
        if (Objects.equals(Objects.requireNonNull(loginInfo.getValue()).getIP(), "0.0.0.0")) {
            connectInfo.setValue("连接失败,请重新连接!");
            return;
        }
        if (Objects.equals(Objects.requireNonNull(loginInfo.getValue()).getIPCamera(), "null:81"))
            connectInfo.setValue("摄像头没有找到，快去找找它吧");
        else connectInfo.setValue("尝试连接摄像头...");
        connectInfo.setValue("");
    }

    /**
     * 网络通讯
     */
    private void useNetwork() {
        if (WiFiStateUtil.getInstance().wifiInit(this)) {
            connectInfo.setValue("使用WiFi通讯");
            //WiFi初始化成功
            search();
        } else {
            connectInfo.setValue("请确认设备已通过WiFi接入竞赛平台!");
        }
    }

    /**
     * 搜索摄像cameraIP
     */
    private void search() {
        connectInfo.setValue("搜索cameraIP");
        CameraConnectUtil.getInstance().search();
    }
}
