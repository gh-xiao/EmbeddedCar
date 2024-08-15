package com.xiao.embeddedcar.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.xiao.embeddedcar.databinding.ActivitySplashBinding
import com.xiao.embeddedcar.utils.QRcode.WeChatQRCodeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import java.util.Arrays

// 权限申请操作码
private const val REQUEST_INIT_PERMISSION = 1001

@SuppressLint("CustomSplashScreen")
@RequiresApi(Build.VERSION_CODES.N)
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val launcher = registerForActivityResult(StartActivityForResult()) { if (it.resultCode == RESULT_OK) request() }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.retryBtn.setOnClickListener { lifecycleScope.launch(Dispatchers.Main.immediate) { gotoMainActivity() } }
        lifecycleScope.launch(Dispatchers.Main.immediate) { gotoMainActivity() }
    }

    /**
     * 权限申请回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_INIT_PERMISSION) {
            /* 如果还存在未授予的权限 */
            if (Arrays.stream(grantResults).anyMatch { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "权限拒绝接受!", Toast.LENGTH_SHORT).show()
                binding.tvInfo.text = "权限检查未通过!"
                binding.retryBtn.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun gotoMainActivity() {
        binding.retryBtn.visibility = View.INVISIBLE
        binding.tvInfo.text = "权限检查中..."
        if (requestAllFilesAccess() || request()) {
            binding.retryBtn.visibility = View.VISIBLE
            binding.tvInfo.text = "请申请权限后重试..."
            return
        }
        binding.tvInfo.text = "初始化库文件..."
        runCatching {
            /* openCV库初始化 */
            binding.tvInfo.text = if (OpenCVLoader.initDebug()) "OpenCV库加载成功!" else "OpenCV库加载失败!"
            delay(500)
            /* WeChat二维码识别对象初始化(一定要在openCV库初始化完成之后) */
            WeChatQRCodeDetector.init(this)
            binding.tvInfo.text = "库文件初始化完毕!"
        }.onFailure { binding.tvInfo.text = "有库文件初始化错误!" }
        delay(500)
        binding.tvInfo.text = "准备完毕!"
        startActivity(Intent(this, MainActivity::class.java))
    }

    /**
     * 动态权限申请
     */
    private fun request(): Boolean {
        /* 待申请的权限 */
        val permissions = mutableListOf(
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
        )
        /* 移除已经允许的权限 */
        permissions.removeIf { PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, it) }
        /* 申请权限 */
        return if (permissions.size > 0) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_INIT_PERMISSION)
            true
        } else false
    }


    /**
     * Android 11 跳转到设置获取SD卡根目录写入权限
     */
    private fun requestAllFilesAccess(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            AlertDialog.Builder(this)
                .setMessage("需授权访问外部存储用于拷贝库资源")
                .setCancelable(false)
                .setPositiveButton("去设置") { _, _ ->
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    launcher.launch(intent)
                }
                .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                .show()
            true
        } else false
}
