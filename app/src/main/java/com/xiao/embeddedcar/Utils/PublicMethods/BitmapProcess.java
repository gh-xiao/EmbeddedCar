package com.xiao.embeddedcar.Utils.PublicMethods;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapProcess {
    // 指定我们想要存储文件的地址
    public static final String TargetPath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Tess/";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.CHINA);
    @SuppressLint("StaticFieldLeak")
    private static BitmapProcess mInstance;
    private Context mContext;

    private BitmapProcess() {
    }

    public static synchronized BitmapProcess getInstance() {
        if (mInstance == null) {
            mInstance = new BitmapProcess();
        }
        return mInstance;
    }

    public void init(Context context) {
        this.mContext = context.getApplicationContext();
    }

    /**
     * 全局使用的图片保存方法
     *
     * @param name 图片名
     * @param mat  需要保存的mat
     * @return 是否保存成功
     */
    @SuppressWarnings("UnusedReturnValue")
    public static String saveBitmap(String name, Mat mat) {
        if (mat == null) return "错误,没有图片!";
        Bitmap bm = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bm);
        return saveBitmap(name, bm);
    }

    /**
     * 全局使用的图片保存方法
     *
     * @param name 图片名
     * @param bm   需要保存的Bitmap
     * @return 是否保存成功
     */
    public static String saveBitmap(String name, Bitmap bm) {
        if (bm == null) return "错误,没有图片!";
        return Build.VERSION.SDK_INT < 29 ? saveImageOld(name, bm) : mInstance.saveImageNew(name, bm);
    }

    /**
     * 旧版本Android保存图片方法
     *
     * @param name 图片名
     * @param bm   等待保存的Bitmap
     * @return 是否保存成功
     */
    private static String saveImageOld(String name, Bitmap bm) {
        Log.d("Save Bitmap", "Ready to save picture");
        StringBuilder append = new StringBuilder().append("Save Path = ");
        String str = TargetPath;
        Log.d("Save Bitmap", append.append(str).toString());
        if (fileIsExist()) {
            try {
                FileOutputStream saveImgOut = new FileOutputStream(new File(str, name + "-" + format.format(new Date()) + ".jpg"));
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                saveImgOut.flush();
                Log.d("Save Bitmap", "The picture is save to your phone!");
                return "保存完毕!";
            } catch (IOException ex) {
                ex.printStackTrace();
                return "IOException!";
            }
        } else {
            Log.d("Save Bitmap", "TargetPath isn't exist");
            return "TargetPath isn't exist!";
        }
    }

    static boolean fileIsExist() {
        File file = new File(TargetPath);
        return file.exists() || file.mkdirs();
    }

    /**
     * 高版本Android保存图片方法
     *
     * @param name 图片名
     * @param bm   等待保存的Bitmap
     * @return 是否保存成功
     */
    private String saveImageNew(String name, Bitmap bm) {
        Log.d("Save Bitmap", "Ready to save picture");
        ContentValues contentValues = new ContentValues();
        contentValues.put("_display_name", name + format.format(new Date()));
        contentValues.put("description", name);
        contentValues.put("mime_type", "image/jpeg");
        contentValues.put("relative_path", "DCIM/Tess");
        try (OutputStream outputStream = mContext.getContentResolver()
                .openOutputStream(mContext.getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues))) {
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            return "保存完毕!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception!";
        }
    }

    /**
     * 通过URL获取图片
     *
     * @param uri -
     * @return Bitmap
     */
    public Bitmap showImage(Uri uri) {
        try (ParcelFileDescriptor parcelFileDescriptor = mContext.getContentResolver().openFileDescriptor(uri, "r")) {
            return BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过图片名获取Tess文件夹对应的图片
     *
     * @param imageName 图片名
     * @return Bitmap
     */
    public static Bitmap getImages(String imageName) {
        String path = TargetPath + imageName;
        return new File(path).exists() ? BitmapFactory.decodeFile(path) : null;
    }

    /**
     * 通过绝对路径获取Bitmap
     *
     * @param realPath 绝对路径
     * @return Bitmap
     */
    public static Bitmap getRealPathImages(String realPath) {
        return new File(realPath).exists() ? BitmapFactory.decodeFile(realPath) : null;
    }
}