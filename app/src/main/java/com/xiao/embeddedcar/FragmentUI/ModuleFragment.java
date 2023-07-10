package com.xiao.embeddedcar.FragmentUI;

import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.luck.picture.lib.basic.PictureSelectionSystemModel;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.xiao.embeddedcar.Utils.PublicMethods.BitmapProcess;
import com.xiao.embeddedcar.ViewModel.MainViewModel;
import com.xiao.embeddedcar.ViewModel.ModuleViewModel;
import com.xiao.embeddedcar.databinding.FragmentModuleBinding;

import java.util.ArrayList;

public class ModuleFragment extends AbstractFragment<FragmentModuleBinding, ModuleViewModel> {

    private FragmentModuleBinding binding;
    private MainViewModel mainViewModel;
    private ModuleViewModel moduleViewModel;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private PictureSelectionSystemModel pictureSelector;

    public ModuleFragment() {
        super(FragmentModuleBinding::inflate, ModuleViewModel.class, true);
    }

    @Override
    public void initFragment(@NonNull FragmentModuleBinding binding, @Nullable ModuleViewModel viewModel, @Nullable Bundle savedInstanceState) {
        this.binding = binding;
        this.moduleViewModel = viewModel;
        this.mainViewModel = getMainViewModel();
    }

    @Override
    public void init() {
        binding.cleanBtn.setOnClickListener(v -> {
            mainViewModel.getModuleInfoTV().setValue(null);
            mainViewModel.getModuleImgShow().setValue(null);
            binding.moduleInfo.setText(null);
        });
        binding.moduleInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.trafficLightModBtn.setOnClickListener(v -> moduleViewModel.module(1, mainViewModel));
        binding.plateOcrModBtn.setOnClickListener(v -> moduleViewModel.module(2, mainViewModel));
        binding.shapeModBtn.setOnClickListener(v -> moduleViewModel.module(3, mainViewModel));
        binding.trafficSignModBtn.setOnClickListener(v -> moduleViewModel.module(4, mainViewModel));
        binding.VIDModBtn.setOnClickListener(v -> moduleViewModel.module(5, mainViewModel));
        binding.QRModBtn.setOnClickListener(v -> moduleViewModel.module(6, mainViewModel));
        binding.cutterBitmapBtn.setOnClickListener(v -> moduleViewModel.module(7, mainViewModel));
        binding.devMethodBtn.setOnClickListener(v -> moduleViewModel.module(0xFF, mainViewModel));
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

    @Override
    public void observerDataStateUpdateAction() {
        mainViewModel.getModuleInfoTV().setValue(null);
        mainViewModel.getModuleImgShow().observe(getViewLifecycleOwner(), b -> binding.moduleImg.setImageBitmap(b));
        mainViewModel.getModuleInfoTV().observe(getViewLifecycleOwner(), s -> {
            if (s != null) {
                binding.moduleInfo.append(s + "\n");
                int offset = binding.moduleInfo.getLineCount() * binding.moduleInfo.getLineHeight();
                if (offset > binding.moduleInfo.getHeight())
                    binding.moduleInfo.scrollTo(0, offset - binding.moduleInfo.getHeight());
            }
        });
        moduleViewModel.getDetectMode().observe(getViewLifecycleOwner(), b -> {
            binding.howDetectBtn.setText(b ? "默认检测" : "使用自定义图片");
            binding.cutterBitmapBtn.setText(b ? "保存摄像头图片" : "保存当前自定义图片");
        });
        moduleViewModel.getGetImgMode().observe(getViewLifecycleOwner(), b -> {
            if (b) {
                binding.getImgBtn.setText("开启实时图片传入");
                mainViewModel.getShowImg().observe(getViewLifecycleOwner(), bitmap -> binding.moduleImg.setImageBitmap(bitmap));
            } else {
                binding.getImgBtn.setText("关闭实时图片传入");
                mainViewModel.getShowImg().removeObservers(getViewLifecycleOwner());
                moduleViewModel.getDetectPicture().setValue(null);
            }
        });
        moduleViewModel.getDetectPicture().observe(getViewLifecycleOwner(), b -> binding.moduleImg.setImageBitmap(b));
    }
}
