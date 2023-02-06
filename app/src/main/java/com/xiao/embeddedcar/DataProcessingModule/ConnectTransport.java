package com.xiao.embeddedcar.DataProcessingModule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bkrcl.control_car_video.camerautil.CameraCommandUtil;
import com.xiao.baiduocr.TestInferOcrTask;
import com.xiao.embeddedcar.Activity.MainActivity;
import com.xiao.embeddedcar.Utils.CameraUtil.XcApplication;
import com.xiao.embeddedcar.Utils.NetworkAndUIUtil.SerialPort;
import com.xiao.embeddedcar.Utils.NetworkAndUIUtil.USBToSerialUtil;
import com.xiao.embeddedcar.Utils.PaddleOCR.PlateDetector;
import com.xiao.embeddedcar.Utils.QRcode.GetCode;
import com.xiao.embeddedcar.Utils.QRcode.QRBitmapCutter;
import com.xiao.embeddedcar.Utils.QRcode.WeChatQRCodeDetector;
import com.xiao.embeddedcar.Utils.Shape.ShapeDetector;
import com.xiao.embeddedcar.Utils.TrafficLight.ColorProcess;
import com.xiao.embeddedcar.Utils.TrafficLight.TrafficLight;
import com.xiao.embeddedcar.Utils.TrafficLight.TrafficLight_fix;
import com.xiao.embeddedcar.ViewModel.MainViewModel;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

/**
 * Socket数据处理类
 */
public class ConnectTransport {
    //目标类的简写名称
    private final String TAG = ConnectTransport.class.getSimpleName();
    //单例对象
    @SuppressLint("StaticFieldLeak")
    private static ConnectTransport mInstance;
    //上下文信息
    private Context mContext;
    //socket客户端对象
    private Socket socket;
    //ViewModel
    private MainViewModel mainViewModel;
    //客户端输入流
    private DataInputStream bInputStream;
    private InputStream SerialInputStream;
    //客户端输出流
    private DataOutputStream bOutputStream;
    private OutputStream SerialOutputStream;
    //消息线程 - 设备数据通讯
    private Handler reHandler;
    public byte[] rByte = new byte[50];
    //串口输入字节
    byte[] serialReadByte = new byte[50];
    private boolean inputDataState = false;
    //消息线程 - UI信息回传
    private Handler reMsgHandler;
    //摄像头命令工具类
    private final CameraCommandUtil cameraCommandUtil = new CameraCommandUtil();
    //从http获取到的图片
    private Bitmap stream;
    //识别的车牌号
    private String plate;
    //交通标志物识别编号
    private static short getTrafficFlag = 0x03;
    //二维码识别结果
    private String qrResult;
    //图形识别结果
    private int shapeResult = 0;
    //主从车控制判断
    public short TYPE = 0xAA;//170
    //从车与其他道具交互类型指令
    public short TYPE1 = 0x02;

    public Bitmap getStream() {
        return stream;
    }

    /**
     * 私有构造器
     */
    private ConnectTransport() {
    }

