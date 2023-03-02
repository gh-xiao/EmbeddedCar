package com.xiao.embeddedcar.ViewModel;

import static com.xiao.embeddedcar.Activity.MainActivity.chief_status_flag;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiao.embeddedcar.Activity.MainActivity;
import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.Utils.PublicMethods.FastClick;
import com.xiao.embeddedcar.Utils.PublicMethods.ToastUtil;
import com.xiao.embeddedcar.Utils.PublicMethods.BaseConversion;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

@SuppressWarnings("unused")
public class HomeViewModel extends ViewModel {

    private static boolean ready = true;
    /* 视图ViewModel */
    //图片显示
    private final MutableLiveData<Bitmap> showImg = new MutableLiveData<>();
    //IP信息区
    private final MutableLiveData<String> ipShow = new MutableLiveData<>();
    //数据接收显示区
    private final MutableLiveData<String> dataShow = new MutableLiveData<>();
    //指令接收区
    private final MutableLiveData<byte[]> commandData = new MutableLiveData<>();
    //debug显示区
    private final MutableLiveData<String> debugArea = new MutableLiveData<>();
    //速度(前进)
    private final MutableLiveData<Integer> sp_n = new MutableLiveData<>(50);
    //速度(转弯)
    private final MutableLiveData<Integer> angle = new MutableLiveData<>(90);
    //码盘(车轮旋转周期/角度)
    private final MutableLiveData<Integer> mp_n = new MutableLiveData<>(100);

    /* 数据ViewModel */
    //连接状态
    private final MutableLiveData<Integer> connectState = new MutableLiveData<>();
    //光敏状态数据
    public MutableLiveData<Long> psStatus = new MutableLiveData<>();
    //超声波数据
    public MutableLiveData<Long> ultraSonic = new MutableLiveData<>();
    //光照强度数据
    public MutableLiveData<Long> light = new MutableLiveData<>();
    //码盘数据
    public MutableLiveData<Long> codedDisk = new MutableLiveData<>();

    /* getter */
    public MutableLiveData<Bitmap> getShowImg() {
        return showImg;
    }

    public MutableLiveData<String> getIpShow() {
        return ipShow;
    }

    public MutableLiveData<String> getDataShow() {
        return dataShow;
    }

    public MutableLiveData<byte[]> getCommandData() {
        return commandData;
    }

    public MutableLiveData<String> getDebugArea() {
        return debugArea;
    }

    public MutableLiveData<Integer> getSp_n() {
        return sp_n;
    }

    public MutableLiveData<Integer> getAngle() {
        return angle;
    }

    public MutableLiveData<Integer> getMp_n() {
        return mp_n;
    }

    public MutableLiveData<Integer> getConnectState() {
        return connectState;
    }

    public MutableLiveData<Long> getPsStatus() {
        return psStatus;
    }

    public MutableLiveData<Long> getUltraSonic() {
        return ultraSonic;
    }

    public MutableLiveData<Long> getLight() {
        return light;
    }

    public MutableLiveData<Long> getCodedDisk() {
        return codedDisk;
    }

    public Handler getRehHandler() {
        return rehHandler;
    }

    public static void setReady(boolean ready) {
        HomeViewModel.ready = ready;
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
        XcApplication.cachedThreadPool.execute(() -> ConnectTransport.getInstance().connect(rehHandler, MainActivity.getLoginInfo().getIP()));
    }

    /**
     * 串口模式下的通讯
     */
    private void serial_thread() {
        XcApplication.cachedThreadPool.execute(() -> ConnectTransport.getInstance().serial_connect(rehHandler));
    }

