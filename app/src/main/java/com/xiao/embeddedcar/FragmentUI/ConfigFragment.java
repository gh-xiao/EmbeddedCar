package com.xiao.embeddedcar.FragmentUI;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModel;

import com.github.gzuliyujiang.wheelview.contract.OnWheelChangedListener;
import com.github.gzuliyujiang.wheelview.widget.WheelView;
import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.R;
import com.xiao.embeddedcar.Utils.QRcode.QRBitmapCutter;
import com.xiao.embeddedcar.Utils.TrafficLight.TrafficLightByLocation;
import com.xiao.embeddedcar.Utils.TrafficSigns.YoloV5_tfLite_TSDetector;
import com.xiao.embeddedcar.Utils.VID.YoloV5_tfLite_VIDDetector;
import com.xiao.embeddedcar.ViewModel.MainViewModel;
import com.xiao.embeddedcar.databinding.FragmentConfigBinding;

import java.util.Objects;

public class ConfigFragment extends AbstractFragment<FragmentConfigBinding, ViewModel> {
    private FragmentConfigBinding binding;
    private MainViewModel mainViewModel;

    public ConfigFragment() {
        super(FragmentConfigBinding::inflate, null, true);
    }

    @Override
    public void initFragment(@NonNull FragmentConfigBinding binding, @Nullable ViewModel viewModel, @Nullable Bundle savedInstanceState) {
        this.binding = binding;
        this.mainViewModel = getMainViewModel();
        //数字滚轮初始化
        initPicker();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void init() {
        binding.rbQRRed.setOnCheckedChangeListener((buttonView, b) -> mainViewModel.getRed().setValue(b));
        binding.rbQRGreen.setOnCheckedChangeListener((buttonView, b) -> mainViewModel.getGreen().setValue(b));
        binding.rbQRBlue.setOnCheckedChangeListener((buttonView, b) -> mainViewModel.getBlue().setValue(b));
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
        /* 红绿灯检测位置选择 */
        binding.detectTrafficLightChooseBtn.setOnCheckedChangeListener((group, id) -> mainViewModel.getDetect_trafficLight().setValue(id == R.id.rb_detect_tl_l ? 1 : 2));
        /* 红绿灯位置阈值设置 */
        binding.sbLightLocationConfidence.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mainViewModel.getLight_location_confidence().setValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /* 车牌检测颜色选择 */
        binding.plateColorChooseRG.setOnCheckedChangeListener((group, id) -> {
            if (id == R.id.rb_plate_all) {
                mainViewModel.getPlate_color().setValue("all");
            }
            if (id == R.id.rb_plate_green) {
                mainViewModel.getPlate_color().setValue("green");
            }
            if (id == R.id.rb_plate_blue) {
                mainViewModel.getPlate_color().setValue("blue");
            }
        });
        /* 车型检测选择 */
        binding.detectCarTypeChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder car_model_choose_builder = new AlertDialog.Builder(requireActivity());
            car_model_choose_builder.setTitle("指定检测车型");
            String[] item = {"bike", "motor", "car", "truck", "van", "bus"};
            String[] showItem = {"自行车", "摩托", "汽车", "卡车/面包车"};
            car_model_choose_builder.setSingleChoiceItems(showItem, -1, (dialog, which) -> mainViewModel.getDetect_car_type().setValue(item[which]));
            car_model_choose_builder.create().show();
        });
        /* 车型识别选择 */
        binding.carTypeChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder car_model_choose_builder = new AlertDialog.Builder(requireActivity());
            car_model_choose_builder.setTitle("指定识别车型");
            String[] item = {"all", "bike", "motor", "car", "truck", "van", "bus"};
            String[] showItem = {"任意", "自行车", "摩托", "汽车", "卡车/面包车"};
            car_model_choose_builder.setSingleChoiceItems(showItem, -1, (dialog, which) -> mainViewModel.getCar_type().setValue(item[which]));
            car_model_choose_builder.create().show();
        });
        /* 交通标志物识别最低置信度 */
        binding.sbTSMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mainViewModel.getTraffic_sign_minimumConfidence().setValue(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /* 车型识别最低置信度 */
        binding.sbVIDMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mainViewModel.getVID_minimumConfidence().setValue(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        custom_btn_init();
        /* 重置置信度 */
        binding.resetTSBtn.setOnClickListener(v -> mainViewModel.getTraffic_sign_minimumConfidence().setValue(YoloV5_tfLite_TSDetector.MINIMUM_CONFIDENCE_TF_OD_API));
        binding.resetVIDBtn.setOnClickListener(v -> mainViewModel.getVID_minimumConfidence().setValue(YoloV5_tfLite_VIDDetector.MINIMUM_CONFIDENCE_TF_OD_API));
        binding.resetLightLocationConfidenceDataBtn.setOnClickListener(v -> mainViewModel.getLight_location_confidence().setValue(TrafficLightByLocation.ORIGIN_LOCATION));
        binding.tvVersion.setText("当前软件版本: " + getVersionName());
    }

    /**
     * 实验性自定义发送数据
     */
    private void custom_btn_init() {
        /* 车牌自定义发送数据选择 */
        binding.etPlateData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mainViewModel.getPlate_data().setValue(s.toString());
            }
        });
        binding.sendPlateChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder save_plate_choose_builder = new AlertDialog.Builder(requireActivity());
            save_plate_choose_builder.setTitle("选择已保存的车牌");
            String[] item = Objects.requireNonNull(ConnectTransport.getInstance().getPlate_list()).toArray(new String[0]);
            save_plate_choose_builder.setSingleChoiceItems(item, -1, (dialog, which) -> binding.etPlateData.setText(item[which]));
            save_plate_choose_builder.create().show();
        });
        binding.cbPlateSendMode.setOnCheckedChangeListener((buttonView, isChecked) -> mainViewModel.getSend_plate_mode().setValue(isChecked));
        /* 车型自定义发送数据选择 */
        binding.sendCarTypeChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder car_model_choose_builder = new AlertDialog.Builder(requireActivity());
            car_model_choose_builder.setTitle("选择发送车型");
            String[] item = {"bike", "motor", "car", "truck"};
            String[] showItem = {"自行车", "摩托", "汽车", "卡车"};
            car_model_choose_builder.setSingleChoiceItems(showItem, -1, (dialog, which) -> mainViewModel.getCar_type_data().setValue(item[which]));
            car_model_choose_builder.create().show();
        });
        binding.cbCarTypeSendMode.setOnCheckedChangeListener((buttonView, isChecked) -> mainViewModel.getSend_car_type_mode().setValue(isChecked));
        /* 交通标志物自定义发送数据选择 */
        binding.sendTSChooseBtn.setOnClickListener(v -> {
            AlertDialog.Builder car_model_choose_builder = new AlertDialog.Builder(requireActivity());
            car_model_choose_builder.setTitle("选择发送交通标志物");
            String[] item = {"go_straight", "turn_left", "turn_around", "no_straight", "no_turn", "turn_right"};
            String[] showItem = {"直行", "左转", "掉头", "禁止直行", "禁止通行", "右转"};
            car_model_choose_builder.setSingleChoiceItems(showItem, -1, (dialog, which) -> mainViewModel.getTraffic_sign_data().setValue(item[which]));
            car_model_choose_builder.create().show();
        });
        binding.cbTSSendMode.setOnCheckedChangeListener((buttonView, isChecked) -> mainViewModel.getSend_TS_mode().setValue(isChecked));
        /* 车牌检测模式选择 */
        binding.detectPlateMethodMode.setOnCheckedChangeListener((buttonView, isChecked) -> mainViewModel.getDetect_methods_choose().setValue(isChecked));
    }

    /**
     * 数字滚轮初始化
     */
    private void initPicker() {
        binding.TSMinData.setRange(0, 100, 1);
        binding.TSMinData.setTextSize(0);
        binding.TSMinData.setCyclicEnabled(true);
        binding.TSMinData.setIndicatorEnabled(false);
        binding.TSMinData.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onWheelScrolled(WheelView view, int offset) {

            }

            @Override
            public void onWheelSelected(WheelView view, int position) {
                mainViewModel.getTraffic_sign_minimumConfidence().setValue(position / 100f);
            }

            @Override
            public void onWheelScrollStateChanged(WheelView view, int state) {

            }

            @Override
            public void onWheelLoopFinished(WheelView view) {

            }
        });

        binding.VIDMinData.setRange(0, 100, 1);
        binding.VIDMinData.setTextSize(0);
        binding.VIDMinData.setCyclicEnabled(true);
        binding.VIDMinData.setIndicatorEnabled(false);
        binding.VIDMinData.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onWheelScrolled(WheelView view, int offset) {

            }

            @Override
            public void onWheelSelected(WheelView view, int position) {
                mainViewModel.getVID_minimumConfidence().setValue(position / 100f);
            }

            @Override
            public void onWheelScrollStateChanged(WheelView view, int state) {

            }

            @Override
            public void onWheelLoopFinished(WheelView view) {

            }
        });

        binding.lightLocationConfidenceData.setRange(0, 200, 1);
        binding.lightLocationConfidenceData.setTextSize(0);
        binding.lightLocationConfidenceData.setCyclicEnabled(true);
        binding.lightLocationConfidenceData.setIndicatorEnabled(false);
        binding.lightLocationConfidenceData.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onWheelScrolled(WheelView view, int offset) {

            }

            @Override
            public void onWheelSelected(WheelView view, int position) {
                mainViewModel.getLight_location_confidence().setValue(position);
            }

            @Override
            public void onWheelScrollStateChanged(WheelView view, int state) {

            }

            @Override
            public void onWheelLoopFinished(WheelView view) {

            }
        });
    }

    @Override
    public void observerDataStateUpdateAction() {
        mainViewModel.getRed().observe(getViewLifecycleOwner(), b -> {
            binding.rbQRRed.setChecked(b);
            if (mainViewModel.getQR_color().getValue() == null) return;
            if (b) {
                if (!mainViewModel.getQR_color().getValue().contains(QRBitmapCutter.QRColor.RED)) {
                    mainViewModel.getQR_color().getValue().add(QRBitmapCutter.QRColor.RED);
                    mainViewModel.getTv_color().setValue(mainViewModel.getTv_color().getValue() + "●红色●");
                }
            } else {
                mainViewModel.getQR_color().getValue().remove(QRBitmapCutter.QRColor.RED);
                mainViewModel.getTv_color().setValue(Objects.requireNonNull(mainViewModel.getTv_color().getValue()).replaceAll("●红色●", ""));
            }
        });
        mainViewModel.getGreen().observe(getViewLifecycleOwner(), b -> {
            binding.rbQRGreen.setChecked(b);
            if (mainViewModel.getQR_color().getValue() == null) return;
            if (b) {
                if (!mainViewModel.getQR_color().getValue().contains(QRBitmapCutter.QRColor.GREEN)) {
                    mainViewModel.getQR_color().getValue().add(QRBitmapCutter.QRColor.GREEN);
                    mainViewModel.getTv_color().setValue(mainViewModel.getTv_color().getValue() + "●绿色●");
                }
            } else {
                mainViewModel.getQR_color().getValue().remove(QRBitmapCutter.QRColor.GREEN);
                mainViewModel.getTv_color().setValue(Objects.requireNonNull(mainViewModel.getTv_color().getValue()).replaceAll("●绿色●", ""));
            }
        });
        mainViewModel.getBlue().observe(getViewLifecycleOwner(), b -> {
            binding.rbQRBlue.setChecked(b);
            if (mainViewModel.getQR_color().getValue() == null) return;
            if (b) {
                if (!mainViewModel.getQR_color().getValue().contains(QRBitmapCutter.QRColor.BLUE)) {
                    mainViewModel.getQR_color().getValue().add(QRBitmapCutter.QRColor.BLUE);
                    mainViewModel.getTv_color().setValue(mainViewModel.getTv_color().getValue() + "●蓝色●");
                }
            } else {
                mainViewModel.getQR_color().getValue().remove(QRBitmapCutter.QRColor.BLUE);
                mainViewModel.getTv_color().setValue(Objects.requireNonNull(mainViewModel.getTv_color().getValue()).replaceAll("●蓝色●", ""));
            }
        });
        mainViewModel.getTv_color().observe(getViewLifecycleOwner(), s -> binding.tvQrColor.setText(s));
        mainViewModel.getShape_color().observe(getViewLifecycleOwner(), s -> binding.tvShapeColor.setText(s));
        mainViewModel.getShape_type().observe(getViewLifecycleOwner(), s -> binding.tvShapeType.setText(s));
        mainViewModel.getSend_trafficLight().observe(getViewLifecycleOwner(), i -> {
            if (i != null) {
                binding.tvTrafficLight.setText(i == 1 ? "A灯" : "B灯");
                binding.trafficLightChooseBtn.check(i == 1 ? R.id.rb_tl_a : R.id.rb_tl_b);
            }
        });
        mainViewModel.getDetect_trafficLight().observe(getViewLifecycleOwner(), i -> {
            if (i != null) {
                binding.tvTrafficLightLocation.setText(i == 1 ? "长线" : "短线");
                binding.detectTrafficLightChooseBtn.check(i == 1 ? R.id.rb_detect_tl_l : R.id.rb_detect_tl_s);
            }
        });
        mainViewModel.getLight_location_confidence().observe(getViewLifecycleOwner(), i -> {
            binding.tvLightLocationConfidence.setText(String.valueOf(i));
            binding.sbLightLocationConfidence.setProgress(i);
            binding.lightLocationConfidenceData.setDefaultValue(i);
            TrafficLightByLocation.setLightLocation(i);
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
        mainViewModel.getDetect_car_type().observe(getViewLifecycleOwner(), s -> {
            switch (s) {
                case "bike":
                    binding.tvDetectCarModel.setText("自行车");
                    break;
                case "motor":
                    binding.tvDetectCarModel.setText("摩托");
                    break;
                case "car":
                    binding.tvDetectCarModel.setText("汽车");
                    break;
                case "truck":
                    binding.tvDetectCarModel.setText("卡车/面包车");
                    break;
            }
        });
        mainViewModel.getCar_type().observe(getViewLifecycleOwner(), s -> {
            switch (s) {
                case "bike":
                    binding.tvCarModel.setText("自行车");
                    break;
                case "motor":
                    binding.tvCarModel.setText("摩托");
                    break;
                case "car":
                    binding.tvCarModel.setText("汽车");
                    break;
                case "truck":
                    binding.tvCarModel.setText("卡车/面包车");
                    break;
                default:
                    binding.tvCarModel.setText("任意");
                    break;
            }
        });
        mainViewModel.getTraffic_sign_minimumConfidence().observe(getViewLifecycleOwner(), f -> {
            binding.tvTSConfidence.setText(String.valueOf(f));
            binding.sbTSMin.setProgress((int) (f * 100));
            binding.TSMinData.setDefaultValue((int) (f * 100));
            YoloV5_tfLite_TSDetector.minimumConfidence = f;
        });
        mainViewModel.getVID_minimumConfidence().observe(getViewLifecycleOwner(), f -> {
            binding.tvVIDConfidence.setText(String.valueOf(f));
            binding.sbVIDMin.setProgress((int) (f * 100));
            binding.VIDMinData.setDefaultValue((int) (f * 100));
            YoloV5_tfLite_VIDDetector.minimumConfidence = f;
        });

        /* 自定义数据 */
        mainViewModel.getPlate_data().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.plateData.setText(s);
        });
        mainViewModel.getCar_type_data().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.tvSendCarType.setText(s);
        });
        mainViewModel.getTraffic_sign_data().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.tvSendTSData.setText(s);
        });
        /* 自定义数据发送方式 */
        mainViewModel.getSend_plate_mode().observe(getViewLifecycleOwner(), b -> {
            binding.cbPlateSendMode.setChecked(b);
            binding.cbPlateSendMode.setText(b ? R.string.use_input_plate : R.string.dont_use_input_plate);
        });
        mainViewModel.getSend_car_type_mode().observe(getViewLifecycleOwner(), b -> {
            binding.cbCarTypeSendMode.setChecked(b);
            binding.cbCarTypeSendMode.setText(b ? R.string.use_choose_car_type : R.string.dont_use_choose_car_type);
        });
        mainViewModel.getSend_TS_mode().observe(getViewLifecycleOwner(), b -> {
            binding.cbTSSendMode.setChecked(b);
            binding.cbTSSendMode.setText(b ? R.string.use_choose_TS : R.string.dont_use_choose_TS);
        });
        mainViewModel.getDetect_methods_choose().observe(getViewLifecycleOwner(), b -> {
            binding.detectPlateMethodMode.setChecked(b);
            binding.detectPlateMethodMode.setText(b ? R.string.use_plate_color_detect_plate : R.string.use_car_type_frame_select_detect_plate);
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
