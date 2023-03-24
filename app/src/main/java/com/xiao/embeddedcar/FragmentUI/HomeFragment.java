package com.xiao.embeddedcar.FragmentUI;

import static com.xiao.embeddedcar.Activity.MainActivity.chief_status_flag;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.bkrcl.control_car_video.camerautil.CameraCommandUtil;
import com.xiao.embeddedcar.Activity.MainActivity;
import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.Entity.LoginInfo;
import com.xiao.embeddedcar.R;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.Utils.Network.WiFiStateUtil;
import com.xiao.embeddedcar.Utils.PublicMethods.BaseConversion;
import com.xiao.embeddedcar.Utils.PublicMethods.FastDo;
import com.xiao.embeddedcar.Utils.PublicMethods.ToastUtil;
import com.xiao.embeddedcar.ViewModel.ConnectViewModel;
import com.xiao.embeddedcar.ViewModel.HomeViewModel;
import com.xiao.embeddedcar.ViewModel.ModuleViewModel;
import com.xiao.embeddedcar.databinding.FragmentHomeBinding;

import java.util.Locale;

public class HomeFragment extends ABaseFragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private ConnectViewModel connectViewModel;
    private ModuleViewModel moduleViewModel;
    private int sp_n = 50;
    private int angle = 90;
    private int mp_n = 100;
    // 摄像头命令工具
    private static final CameraCommandUtil cameraCommandUtil = new CameraCommandUtil();
    private float x1 = 0;
    private float y1 = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        connectViewModel = new ViewModelProvider(requireActivity()).get(ConnectViewModel.class);
        moduleViewModel = new ViewModelProvider(requireActivity()).get(ModuleViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //控件动作初始化
        init();
        //设置观察者
        observerDataStateUpdateAction();
        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    void init() {
        //设置TextView滚动
        binding.Debug.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.commandData.setMovementMethod(ScrollingMovementMethod.getInstance());
        //清空重置按钮监听事件
        binding.clearDebugArea.setOnClickListener(view -> {
            binding.Debug.setText("Debug显示\n");
            homeViewModel.getShowImg().setValue(null);
            moduleViewModel.getModuleImgShow().setValue(null);
            binding.commandData.setText(R.string.command_data_show);
            homeViewModel.getMp_n().setValue(100);
            homeViewModel.getAngle().setValue(50);
            homeViewModel.getSp_n().setValue(90);
            binding.mpData.setText(R.string.mp_data);
            binding.angleData.setText(R.string.line_data);
            binding.speedData.setText(R.string.angle_data);


        });
        //刷新连接操作
        binding.refreshBtn.setOnClickListener(view -> {
            if (FastDo.isFastClick()) {
                if (WiFiStateUtil.getInstance().wifiInit() || XcApplication.isSerial != XcApplication.Mode.SOCKET) {
                    homeViewModel.refreshConnect();
                    HomeViewModel.setReady(true);
                } else ToastUtil.getInstance().ShowToast("还未进行连接操作!", 10);
            } else ToastUtil.getInstance().ShowToast("请勿快速连按!", 10);
        });
        //方向按钮操作监听事件
        binding.upButton.setOnClickListener(view -> {
            ToastUtil.getInstance().ShowToast("码盘" + mp_n + "\n前进速度" + sp_n + "\n转弯速度" + angle, 10);
            ConnectTransport.getInstance().go(sp_n, mp_n);
        });
        binding.belowButton.setOnClickListener(view -> {
            ToastUtil.getInstance().ShowToast("码盘" + mp_n + "\n前进速度" + sp_n + "\n转弯速度" + angle, 10);
            ConnectTransport.getInstance().back(sp_n, mp_n);
        });
        binding.stopButton.setOnClickListener(view -> ConnectTransport.getInstance().stop());
        binding.leftButton.setOnClickListener(view -> {
            ToastUtil.getInstance().ShowToast("码盘" + mp_n + "\n前进速度" + sp_n + "\n转弯速度" + angle, 10);
            ConnectTransport.getInstance().left(angle);
        });
        binding.rightButton.setOnClickListener(view -> {
            ToastUtil.getInstance().ShowToast("码盘" + mp_n + "\n前进速度" + sp_n + "\n转弯速度" + angle, 10);
            ConnectTransport.getInstance().right(angle);
        });
        binding.upButton.setOnLongClickListener(view -> {
            ToastUtil.getInstance().ShowToast("码盘" + mp_n + "\n前进速度" + sp_n + "\n转弯速度" + angle, 10);
            ConnectTransport.getInstance().line(sp_n);
            /*如果将onLongClick返回false，那么执行完长按事件后，还有执行单击事件。如果返回true，只执行长按事件*/
            return true;
        });
        //码盘设置
        binding.mpData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                homeViewModel.getMp_n().setValue(!e.toString().equals("") ? Integer.parseInt(e.toString()) : 0);
            }
        });
        //速度(前进)设置
        binding.speedData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                homeViewModel.getSp_n().setValue(!e.toString().equals("") ? Integer.parseInt(e.toString()) : 0);
            }
        });
        //速度(转弯)设置
        binding.angleData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                homeViewModel.getAngle().setValue(!e.toString().equals("") ? Integer.parseInt(e.toString()) : 0);
            }
        });
        //左侧图片监听事件
        binding.img.setOnTouchListener(new onTouchListener1());
    }

    @Override
    void observerDataStateUpdateAction() {
        homeViewModel.getDebugArea().setValue(null);
        //图片信息显示
        homeViewModel.getShowImg().observe(getViewLifecycleOwner(), showImg -> binding.img.setImageBitmap(showImg));
        //设备数据接收
        homeViewModel.getDataShow().observe(getViewLifecycleOwner(), s -> {
            binding.rvData.setTextColor(chief_status_flag ? getResources().getColor(R.color.white) : getResources().getColor(R.color.black));
            binding.commandData.setTextColor(chief_status_flag ? getResources().getColor(R.color.white) : getResources().getColor(R.color.black));
            binding.rvData.setText(s);
        });
        //设备指令接收区
        homeViewModel.getCommandData().observe(getViewLifecycleOwner(), bytes -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 50; i++) {
                sb.append("0x").append(BaseConversion.decToHex(bytes[i]).substring(6)).append(",");
                if (sb.toString().contains("bb") || sb.toString().contains("BB")) break;
            }
            binding.commandData.setText(sb.toString().toUpperCase(Locale.ROOT));
        });
        //debug显示
        homeViewModel.getDebugArea().observe(getViewLifecycleOwner(), s -> {
            if (s != null) {
                binding.Debug.append(s + "\n");
                int offset = binding.Debug.getLineCount() * binding.Debug.getLineHeight();
                if (offset > binding.Debug.getHeight())
                    binding.Debug.scrollTo(0, offset - binding.Debug.getHeight());
            }
        });
        //连接状态
        homeViewModel.getConnectState().observe(getViewLifecycleOwner(), integer -> {
            switch (integer) {
                case 3:
                    homeViewModel.getDebugArea().setValue("平台已连接,代码: 3");
                    break;
                case 4:
                    homeViewModel.getDebugArea().setValue("平台连接失败或已关闭!,代码: 4");
                    break;
                case 5:
                    homeViewModel.getDebugArea().setValue("WiFi通讯建立失败!,代码: 5");
                    break;
                default:
                    homeViewModel.getDebugArea().setValue("请检查WiFi连接状态!,代码: 0");
                    break;
            }
        });
        //码盘(车轮旋转周期/角度)
        homeViewModel.getMp_n().observe(getViewLifecycleOwner(), i -> {
            if (i != null) mp_n = i;
        });
        //速度(前进)
        homeViewModel.getSp_n().observe(getViewLifecycleOwner(), i -> {
            if (i != null) sp_n = i;
        });
        //速度(转弯)
        homeViewModel.getAngle().observe(getViewLifecycleOwner(), i -> {
            if (i != null) angle = i;
        });
        //IP信息设置与图片显示
        homeViewModel.getIpShow().observe(getViewLifecycleOwner(), s -> binding.showIP.setText(s));
        connectViewModel.getLoginInfo().observe(getViewLifecycleOwner(), loginInfo -> {
            if (loginInfo == null || loginInfo.getIP() == null) return;
            if (XcApplication.isSerial == XcApplication.Mode.SOCKET && !(loginInfo.getIPCamera() == null || loginInfo.getIPCamera().equals("null:81"))) {
                //开启接收网络传入图片
                homeViewModel.getCameraPicture();
                //开启接收设备传入数据
                homeViewModel.refreshConnect();
                homeViewModel.getIpShow().setValue("WiFi连接成功! " + "IP地址: " + loginInfo.getIP() + "\n"
                        + "摄像头连接成功! " + "Camera-IP: " + loginInfo.getPureCameraIP());
            } else if (XcApplication.isSerial == XcApplication.Mode.SOCKET && loginInfo.getIP().equals("0.0.0.0")) {
                homeViewModel.getIpShow().setValue("WiFi连接失败!请重新连接\n" + "摄像头连接失败! " + "请重启您的平台设备!");
            } else if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
                //开始接收网络传入图片 - 测试用
//                homeViewModel.getCameraPicture();
                homeViewModel.getIpShow().setValue("WiFi连接成功! " + "IP地址: " + loginInfo.getIP() + "\n"
                        + "摄像头连接失败! " + "请重启您的平台设备!");
            }
        });
        //
        moduleViewModel.getModuleInfoTV().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.Debug.append(s + "\n");
        });
    }

    /**
     * 摄像头位置调整
     */
    private class onTouchListener1 implements View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            LoginInfo loginInfo = MainActivity.getLoginInfo();
            if (loginInfo == null) return true;
            if (!(loginInfo.getIPCamera() == null || loginInfo.getIPCamera().equals("null:81"))) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    // 点击位置坐标
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        y1 = event.getY();
                        break;
                    // 弹起坐标
                    case MotionEvent.ACTION_UP:
                        float x2 = event.getX();
                        float y2 = event.getY();
                        float xx = x1 > x2 ? x1 - x2 : x2 - x1;
                        float yy = y1 > y2 ? y1 - y2 : y2 - y1;
                        // 判断滑屏趋势
                        int MINLEN = 30;
                        if (xx > yy) {
                            // left
                            if ((x1 > x2) && (xx > MINLEN)) {
                                ToastUtil.getInstance().ShowToast("向左微调");
                                XcApplication.cachedThreadPool.execute(() -> {
                                    //左
                                    cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 4, 1);
                                });
                            }
                            // right
                            else if ((x1 < x2) && (xx > MINLEN)) {
                                ToastUtil.getInstance().ShowToast("向右微调");
                                XcApplication.cachedThreadPool.execute(() -> {
                                    //右
                                    cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 6, 1);
                                });
                            }
                        }
                        // up
                        else {
                            if ((y1 > y2) && (yy > MINLEN)) {
                                ToastUtil.getInstance().ShowToast("向上微调");
                                XcApplication.cachedThreadPool.execute(() -> {
                                    //上
                                    cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 0, 1);
                                });
                            }
                            // down
                            else if ((y1 < y2) && (yy > MINLEN)) {
                                ToastUtil.getInstance().ShowToast("向下微调");
                                XcApplication.cachedThreadPool.execute(() -> {
                                    //下
                                    cameraCommandUtil.postHttp(loginInfo.getIPCamera(), 2, 1);
                                });
                            }
                        }
                        x1 = 0;
                        y1 = 0;
                        break;
                }
            }
            return true;
        }
    }
}