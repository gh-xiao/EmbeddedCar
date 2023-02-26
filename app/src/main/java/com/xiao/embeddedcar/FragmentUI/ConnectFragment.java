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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.xiao.embeddedcar.Activity.MainActivity;
import com.xiao.embeddedcar.R;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.Utils.NetworkAndUIUtil.FastClick;
import com.xiao.embeddedcar.Utils.NetworkAndUIUtil.ToastUtil;
import com.xiao.embeddedcar.Utils.NetworkAndUIUtil.USBToSerialUtil;
import com.xiao.embeddedcar.ViewModel.ConnectViewModel;
import com.xiao.embeddedcar.databinding.FragmentConnectBinding;

import java.util.Objects;

public class ConnectFragment extends ABaseFragment {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private FragmentConnectBinding binding;
    private ConnectViewModel connectViewModel;
    //USB管理对象
    private final UsbManager mUsbManager = USBToSerialUtil.getInstance().getmUsbManager();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //ViewModel
        connectViewModel = new ViewModelProvider(requireActivity()).get(ConnectViewModel.class);
        //xml文件绑定
        binding = FragmentConnectBinding.inflate(inflater, container, false);
        //视图绑定
        View root = binding.getRoot();
        //控件动作初始化
        init();
        //设置观察者
        observerDataStateUpdateAction();
        return root;
    }

    @SuppressLint("SetTextI18n")
    @Override
    void init() {
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
        });
        /* 连接Button */
        binding.connect.setOnClickListener(view -> {
            //禁止快速连按
            if (FastClick.isFastClick()) {
                if (XcApplication.isSerial != XcApplication.Mode.SOCKET) tryGetUsbPermission();
                connectViewModel.requestConnect();
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_nav_connect_to_nav_home);
            }
        });
    }

    @Override
    void observerDataStateUpdateAction() {
        connectViewModel.getConnectInfo().observe(getViewLifecycleOwner(), s -> binding.connectInfo.append(s + "\n"));
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
        connectViewModel.getLoginInfo().observe(getViewLifecycleOwner(), loginInfo -> {
            if (loginInfo == null || loginInfo.getIP() == null) return;
            connectViewModel.getConnectInfo().setValue(loginInfo.toString());
            MainActivity.setLoginInfo(loginInfo);
        });
        connectViewModel.getLoginState().observe(getViewLifecycleOwner(), s -> {
            if (Objects.equals(s, "Fail"))
                connectViewModel.getConnectInfo().setValue("=====摄像头连接失败!=====\n");
        });
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
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
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