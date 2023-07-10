package com.xiao.embeddedcar.ViewModel;

import static com.xiao.embeddedcar.Activity.MainActivity.chief_status_flag;

import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.Utils.PublicMethods.BaseConversion;
import com.xiao.embeddedcar.Utils.PublicMethods.FastDo;
import com.xiao.embeddedcar.Utils.PublicMethods.ToastUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

@SuppressWarnings("unused")
public class HomeViewModel extends ViewModel {
    /* 视图ViewModel */
    //模块接收图片设置
//    private final MutableLiveData<Boolean> moduleImgMode = new MutableLiveData<>(true);
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
    //光敏状态数据
    public MutableLiveData<Long> psStatus = new MutableLiveData<>();
    //超声波数据
    public MutableLiveData<Long> ultraSonic = new MutableLiveData<>();
    //光照强度数据
    public MutableLiveData<Long> light = new MutableLiveData<>();
    //码盘数据
    public MutableLiveData<Long> codedDisk = new MutableLiveData<>();

    /* getter */

//    public MutableLiveData<Boolean> getModuleImgMode() {
//        return moduleImgMode;
//    }

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

    /**
     * 接受设备发送的数据
     * (此写法避免引发内存泄露)
     */
    private final Handler rehHandler = new Handler(new WeakReference<Handler.Callback>(msg -> {
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
                    for (int i = 0; i < 16; i++) {
                        try {
                            data[i] = (char) Integer.parseInt(BaseConversion.decToHex(mByte[i + 4]).substring(6));
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            ConnectTransport.getInstance().sendUIMassage(1, "数据解析错误!ERROR:\n" + sw);
                            data = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
                        }
                    }
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
                if (FastDo.isFastSend()) {
                    ConnectTransport.setMark(mByte[3]);
                    XcApplication.cachedThreadPool.execute(() -> ConnectTransport.getInstance().half_Android());
                }
                commandData.setValue(mByte);
            }
            return true;
        } else getConnectState(msg.what);
        return false;
    }).get());

    private void getConnectState(int i) {
        //连接状态
        switch (i) {
            case 3:
                debugArea.setValue("平台已连接,代码: 3");
                break;
            case 4:
                debugArea.setValue("平台连接失败或已关闭!,代码: 4");
                break;
            case 5:
                debugArea.setValue("WiFi通讯建立失败!,代码: 5");
                break;
            default:
                debugArea.setValue("请检查WiFi连接状态!,代码: 0");
                break;
        }
    }
}