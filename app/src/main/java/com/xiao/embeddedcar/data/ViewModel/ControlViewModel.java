package com.xiao.embeddedcar.data.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ControlViewModel extends ViewModel {
    MutableLiveData<String> selectName = new MutableLiveData<>();
    MutableLiveData<Integer> selectImage = new MutableLiveData<>();
    MutableLiveData<String> showMsg = new MutableLiveData<>();

    public MutableLiveData<String> getSelectName() {
        return selectName;
    }

    public MutableLiveData<Integer> getSelectImage() {
        return selectImage;
    }

    public MutableLiveData<String> getShowMsg() {
        return showMsg;
    }
}
