package com.xiao.embeddedcar.FragmentUI;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.xiao.embeddedcar.Utils.QRcode.QRBitmapCutter;
import com.xiao.embeddedcar.ViewModel.MainViewModel;
import com.xiao.embeddedcar.databinding.FragmentConfigBinding;

public class ConfigFragment extends Fragment {
    private FragmentConfigBinding binding;
    private MainViewModel mainViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding = FragmentConfigBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //控件动作初始化
        init();
        //设置观察者
        observerDataStateUpdateAction();
        return root;
    }

    /**
     * 控件动作初始化
     */
    private void init() {
        binding.QRColorChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder qr_color_choose_builder = new AlertDialog.Builder(requireActivity());
            qr_color_choose_builder.setTitle("二维码检测颜色选择");
            qr_color_choose_builder.setSingleChoiceItems(new String[]{"红色", "绿色", "蓝色"}, -1, (dialog, which) -> {
                switch (which) {
                    case 0:
                        mainViewModel.getQR_color().setValue(QRBitmapCutter.QRColor.RED);
                        break;
                    case 1:
                        mainViewModel.getQR_color().setValue(QRBitmapCutter.QRColor.GREEN);
                        break;
                    case 2:
                        mainViewModel.getQR_color().setValue(QRBitmapCutter.QRColor.BLUE);
                        break;
                }
            });
            qr_color_choose_builder.create().show();
        });
        binding.shapeColorChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder shape_color_choose_builder = new AlertDialog.Builder(requireActivity());
            shape_color_choose_builder.setTitle("图形检测颜色选择");
            String[] item = {"红色", "黄色", "绿色", "青色", "蓝色", "紫色"};
            shape_color_choose_builder.setSingleChoiceItems(item, -1, (dialog, which) -> mainViewModel.getShape_color().setValue(item[which]));
            shape_color_choose_builder.create().show();
        });
        binding.shapeTypeChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder shape_type_choose_builder = new AlertDialog.Builder(requireActivity());
            shape_type_choose_builder.setTitle("图形检测类型选择");
            String[] item = {"三角形", "矩形", "菱形", "五角星", "圆形", "总计"};
            shape_type_choose_builder.setSingleChoiceItems(item, -1, (dialog, which) -> mainViewModel.getShape_type().setValue(item[which]));
            shape_type_choose_builder.create().show();
        });
        binding.trafficLightChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder shape_type_choose_builder = new AlertDialog.Builder(requireActivity());
            shape_type_choose_builder.setTitle("红绿灯信息发送选择");
            shape_type_choose_builder.setSingleChoiceItems(new String[]{"A灯", "B灯"}, -1, (dialog, which) -> mainViewModel.getSend_trafficLight().setValue(which == 0 ? 1 : 2));
            shape_type_choose_builder.create().show();
        });
        binding.tvVersion.setText(getVersionName());
    }

    /**
     * 观察者数据状态更新活动
     */
    private void observerDataStateUpdateAction() {
        mainViewModel.getQR_color().observe(getViewLifecycleOwner(), qrColor -> {
            QRBitmapCutter.color = qrColor;
            switch (qrColor) {
                case RED:
                    binding.tvQrColor.setText("红色");
                    return;
                case GREEN:
                    binding.tvQrColor.setText("绿色");
                    return;
                case BLUE:
                    binding.tvQrColor.setText("蓝色");
            }
        });
        mainViewModel.getShape_color().observe(getViewLifecycleOwner(), s -> binding.tvShapeColor.setText(s));
        mainViewModel.getShape_type().observe(getViewLifecycleOwner(), s -> binding.tvShapeType.setText(s));
        mainViewModel.getSend_trafficLight().observe(getViewLifecycleOwner(), i -> {
            if (i != null) binding.tvTrafficLight.setText(i == 1 ? "A灯" : "B灯");
        });
    }

    /**
     * 获取软件版本
     *
     * @return VersionName
     */
    private String getVersionName() {
        try {
            return requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "ERROR GET VERSION!";
        }
    }
}
