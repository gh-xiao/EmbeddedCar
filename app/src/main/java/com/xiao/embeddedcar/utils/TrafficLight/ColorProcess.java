package com.xiao.embeddedcar.utils.TrafficLight;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

public class ColorProcess {

    private final Context context;
    //图像亮度
    private final int brightness = 25;
    //图像模糊度(>25即可)
    private int radius = 100;
    //图像饱和度
    private final int progress = 3;
    //已经处理好的图像
    private Bitmap result;

    public ColorProcess(Context context) {
        this.context = context.getApplicationContext();
    }

    public Bitmap getResult() {
        return result;
    }

    /**
     * 执行图像处理流程
     * 改变亮度 → 改变模糊度 → 提升饱和度 → 输出至result
     *
     * @param bitmap 传入需要处理的图片
     */
    public void PictureProcessing(Bitmap bitmap) {
        if (bitmap == null) return;
        Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        ColorMatrix cMatrix = new ColorMatrix();
        // 改变亮度
        cMatrix.set(new float[]{
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0});

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));
        Canvas canvas = new Canvas(bmp);
        // 在Canvas上绘制一个已经存在的Bitmap。这样，dstBitmap就和srcBitmap一摸一样了
        canvas.drawBitmap(bitmap, 0, 0, paint);
        rsBlur(bmp);
    }

    /**
     * 图像模糊
     * 模糊的大小 范围：0-25
     *
     * @param source 要模糊的图像
     */
    public void rsBlur(Bitmap source) {
        if (radius > 25) radius = 25;
        //(1)
        RenderScript renderScript = RenderScript.create(context);
        Log.i(TAG, "scale size:" + source.getWidth() + "*" + source.getHeight());
        //(2)
        // Allocate memory for Renderscript to work with
        final Allocation input = Allocation.createFromBitmap(renderScript, source);
        final Allocation output = Allocation.createTyped(renderScript, input.getType());
        //(3)
        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        //(4)
        scriptIntrinsicBlur.setInput(input);
        //(5)
        // Set the blur radius
        scriptIntrinsicBlur.setRadius(radius);
        //(6)
        // Start the ScriptIntrinsicBlur
        scriptIntrinsicBlur.forEach(output);
        //(7)
        // Copy the output to the blurred bitmap
        output.copyTo(source);
        //(8)
        renderScript.destroy();

        imageCrop(source);
    }

    /**
     * 设置图片饱和度
     * 在此返回处理后的图像
     *
     * @param bitmap 需要处理的Bitmap
     */
    private void imageCrop(Bitmap bitmap) {

        Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        ColorMatrix cMatrix = new ColorMatrix();
        // 设置饱和度
        cMatrix.setSaturation((float) progress);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

        Canvas canvas = new Canvas(bmp);
        // 在Canvas上绘制一个已经存在的Bitmap。这样，dstBitmap就和srcBitmap一模一样了
        canvas.drawBitmap(bitmap, 0, 0, paint);
        //bmp在这里就已经处理完成了
        result = bmp;
    }

}
