package com.xiao.embeddedcar.FragmentUI;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.github.gzuliyujiang.wheelview.contract.OnWheelChangedListener;
import com.github.gzuliyujiang.wheelview.widget.WheelView;
import com.xiao.embeddedcar.Utils.PublicMethods.BitmapProcess;
import com.xiao.embeddedcar.Utils.PublicMethods.TFTAutoCutter;
import com.xiao.embeddedcar.ViewAdapter.AnalyseAdapter;
import com.xiao.embeddedcar.ViewModel.AnalyseViewModel;
import com.xiao.embeddedcar.databinding.FragmentAnalyseBinding;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class AnalyseFragment extends ABaseFragment {
    FragmentAnalyseBinding binding;
    AnalyseViewModel analyseViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        analyseViewModel = new ViewModelProvider(this).get(AnalyseViewModel.class);
        binding = FragmentAnalyseBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        init();
        observerDataStateUpdateAction();
        return root;
    }

    @Override
    void init() {
        /* HSV参数设置 */
        RestoreHSV();
        /* 图片列表设置 */
        AnalyseAdapter analyseAdapter = new AnalyseAdapter(getContext());
        binding.lvList.setAdapter(analyseAdapter);
        binding.lvList.setOnItemClickListener((parent, view, p, id) -> {
            analyseAdapter.setSelectItem(p);
            analyseAdapter.notifyDataSetChanged();
            analyseViewModel.getDetectBitmap().setValue(BitmapProcess.getImages((String) analyseAdapter.getItem(p)));
        });
        /* HSV滑块参数设置 */
        binding.sbHMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                analyseViewModel.getHMin().setValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.sbHMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                analyseViewModel.getHMax().setValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.sbSMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                analyseViewModel.getSMin().setValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.sbSMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                analyseViewModel.getSMax().setValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.sbVMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                analyseViewModel.getVMin().setValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.sbVMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                analyseViewModel.getVMax().setValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /* 分析HSV色彩参数按钮 */
        binding.getHsvMatBtn.setOnClickListener(v -> {
            /* 设置参数 */
            int a1 = analyseViewModel.getHMin().getValue() != null ? analyseViewModel.getHMin().getValue() : 0;
            int a2 = analyseViewModel.getHMax().getValue() != null ? analyseViewModel.getHMax().getValue() : 0;
            int a3 = analyseViewModel.getSMin().getValue() != null ? analyseViewModel.getSMin().getValue() : 0;
            int a4 = analyseViewModel.getSMax().getValue() != null ? analyseViewModel.getSMax().getValue() : 0;
            int a5 = analyseViewModel.getVMin().getValue() != null ? analyseViewModel.getVMin().getValue() : 0;
            int a6 = analyseViewModel.getVMax().getValue() != null ? analyseViewModel.getVMax().getValue() : 0;
            /* 获取Bitmap */
            Bitmap detectBitmap = analyseViewModel.getDetectBitmap().getValue();
            if (detectBitmap == null) return;
            /* 转化为Mat对象 */
            Mat mat = new Mat();
            Utils.bitmapToMat(detectBitmap, mat);
            /* 获取hsvMat */
            Mat hsvMat = new Mat();
            Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGB2HSV);
            Core.inRange(hsvMat, new Scalar(a1, a3, a5), new Scalar(a2, a4, a6), hsvMat);
            /* 输出 */
            Bitmap bitmap = Bitmap.createBitmap(hsvMat.width(), hsvMat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(hsvMat, bitmap);
            analyseViewModel.getHsvMat().setValue(hsvMat);
            binding.imgShow.setImageBitmap(bitmap);
            analyseViewModel.getRecursionMorphologyExMat().setValue(null);
        });
        /* 形态学操作按钮 */
        binding.morphologyExBtn.setOnClickListener(v -> {
            /* 确定形态学操作 */
            /* 结构元形状构造函数 - getStructuringElement(int shape, Size ksize, Point anchor)
                1:MORPH_RECT 表示产生矩形的结构元
                2:MORPH_ELLIPSE 表示产生椭圆形的结构元
                3:MORPH_CROSS 表示产生十字交叉形的结构元
                ksize：表示结构元的尺寸，即（宽，高），必须是奇数
                anchor：表示结构元的锚点，即参考点。默认值Point(-1, -1)代表中心像素为锚点 */
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
            /* 检测模式 */
            if (analyseViewModel.getMorphologyExMode().getValue() == null) return;
            /* 获取Mat */
            Mat hsvMat = analyseViewModel.getHsvMat().getValue();
            if (hsvMat == null) return;
            /* 创建Bitmap */
            Bitmap bitmap = Bitmap.createBitmap(hsvMat.width(), hsvMat.height(), Bitmap.Config.ARGB_8888);
            if (analyseViewModel.getMorphologyExMode().getValue()) {
                Mat morphologyExMat = new Mat();
                /* 形态学操作 */
                Imgproc.morphologyEx(hsvMat, morphologyExMat, analyseViewModel.getChooseMorphologyExMode().getValue() == null ?
                        Imgproc.MORPH_DILATE : analyseViewModel.getChooseMorphologyExMode().getValue(), kernel);
                /* 输出 */
                Utils.matToBitmap(morphologyExMat, bitmap);
                analyseViewModel.getRecursionMorphologyExMat().setValue(morphologyExMat);
            } else {
                /* 获取递归Mat */
                Mat recursionMorphologyExMat = analyseViewModel.getRecursionMorphologyExMat().getValue() == null ?
                        hsvMat : analyseViewModel.getRecursionMorphologyExMat().getValue();
                /* 形态学操作 - 膨胀 */
                Imgproc.morphologyEx(recursionMorphologyExMat, recursionMorphologyExMat, Imgproc.MORPH_DILATE, kernel);
                /* 输出 */
                Utils.matToBitmap(recursionMorphologyExMat, bitmap);
                analyseViewModel.getRecursionMorphologyExMat().setValue(recursionMorphologyExMat);
            }
            binding.imgShow.setImageBitmap(bitmap);
        });
        /* 选择形态学操作 */
        binding.chooseMorphologyExBtn.setOnClickListener(v -> {
            AlertDialog.Builder shape_color_choose_builder = new AlertDialog.Builder(requireActivity());
            shape_color_choose_builder.setTitle("形态学操作选择");
            String[] item = {"腐蚀", "膨胀", "开操作", "闭操作", "梯度操作", "黑帽操作"};
            shape_color_choose_builder.setSingleChoiceItems(item, -1, (dialog, which) ->
                    analyseViewModel.getChooseMorphologyExMode().setValue(which));
            shape_color_choose_builder.create().show();
        });
        /* 形态学操作模式选择(对当前图像单次或多次操作) */
        binding.morphologyExMode.setOnCheckedChangeListener((buttonView, b) -> analyseViewModel.getMorphologyExMode().setValue(b));
        /* TFT智能裁剪 */
        binding.TFTAutoCutterBtn.setOnClickListener(v -> {
            /* 获取Bitmap */
            Bitmap detectBitmap = analyseViewModel.getDetectBitmap().getValue();
            if (detectBitmap == null) return;
            binding.imgShow.setImageBitmap(TFTAutoCutter.TFTCutter(detectBitmap));
        });
        /* 导入参数到裁剪 */
        binding.importBtn.setOnClickListener(v -> importHSV());
        /* 导出裁剪参数 */
        binding.exportBtn.setOnClickListener(v -> exportHSV());
        /* 还原HSV参数 */
        binding.restoreBtn.setOnClickListener(v -> RestoreHSV());
        /* 数字滚轮设置 */
        initPicker();
    }

    @Override
    void observerDataStateUpdateAction() {
        analyseViewModel.getDetectBitmap().observe(getViewLifecycleOwner(), b -> {
            if (b != null) binding.imgShow.setImageBitmap(b);
        });

        analyseViewModel.getHMin().observe(getViewLifecycleOwner(), i -> {
            binding.sbHMin.setProgress(i);
            binding.HMinData.setDefaultValue(i);
        });
        analyseViewModel.getHMax().observe(getViewLifecycleOwner(), i -> {
            binding.sbHMax.setProgress(i);
            binding.HMaxData.setDefaultValue(i);
        });
        analyseViewModel.getSMin().observe(getViewLifecycleOwner(), i -> {
            binding.sbSMin.setProgress(i);
            binding.SMinData.setDefaultValue(i);
        });
        analyseViewModel.getSMax().observe(getViewLifecycleOwner(), i -> {
            binding.sbSMax.setProgress(i);
            binding.SMaxData.setDefaultValue(i);
        });
        analyseViewModel.getVMin().observe(getViewLifecycleOwner(), i -> {
            binding.sbVMin.setProgress(i);
            binding.VMinData.setDefaultValue(i);
        });
        analyseViewModel.getVMax().observe(getViewLifecycleOwner(), i -> {
            binding.sbVMax.setProgress(i);
            binding.VMaxData.setDefaultValue(i);
        });

        analyseViewModel.getChooseMorphologyExMode().observe(getViewLifecycleOwner(), i -> {
            if (i == null) return;
            String[] item = {"腐蚀", "膨胀", "开操作", "闭操作", "梯度操作", "黑帽操作"};
            binding.morphologyExBtn.setText(item[i]);
        });
        analyseViewModel.getMorphologyExMode().observe(getViewLifecycleOwner(), b -> binding.morphologyExMode.setText(b ? "常规" : "递归"));
    }

    /**
     * 数字滚轮初始化
     */
    private void initPicker() {
        binding.HMinData.setRange(0, 180, 1);
        binding.HMinData.setTextSize(0);
        binding.HMinData.setCyclicEnabled(true);
        binding.HMinData.setIndicatorEnabled(false);
        binding.HMinData.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onWheelScrolled(WheelView view, int offset) {

            }

            @Override
            public void onWheelSelected(WheelView view, int position) {
                analyseViewModel.getHMin().setValue(position);
            }

            @Override
            public void onWheelScrollStateChanged(WheelView view, int state) {

            }

            @Override
            public void onWheelLoopFinished(WheelView view) {

            }
        });

        binding.HMaxData.setRange(0, 180, 1);
        binding.HMaxData.setTextSize(0);
        binding.HMaxData.setCyclicEnabled(true);
        binding.HMaxData.setIndicatorEnabled(false);
        binding.HMaxData.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onWheelScrolled(WheelView view, int offset) {

            }

            @Override
            public void onWheelSelected(WheelView view, int position) {
                analyseViewModel.getHMax().setValue(position);
            }

            @Override
            public void onWheelScrollStateChanged(WheelView view, int state) {

            }

            @Override
            public void onWheelLoopFinished(WheelView view) {

            }
        });

        binding.SMinData.setRange(0, 255, 1);
        binding.SMinData.setTextSize(0);
        binding.SMinData.setCyclicEnabled(true);
        binding.SMinData.setIndicatorEnabled(false);
        binding.SMinData.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onWheelScrolled(WheelView view, int offset) {

            }

            @Override
            public void onWheelSelected(WheelView view, int position) {
                analyseViewModel.getSMin().setValue(position);
            }

            @Override
            public void onWheelScrollStateChanged(WheelView view, int state) {

            }

            @Override
            public void onWheelLoopFinished(WheelView view) {

            }
        });

        binding.SMaxData.setRange(0, 255, 1);
        binding.SMaxData.setTextSize(0);
        binding.SMaxData.setCyclicEnabled(true);
        binding.SMaxData.setIndicatorEnabled(false);
        binding.SMaxData.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onWheelScrolled(WheelView view, int offset) {

            }

            @Override
            public void onWheelSelected(WheelView view, int position) {
                analyseViewModel.getSMax().setValue(position);
            }

            @Override
            public void onWheelScrollStateChanged(WheelView view, int state) {

            }

            @Override
            public void onWheelLoopFinished(WheelView view) {

            }
        });

        binding.VMinData.setRange(0, 255, 1);
        binding.VMinData.setTextSize(0);
        binding.VMinData.setCyclicEnabled(true);
        binding.VMinData.setIndicatorEnabled(false);
        binding.VMinData.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onWheelScrolled(WheelView view, int offset) {

            }

            @Override
            public void onWheelSelected(WheelView view, int position) {
                analyseViewModel.getVMin().setValue(position);
            }

            @Override
            public void onWheelScrollStateChanged(WheelView view, int state) {

            }

            @Override
            public void onWheelLoopFinished(WheelView view) {

            }
        });

        binding.VMaxData.setRange(0, 255, 1);
        binding.VMaxData.setTextSize(0);
        binding.VMaxData.setCyclicEnabled(true);
        binding.VMaxData.setIndicatorEnabled(false);
        binding.VMaxData.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onWheelScrolled(WheelView view, int offset) {

            }

            @Override
            public void onWheelSelected(WheelView view, int position) {
                analyseViewModel.getVMax().setValue(position);
            }

            @Override
            public void onWheelScrollStateChanged(WheelView view, int state) {

            }

            @Override
            public void onWheelLoopFinished(WheelView view) {

            }
        });
    }

    /**
     * 重置HSV参数
     */
    private void RestoreHSV() {
        int[] para = TFTAutoCutter.getOriginPara();
        analyseViewModel.getHMin().setValue(para[1]);
        analyseViewModel.getHMax().setValue(para[2]);
        analyseViewModel.getSMin().setValue(para[3]);
        analyseViewModel.getSMax().setValue(para[4]);
        analyseViewModel.getVMin().setValue(para[5]);
        analyseViewModel.getVMax().setValue(para[6]);
    }

    /**
     * 导入参数到裁剪
     */
    private void importHSV() {
        int[] HSVPara = new int[7];
        HSVPara[1] = analyseViewModel.getHMin().getValue() != null ? analyseViewModel.getHMin().getValue() : 0;
        HSVPara[2] = analyseViewModel.getHMax().getValue() != null ? analyseViewModel.getHMax().getValue() : 0;
        HSVPara[3] = analyseViewModel.getSMin().getValue() != null ? analyseViewModel.getSMin().getValue() : 0;
        HSVPara[4] = analyseViewModel.getSMax().getValue() != null ? analyseViewModel.getSMax().getValue() : 0;
        HSVPara[5] = analyseViewModel.getVMin().getValue() != null ? analyseViewModel.getVMin().getValue() : 0;
        HSVPara[6] = analyseViewModel.getVMax().getValue() != null ? analyseViewModel.getVMax().getValue() : 0;
        TFTAutoCutter.setCutterPara(HSVPara);
    }

    /**
     * 导出裁剪参数
     */
    private void exportHSV() {
        int[] para = TFTAutoCutter.getCutterPara();
        analyseViewModel.getHMin().setValue(para[1]);
        analyseViewModel.getHMax().setValue(para[2]);
        analyseViewModel.getSMin().setValue(para[3]);
        analyseViewModel.getSMax().setValue(para[4]);
        analyseViewModel.getVMin().setValue(para[5]);
        analyseViewModel.getVMax().setValue(para[6]);
    }
}