    /**
     * 获取ConnectTransport单例对象
     */
    public static synchronized ConnectTransport getInstance() {
        if (null == mInstance) {
            mInstance = new ConnectTransport();
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param context       MainActivityContext
     * @param mainViewModel -
     */
    public void init(Context context, MainViewModel mainViewModel) {
        this.mContext = context.getApplicationContext();
        this.mainViewModel = mainViewModel;
    }

    /**
     * 销毁socket
     */
    public void destroy() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                bInputStream.close();
                bOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 建立通讯
     *
     * @param reHandler 数据解析线程
     * @param IP        有效IP地址
     */
    public void connect(Handler reHandler, String IP) {
        try {
            this.reHandler = reHandler;
            int port = 60000;
            socket = new Socket(IP, port);
            bInputStream = new DataInputStream(socket.getInputStream());
            bOutputStream = new DataOutputStream(socket.getOutputStream());
            if (!inputDataState) reThread();
        } catch (SocketException e) {
            Log.e(TAG, "WiFi通讯建立失败!");
            Message msg = Message.obtain();
            msg.what = 5;
            reHandler.sendMessage(msg);
        } catch (IOException e) {
            Message msg = Message.obtain();
            msg.what = 0;
            reHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    /**
     * 串口通讯
     *
     * @param reHandler 数据解析线程
     */
    public void serial_connect(Handler reHandler) {
        this.reHandler = reHandler;
        try {
            int baudRate = 115200;
            String path = "/dev/ttyS4";
            SerialPort mSerialPort = new SerialPort(new File(path), baudRate, 0);
            SerialOutputStream = mSerialPort.getOutputStream();
            SerialInputStream = mSerialPort.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        XcApplication.cachedThreadPool.execute(new SerialRunnable());
    }

    /**
     * 串口通讯线程
     */
    class SerialRunnable implements Runnable {
        @Override
        public void run() {
            while (SerialInputStream != null) {
                try {
                    int num = SerialInputStream.read(serialReadByte);
                    String readSerialStr = new String(serialReadByte, 0, num, StandardCharsets.UTF_8);
                    Log.e("----serialReadByte----", "******" + readSerialStr);
                    Message msg = reHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = serialReadByte;
                    reHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Activity消息通讯线程 - 核心通讯
     */
    private void reThread() {
        new Thread(() -> {
            while (socket != null && !socket.isClosed()) {
                Message msg = this.reHandler.obtainMessage();
                try {
                    inputDataState = true;
                    //读取数据到rByte
                    //noinspection ResultOfMethodCallIgnored
                    bInputStream.read(rByte);
                    msg.what = 1;
                    msg.obj = rByte;
                    reHandler.sendMessage(msg);
                } catch (SocketException e) {
                    msg.what = 4;
                    reHandler.sendMessage(msg);
                    Log.e(TAG, "平台连接失败或已关闭!");
                    destroy();
                    inputDataState = false;
                } catch (IOException e) {
                    e.printStackTrace();
//                    vm.getConnectState().setValue(4);
                    Log.e(TAG, "IOException: 平台连接失败!");
                    destroy();
                    inputDataState = false;
                } catch (UnsupportedOperationException ignored) {
                    inputDataState = false;
                }
            }
        }).start();
    }

    /**
     * HomeFragment图片获取线程 - 核心通讯
     *
     * @param getBitmapHandle Handle
     */
    public void getPicture(Handler getBitmapHandle) {
        new Thread(() -> {
            boolean state = true;
            while (MainActivity.getLoginInfo().getIPCamera() != null && state &&
                    !MainActivity.getLoginInfo().getIPCamera().equals("null:81")) {
                stream = cameraCommandUtil.httpForImage(MainActivity.getLoginInfo().getIPCamera());
                Message msg = getBitmapHandle.obtainMessage();
                if (stream != null) {
                    msg.what = 1;
                    msg.obj = stream;
                    getBitmapHandle.sendMessage(msg);
                } else {
                    state = false;
                    msg.what = 0;
                    getBitmapHandle.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * @param reMsgHandler 设置消息返回的Handler
     */
    public void setReMsgHandler(Handler reMsgHandler) {
        this.reMsgHandler = reMsgHandler;
    }

    /**
     * 向UI线程回传消息
     *
     * @param what 类型
     * @param obj  msg/bitmap
     */
    public void sendUIMassage(int what, Object obj) {
        Message msg = this.reMsgHandler.obtainMessage();
        msg.what = what;
        if (what == 1) {
            Log.i(this.TAG, (String) obj);
            msg.obj = obj;
            this.reMsgHandler.sendMessage(msg);
        }
        if (what == 2) {
            msg.obj = obj;
            this.reMsgHandler.sendMessage(msg);
        }
    }

    /**
     * <p>zigbee主车与从车通讯方法</p>
     * <p>TYPE = 0xAA -> 主车</p>
     * <p>TYPE = 0x02 -> 从车</p>
     *
     * @param MAJOR  操作指令-在主车上也被称为包头
     * @param FIRST  指令1
     * @param SECOND 指令2
     * @param THIRD  指令3
     */
    private void send(short MAJOR, short FIRST, short SECOND, short THIRD) {
        short CHECKSUM = (short) ((MAJOR + FIRST + SECOND + THIRD) % 256);
        // 发送数据字节数组
        final byte[] sByte = {0x55, (byte) TYPE, (byte) MAJOR, (byte) FIRST, (byte) SECOND, (byte) THIRD, (byte) CHECKSUM, (byte) 0xBB};
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
            XcApplication.cachedThreadPool.execute(() -> {
                try {
                    if (socket != null && !socket.isClosed()) {
                        bOutputStream.write(sByte, 0, sByte.length);
                        bOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.SERIAL) {
            XcApplication.cachedThreadPool.execute(() -> {
                try {
                    SerialOutputStream.write(sByte, 0, sByte.length);
                    SerialOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.USB_SERIAL)
            try {
                USBToSerialUtil.sPort.write(sByte, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {
                Log.e("UART:", "offline");
            }
//        FirstActivity.toastUtil.ShowToast("成功发送指令" + Arrays.toString(sByte));
    }

    /**
     * <p>zigbee其他通讯方法</p>
     * <p>[ 0x55, TYPE1, MAJOR, FIRST, SECOND, THIRD, CHECKSUM, 0xBB ]</p>
     *
     * @param MAJOR  操作指令-在主车上也被称为包头
     * @param FIRST  指令1
     * @param SECOND 指令2
     * @param THIRD  指令3
     */
    private void sendOther(short MAJOR, short FIRST, short SECOND, short THIRD) {
        short CHECKSUM = (short) ((MAJOR + FIRST + SECOND + THIRD) % 256);
        // 发送数据字节数组
        final byte[] sByte = {0x55, (byte) TYPE1, (byte) MAJOR, (byte) FIRST, (byte) SECOND, (byte) THIRD, (byte) CHECKSUM, (byte) 0xBB};
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
            XcApplication.cachedThreadPool.execute(() -> {
                try {
                    if (socket != null && !socket.isClosed()) {
                        bOutputStream.write(sByte, 0, sByte.length);
                        bOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.SERIAL) {
            XcApplication.cachedThreadPool.execute(() -> {
                try {
                    SerialOutputStream.write(sByte, 0, sByte.length);
                    SerialOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.USB_SERIAL) {
            try {
                USBToSerialUtil.sPort.write(sByte, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {
                Log.e("UART:", "offline");
            }
        }

//        FirstActivity.toastUtil.ShowToast("成功发送指令" + Arrays.toString(sByte));
    }

    /**
     * 语音播报
     *
     * @param textByte -
     */
    public void send_voice(final byte[] textByte) {
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
            XcApplication.cachedThreadPool.execute(() -> {
                try {
                    if (socket != null && !socket.isClosed()) {
                        bOutputStream.write(textByte, 0, textByte.length);
                        bOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.SERIAL) {
            XcApplication.cachedThreadPool.execute(() -> {
                try {
                    SerialOutputStream.write(textByte, 0, textByte.length);
                    SerialOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.USB_SERIAL)
            try {
                USBToSerialUtil.sPort.write(textByte, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {
                Log.e("UART:", "offline");
            }
    }

    /**
     * 停车
     */
    public void stop() {
        short MAJOR = 0x01;
        send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 前进
     *
     * @param sp_n 速度
     * @param en_n 码盘
     */
    public void go(int sp_n, int en_n) {
        short MAJOR = 0x02;
        send(MAJOR, (byte) (sp_n & 0xFF), (byte) (en_n & 0xff), (byte) (en_n >> 8));
    }

    /**
     * 后退
     *
     * @param sp_n 速度
     * @param en_n 码盘
     */
    public void back(int sp_n, int en_n) {
        short MAJOR = 0x03;
        send(MAJOR, (byte) (sp_n & 0xFF), (byte) (en_n & 0xff), (byte) (en_n >> 8));
    }

    /**
     * 左转
     *
     * @param angle 速度
     */
    public void left(int angle) {
        short MAJOR = 0x04;
        send(MAJOR, (byte) (angle & 0xFF), (byte) 0x00, (byte) 0x00);
    }

    /**
     * 右转
     *
     * @param angle 速度
     */
    public void right(int angle) {
        short MAJOR = 0x05;
        send(MAJOR, (byte) (angle & 0xFF), (byte) 0x00, (byte) 0x00);
    }

    /**
     * 循迹
     *
     * @param sp_n 速度
     */
    public void line(int sp_n) {
        short MAJOR = 0x06;
        send(MAJOR, (byte) (sp_n & 0xFF), (short) 0x00, (short) 0x00);
    }

    /**
     * 清除码盘值
     */
    public void clear() {
        short MAJOR = 0x07;
        send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 主从车状态转换
     *
     * @param i =1:从车/=2:主车
     */
    public void stateChange(final int i) {
        final short temp = TYPE;
        short MAJOR = 0x80;
        new Thread(() -> {
            //从车状态
            if (i == 1) {
                //接收从车数据
                TYPE = 0x02;
                send(MAJOR, (short) 0x01, (short) 0x00, (short) 0x00);
                YanChi(500);
                //关闭接送主车数据
                TYPE = 0xAA;
                send(MAJOR, (short) 0x01, (short) 0x00, (short) 0x00);
            }
            // 主车状态
            else if (i == 2) {
                //关闭接送从车数据
                TYPE = 0x02;
                send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
                YanChi(500);
                //接收主车数据
                TYPE = 0xAA;
                send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
            }
            TYPE = temp;
        }).start();
    }

    /**
     * 红外
     *
     * @param one   指令1
     * @param two   指令2
     * @param third 指令3
     * @param four  指令4
     * @param five  指令5
     * @param six   指令6
     */
    public void infrared(final byte one, final byte two, final byte third, final byte four, final byte five, final byte six) {
        new Thread(() -> {
            short MAJOR = 0x10;
            send(MAJOR, one, two, third);
            YanChi(500);
            MAJOR = 0x11;
            send(MAJOR, four, five, six);
            YanChi(500);
            MAJOR = 0x12;
            send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
            YanChi(1000);
        }).start();
    }

    /**
     * 双色led灯
     *
     * @param command -
     */
    public void lamp(byte command) {
        short MAJOR = 0x40;
        send(MAJOR, command, (short) 0x00, (short) 0x00);
    }

    /**
     * 小车方向指示灯
     *
     * @param left  0/1 - 关/开
     * @param right 0/1 - 关/开
     */
    public void light(int left, int right) {
        short MAJOR = 0x20;
        send(MAJOR, (short) left, (short) right, (short) 0x00);
    }

    /**
     * 蜂鸣器
     *
     * @param i 0/1 - 关/开
     */
    public void buzzer(int i) {
        short MAJOR = 0x30;
        short FIRST = (short) (i == 1 ? 0x01 : 0x00);
        send(MAJOR, FIRST, (short) 0x00, (short) 0x00);
//        小车内置硬件,如需要让从车启动,则将TYPE设为0x02
//        sendSecond(MAJOR, FIRST, (short) 0x00, (short) 0x00);
    }

    /**
     * 从车二维码识别
     * TODO 注意查看从车重新修改过的指令
     *
     * @param state 开启/关闭识别
     */
    public void qr_rec(int state) {
//        sendOther()调用的TYPE1默认值为0x02,即从车通讯
        short MAJOR = (byte) state;
        sendOther(MAJOR, (short) 0x92, (short) 0x00, (short) 0x00);
    }

    /**
     * 智能路灯标志物
     *
     * @param i 1,2,3
     */
    public void gear(int i) {
        short MAJOR = 0x61;
        if (i == 2) MAJOR = 0x62;
        else if (i == 3) MAJOR = 0x63;
        send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
//        红外通讯,如需要让从车启动,则将TYPE设为0x02
//        sendSecond(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 立体显示标志物
     *
     * @param data -
     */
    public void infrared_stereo(final short[] data) {
        short MAJOR = 0x10;
        send(MAJOR, (short) 0xff, data[0], data[1]);
        YanChi(500);
        MAJOR = 0x11;
        send(MAJOR, data[2], data[3], data[4]);
        YanChi(500);
        MAJOR = 0x12;
        send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(700);
    }

    /**
     * 智能交通灯标志物
     *
     * @param type  0x0E/0x0F - A灯/B灯
     * @param major 操作指令
     * @param first 主指令1
     */
    public void traffic_control(int type, int major, int first) {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) type;
        sendOther((byte) major, (byte) first, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    /**
     * 舵机角度控制
     *
     * @param major 左侧舵机
     * @param first 右侧舵机
     */
    public void rudder_control(int major, int first) {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) 0x0C;
        sendOther((byte) 0x08, (byte) major, (byte) first, (short) 0x00);
        TYPE1 = temp;
    }

    /**
     * 立体车库标志物
     *
     * @param type  0x0D/0x05 - A库/B库
     * @param major 操作指令
     * @param first 主指令1
     */
    public void garage_control(int type, int major, int first) {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) type;
        sendOther((byte) major, (byte) first, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    /**
     * 道闸标志物
     *
     * @param major  操作指令
     * @param first  主指令1
     * @param second 主指令2
     * @param third  主指令3
     */
    public void gate(int major, int first, int second, int third) {
        byte temp = (byte) TYPE1;
        TYPE1 = 0x03;
        sendOther((byte) major, (byte) first, (byte) second, (byte) third);
        TYPE1 = temp;
    }

    /**
     * LCD显示标志物
     */
    /* 数码管停止计时 */
    public void digital_close() {
        byte temp = (byte) TYPE1;
        TYPE1 = 0x04;
        short MAJOR = 0x03;
        sendOther(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    /* 数码管开始计时 */
    public void digital_open() {
        byte temp = (byte) TYPE1;
        TYPE1 = 0x04;
        short MAJOR = 0x03;
        sendOther(MAJOR, (short) 0x01, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    /* 数码管清零 */
    public void digital_clear() {
        byte temp = (byte) TYPE1;
        TYPE1 = 0x04;
        short MAJOR = 0x03;
        sendOther(MAJOR, (short) 0x02, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    /* LCD显示标志物第二排显示距离 */
    public void digital_dic(int dis) {
        byte temp = (byte) TYPE1;
        int a, b, c;
        a = (dis / 100) & (0xF);
        b = (dis % 100 / 10) & (0xF);
        c = (dis % 10) & (0xF);
        b = b << 4;
        b = b | c;
        TYPE1 = 0x04;
        short MAJOR = 0x04;
        sendOther(MAJOR, (short) 0x00, (short) a, (short) b);
        TYPE1 = temp;
    }

    /* 数码管 */
    public void digital(int i, int one, int two, int three) {
        byte temp = (byte) TYPE1;
        TYPE1 = 0x04;
        //i==1数据写入第一排数码管//i==2数据写入第二排数码管
        short MAJOR = (short) (i == 1 ? 0x01 : 0x02);
        sendOther(MAJOR, (byte) one, (byte) two, (byte) three);
        TYPE1 = temp;
    }

    /**
     * 语音播报随机指令
     */
    public void VoiceBroadcast() {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) 0x06;
        sendOther((short) 0x20, (short) 0x01, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    /**
     * 智能TFT显示标志物
     *
     * @param type    0x0B/0x08 - TFT(A)/TFT(B)
     * @param MAIN    操作指令
     * @param KIND    指令1
     * @param COMMAND 指令2
     * @param DEPUTY  指令3
     */
    public void TFT_LCD(int type, int MAIN, int KIND, int COMMAND, int DEPUTY) {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) type;
        sendOther((short) MAIN, (byte) KIND, (byte) COMMAND, (byte) DEPUTY);
        TYPE1 = temp;
    }

    /**
     * 无线充电标志物
     *
     * @param MAIN    操作指令
     * @param KIND    指令1
     * @param COMMAND 指令2
     * @param DEPUTY  指令3
     */
    public void magnetic_suspension(int MAIN, int KIND, int COMMAND, int DEPUTY) {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) 0x0A;
        sendOther((short) MAIN, (byte) KIND, (byte) COMMAND, (byte) DEPUTY);
        TYPE1 = temp;
    }

    /**
     * 使用线程沉睡进行阻塞式线程延迟
     *
     * @param time ms(毫秒)
     */
    public void YanChi(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取超声波数据
     *
     * @return long型整数
     */
    private long getUltraSonic() {
        long aUltraSonic = rByte[5] & 0xff;
        aUltraSonic = aUltraSonic << 8;
        aUltraSonic += rByte[4] & 0xff;
        return aUltraSonic;
    }

    /* ==================================================================================================== */

    /**
     * <p>程序自动执行</p>
     * <p>(也可以从这里修改需要启动的自动驾驶方案或需要测试的模块) - 当前已经不建议这样做,你可以选择从{@link com.xiao.embeddedcar.FragmentUI.ModuleFragment}中的module_select()方法添加并测试相应模块</p>
     */
    public void autoDrive() {
        /* 通讯测试 */
//        final short temp = TYPE;
//        short MAJOR = 0x00;
//        TYPE = 0xEE;
//        send(MAJOR, (short) 0x01, (short) 0x00, (short) 0x00);
//        YanChi(500);
//        TYPE = temp;
        /* 全安卓1方案 */
//        Q1();
        /* 半安卓2方案 */
        half_Android();
        /* 全安卓2方案 */
//        Q2();
        /* 二维码模块测试 */
//        QR_mod();
        /* 车牌OCR识别模块测试 */
//        plate_mod_branch2();
        /* 形状识别测试 */
//        Shape_mod();
        /* 立体显示标志物 */
//        infrared_stereo(new short[]{0x11, getTrafficFlag, getTrafficFlag, 0x00, 0x00});
        /* ----- */
//        ReadCard_long2crossroads();
        /* ????? */
//        YanChi(1500);
//        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
//        YanChi(2500);
//        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
//        ReadCard_short2crossroads();
//        YanChi(1000);
//        left(90);
//        YanChi(2500);
//        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
//        YanChi(4000);
//        ReadCard_longLine();
//        YanChi(1000);
//        ReadCard_short2crossroads();
    }

    /**
     * <p>程序自动执行</p>
     * 官方原方法 - 弃用
     */
    @Deprecated
    public void Deprecated_autoDrive() {
        short MAJOR = 0xA0;
        send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /* ================================================== */

    /**
     * 半安卓控制方案
     */
    //半安卓控制的处理模块值
    private static int mark = 1;
    //半安卓控制主车行进路线的字段
    private static int carGoto = 1;

    /**
     * 重置Android控制的处理模块值
     *
     * @param mark TODO 注意题意标志物的顺序,可根据Android或主车控制该值
     */
    public static void setMark(int mark) {
        ConnectTransport.mark = mark;
    }

    /**
     * 重置主车行进路线模块的方法
     *
     * @param carGoto 默认设为1,从头开始
     */
    public static void setCarGoto(int carGoto) {
        ConnectTransport.carGoto = carGoto;
    }

    public void half_Android() {
        switch (mark) {
            //开始半自动
            case 1:
                //回发握手指令
//                send((short) 0, (short) 0, (short) 0, (short) 0);
                YanChi(500);
                send((short) (0xA0 + carGoto++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //车牌识别/图形识别/交通标志物识别+发送道闸车牌
            case 2:
                send((short) (0xA0 + carGoto++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //二维码识别
            case 3:
                WeChatQR_mod();
                send((short) (0xA0 + carGoto++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //红绿灯识别
            case 4:
                trafficLight_mod();//A
                send((short) (0xA0 + carGoto++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //立体显示 - 安卓控制
            case 5:
                send((short) (0xA0 + carGoto++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //TFT合并项目 - TFTA
            case 6:
                System.out.println("TFT");

                plate_mod_branch3();
                Shape_mod();

                YanChi(1500);
                short i10 = (short) (0xA0 + carGoto++);
                for (int J = 0; J < 3; J++) send(i10, (short) 0x00, (short) 0x00, (short) 0x00);
                System.out.println("启动主车: " + i10);
                break;
            //TFT合并项目 - TFTB
            case 7:
                send((short) (0xA0 + carGoto++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //-----
            case 8:
                send((short) (0xA0 + carGoto++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            case 9:
                break;
        }
    }

    /* ================================================== */

    /**
     * <p>全安卓控制方案</p>
     * 指令协议:
     * B1前进
     * B2短前进
     * B3左转
     * B4右转
     * B5倒车入库
     * B6读卡(未测试)(停车版本)
     * B7读卡2(未测试)(无需停车版本)
     * B8左45
     * B9右45
     * C0语音识别
     * C1短后退
     * C2返回语音标志物识别信息至立体显示标志物
     * C3(仅)获取智能路灯目标初始挡位
     * TODO C_获取智能路灯目标初始挡位并调灯-----
     * C4初始化赛场LED数码显示管和车库
     * C5(仅)倒车入库
     */
    private void Q1() {

        digital_clear();
        YanChi(500);
        digital_open();

        // B8->B6->右转
        YanChi(1000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("B8->B6->右转-----模块完成");

        // 过ETC->D6->右转面向智能语音播报系统
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("过ETC->D6->右转面向智能语音播报系统-----模块完成");
        /*TODO
         * 语音播报控制
         */
        //----------

        //----------

        // 左转->过障碍物通道->左转面向红绿灯
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        /*TODO
         * 红绿灯
         */
        //----------
        trafficLight_mod();
        WeChatQR_mod();
        //----------
        System.out.println("左转->过障碍物通道->左转->红绿灯识别完成-----模块完成");

        // 前进到F4->识别二维码
        YanChi(1500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        /*TODO
         * 二维码识别
         */
        YanChi(1500);
        //---------

        //---------
        System.out.println("前进到F4->识别二维码-----模块完成");

        // 左转->前进到D4
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("左转->前进到D4-----模块完成");

        // 右转->前进到D2->OCR识别/车牌识别
        YanChi(2500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        ReadCard_short2crossroads();
        YanChi(1000);

        /*TODO
         * OCR识别/车牌识别
         */
        //----------
//        plate_mod_branch1();
        //----------
        System.out.println("右转->前进到D2->OCR识别/车牌识别-----模块完成");

        // 左转->读卡并前进到B2
        YanChi(2500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        /*TODO
         * 这里改为循迹读卡
         */
        YanChi(2500);
        //----------
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3000);
        //长线-----半路程
        ReadCard_longLine();
        YanChi(2500);
        //B2卡位
        line(90);
        YanChi(800);
        stop();
        YanChi(500);
        send((short) 0xB2, (short) 0x00, (short) 0x00, (short) 0x00);
        //读卡
        YanChi(1500);
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        System.out.println("左转->读卡并前进到B2-----模块完成");

//        System.out.println(easyDL());

        //倒车入库&&启动从车
        YanChi(3500);
        sendOther((short) 160, (short) 162, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xA6, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("倒车入库启动");

    }

    private void Q2() {

        YanChi(1000);
        garage_control(0x0D, 0x01, 0x01);
        YanChi(1000);
        digital_clear();
        YanChi(1000);
        digital_open();

        // F7->F6->右转45
        YanChi(1000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB9, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        //向TFT前进减少干扰
        send((short) 0xB2, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2000);
        System.out.println("F7->F6->右转45-----模块完成");
        //----------
        //识别图形
//        Shape_mod();
        //识别车牌
//        plate_mod_branch3();
        //识别交通标志物

        //----------
        System.out.println("识别完成");
        //左转面向E6道闸
        //后退到原位置
        YanChi(1000);
        send((short) 0xC1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("左转面向E6道闸完成");

        //F6->打开道闸->D6
        YanChi(3500);
        //发送信息给道闸
        for (int J = 0; J < 3; J++) {
            gate(0x10, plate.charAt(0), plate.charAt(1), plate.charAt(2));
            YanChi(500);
        }
        //发送信息给道闸
        for (int J = 0; J < 3; J++) {
            gate(0x11, plate.charAt(3), plate.charAt(4), plate.charAt(5));
            YanChi(100);
        }
        YanChi(500);
        gate(0x01, 0x01, 0x00, 0x00);
        YanChi(1000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F6->打开道闸->D6-----模块完成");

        // D6->B6->左转面向静态标志物
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("D6->B6->左转面向静态标志物-----模块完成");

        // 识别二维码/LCD发送测距信息->右转180->识别红绿灯
        WeChatQR_mod();
        YanChi(500);
        for (int J = 0; J < 3; J++) {
            digital_dic((int) getUltraSonic());
            YanChi(100);
        }
        YanChi(1500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
//        trafficLight_mod(1);
        System.out.println("识别二维码->左转/右转180->识别红绿灯-----模块完成");

        // B6->B4->左转面向语音播报
        YanChi(1500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        //----------
        send((short) 0xC0, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        System.out.println("B6->B4->左转面向语音播报-----模块完成");

        // 右转面向立体显示->发送数据
        YanChi(35000);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB9, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        YanChi(2000);
        infrared_stereo(new short[]{0x15, getTrafficFlag, getTrafficFlag, 0x00, 0x00});
        //----------
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("右转面向立体显示->发送数据-----模块完成");

        // B4->寻卡->F4
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);

//        YanChi(2500);
//        ReadCard_longLine();
//        YanChi(2500);
//        ReadCard_short2crossroads();
//        YanChi(2500);
//        ReadCard_longLine();
//        YanChi(2500);
//        ReadCard_short2crossroads();


        System.out.println("B4->寻卡->F4-----模块完成");

        // F4->左转->F2
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F4->左转->F2-----模块完成");

        // 右转->调整智能路灯->左转面向报警台->左转面向特殊地形
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        YanChi(3500);
        //RFID卡解密结果
        int k = 2;
        //图形解析结果 - 红色图形数量
        int r = shapeResult;
        //灯光初始挡位
//        int n = HomeFragment.getLight();
        int n = 500;
        Log.i(TAG, "RFID解密结果: " + k + " 图形解析结果: " + r + " 灯光初始挡位: " + n);
        int i = (int) (Math.pow((k + r), n) % 4 + 1);
        gear(i);
        //----------
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB8, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        //开启报警台
        //----------
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);

        // F2->特殊地形->B2
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);

        //倒车入库&&启动从车
        YanChi(9000);
//        sendOther((short) 160, (short) 162, (short) 0x00, (short) 0x00);
//        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB5, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("倒车入库启动");


    }

    private void Q3() {

        YanChi(1000);
        for (int J = 0; J < 3; J++) {
            digital_clear();
            YanChi(50);
        }
        YanChi(1500);
        digital_open();

        // F7->F6
        YanChi(1000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F7->F6-----模块完成");

        //左转面向ETC
        YanChi(2500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("左转面向ETC完成");

        //F6->打开ETC->B6
        YanChi(3500);
        //-----?????-----
//        rudder_control(0x01, 0x01);
        //-----?????-----
        line(90);
        YanChi(800);
        stop();
        //等待ETC开启
        YanChi(1500);
        //通过ETC到D6
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F6->打开ETC->D6-----模块完成");

        // D6->B6->左面向静态标志物
        YanChi(1500);
        line(90);
        YanChi(3500);
        System.out.println("D6->B6->面向静态标志物-----模块完成");

        // 识别二维码/LCD发送测距信息->右转180->识别红绿灯
        WeChatQR_mod();
        for (int J = 0; J < 3; J++) {
            digital_dic((int) getUltraSonic());
            YanChi(100);
        }
        YanChi(1000);
        send((short) 0xB2, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1000);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("识别二维码->右转180-----模块完成");

        // B6->B4->左转面向TFT(A)
        YanChi(2500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        //----------
        plate_mod_branch3();
        Shape_mod();
        sendOther((short) 160, (short) 162, (short) 0x00, (short) 0x00);
        //----------
        System.out.println("B6->B4->左转面向TFT(A)-----模块完成");

        // 右转面向烽火台->发送数据
        YanChi(2000);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB9, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        YanChi(2000);
        //开启烽火台
        //----------
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("右转面向烽火台->发送数据-----模块完成");

        // B4->红绿灯识别->D4->开启道闸->F4
        //----------
        trafficLight_mod();
        //----------
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        //----------
        //发送信息给道闸
        for (int J = 0; J < 3; J++) {
            gate(0x10, plate.charAt(0), plate.charAt(1), plate.charAt(2));
            YanChi(500);
        }
        //发送信息给道闸
        for (int J = 0; J < 3; J++) {
            gate(0x11, plate.charAt(3), plate.charAt(4), plate.charAt(5));
            YanChi(100);
        }
        YanChi(500);
        gate(0x01, 0x01, 0x00, 0x00);
        //----------
        YanChi(3000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("B4->红绿灯识别->D4->开启道闸->F4-----模块完成");

        // F4->识别交通标志物->左转->F2
        //----------
//        easyDL();
        //----------
        YanChi(1500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F4->左转->F2-----模块完成");

        // 左转45面向报警台->左转面向特殊地形
        YanChi(1500);
        send((short) 0xB8, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        //向立体显示物发数据
        //----------
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);

        // F2->特殊地形->B2->语音识别
        YanChi(2500);
        line(90);
        YanChi(1300);
        go(50, 1200);
        YanChi(2500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2000);
        //----------
        send((short) 0xC0, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        YanChi(30000);

        //倒车入库
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB5, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("倒车入库启动");

    }

    public void Q4() {

        YanChi(1000);
        for (int J = 0; J < 3; J++) {
            digital_clear();
            YanChi(50);
        }
        YanChi(1500);
        digital_open();


        // B7->B6
        YanChi(500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("B7->B6-----模块完成");

        //左转面向静态标志物A
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);

        System.out.println("右转面向静态标志物A完成");

        //B6->测距
        YanChi(3500);
        for (int J = 0; J < 3; J++) {
            digital_dic((int) getUltraSonic());
            YanChi(100);
        }
        YanChi(1000);
        System.out.println("B6->测距-----模块完成");

        // B6->左转/右转面向F6->D6->F6
        YanChi(2500);
        //右转
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3000);
        //右转
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        System.out.println("B6->左转/右转面向F6->D6->F6-----模块完成");

        // 语音播报->左转向立体显示物->向立体显示物发送语音播报数据->右转面向F2
        //-----语音播报-----
        send((short) 0xC0, (short) 0x00, (short) 0x00, (short) 0x00);
        //-----语音播报-----
        YanChi(21000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        //左转45
        YanChi(3500);
        send((short) 0xB8, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2000);
        //-----向立体显示物发送数据-----
        send((short) 0xC2, (short) 0x00, (short) 0x00, (short) 0x00);
        //-----向立体显示物发送数据-----
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("语音播报->向立体显示物发送语音播报数据->有转面向F2-----模块完成");

        // F6->F4->F2
        YanChi(2000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F6->F4->F2-----模块完成");

        // 获取智能路灯标志物初始档位->调灯->左转面向特殊地形
        YanChi(3500);
        //-----获取智能路灯标志物初始档位-----
        send((short) 0xC3, (short) 0x00, (short) 0x00, (short) 0x00);
        //-----获取智能路灯标志物初始档位-----
        YanChi(15000);
        //-----从车启动-----
        //TODO 从车启动
        //-----从车启动-----
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("获取智能路灯标志物初始档位->调灯->左转面向特殊地形-----模块完成");

        //F2->B2
        YanChi(3000);
        //-----读卡前进并通过地形标志物-----
        send((short) 0xB7, (short) 0x00, (short) 0x00, (short) 0x00);
        //-----读卡前进并通过地形标志物-----
        YanChi(15000);
        System.out.println("F2->B2-----模块完成");

        // 左转面向B4->B4->--左转面向ETC->通过ETC到D4
        YanChi(2000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        //通过ETC
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("左转面向B4->B4->--左转面向ETC->通过ETC到D4-----模块完成");

        //左转45面向烽火台->发送数据->右转面向车库
        YanChi(4000);
        send((short) 0xB8, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        //TODO 开启烽火台

        //----------
        YanChi(4000);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2000);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("右转面向烽火台->发送数据->右转面向车库-----模块完成");

        // D4->D6
        YanChi(3000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("D4->D6-----模块完成");

        //左转->左转面向特殊地形
        YanChi(4000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("左转->左转面向特殊地形-----模块完成");

        //倒车入库
        YanChi(1500);
        send((short) 0xB5, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("倒车入库启动");

    }

    /* ================================================== */

    /**
     * 红绿灯模块
     */
    public synchronized void trafficLight_mod() {
        int i = mainViewModel.getSend_trafficLight().getValue() != null ? mainViewModel.getSend_trafficLight().getValue() : 1;
        sendUIMassage(1, "等待摄像头向上微调延迟...");
        YanChi(1500);
        cameraCommandUtil.postHttp(MainActivity.getLoginInfo().getIPCamera(), 0, 1);
        for (int J = 0; J < 3; J++) {
            YanChi(100);
            traffic_control(i + 13, 1, 0);
        }
        sendUIMassage(1, "智能交通灯标志物进入识别模式");
        ColorProcess c = new ColorProcess(mContext);
        YanChi(3500);
        sendUIMassage(1, "对图片进行加工...");
        c.PictureProcessing(stream);
        sendUIMassage(1, "生成结果...");
        String color = TrafficLight_fix.Identify(c.getResult());
        sendUIMassage(1, "保存图片...");
        TrafficLight_fix.saveBitmap();
        sendUIMassage(2, c.getResult());
        sendUIMassage(1, "识别的颜色: " + color);
        sendToTrafficLight(color, i);
        sendUIMassage(1, "发送给交通灯: " + (i == 1 ? "A" : "B"));
        sendUIMassage(1, "复位摄像头...");
        cameraCommandUtil.postHttp(MainActivity.getLoginInfo().getIPCamera(), 2, 1);
        YanChi(1000);
    }

    /**
     * 给智能交通灯标志物发送信息
     *
     * @param color 识别的颜色
     * @param i     智能交通灯标志物A/B
     */
    private void sendToTrafficLight(String color, int i) {
        switch (color) {
            case "红灯":
                for (int J = 0; J < 10; J++) {
                    YanChi(100);
                    traffic_control(0x0D + i, 0x02, 0x01);
                }
                System.out.println("识别为红灯");
                break;
            case "绿灯":
                for (int J = 0; J < 10; J++) {
                    YanChi(100);
                    traffic_control(0x0D + i, 0x02, 0x02);
                }
                System.out.println("识别为绿灯");
                break;
            case "黄灯":
                for (int J = 0; J < 10; J++) {
                    YanChi(100);
                    traffic_control(0x0D + i, 0x02, 0x03);
                }
                System.out.println("识别为黄灯");
                break;
        }
        YanChi(1000);
    }

    /**
     * 红绿灯识别模块 - 功能性单独测试
     *
     * @param detect 待检测的bitmap
     */
    public synchronized void trafficLight(Bitmap detect) {
        ColorProcess c = new ColorProcess(mContext);
        sendUIMassage(1, "对图片进行加工...");
        c.PictureProcessing(detect);
        sendUIMassage(1, "生成结果...");
        String color = TrafficLight_fix.Identify(c.getResult());
        sendUIMassage(1, "保存图片...");
        TrafficLight_fix.saveBitmap();
        sendUIMassage(2, c.getResult());
        sendUIMassage(1, "识别的颜色: " + color);
    }

    /* ================================================== */

    /**
     * 形状识别模块
     */
    //TODO 仍需要优化
    public synchronized void Shape_mod() {
        sendUIMassage(1, "----------形状识别开始----------");
        int totals;
        boolean fail = true;
        int fre = 1;
        shapeResult = 0;
        ShapeDetector task = new ShapeDetector();
        do {
            YanChi(6000);
            task.setTotals(0);
            sendUIMassage(2, stream);
            task.shapePicProcess(stream);
            totals = task.getTotals();
            if (totals <= 3 /*|| totals >= 6*/) {
                for (int J = 0; J < 3; J++) {
                    YanChi(100);
                    TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                }
                sendUIMassage(1, "当前识别次数: " + fre + "\n图形数量小于3,TFT_A翻页");
            } else {
                fail = false;
                shapeResult = Objects.requireNonNull(task.getShape().get(mainViewModel.getShape_color().getValue())).getCounts(mainViewModel.getShape_type().getValue());
            }
        } while (fail && fre++ < 5);
        sendUIMassage(1, "检测出的全部的图形数量: " + totals + "\n题意所需数据: " + shapeResult + "个" + mainViewModel.getShape_color().getValue() + mainViewModel.getShape_type().getValue());
        sendUIMassage(1, "----------形状识别完成----------");
    }

    public synchronized void Shape(Bitmap detect) {
        sendUIMassage(1, "----------形状识别开始----------");
        ShapeDetector task = new ShapeDetector();
        sendUIMassage(2, detect);
        task.shapePicProcess(detect);
        int totals = task.getTotals();
        shapeResult = Objects.requireNonNull(task.getShape().get(mainViewModel.getShape_color().getValue())).getCounts(mainViewModel.getShape_type().getValue());
        sendUIMassage(1, "检测出的全部的图形数量: " + totals + "\n题意所需数据: " + shapeResult + "个" + mainViewModel.getShape_color().getValue() + mainViewModel.getShape_type().getValue());
        sendUIMassage(1, "----------形状识别完成----------");
    }

    /* ================================================== */

    /**
     * 基于openCV基本库的二维码识别模块
     * 已弃用,已有更好的替代方案
     */
    @Deprecated
    private void openCVQR() {
        Bitmap Btmp;
        String qrStr = null;
        int i = 1;
        while ((qrStr == null || qrStr.isEmpty()) && i <= 10) {
            YanChi(2500);
            Btmp = QRBitmapCutter.bitmap2Gray(stream);
            QRCodeDetector qrCodeDetector = new QRCodeDetector();
            Mat mat = new Mat();
            Utils.bitmapToMat(Btmp, mat);
            qrStr = qrCodeDetector.detectAndDecode(mat);
            System.out.println("第" + i + "次识别二维码: \n");
            System.out.println(qrStr);
            Log.i("QRCode", qrStr);
            i++;
        }
        if (qrStr == null || qrStr.isEmpty()) qrStr = "A1B2C3D4E5";
        GetCode.parsing(qrStr);
    }

    /**
     * WeChat二维码扫描
     */
    public synchronized void WeChatQR_mod() {
        String qrStr = null;
        int fre = 1;
        sendUIMassage(1, "开始识别二维码!");
        do {
            YanChi(3500);
            Bitmap detect = QRBitmapCutter.QRCutter(stream);
            sendUIMassage(2, detect);
            try {
                qrStr = WeChatQRCodeDetector.detectAndDecode(detect).get(0);
                sendUIMassage(1, "第" + fre + "次识别二维码: ■■■" + qrStr + "■■■");
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                sendUIMassage(1, "识别错误!");
                e.printStackTrace();
            }
        } while ((qrStr == null || qrStr.isEmpty()) && fre++ <= 5);
        if (qrStr == null || qrStr.isEmpty()) qrStr = "A1B2C3D4E5";
        qrResult = GetCode.parsing(qrStr);
        sendUIMassage(1, "最终结果: ■■■" + qrResult + "■■■");
    }

    public synchronized void WeChatQR(Bitmap inputBitmap) {
        sendUIMassage(1, "开始识别二维码!");
        Bitmap detect = QRBitmapCutter.QRCutter(inputBitmap);
        sendUIMassage(2, detect);
        String qrStr = null;
        try {
            qrStr = WeChatQRCodeDetector.detectAndDecode(detect).get(0);
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            sendUIMassage(1, "识别错误!");
            e.printStackTrace();
        }
        if (qrStr == null || qrStr.isEmpty()) qrStr = "A1B2C3D4E5";
        qrResult = GetCode.parsing(qrStr);
        sendUIMassage(1, "最终结果: ■■■" + qrResult + "■■■");
    }

    /* ================================================== */

    /**
     * 车牌识别/OCR文字识别模块
     */
    public synchronized String DetectPlate(Bitmap inputBitmap) {
        sendUIMassage(2, inputBitmap);
        return TestInferOcrTask.getInstance().detector(inputBitmap);
    }

    /**
     * 过滤与补全
     *
     * @param str 车牌识别结果
     * @return 过滤后的车牌
     */
    private String completion(String str) {
        StringBuilder sb = new StringBuilder();
        for (char ch : str.toCharArray()) {
            //如果为数字或字母则添加进sb中
            if (Character.isDigit(ch) || Character.isUpperCase(ch) || Character.isLowerCase(ch))
                sb.append(ch);
        }
        //不满6个数则补全到6
        while (sb.toString().length() < 6) sb.append(0);
        return sb.toString().toUpperCase(Locale.ROOT);
    }

    /**
     * 判断是否为正常的车牌号
     *
     * @param s 车牌号
     * @param i 已经识别次数
     * @return -
     */
    private boolean all0(String s, int i) {
        if (i >= 5 || s.isEmpty()) return false;
        int total = 0;
        for (char c : s.toCharArray()) if (c == '0') total++;
        return total >= 4;
    }

    /**
     * <p>车牌识别模块 - 分支1</p>
     * <p>使用颜色识别 - 可能不稳定</p>
     * <p>针对于赛场使用干扰颜色车牌</p>
     * <p>建议使用3分支</p>
     */
    @Deprecated
    private void plate_mod_branch1() {
//        //重新识别车牌号的次数
//        int fre = 1;
//        //翻页次数
//        int flip = 1;
//        YanChi(1500);
//        do {
//            //做基本判断,输入图片主题色是否为蓝色
//            while (Antijamming.ColorTask(stream) && flip++ <= 8) {
//                //TFT_A
//                for (int J = 0; J < 3; J++) {
//                    YanChi(500);
//                    TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
//                }
//                System.out.println("TFT_A翻页成功");
//                YanChi(6000);
//            }
//            plate = DetectPlate(stream);
//            plate = completion(plate);
//            System.out.print("*****这里是车牌号*****" + plate + "\n");
//        } while (all0(plate, fre++));
//        //发送车牌给TFT
//        YanChi(2000);
////        for (int J = 0; J < 5; J++) {
////            YanChi(500);
////            TFT_LCD(0x0B, 0x20, plate.charAt(0), plate.charAt(1), plate.charAt(2));
////        }
////        System.out.println("第一次发送成功");
////        YanChi(1500);
////        for (int J = 0; J < 5; J++)
////            TFT_LCD(0x0B, 0x21, plate.charAt(3), plate.charAt(4), plate.charAt(5));
////        System.out.println("第二次发送成功");
//        YanChi(500);
    }

    /**
     * <p>车牌识别模块 - 分支2</p>
     * <p>使用车牌号判断 - 无法区分颜色,但遇号既出结果</p>
     * <p>针对于赛场不使用干扰颜色车牌</p>
     */
    private void plate_mod_branch2() {
        //重新识别车牌号的次数
        int fre = 1;
        YanChi(2000);
        do {
            //生成结果
            plate = DetectPlate(stream);
            //过滤与补全
            plate = completion(plate);
            System.out.print("*****这里是车牌号*****" + plate + "\n");
            //翻页,使用车牌判断
            if (all0(plate, fre)) {
                for (int J = 0; J < 3; J++) {
                    YanChi(500);
                    TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                }
                System.out.println("TFT_A翻页成功");
                Log.i("plate", plate);
                YanChi(6000);
            }
        } while (all0(plate, fre++));
        //发送车牌给TFT
        YanChi(2000);
//        for (int J = 0; J < 5; J++) {
//            YanChi(500);
//            TFT_LCD(0x0B, 0x20, plate.charAt(0), plate.charAt(1), plate.charAt(2));
//        }
//        System.out.println("第一次发送成功");
//        YanChi(1500);
//        for (int J = 0; J < 5; J++)
//            TFT_LCD(0x0B, 0x21, plate.charAt(3), plate.charAt(4), plate.charAt(5));
//        System.out.println("第二次发送成功");
        YanChi(500);

    }

    /**
     * <p>车牌识别模块 - 分支3</p>
     * 结合openCV库(仅)定位车牌,识别车牌种类和车牌号
     */
    private void plate_mod_branch3() {
        //重新识别车牌号的次数
        int fre = 1;
        YanChi(2000);
        plate = null;
        do {
            /* 车牌识别图片处理 */
            PlateDetector plateDetector = new PlateDetector();
            String plateType = plateDetector.plateOnlyDetector(stream);
            /* 翻页,使用车牌种类判断 */
            //plateType.equals("填入需要屏蔽的车牌颜色")
            //比如:蓝/绿/...
            if ((plateType == null || plateType.equals("绿")) && fre < 5) {
                for (int J = 0; J < 3; J++) {
                    YanChi(100);
                    TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                }
                System.out.println("检测车牌翻页成功");
            } else {
                if (plateDetector.getRectBitmap() == null) continue;
                //生成结果
                //TODO 车牌处理2,灰度化处理,将黑字替换成白字


                plate = DetectPlate(plateDetector.getRectBitmap());
                /* 保存图片 */
                TrafficLight.saveBitmap("裁剪后的车牌.jpg", plateDetector.getRectBitmap());
                plate = plateDetector.completion(plate);
//                System.out.println("车牌种类: " + plateType + "\n车牌号: " + plate);
                Log.i(TAG, "车牌种类: " + plateType + "\n车牌号: " + plate);
            }
            if (plate == null || plate.equals("D000D0")) {
                for (int J = 0; J < 3; J++) {
                    YanChi(100);
                    TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                }
                System.out.println("非法车牌-----TFT_A翻页成功");
            }
            YanChi(6000);
        } while ((plate == null || plate.equals("D000D0")) && fre++ < 5);
        //发送车牌给TFT
        YanChi(2000);
//        for (int J = 0; J < 5; J++) {
//            YanChi(500);
//            TFT_LCD(0x0B, 0x20, plate.charAt(0), plate.charAt(1), plate.charAt(2));
//        }
//        System.out.println("第一次发送成功");
//        YanChi(1500);
//        for (int J = 0; J < 5; J++)
//            TFT_LCD(0x0B, 0x21, plate.charAt(3), plate.charAt(4), plate.charAt(5));
//        System.out.println("第二次发送成功");
        YanChi(500);
    }

    /**
     * 车牌识别 - 颜色选择
     */
    public synchronized void plate_DetectByColor() {

    }

    /**
     * 车牌识别 - 车种识别框选
     */
    public synchronized void plate_DetectByVID() {

    }

    /* ================================================== */

    /*TODO
     * 解密
     * {}
     */

    /* ================================================== */

    /**
     * TODO 交通标志物识别
     */
    public void trafficSign_mod() {
        YanChi(3500);
        try {
            String result = "";
            Bitmap bitmap;
            //车牌识别后进行翻页
            for (int J = 0; J < 3; J++) {
                YanChi(500);
                TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
            }
            System.out.println("TFT_A翻页成功");
            YanChi(6000);
            for (int i = 1; i <= 6; i++) {
                Bitmap tmp = stream;
                bitmap = Bitmap.createBitmap(tmp,
                        (tmp.getWidth() / 100) * 25,
                        (tmp.getHeight() / 100) * 50,
                        (tmp.getWidth() / 100) * 35,
                        (tmp.getHeight() / 100) * 60);

                result = MainActivity.getYolov5_tflite_tsDetector().processImage(bitmap);
            }

            switch (result) {
                case "go_straight":
                    getTrafficFlag = 0x01;
                    break;
                case "turn_left":
                    getTrafficFlag = 0x02;
                    break;
                case "turn_around":
                    getTrafficFlag = 0x04;
                    break;
                case "no_straight":
                    getTrafficFlag = 0x05;
                    break;
                case "no_turn":
                    getTrafficFlag = 0x06;
                    break;
                case "turn_right":
                default:
                    getTrafficFlag = 0x03;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /* 以下全安卓控制主车读卡循迹未必完全有效,仅作为备用手段使用 */

    /**
     * 读卡-----沙盘横向长线-----半路程
     */
    private void ReadCard_longLine() {
        YanChi(1000);
        line(90);
        YanChi(800);
        stop();
        //读卡
        YanChi(1500);
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 读卡-----沙盘竖向短线-----半路程
     */
    private void ReadCard_shortLine() {
        YanChi(1000);
        line(90);
        YanChi(500);
        stop();
        //读卡
        YanChi(1500);
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 读卡-----沙盘十字路口-----短线行驶
     */
    private void ReadCard_short2crossroads() {
        YanChi(1000);
        line(90);
        YanChi(1050);
        stop();
        YanChi(1000);
        send((short) 0xB2, (short) 0x00, (short) 0x00, (short) 0x00);
        //读卡
        YanChi(1500);
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 读卡-----沙盘十字路口-----长线行驶
     */
    private void ReadCard_long2crossroads() {
        YanChi(1000);
        line(90);
        YanChi(1300);
        stop();
        YanChi(500);
        send((short) 0xB2, (short) 0x00, (short) 0x00, (short) 0x00);
        //读卡
        YanChi(1500);
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
    }
}
