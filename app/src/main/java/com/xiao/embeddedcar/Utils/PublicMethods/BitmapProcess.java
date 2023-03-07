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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapProcess {
    @SuppressLint("StaticFieldLeak")
    private static BitmapProcess mInstance;
    private Context mContext;
    // 指定我们想要存储文件的地址
    public static final String TargetPath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Tess/";
    // 获取时间
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.CHINA);

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
     */
    public static void saveBitmap(String name, Mat mat) {
        if (mat == null) return;
        Bitmap bm = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bm);
        saveBitmap(name, bm);
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
        ContentValues contentValues = new ContentValues();
        contentValues.put("_display_name", name + format.format(new Date()));
        contentValues.put("description", name);
        contentValues.put("mime_type", "image/jpeg");
        contentValues.put("relative_path", "DCIM/Tess");
        try (OutputStream outputStream = mContext.getContentResolver()
                .openOutputStream(mContext.getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues))) {
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Log.d("Save Bitmap", "Save success!");
            return "保存完毕!";
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Save Bitmap", "Save fail!");
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

    /**
     * 灰度化图像
     *
     * @param inputBitmap 需要灰度化图像的Bitmap
     * @return 灰度化后的Bitmap
     */
    public static Bitmap GrayscaleImage(Bitmap inputBitmap) {
        if (inputBitmap == null) return null;
        Mat mat = new Mat();
        Utils.bitmapToMat(inputBitmap, mat);
        return GrayscaleImage(mat);
    }

    /**
     * 灰度化图像
     *
     * @param colorImage 需要灰度化图像的Bitmap
     * @return 灰度化后的Bitmap
     */
    public static Bitmap GrayscaleImage(Mat colorImage) {
        if (colorImage == null) return null;
        // 创建一个空的灰度图像
        Mat grayscaleImage = new Mat();
        // 调用cvtColor函数，将彩色图像转换为灰度图像
        Imgproc.cvtColor(colorImage, grayscaleImage, Imgproc.COLOR_RGB2GRAY);
        Bitmap result = Bitmap.createBitmap(colorImage.width(), colorImage.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(grayscaleImage, result);
        return result;
    }

    /**
     * 灰度化图像并保存
     */
    private void GrayscaleImage() {
        // 定义源文件夹和目标文件夹的路径
        String sourcePath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/srcImg/";
        String targetPath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/dstImg/";
        // 创建File对象，表示源文件夹和目标文件夹
        File sourceFolder = new File(sourcePath);
        File targetFolder = new File(targetPath);
        // 检查源文件夹是否存在，如果不存在，打印错误信息并退出
        if (!sourceFolder.exists()) {
            System.out.println("Source folder does not exist.");
            return;
        }
        // 检查目标文件夹是否存在，如果不存在，就创建一个
        if (!targetFolder.exists()) if (!targetFolder.mkdir()) return;
        // 获取源文件夹中的所有文件，存放在一个File数组中
        File[] files = sourceFolder.listFiles();
        if (files == null) return;
        // 遍历File数组，对每个文件进行灰度化处理
        for (File file : files) {
            // 获取文件
            Bitmap bitmap = BitmapProcess.getRealPathImages(sourcePath + file.getName());
            if (bitmap == null) continue;
            // 加载原始图像
            Mat colorImage = new Mat();
            Utils.bitmapToMat(bitmap, colorImage);
            // 创建一个空的灰度图像
            Mat grayscaleImage = new Mat();
            // 调用cvtColor函数，将彩色图像转换为灰度图像
            Imgproc.cvtColor(colorImage, grayscaleImage, Imgproc.COLOR_RGB2GRAY);
            Utils.matToBitmap(grayscaleImage, bitmap);
            // 获取文件的名称，不包括扩展名
            StringBuilder fileName = new StringBuilder();
            for (int j = 0; j < file.getName().split("\\.").length - 1; j++) {
                fileName.append(file.getName().split("\\.")[j]).append(".");
            }
            Imgcodecs.imwrite(file.getName(), grayscaleImage);
            if (file.getName().split("\\.")[file.getName().split("\\.").length - 1].equals("jpg")) {
                try {
                    FileOutputStream saveImgOut = new FileOutputStream(new File(targetPath, fileName + "jpg"));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                    saveImgOut.flush();
                    Log.d("Save Bitmap", "The picture is save to your phone!");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    FileOutputStream saveImgOut = new FileOutputStream(new File(targetPath, fileName + "png"));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                    saveImgOut.flush();
                    Log.d("Save Bitmap", "The picture is save to your phone!");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}