package com.xiao.embeddedcar.Utils.Network;

import static android.content.Context.USB_SERVICE;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.Utils.PublicMethods.ToastUtil;
import com.xiao.embeddedcar.ViewModel.HomeViewModel;
import com.xiao.embeddedcar.ViewModel.MainViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//???未测试???
public class USBToSerialUtil {
    private final String TAG = this.getClass().getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static volatile USBToSerialUtil mInstance;
    private Context mContext;
    private HomeViewModel hvm;
    private MainViewModel mvm;

    /**
     * 私有构造器
     */
    private USBToSerialUtil() {}

    /**
     * 单例对象获取
     *
     * @return SerialUtil对象
     */
    public static USBToSerialUtil getInstance() {
        if (null == mInstance) synchronized (USBToSerialUtil.class) {
            if (null == mInstance) mInstance = new USBToSerialUtil();
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param context Activity上下文
     */
    public void init(Context context, HomeViewModel homeViewModel, MainViewModel mainViewModel) {
        this.mContext = context;
        this.hvm = homeViewModel;
        this.mvm = mainViewModel;
        this.mUsbManager = (UsbManager) context.getSystemService(USB_SERVICE);
    }

    /* =========================下面的代码实现了usb的功能========================= */
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    //消息刷新代码
    private static final int MESSAGE_REFRESH = 101;
    private final List<UsbSerialPort> mEntries = new ArrayList<>();
    //USB串口端口对象
    public static UsbSerialPort sPort;
    //USB管理对象
    private UsbManager mUsbManager;
    //USB设备连接对象
    private UsbDeviceConnection connection;
    //串口I/O管理对象
    private SerialInputOutputManager mSerialIoManager;
    //串口I/O管理对象监听
    private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
        @Override
        public void onRunError(Exception e) {
            Log.e(TAG, "Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {
            //通过USB接收小车回传的数据
            Message msg = hvm.getRehHandler().obtainMessage(1, data);
            msg.sendToTarget();
            final String message = "Read " + data.length + " bytes: \n" + HexDump.dumpHexString(data) + "\n\n";
            Log.e("SerialGetData", message);

        }
    };

    public UsbManager getmUsbManager() {
        return mUsbManager;
    }

    /**
     * 设置USB设备连接对象
     *
     * @param connection UsbDeviceConnection
     */
    public void setConnection(UsbDeviceConnection connection) {
        this.connection = connection;
    }

    /**
     * 连接USB串口
     */
    public void connectUSBSerial() {
        refreshDeviceList();
    }


    /**
     * 通过异步任务AsyncTask实现usb的获取
     */
    private void refreshDeviceList() {
        mvm.getModuleInfoTV().setValue("启动AsyncTask获取设备列表...");
        //https://shoewann0402.github.io/2020/03/06/android-R-AsyncTask-deprecated/
        new AsyncTask<Void, Void, List<UsbSerialPort>>() {
            @Override
            protected List<UsbSerialPort> doInBackground(Void... params) {
                Log.e(TAG, "Refreshing device list ...");
                final List<UsbSerialDriver> drivers =
                        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
                final List<UsbSerialPort> result = new ArrayList<>();
                /* 保存usb端口 */
                for (final UsbSerialDriver driver : drivers) {
                    final List<UsbSerialPort> ports = driver.getPorts();
                    Log.e(TAG, String.format("+ %s: %s port%s",
                            driver, ports.size(), ports.size() == 1 ? "" : "s"));
                    result.addAll(ports);
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<UsbSerialPort> result) {
                mEntries.clear();
                //保存usb端口
                mEntries.addAll(result);
                useUSBToSerial();
                Log.e(TAG, "Done refreshing, " + mEntries.size() + " entries found.");
                mvm.getModuleInfoTV().setValue("刷新成功,已发现" + mEntries.size() + "台设备!");
            }
        }.execute((Void) null);
    }

    /**
     * 使用USB转转口
     * 获取usb相关的一些变量
     */
    private void useUSBToSerial() {
        try {
            //A72上只有一个USB转串口,用position = 0即可
            sPort = mEntries.get(0);
            final UsbSerialDriver driver = sPort.getDriver();
            final UsbDevice device = driver.getDevice();
            final String usbId = String.format("Vendor %s  ，Product %s",
                    HexDump.toHexString((short) device.getVendorId()),
                    HexDump.toHexString((short) device.getProductId()));

        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "IndexOutOfBoundsException: 请检查USB连接状态!");
            mvm.getModuleInfoTV().setValue("IndexOutOfBoundsException: 串口通信失败,请检查USB连接状态!");
//            ToastUtil.getInstance().ShowToast("串口通信失败,请检查设备连接状态!");
        }
        //使用usb功能
        if (sPort != null) controlUSB();
    }

    /**
     * 打开usb设备,对usb参数进行设置.比如波特率、数据位、停止位、校验位
     */
    private void controlUSB() {
        Log.e(TAG, "Resumed,port= " + sPort);
        if (sPort == null)
            mvm.getModuleInfoTV().setValue(" UsbSerialPort对象为空,没有串口驱动!");
        else {
            tryGetUsbPermission();
            if (connection == null) {
                refreshDeviceList();
                mvm.getModuleInfoTV().setValue("UsbDeviceConnection对象为空,串口驱动失败!");
                return;
            }
            try {
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                mvm.getModuleInfoTV().setValue("IOException: 串口驱动错误!");
                try {
                    sPort.close();
                } catch (IOException ignored) {
                }
                sPort = null;
                return;
            }
            mvm.getModuleInfoTV().setValue("串口设备: " + sPort.getClass().getSimpleName());
        }
        onDeviceStateChange();
    }

    /**
     * 设备状态更改
     */
    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.e(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.e(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener); //添加监听
            XcApplication.mExecutor.submit(mSerialIoManager); //在新的线程中监听串口的数据变化
        }
    }

    public void onDestroy() {
        if (sPort == null) return;
        try {
            sPort.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sPort = null;
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
        mContext.registerReceiver(mUsbPermissionActionReceiver, filter);
        //https://zhuanlan.zhihu.com/p/396790121
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //here do emulation to ask all connected usb device for permission
        /* 向所有连接的usb设备请求许可 */
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            //add some conditional check if necessary
            //if(isWeCaredUsbDevice(usbDevice)){
            if (mUsbManager.hasPermission(usbDevice)) {
                //if has already got permission, just goto connect it
                //that means: user has choose yes for your previously popup window asking for grant permission for this usb device
                //and also choose option: not ask again
                afterGetUsbPermission(usbDevice);
            } else {
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
            //}
            //注销广播接收器
            mContext.unregisterReceiver(mUsbPermissionActionReceiver);
        }
    }

    /**
     * 权限获取后的动作片
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

    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
        Log.e("read data is :", "  " + message);
    }
}
