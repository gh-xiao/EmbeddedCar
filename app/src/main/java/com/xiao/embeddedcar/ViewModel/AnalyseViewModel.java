package com.xiao.embeddedcar.ViewModel;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.opencv.core.Mat;

public class AnalyseViewModel extends ViewModel {

    private final MutableLiveData<Integer> HMin = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> HMax = new MutableLiveData<>(20);
    private final MutableLiveData<Integer> SMin = new MutableLiveData<>(225);
    private final MutableLiveData<Integer> SMax = new MutableLiveData<>(255);
    private final MutableLiveData<Integer> VMin = new MutableLiveData<>(110);
    private final MutableLiveData<Integer> VMax = new MutableLiveData<>(255);
    private final MutableLiveData<Bitmap> detectBitmap = new MutableLiveData<>();
    private final MutableLiveData<Mat> hsvMat = new MutableLiveData<>();
    private final MutableLiveData<Mat> morphologyExMat = new MutableLiveData<>();
    private final MutableLiveData<Mat> recursionMorphologyExMat = new MutableLiveData<>();
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

    public MutableLiveData<Mat> getMorphologyExMat() {
        return morphologyExMat;
    }

    public MutableLiveData<Mat> getRecursionMorphologyExMat() {
        return recursionMorphologyExMat;
    }

    public MutableLiveData<Boolean> getMorphologyExMode() {
        return MorphologyExMode;
    }
}
