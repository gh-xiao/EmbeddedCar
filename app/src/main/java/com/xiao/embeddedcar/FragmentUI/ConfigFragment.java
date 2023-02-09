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

import com.xiao.embeddedcar.R;
import com.xiao.embeddedcar.Utils.QRcode.QRBitmapCutter;
import com.xiao.embeddedcar.ViewModel.MainViewModel;
import com.xiao.embeddedcar.ViewModel.ModuleViewModel;
import com.xiao.embeddedcar.databinding.FragmentConfigBinding;

public class ConfigFragment extends Fragment {
    private FragmentConfigBinding binding;
    private MainViewModel mainViewModel;
    private ModuleViewModel moduleViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        moduleViewModel = new ViewModelProvider(requireActivity()).get(ModuleViewModel.class);
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
        /* 二维码颜色选择 */
        binding.QRColorChooseRG.setOnCheckedChangeListener((group, id) -> {
            if (id == R.id.rb_QR_red)
                mainViewModel.getQR_color().setValue(QRBitmapCutter.QRColor.RED);
            if (id == R.id.rb_QR_green)
                mainViewModel.getQR_color().setValue(QRBitmapCutter.QRColor.GREEN);
            if (id == R.id.rb_QR_blue)
                mainViewModel.getQR_color().setValue(QRBitmapCutter.QRColor.BLUE);
        });
        /* 图形颜色选择 */
        binding.shapeColorChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder shape_color_choose_builder = new AlertDialog.Builder(requireActivity());
            shape_color_choose_builder.setTitle("图形检测颜色选择");
            String[] item = {"红色", "黄色", "绿色", "青色", "蓝色", "紫色"};
            shape_color_choose_builder.setSingleChoiceItems(item, -1, (dialog, which) -> mainViewModel.getShape_color().setValue(item[which]));
            shape_color_choose_builder.create().show();
        });
        /* 图形类型选择 */
        binding.shapeTypeChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder shape_type_choose_builder = new AlertDialog.Builder(requireActivity());
            shape_type_choose_builder.setTitle("图形检测类型选择");
            String[] item = {"三角形", "矩形", "菱形", "五角星", "圆形", "总计"};
            shape_type_choose_builder.setSingleChoiceItems(item, -1, (dialog, which) -> mainViewModel.getShape_type().setValue(item[which]));
            shape_type_choose_builder.create().show();
        });
        /* 红绿灯信息发送选择 */
        binding.trafficLightChooseBtn.setOnCheckedChangeListener((group, id) -> mainViewModel.getSend_trafficLight().setValue(id == R.id.rb_tl_a ? 1 : 2));
        /* 车牌检测颜色选择 */
        binding.plateColorChooseRG.setOnCheckedChangeListener((group, id) -> {
            if (id == R.id.rb_plate_all) {
                mainViewModel.getPlate_color().setValue("all");
                moduleViewModel.getPlate_color().setValue("all");
            }
            if (id == R.id.rb_plate_green) {
                mainViewModel.getPlate_color().setValue("green");
                moduleViewModel.getPlate_color().setValue("green");
            }
            if (id == R.id.rb_plate_blue) {
                mainViewModel.getPlate_color().setValue("blue");
                moduleViewModel.getPlate_color().setValue("blue");
            }
        });
        /* 车型选择 */
        binding.carModelChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder car_model_choose_builder = new AlertDialog.Builder(requireActivity());
            car_model_choose_builder.setTitle("指定检测车型");
            String[] item = {"bike", "motor", "car", "truck", "van", "bus"};
            String[] showItem = {"单车", "摩托", "汽车", "卡车"};
            car_model_choose_builder.setSingleChoiceItems(showItem, -1, (dialog, which) -> mainViewModel.getCar_model().setValue(item[which]));
            car_model_choose_builder.create().show();
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
                    binding.QRColorChooseRG.check(R.id.rb_QR_red);
                    break;
                case GREEN:
                    binding.tvQrColor.setText("绿色");
                    binding.QRColorChooseRG.check(R.id.rb_QR_green);
                    break;
                case BLUE:
                    binding.tvQrColor.setText("蓝色");
                    binding.QRColorChooseRG.check(R.id.rb_QR_blue);
                    break;
            }
        });
        mainViewModel.getShape_color().observe(getViewLifecycleOwner(), s -> binding.tvShapeColor.setText(s));
        mainViewModel.getShape_type().observe(getViewLifecycleOwner(), s -> binding.tvShapeType.setText(s));
        mainViewModel.getSend_trafficLight().observe(getViewLifecycleOwner(), i -> {
            if (i != null) {
                binding.tvTrafficLight.setText(i == 1 ? "A灯" : "B灯");
                binding.trafficLightChooseBtn.check(i == 1 ? R.id.rb_tl_a : R.id.rb_tl_b);
            }
        });
        mainViewModel.getPlate_color().observe(getViewLifecycleOwner(), s -> {
            switch (s) {
                case "all":
                    binding.tvPlateColor.setText("任意");
                    binding.plateColorChooseRG.check(R.id.rb_plate_all);
                    break;
                case "green":
                    binding.tvPlateColor.setText("新能源");
                    binding.plateColorChooseRG.check(R.id.rb_plate_green);
                    break;
                case "blue":
                    binding.tvPlateColor.setText("蓝色");
                    binding.plateColorChooseRG.check(R.id.rb_plate_blue);
                    break;
            }
        });
        mainViewModel.getCar_model().observe(getViewLifecycleOwner(), s -> {
            switch (s) {
                case "bike":
                    binding.tvCarModel.setText("单车");
                    break;
                case "motor":
                    binding.tvCarModel.setText("摩托");
                    break;
                case "car":
                    binding.tvCarModel.setText("汽车");
                    break;
                case "truck":
                    binding.tvCarModel.setText("卡车");
                    break;

            }
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
