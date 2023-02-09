package com.xiao.embeddedcar.FragmentUI;

import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.luck.picture.lib.basic.PictureSelectionSystemModel;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.xiao.embeddedcar.Utils.PublicMethods.BitmapProcess;
import com.xiao.embeddedcar.ViewModel.HomeViewModel;
import com.xiao.embeddedcar.ViewModel.ModuleViewModel;
import com.xiao.embeddedcar.databinding.FragmentModuleBinding;

import java.util.ArrayList;

public class ModuleFragment extends Fragment {

    private FragmentModuleBinding binding;
    public ModuleViewModel moduleViewModel;
    private HomeViewModel homeViewModel;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private PictureSelectionSystemModel pictureSelector;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        moduleViewModel = new ViewModelProvider(requireActivity()).get(ModuleViewModel.class);
        binding = FragmentModuleBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //控件动作初始化
        init();
        //设置观察者
        observerDataStateUpdateAction();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        binding = null;
    }

    /**
     * 控件动作初始化
     */
    private void init() {
        /* 设置模块消息接收线程 */
        moduleViewModel.getThreadReturnMsg();
        binding.moduleInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.trafficLightModBtn.setOnClickListener(v -> moduleViewModel.module(1));
        binding.plateOcrModBtn.setOnClickListener(v -> moduleViewModel.module(2));
        binding.shapeModBtn.setOnClickListener(v -> moduleViewModel.module(3));
        binding.trafficSignModBtn.setOnClickListener(v -> moduleViewModel.module(4));
        binding.VIDModBtn.setOnClickListener(v -> moduleViewModel.module(5));
        binding.QRModBtn.setOnClickListener(v -> moduleViewModel.module(6));
        binding.cutterBitmapBtn.setOnClickListener(v -> moduleViewModel.module(7));
        binding.howDetectBtn.setOnCheckedChangeListener((buttonView, b) -> moduleViewModel.getDetectMode().setValue(b));
        binding.getImgBtn.setOnCheckedChangeListener((buttonView, b) -> moduleViewModel.getGetImgMode().setValue(b));
        binding.chooseDetectPicBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 30)
                pickMedia.launch(new PickVisualMediaRequest.Builder().build());
            else {
                pictureSelector.forSystemResultActivity(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        moduleViewModel.getDetectPicture().setValue(BitmapProcess.getRealPathImages(result.get(0).getRealPath()));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null)
                moduleViewModel.getDetectPicture().setValue(BitmapProcess.getInstance().showImage(uri));
        });
        pictureSelector = PictureSelector.create(this).openSystemGallery(SelectMimeType.ofImage()).setSelectionMode(1);
    }

    /**
     * 观察者数据状态更新活动
     */
    private void observerDataStateUpdateAction() {
        moduleViewModel.getModuleInfoTV().setValue(null);
        moduleViewModel.getModuleImgShow().observe(getViewLifecycleOwner(), b -> {
            if (b != null) binding.moduleImg.setImageBitmap(b);
        });
        moduleViewModel.getModuleInfoTV().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.moduleInfo.append(s + "\n");
        });
        moduleViewModel.getDetectMode().observe(getViewLifecycleOwner(), b -> {
            binding.howDetectBtn.setText(b ? "默认检测" : "使用自定义图片");
            binding.cutterBitmapBtn.setText(b ? "保存摄像头图片" : "保存当前自定义图片");
        });
        moduleViewModel.getGetImgMode().observe(getViewLifecycleOwner(), b -> {
            if (b) {
                binding.getImgBtn.setText("开启实时图片传入");
                homeViewModel.getShowImg().observe(getViewLifecycleOwner(), bitmap -> {
                    if (bitmap != null) binding.moduleImg.setImageBitmap(bitmap);
                });
            } else {
                binding.getImgBtn.setText("关闭实时图片传入");
                homeViewModel.getShowImg().removeObservers(getViewLifecycleOwner());
                moduleViewModel.getDetectPicture().setValue(null);
            }
        });
        moduleViewModel.getDetectPicture().observe(getViewLifecycleOwner(), b -> binding.moduleImg.setImageBitmap(b));
    }
}
