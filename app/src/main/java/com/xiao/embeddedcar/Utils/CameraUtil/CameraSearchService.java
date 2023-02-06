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
        SearchCameraUtil searchCameraUtil;
        Intent mintent = new Intent(CameraConnectUtil.getaS());
        for (int i = 0; i < 3 && IP == null; i++) {
            searchCameraUtil = new SearchCameraUtil();
//            IP = searchCameraUtil !=null ? searchCameraUtil.send(): null;
            IP = searchCameraUtil.send();
            //线程休眠
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (IP == null) mintent.putExtra("loginState", "Fail");
        mintent.putExtra("IP", IP + ":81");
        mintent.putExtra("pureip", IP);
        //广播发送器 - 发送给CameraSearchService的广播
        sendBroadcast(mintent);
    }

    static class SearchCameraUtil {
        private String IP = "";
        private final String TAG = "UDPClient";
        //端口
        private final int PORT = 3565;
        //服务器端口
        private final int SERVER_PORT = 8600;
        private final byte[] mByte = new byte[]{68, 72, 1, 1};
        private DatagramSocket dSocket = null;
        private final byte[] msg = new byte[1024];
        boolean isConn = false;

        public String send() {
            InetAddress local;

            try {
                local = InetAddress.getByName("255.255.255.255");
                Log.e(this.TAG, "已找到服务器,连接中...");
            } catch (UnknownHostException var1) {
                Log.e(this.TAG, "未找到服务器.");
                var1.printStackTrace();
                return null;
            }

            try {
                if (this.dSocket != null) this.dSocket.close();
                this.dSocket = null;
                // 第一次连接没有报错，第二次开始报这个错误。字面意思看出是由于端口被占用，未释放导致。
                // 虽然程序貌似已经退出，个人猜测是由于系统还没有及时释放导致的。
//				this.dSocket = new DatagramSocket(3565);
                dSocket = new DatagramSocket(null);
                dSocket.setReuseAddress(true);
                dSocket.bind(new InetSocketAddress(PORT));
                Log.e(this.TAG, "正在连接服务器...");
            } catch (SocketException var2) {
                var2.printStackTrace();
                Log.e(this.TAG, "服务器连接失败.");
                return null;
            }

            DatagramPacket sendPacket = new DatagramPacket(this.mByte, 4, local, SERVER_PORT);
            DatagramPacket recPacket = new DatagramPacket(this.msg, this.msg.length);

            try {
                this.dSocket.send(sendPacket);

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

                this.dSocket.receive(recPacket);


                timer.cancel();
                String text = new String(this.msg, 0, recPacket.getLength());
                if (text.substring(0, 2).equals("DH")) this.getIP(text);

                Log.e("IP值", this.IP);
                this.dSocket.close();
                Log.e(this.TAG, "消息发送成功!");
            } catch (SocketException var3) {
                Log.e(this.TAG, "消息接收失败.");
                return null;
            } catch (IOException var4) {
                Log.e(this.TAG, "IOException: 消息发送失败.");
                return null;
            }
            return this.IP;
        }

        private void getIP(String text) {
            byte[] ipbyte = text.getBytes(StandardCharsets.UTF_8);
            for (int i = 4; i < 22 && ipbyte[i] != 0; ++i) {
                if (ipbyte[i] == 46) this.IP += ".";
                else this.IP += (ipbyte[i] - 48);
            }
        }
    }
}
