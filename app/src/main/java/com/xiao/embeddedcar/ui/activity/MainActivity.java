package com.xiao.embeddedcar.ui.activity;

import static com.xiao.embeddedcar.MainApplication.cachedThreadPool;

import android.annotation.SuppressLint;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.xiao.baiduocr.TestInferOcrTask;
import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.R;
import com.xiao.embeddedcar.data.Entity.LoginInfo;
import com.xiao.embeddedcar.data.ViewModel.MainViewModel;
import com.xiao.embeddedcar.databinding.ActivityMainBinding;
import com.xiao.embeddedcar.utils.CameraUtil.CameraConnectUtil;
import com.xiao.embeddedcar.utils.Network.USBToSerialUtil;
import com.xiao.embeddedcar.utils.PublicMethods.ToastUtil;
import com.xiao.embeddedcar.utils.TrafficSigns.YoloV5_tfLite_TSDetector;
import com.xiao.embeddedcar.utils.VID.YoloV5_tfLite_VIDDetector;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    //xml绑定类
    ActivityMainBinding binding;
    //应用栏配置
    private AppBarConfiguration mAppBarConfiguration;
    //导航控制对象
    private NavController navController;
    //MainActivity的ViewModel
    private MainViewModel mainViewModel;
    //全局登录信息
    private static LoginInfo loginInfo = new LoginInfo();
    //主从状态
    public static boolean chief_status_flag = true;
    //Yolo_tfLite检测模型对象
    private static final YoloV5_tfLite_TSDetector TS_Detector = new YoloV5_tfLite_TSDetector();
    //Yolo_tfLite检测模型对象
    private static final YoloV5_tfLite_VIDDetector VID_Detector = new YoloV5_tfLite_VIDDetector();
    //显示初始化对象状态的消息
    private final StringBuilder initMsg = new StringBuilder();

    /**
     * 获取交通标志物检测对象
     *
     * @return YoloV5_tfLite_TSDetector
     */
    public static YoloV5_tfLite_TSDetector getTS_Detector() {
        return TS_Detector;
    }

    /**
     * 获取车型识别检测对象
     *
     * @return YoloV5_tfLite_VIDDetector
     */
    public static YoloV5_tfLite_VIDDetector getVID_Detector() {
        return VID_Detector;
    }

    public static LoginInfo getLoginInfo() {
        return loginInfo;
    }

    public static void setLoginInfo(LoginInfo loginInfo) {
        MainActivity.loginInfo = loginInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onWindowFocusChanged(true);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        /* 绑定xml文件 */
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        /* 设置内容视图为绑定类的根视图 */
        setContentView(binding.getRoot());
        ShowWiFiSSID();
        /* 设置顶部工具栏 */
        setSupportActionBar(binding.contentStruct.toolbar);
        /* 设置抽屉布局 */
        DrawerLayout drawer = binding.drawerLayout;
        /* 设置导航视图 */
        NavigationView navigationView = binding.navView;
        /* Passing each menu ID as a set of Ids because each menu should be considered as top level destinations. */
        /* 将每个菜单ID作为一组ID传递，因为每个菜单都应被视为顶级目的地。 */
        /* 以配置的方式把导航栏配置到APP中,并绑定导航栏中的项对应的Fragment页面，实现联动 */
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_connect, R.id.nav_control, R.id.nav_other,
                R.id.nav_module, R.id.nav_analyse, R.id.nav_config, R.id.nav_test).setOpenableLayout(drawer).build();
        /* https://zhuanlan.zhihu.com/p/338437260 */
        /* NavController:管理应用导航的对象，实现Fragment之间的跳转等操作 */
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        /* 将AppBar与NavController绑定 */
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        /* 初始化单例对象 */
        initSingleton();
        /* 初始化library文件 */
        initLibrary();
        /* 设置观察者 */
        observerDataStateUpdateAction();
    }

    /**
     * activity创建时创建菜单Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //菜单填充:这将在动作栏中添加项目(如果存在)
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    /**
     * 菜单项监听
     */
    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.car_status:
                if (item.getTitle().equals("接收实训平台状态")) {
                    mainViewModel.getChief_state_flag().setValue(true);
                    item.setTitle(getResources().getText(R.string.follow_status));
                    ConnectTransport.getInstance().stateChange(2);
                    ToastUtil.getInstance().ShowToast("当前接收主车信息");
                } else if (item.getTitle().equals("接收移动机器人状态")) {
                    mainViewModel.getChief_state_flag().setValue(false);
                    item.setTitle(getResources().getText(R.string.main_status));
                    ConnectTransport.getInstance().stateChange(1);
                    ToastUtil.getInstance().ShowToast("当前接收从车信息");
                }
                break;
            case R.id.car_control:
                if (item.getTitle().equals("控制实训平台")) {
                    item.setTitle(getResources().getText(R.string.follow_control));
                    ConnectTransport.getInstance().TYPE = 0xAA;
                    ToastUtil.getInstance().ShowToast("当前为主车");
                } else if (item.getTitle().equals("控制移动机器人")) {
                    item.setTitle(getResources().getText(R.string.main_control));
                    ConnectTransport.getInstance().TYPE = 0x02;
                    ToastUtil.getInstance().ShowToast("当前为从车");
                }
                break;
            case R.id.clear_coded_disc:
                ConnectTransport.getInstance().clear();
                break;
            case R.id.set_ConnectTransport_mark:
                ConnectTransport.setMark(1);
                ConnectTransport.setCarGoto(1);
                break;
            case R.id.Android_Control:
                cachedThreadPool.execute(ConnectTransport.getInstance()::Q4);
                break;
            case R.id.half_Control:
                //对话框构造器
                new AlertDialog.Builder(this)
                        // 设置Title的内容
                        .setIcon(R.mipmap.rc_logo)
                        .setTitle("温馨提示")
                        // 设置Content(内容)来显示一个信息
                        .setMessage("请确认是否开始自动驾驶！")
                        // 设置一个PositiveButton(确认按钮)
                        .setPositiveButton("开始", (dialog, which) -> {
                            new Thread(() -> ConnectTransport.getInstance().half_Android()).start();
                            ToastUtil.getInstance().ShowToast("开始自动驾驶，请检查车辆周围环境！");
                        })
                        // 设置一个NegativeButton
                        .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    /**
     * Override the default implementation when the user presses the back key.
     */
    @Override
    public void onBackPressed() {
        // Move the task containing the MainActivity to the back of the activity stack, instead of
        // destroying it. Therefore, MainActivity will be shown when the user switches back to the app.
        moveTaskToBack(true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) setSystemUiFullScreen();
    }

    /**
     * Activity销毁
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectTransport.getInstance().destroy();
        CameraConnectUtil.getInstance().destroy();
        USBToSerialUtil.getInstance().onDestroy();
        try {
            TestInferOcrTask.getInstance().destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 观察者数据状态更新活动
     */
    private void observerDataStateUpdateAction() {
        mainViewModel.getChief_state_flag().observe(this, b -> chief_status_flag = b);
        /* 消息回传解析线程启动 */
        cachedThreadPool.execute(() -> ConnectTransport.getInstance().setReMsgHandler(mainViewModel.getGetModuleInfoHandle()));
    }

    /**
     * 展示 WiFi SSID
     */
    private void ShowWiFiSSID() {
        String WiFiSSID = getConnectWifiSSID();
        String finalSSID = WiFiSSID.equals("<unknown ssid>") ?
                getConnectWifiSsidTwo() : WiFiSSID;
        initMsg.append(finalSSID.equals("<unknown ssid>") ?
                "当前未连接到WiFi,请接入设备WiFi后再试!" :
                "当前连接的WiFi: " + finalSSID).append("\n");
    }

    /**
     * 获取WiFi连接SSID(服务集合标识符) - 方法1
     *
     * @return WiFiSSID
     */
    private String getConnectWifiSSID() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID", wifiInfo.getSSID());
        return wifiInfo.getSSID();
    }

    /**
     * 获取WiFi连接SSID(服务集合标识符) - 方法2
     *
     * @return WiFiSSID
     */
    public String getConnectWifiSsidTwo() {
        WifiManager wifiManager = ((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE));
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String SSID = wifiInfo.getSSID();
        int networkId = wifiInfo.getNetworkId();
        try {
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                if (wifiConfiguration.networkId == networkId) SSID = wifiConfiguration.SSID;
                break;
            }
            return SSID.replace("\"", "");
        } catch (SecurityException e) {
            return e.getMessage();
        }
    }

    /**
     * 单例对象初始化
     */
    private void initSingleton() {
        /* 初始化摄像头连接工具类 */
        CameraConnectUtil.getInstance().init(this, mainViewModel);
    }

    /**
     * 库文件初始化
     */
    private void initLibrary() {
        try {
            /* 百度OCR模型初始化 */
            initMsg.append(TestInferOcrTask.getInstance().init(this) ? "车牌识别模型初始化成功\n" : "车牌识别模型初始化失败\n");
            /* YoloV5s-tfLite模型初始化 */
            /* TODO 在此更改模型加载参数 */
            initMsg.append(TS_Detector.LoadModel("CPU", 4, this.getAssets()) ? "交通标志物识别模型创建成功\n" : "交通标志物识别模型创建失败\n");
            initMsg.append(VID_Detector.LoadModel("CPU", 4, this.getAssets()) ? "车型识别模型创建成功" : "车型识别模型创建失败");
        } catch (Exception e) {
            initMsg.append("Exception!有库文件初始化错误!");
        }
        mainViewModel.getModuleInfoTV().setValue(initMsg.toString());
    }

    protected void setSystemUiFullScreen() {
        Window window = getWindow();
        if (window != null) window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
