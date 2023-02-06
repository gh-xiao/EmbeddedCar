package com.xiao.embeddedcar.FragmentUI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bkrcl.control_car_video.camerautil.CameraCommandUtil;
import com.xiao.embeddedcar.Activity.MainActivity;
import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.Entity.LoginInfo;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.databinding.FragmentOtherBinding;

public class OtherFragment extends Fragment {

    private FragmentOtherBinding binding;
    private int state_camera = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOtherBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //控件动作初始化
        init();
        //设置观察者
        observerDataStateUpdateAction();
        return root;
    }


    /**
     * 控件动作初始化
     */
    private void init() {
        binding.positionDialog.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("摄像头角度预设位调节");
            String[] set_item = {"预设位 ①", "预设位 ②", "预设位 ③", "预设位 ④", "预设位 ⑤", "预设位 ⑥"};
            builder.setSingleChoiceItems(set_item, -1, (dialog, which) -> {
                state_camera = which + 5;
                cameraState_control();
            });
            builder.create().show();
        });
        binding.QRDialog.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("智能移动机器人二维码识别");
            String[] set_item = {"开始识别", "取消识别"};
            builder.setSingleChoiceItems(set_item, -1, (dialog, which) -> ConnectTransport.getInstance().qr_rec(which + 1));
            builder.create().show();
        });
        binding.buzzerController.setOnClickListener(view -> {
            AlertDialog.Builder build = new AlertDialog.Builder(requireActivity());
            build.setTitle("蜂鸣器控制");
            String[] im = {"打开", "关闭"};
            build.setSingleChoiceItems(im, -1, (dialog, which) -> {
                // 1/0 - 打开/关闭蜂鸣器
                ConnectTransport.getInstance().buzzer(which == 0 ? 1 : 0);
            });
            build.create().show();
        });
        binding.lightController.setOnClickListener(view -> {
            AlertDialog.Builder lt_builder = new AlertDialog.Builder(requireActivity());
            lt_builder.setTitle("转向灯控制");
            String[] item = {"左转", "右转", "停车", "临时停车"};
            lt_builder.setSingleChoiceItems(item, -1, (dialog, which) -> {
                if (which == 0) ConnectTransport.getInstance().light(1, 0);
                else if (which == 1) ConnectTransport.getInstance().light(0, 1);
                else if (which == 2) ConnectTransport.getInstance().light(0, 0);
                else if (which == 3) ConnectTransport.getInstance().light(1, 1);
            });
            lt_builder.create().show();
        });
    }

    /**
     * 观察者数据状态更新活动
     */
    private void observerDataStateUpdateAction() {
    }

    private void cameraState_control() {
        LoginInfo loginInfo = MainActivity.getLoginInfo();
        if (loginInfo == null) return;
        if (!(loginInfo.getIPCamera() == null || loginInfo.getIPCamera().equals("null:81"))) {
            CameraCommandUtil cameraCommandUtil = new CameraCommandUtil();
            XcApplication.cachedThreadPool.execute(() -> {
                switch (state_camera) {
                    //上下左右转动
                    case 1:
                        cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 0, 1);  //向上
                        break;
                    case 2:
                        cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 2, 1);  //向下
                        break;
                    case 3:
                        cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 4, 1);  //向左
                        break;
                    case 4:
                        cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 6, 1);  //向右
                        break;
                    // / 5-7   设置预设位1到3
                    case 5:
                        cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 30, 0);
                        break;
                    case 6:
                        cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 32, 0);
                        break;
                    case 7:
                        cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 34, 0);
                        break;
                    //调用预设位1-3
                    case 8:
                        cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 31, 0);
                        break;
                    case 9:
                        cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 33, 0);
                        break;
                    case 10:
                        cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 35, 0);
                        break;
                    default:
                        break;
                }
                state_camera = 0;
            });
        }

    }
}