    /**
     * 接受设备发送的数据
     * (此写法避免引发内存泄露)
     */
    public final Handler rehHandler = new Handler(new WeakReference<Handler.Callback>(msg -> {
        if (msg.what == 1) {
            byte[] mByte = (byte[]) msg.obj;
            //信息获取
            if (mByte[0] == 0x55) {
                //光敏状态
                psStatus.setValue((long) (mByte[3] & 0xff));
                //超声波数据
                long aUltraSonic = mByte[5] & 0xff;
                aUltraSonic = aUltraSonic << 8;
                aUltraSonic += mByte[4] & 0xff;
                ultraSonic.setValue(aUltraSonic);
                //光照强度
                long aLight = mByte[7] & 0xff;
                aLight = aLight << 8;
                aLight += mByte[6] & 0xff;
                light.setValue(aLight);
                //码盘
                long aCodedDisk = mByte[9] & 0xff;
                aCodedDisk = aCodedDisk << 8;
                aCodedDisk += mByte[8] & 0xff;
                codedDisk.setValue(aCodedDisk);
                //主车传入数据
                if (mByte[1] == (byte) 0xaa && chief_status_flag) {
                    //显示数据
                    dataShow.setValue("超声波:" + aUltraSonic + "mm  " +
                            "光照度:" + aLight + "lx  " +
                            "码盘:" + aCodedDisk + "  " +
                            "运行状态:" + (String.valueOf(mByte[2])));
                    //主车防撞功能简易实现
                    if (aUltraSonic <= 100) ConnectTransport.getInstance().stop();
                }
                //从车传入数据
                if (mByte[1] == (byte) 0x02 && !chief_status_flag) {
                    if (mByte[2] == -110) {
                        byte[] newData;
                        Log.e("data", "" + mByte[4]);
                        newData = Arrays.copyOfRange(mByte, 5, mByte[4] + 5);
                        Log.e("data", "" + "长度" + newData.length);
                        //第二个参数指定编码方式
                        String str = new String(newData, StandardCharsets.US_ASCII);
                        ToastUtil.getInstance().ShowToast(str);
                    } else {
                        //显示数据
                        dataShow.setValue("超声波:" + aUltraSonic + "mm  " +
                                "光照度:" + aLight + "lx  " +
                                "码盘:" + aCodedDisk + "  " +
                                "运行状态:" + (String.valueOf(mByte[2])));
                    }
                }
            }
            /*TODO
             * 以下内容需要自己添加或修改
             * 自定义信息指令
             * 验证是否为自定义发送数据启动 */
            if (mByte[0] == (byte) 0xAA) {
                /* 验证是否为自定义发送数据启动 */
                if (mByte[1] == (byte) 0x12) {
                    debugArea.setValue("Android获取RFID卡成功!");
                    //RFID卡识别数据传入
                    char[] data = new char[16];
                    /* 获取RFID卡原数据 */
                    for (int i = 0; i < 16; i++)
                        data[i] = (char) Integer.parseInt(BaseConversion.decToHex(mByte[i + 4]).substring(6));
                    /* 检测到卡1处理 */
                    if (mByte[2] == (byte) 0x0A) {
                        debugArea.setValue("卡(1)数据:\n" + Arrays.toString(data));
                        ConnectTransport.setRFID1(data);
                        XcApplication.cachedThreadPool.execute(() -> ConnectTransport.getInstance().RFID1(mByte[3]));
                    }
                    /* 检测到卡2处理 */
                    if (mByte[2] == (byte) 0x0B) {
                        debugArea.setValue("卡(2)数据:\n" + Arrays.toString(data));
                        ConnectTransport.setRFID2(data);
                        XcApplication.cachedThreadPool.execute(() -> ConnectTransport.getInstance().RFID2(mByte[3]));
                    }
                    commandData.setValue(mByte);
                    return true;
                }
                /* 启动全自动 */
                debugArea.setValue("接收到主车传入0xAA指令: " + BaseConversion.decToHex(mByte[3]).substring(6).toUpperCase(Locale.ROOT));
                ConnectTransport.setMark(mByte[3]);
                XcApplication.cachedThreadPool.execute(() -> ConnectTransport.getInstance().half_Android());
                commandData.setValue(mByte);
            }
            return true;
        } else connectState.setValue(msg.what);
        return false;
    }).get());

    /**
     * 获取摄像头回传的图片
     */
    public void getCameraPicture() {
        if (ready) {
            debugArea.setValue("正在开启线程尝试获取摄像头传入图片...");
            ready = !ready;
            //单线程池
            XcApplication.singleThreadExecutor.execute(() -> ConnectTransport.getInstance().getPicture(getBitmapHandle));
        }
    }

    /**
     * 获取摄像头回传图片Handle
     */
    private final Handler getBitmapHandle = new Handler(new WeakReference<Handler.Callback>(msg -> {
        if (msg.what == 1) {
            showImg.setValue((Bitmap) msg.obj);
            return true;
        } else if (msg.what == 0) {
            if (FastClick.isFastClick()) refreshConnect();
        } else connectState.setValue(msg.what);

        return false;
    }).get());
}