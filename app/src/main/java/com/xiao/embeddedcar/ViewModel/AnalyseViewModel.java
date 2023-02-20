package com.xiao.embeddedcar.ViewModel;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class AnalyseViewModel extends ViewModel {

    private final MutableLiveData<Integer> HMin = new MutableLiveData<>();
    private final MutableLiveData<Integer> HMax = new MutableLiveData<>();
    private final MutableLiveData<Integer> SMin = new MutableLiveData<>();
    private final MutableLiveData<Integer> SMax = new MutableLiveData<>();
    private final MutableLiveData<Integer> VMin = new MutableLiveData<>();
    private final MutableLiveData<Integer> VMax = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> detectBitmap = new MutableLiveData<>();
    private final MutableLiveData<Mat> hsvMat = new MutableLiveData<>();
    private final MutableLiveData<Mat> recursionMorphologyExMat = new MutableLiveData<>();
    private final MutableLiveData<Integer> chooseMorphologyExMode = new MutableLiveData<>(Imgproc.MORPH_DILATE);
    private final MutableLiveData<Boolean> MorphologyExMode = new MutableLiveData<>(true);

    public MutableLiveData<Integer> getHMin() {
        return HMin;
    }

    public MutableLiveData<Integer> getHMax() {
        return HMax;
    }

    public MutableLiveData<Integer> getSMin() {
        return SMin;
    }

    public MutableLiveData<Integer> getSMax() {
        return SMax;
    }

    public MutableLiveData<Integer> getVMin() {
        return VMin;
    }

    public MutableLiveData<Integer> getVMax() {
        return VMax;
    }

    public MutableLiveData<Bitmap> getDetectBitmap() {
        return detectBitmap;
    }

    public MutableLiveData<Mat> getHsvMat() {
        return hsvMat;
    }

    public MutableLiveData<Mat> getRecursionMorphologyExMat() {
        return recursionMorphologyExMat;
    }

    public MutableLiveData<Integer> getChooseMorphologyExMode() {
        return chooseMorphologyExMode;
    }

    public MutableLiveData<Boolean> getMorphologyExMode() {
        return MorphologyExMode;
    }
}
