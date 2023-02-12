package com.xiao.embeddedcar.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.xiao.baiduocr.TestInferOcrTask;
import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.DataProcessingModule.CrashHandler;
import com.xiao.embeddedcar.Entity.LoginInfo;
import com.xiao.embeddedcar.R;
import com.xiao.embeddedcar.Utils.CameraUtil.CameraConnectUtil;
import com.xiao.embeddedcar.Utils.NetworkAndUIUtil.ToastUtil;
import com.xiao.embeddedcar.Utils.NetworkAndUIUtil.USBToSerialUtil;
import com.xiao.embeddedcar.Utils.NetworkAndUIUtil.WiFiStateUtil;
import com.xiao.embeddedcar.Utils.PublicMethods.BitmapProcess;
import com.xiao.embeddedcar.Utils.QRcode.WeChatQRCodeDetector;
import com.xiao.embeddedcar.Utils.TrafficSigns.YoloV5_tfLite_TSDetector;
import com.xiao.embeddedcar.Utils.VID.YoloV5_tfLite_VIDDetector;
import com.xiao.embeddedcar.ViewModel.ConnectViewModel;
import com.xiao.embeddedcar.ViewModel.HomeViewModel;
import com.xiao.embeddedcar.ViewModel.MainViewModel;
import com.xiao.embeddedcar.ViewModel.ModuleViewModel;
import com.xiao.embeddedcar.databinding.ActivityMainBinding;

