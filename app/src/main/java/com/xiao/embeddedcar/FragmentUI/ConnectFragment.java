package com.xiao.embeddedcar.FragmentUI;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.ScrollingMovementMethod;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.xiao.embeddedcar.Activity.MainActivity;
import com.xiao.embeddedcar.R;
import com.xiao.embeddedcar.Utils.CameraUtil.CameraConnectUtil;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.Utils.Network.USBToSerialUtil;
import com.xiao.embeddedcar.Utils.PublicMethods.FastDo;
import com.xiao.embeddedcar.Utils.PublicMethods.ToastUtil;
import com.xiao.embeddedcar.ViewModel.ConnectViewModel;
import com.xiao.embeddedcar.ViewModel.MainViewModel;
import com.xiao.embeddedcar.databinding.FragmentConnectBinding;

import java.util.Objects;

public class ConnectFragment extends AbstractFragment<FragmentConnectBinding, ConnectViewModel> {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private FragmentConnectBinding binding;
    private MainViewModel mainViewModel;
    private ConnectViewModel connectViewModel;
    //USB管理对象
    private final UsbManager mUsbManager = USBToSerialUtil.getInstance().getmUsbManager();

    public ConnectFragment() {
        super(FragmentConnectBinding::inflate, ConnectViewModel.class, true);
    }

    @Override
    public void initFragment(@NonNull FragmentConnectBinding binding, @Nullable ConnectViewModel viewModel, @Nullable Bundle savedInstanceState) {
        this.binding = binding;
        this.connectViewModel = viewModel;
        mainViewModel = getMainViewModel();
        binding.connectInfo.setText("");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void init() {
        /* 设置左侧TextView滚动 */
        binding.connectInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        /* 密码显示与隐藏 */
        binding.eyes.setOnClickListener(view -> {
            if (binding.loginPsd.getTransformationMethod() == HideReturnsTransformationMethod.getInstance()) {
                binding.loginPsd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.eyes.setImageResource(R.drawable.hide);
            } else {
                binding.loginPsd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.eyes.setImageResource(R.drawable.seek);
            }
        });
        /* 通讯switch控件监听事件 */
        binding.connectionMode.setOnCheckedChangeListener((compoundButton, b) -> connectViewModel.getConnectMode().setValue(b));
        /* 重置Button */
        binding.reset.setOnClickListener(view -> {
            binding.deviceId.setText(R.string.device_text);
            binding.loginName.setText(R.string.login_text);
            binding.loginPsd.setText(R.string.edit_password);
            binding.connectInfo.setText("");
            binding.IP.setText(R.string.null_data);
            binding.IPCamera.setText(R.string.null_data);
            binding.PureCameraIP.setText(R.string.null_data);
        });
        /* 连接Button */
        binding.connect.setOnClickListener(view -> {
            //禁止快速连按
            if (FastDo.isFastClick()) {
                if (XcApplication.isSerial != XcApplication.Mode.SOCKET) tryGetUsbPermission();
                requestConnect();
            }
        });
        /* 跳转到主页 */
        binding.navigation.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).navigate(R.id.action_nav_connect_to_nav_home));
    }

    @Override
    public void observerDataStateUpdateAction() {
        connectViewModel.getConnectInfo().setValue(null);
        connectViewModel.getConnectInfo().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.connectInfo.append(s + "\n");
        });
        connectViewModel.getConnectMode().observe(getViewLifecycleOwner(), b -> {
            binding.connectionMode.setChecked(b);
            if (b) {
                XcApplication.isSerial = XcApplication.Mode.SOCKET;
                binding.connectionMode.setText(R.string.wifi);
            } else {
                XcApplication.isSerial = XcApplication.Mode.USB_SERIAL;
                binding.connectionMode.setText(R.string.serial);
            }
        });
        mainViewModel.getLoginInfo().observe(getViewLifecycleOwner(), loginInfo -> {
            if (loginInfo == null) return;
            binding.IP.setText((loginInfo.getIP() == null || loginInfo.getIP().equals("0.0.0.0")) ? "IP获取失败" : loginInfo.getIP());
            binding.IPCamera.setText((loginInfo.getIPCamera() == null || loginInfo.getIPCamera().equals("null:81")) ? "摄像头IP获取失败" : loginInfo.getIPCamera());
            binding.IP.setText((loginInfo.getPureCameraIP() == null) ? "摄像头端口失败" : loginInfo.getPureCameraIP());
            connectViewModel.getConnectInfo().setValue("");
            MainActivity.setLoginInfo(loginInfo);
        });
        mainViewModel.getLoginState().observe(getViewLifecycleOwner(), s -> {
            if (Objects.equals(s, "Fail")) {
                connectViewModel.getConnectInfo().setValue("=====摄像头连接失败!=====\n");
                mainViewModel.getLoginState().setValue("");
            }
            if (Objects.equals(s, "Success")) {
                connectViewModel.getConnectInfo().setValue("=====摄像头连接成功!=====\n");
                mainViewModel.getLoginState().setValue("");
            }
        });
    }

    /**
     * 请求连接
     */
    public void requestConnect() {
        CameraConnectUtil.getInstance().cameraInit();
        connectViewModel.getConnectInfo().setValue("检查通讯方式...\n");
        //网络通讯
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET)
            connectViewModel.useNetwork(mainViewModel);
        else {
            //搜索摄像头然后启动摄像头
            connectViewModel.getConnectInfo().setValue("使用串口通讯\n");
            USBToSerialUtil.getInstance().connectUSBSerial();
            CameraConnectUtil.getInstance().search();
        }
    }

    /**
     * 获得 usb 权限
     * before open usb device
     * should try to get usb permission
     */
    private void tryGetUsbPermission() {
        //意图筛选器
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        //注册广播接收器
        requireActivity().registerReceiver(mUsbPermissionActionReceiver, filter);
        //https://zhuanlan.zhihu.com/p/396790121
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        //here do emulation to ask all connected usb device for permission
        /* 向所有连接的usb设备请求许可 */
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            //add some conditional check if necessary
            //if(isWeCaredUsbDevice(usbDevice)){
            if (mUsbManager.hasPermission(usbDevice)) {
                //if has already got permission, just goto connect it
                //that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
                //and also choose option: not ask again
                afterGetUsbPermission(usbDevice);
            } else {
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
            //}
            //注销广播接收器
            requireActivity().unregisterReceiver(mUsbPermissionActionReceiver);
        }
    }

    /**
     * 权限获取后的操作
     *
     * @param usbDevice -
     */
    private void afterGetUsbPermission(UsbDevice usbDevice) {
        //call method to set up device communication
        //Toast.makeText(this, String.valueOf("Got permission for usb device: " + usbDevice), Toast.LENGTH_LONG).show();
        ToastUtil.getInstance().ShowToast("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId(), 10);

        //now follow line will NOT show: User has not given permission to device UsbDevice
        USBToSerialUtil.getInstance().setConnection(mUsbManager.openDevice(usbDevice));
        //add your operation code here
    }

    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) synchronized (this) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (null != usbDevice) afterGetUsbPermission(usbDevice);
                } else
                    ToastUtil.getInstance().ShowToast("Permission denied for device" + usbDevice, 10);
            }
        }
    };
}