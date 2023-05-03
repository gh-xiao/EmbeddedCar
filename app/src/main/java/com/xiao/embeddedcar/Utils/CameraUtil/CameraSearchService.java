package com.xiao.embeddedcar.Utils.CameraUtil;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;


public class CameraSearchService extends IntentService {

    public CameraSearchService() {
        super("CameraSearchService");
    }

    //摄像头IP
    private String IP = null;

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent mIntent = new Intent(CameraConnectUtil.getaS());
//        for (int i = 0; i < 3 && IP == null; i++) {
//            searchCameraUtil = new SearchCameraUtil();
////            IP = searchCameraUtil !=null ? searchCameraUtil.send(): null;
//            IP = searchCameraUtil.send();
//            //线程休眠
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        Timer getServerIPTimer = new Timer();
        getServerIPTimer.schedule(new TimerTask() {
            private int reGet = 0;

            @Override
            public void run() {
                this.reGet++;
                if (reGet >= 3) {
                    this.cancel();
                    this.reGet = 0;
                }
                IP = new SearchCameraUtil().send();
            }
        }, 0, 1000);

        if (IP == null) mIntent.putExtra("loginState", "Fail");
        mIntent.putExtra("IP", IP + ":81");
        mIntent.putExtra("pureip", IP);
        //广播发送器 - 发送给CameraSearchService的广播
        sendBroadcast(mIntent);
    }

    /**
     * 摄像头搜索工具类
     */
    static class SearchCameraUtil {
        private final static String TAG = "UDPClient";
        //服务端IP地址
        private String IP = "";
        //端口
        private final static int PORT = 3565;
        //服务器端口
        private final static int SERVER_PORT = 8600;
        //客户端往服务器发送的校验字节
        private final byte[] mByte = new byte[]{68, 72, 1, 1};
        //UDPSocket对象
        private DatagramSocket dSocket = null;
        //回传数据存放数组
        private final byte[] msg = new byte[1024];
        private boolean isConn = false;

        /**
         * 通讯检测
         *
         * @return 服务器IP地址
         */
        public String send() {
            InetAddress local;

            try {
                local = InetAddress.getByName("255.255.255.255");
                Log.e(TAG, "已找到服务器,连接中...");
            } catch (UnknownHostException var1) {
                Log.e(TAG, "未找到服务器.");
                var1.printStackTrace();
                return null;
            }

            try {
                /* 重置UDP_Socket */
                if (dSocket != null) dSocket.close();
                dSocket = null;
                /* 绑定一个新的UDP_Socket */
                dSocket = new DatagramSocket(null);
                /* 设置重用Socket所绑定的本地地址 */
                dSocket.setReuseAddress(true);
                /* 绑定3565端口 */
                dSocket.bind(new InetSocketAddress(PORT));
                Log.e(TAG, "正在连接服务器...");
            } catch (SocketException var2) {
                var2.printStackTrace();
                Log.e(TAG, "服务器连接失败.");
                return null;
            }
            /* 设置数据报包发送 */
            DatagramPacket sendPacket = new DatagramPacket(this.mByte, 4, local, SERVER_PORT);
            /* 设置数据报包接收 */
            DatagramPacket recPacket = new DatagramPacket(this.msg, this.msg.length);

            try {
                dSocket.send(sendPacket);
                /* 设置定时器 */
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (isConn) {
                            dSocket.close();
                            this.cancel();
                        }
                        isConn = true;
                    }
                }, 0, 1000);
                /* 接收数据 */
                dSocket.receive(recPacket);
                /* 关闭定时器 */
                timer.cancel();
                /* 获取回传文本 */
                String text = new String(this.msg, 0, recPacket.getLength());
                /* 解析IP地址 */
                if (text.startsWith("DH")) this.getIP(text);
                Log.e("IP值", this.IP);
                /* 关闭UDP_Socket */
                dSocket.close();
                Log.e(TAG, "消息发送成功!");
            } catch (SocketException var3) {
                dSocket.close();
                Log.e(TAG, "消息接收失败.");
                return null;
            } catch (IOException var4) {
                dSocket.close();
                Log.e(TAG, "IOException: 消息发送失败.");
                return null;
            }
            return this.IP;
        }

        private void getIP(String text) {
            byte[] ipByte = text.getBytes(StandardCharsets.UTF_8);
            for (int i = 4; i < 22 && ipByte[i] != 0; ++i) {
                if (ipByte[i] == 46) this.IP += ".";
                else this.IP += (ipByte[i] - 48);
            }
        }
    }
}