import org.opencv.android.OpenCVLoader;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    //应用栏配置
    private AppBarConfiguration mAppBarConfiguration;
    //导航控制对象
    private NavController navController;
    //MainActivity的ViewModel
    private MainViewModel mainViewModel;
    //基于ActivityContext的ViewModel
    private HomeViewModel homeViewModel;
    private ConnectViewModel connectViewModel;
    private ModuleViewModel moduleViewModel;
    //权限申请状态
    private boolean allPermissionsGranted;
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
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        connectViewModel = new ViewModelProvider(this).get(ConnectViewModel.class);
        moduleViewModel = new ViewModelProvider(this).get(ModuleViewModel.class);
        /* 绑定xml文件 */
        //xml绑定类
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        /* 设置内容视图为绑定类的根视图 */
        setContentView(binding.getRoot());
        /* 权限检查 */
        Request();
        /* 设置顶部工具栏 */
        setSupportActionBar(binding.appBar.toolbar);
        /* 设置抽屉布局 */
        DrawerLayout drawer = binding.drawerLayout;
        /* 设置导航视图 */
        NavigationView navigationView = binding.navView;
        /* Passing each menu ID as a set of Ids because each menu should be considered as top level destinations. */
        /* 将每个菜单ID作为一组ID传递，因为每个菜单都应被视为顶级目的地。 */
        /* 以配置的方式把导航栏配置到APP中,并绑定导航栏中的项对应的Fragment页面，实现联动 */
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_connect, R.id.nav_control, R.id.nav_other,
                R.id.nav_module, R.id.nav_analyse, R.id.nav_config).setOpenableLayout(drawer).build();
        /* https://zhuanlan.zhihu.com/p/338437260 */
        /* NavController:管理应用导航的对象，实现Fragment之间的跳转等操作 */
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        /* 将AppBar与NavController绑定 */
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        /* 初始化单例对象 */
        initSingleton();
        /* 初始化library文件 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || (Build.VERSION.SDK_INT >= 30 && Environment.isExternalStorageManager())))
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
                moduleViewModel.module(0xB4);
                break;
            case R.id.half_Control:
                //对话框构造器
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // 设置Title的内容
                builder.setIcon(R.mipmap.rc_logo);
                builder.setTitle("温馨提示");
                // 设置Content(内容)来显示一个信息
                builder.setMessage("请确认是否开始自动驾驶！");
                // 设置一个PositiveButton(确认按钮)
                builder.setPositiveButton("开始", (dialog, which) -> {
                    new Thread(() -> ConnectTransport.getInstance().half_Android()).start();
                    ToastUtil.getInstance().ShowToast("开始自动驾驶，请检查车辆周围环境！");
                });
                // 设置一个NegativeButton
                builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
                builder.show();
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
//        WindowInsetsControllerCompat window = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
//        if (!hasFocus)
//            window.show(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.captionBar());
//        else
//            window.hide(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.captionBar());
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
    }

    /**
     * 观察者数据状态更新活动
     */
    private void observerDataStateUpdateAction() {
        mainViewModel.getChief_state_flag().observe(this, b -> chief_status_flag = b);
    }

    /**
     * 动态权限申请回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                String WiFiSSID = getConnectWifiSSID();
                String finalSSID = WiFiSSID.equals("<unknown ssid>") ? getConnectWifiSsidTwo() : WiFiSSID;
                initMsg.append(finalSSID.equals("<unknown ssid>") ? "当前未连接到WiFi,请接入设备WiFi后再试!" : "当前连接的WiFi: " + finalSSID);
                requestAllFilesAccess();
            } else if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PermissionChecker.PERMISSION_GRANTED)
                initMsg.append("WiFi检查异常,请检查网络权限!");
            else initMsg.append("未正常授予所有权限,库文件未完全初始化!");
            initMsg.append("\n");
            initLibrary();
        }
    }

    /**
     * 动态权限申请
     */
    private void Request() {
        String[] permissions = {
                //网络权限
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                //定位权限 - 粗/细
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                //外部存储读写权限
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
        allPermissionsGranted = true;
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                allPermissionsGranted = false;
                break;
            }
        }
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, 1);
            return;
        }
        String WiFiSSID = getConnectWifiSSID();
        String finalSSID = WiFiSSID.equals("<unknown ssid>") ? getConnectWifiSsidTwo() : WiFiSSID;
        initMsg.append(finalSSID.equals("<unknown ssid>") ? "当前未连接到WiFi,请接入设备WiFi后再试!" : "当前连接的WiFi: " + finalSSID);
        initMsg.append("\n");
        requestAllFilesAccess();
    }

    /**
     * Android 11 跳转到设置获取SD卡根目录写入权限
     * 仅实现了Androidx特性时,以下语句使用才不会报错
     */
    private void requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            allPermissionsGranted = false;
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage("需授权访问外部存储用于拷贝库资源");
            alertBuilder.setCancelable(false);
            alertBuilder.setPositiveButton("去设置", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            alertBuilder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
            alertBuilder.show();
        }
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
        /* 初始化全局异常捕获对象 */
        CrashHandler.getInstance().init(this);
        initMsg.append("初始化全局异常捕获完毕\n");
        /* Toast工具类(底部弹出提示框)初始化 */
        ToastUtil.getInstance().init(this);
        /* 初始化Bitmap处理对象类 */
        BitmapProcess.getInstance().init(this);
        /* 初始化摄像头连接工具类 */
        CameraConnectUtil.getInstance().init(this, connectViewModel);
        /* 初始化WiFi状态工具类 */
        WiFiStateUtil.getInstance().init(this);
    }

    /**
     * 库文件初始化
     */
    private void initLibrary() {
        try {
            /* openCV库初始化 */
            initMsg.append(OpenCVLoader.initDebug() ? "OpenCV库加载成功\n" : "OpenCV库加载失败\n");
            /* WeChat二维码识别对象初始化(一定要在openCV库初始化完成之后) */
            WeChatQRCodeDetector.init(this);
            /* 百度OCR模型初始化 */
            initMsg.append(TestInferOcrTask.getInstance().init(this) ? "车牌识别模型初始化成功\n" : "车牌识别模型初始化失败\n");
            /* YoloV5s-tfLite模型初始化 */
            /* TODO 在此更改模型加载参数 */
            initMsg.append(TS_Detector.LoadModel("CPU", 4, this.getAssets()) ? "交通标志物识别模型创建成功\n" : "交通标志物识别模型创建失败\n");
            initMsg.append(VID_Detector.LoadModel("CPU", 4, this.getAssets()) ? "车种识别模型创建成功" : "车种识别模型创建失败");
        } catch (Exception e) {
            e.printStackTrace();
        }
        /* 实例化连接类(需要库文件先初始化完毕) */
        ConnectTransport.getInstance().init(this, mainViewModel);
        /* 使用非Socket通讯将初始化SerialUtil类 */
        USBToSerialUtil.getInstance().init(this, homeViewModel, connectViewModel);
        homeViewModel.getDebugArea().setValue(initMsg.toString());
    }
}
