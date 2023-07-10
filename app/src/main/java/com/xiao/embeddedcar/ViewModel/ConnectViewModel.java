package com.xiao.embeddedcar.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiao.embeddedcar.Utils.CameraUtil.CameraConnectUtil;
import com.xiao.embeddedcar.Utils.Network.WiFiStateUtil;

public class ConnectViewModel extends ViewModel {
    /* 控件ViewModel */
    //左侧TextView显示
    private final MutableLiveData<String> connectInfo = new MutableLiveData<>();
    //Switch控件值
    private final MutableLiveData<Boolean> connectMode = new MutableLiveData<>(true);

    public MutableLiveData<String> getConnectInfo() {
        return connectInfo;
    }

    public MutableLiveData<Boolean> getConnectMode() {
        return connectMode;
    }

    /**
     * 网络通讯
     */
    public void useNetwork(MainViewModel mvm) {
        if (WiFiStateUtil.getInstance().wifiInit(mvm)) {
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