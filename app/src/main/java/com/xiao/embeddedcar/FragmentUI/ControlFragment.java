package com.xiao.embeddedcar.FragmentUI;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xiao.embeddedcar.R;
import com.xiao.embeddedcar.ViewAdapter.MenuAdapter;
import com.xiao.embeddedcar.ViewAdapter.SecAdapter;
import com.xiao.embeddedcar.ViewAdapter.TopAdapter;
import com.xiao.embeddedcar.ViewModel.ControlViewModel;
import com.xiao.embeddedcar.databinding.FragmentControlBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControlFragment extends AbstractFragment<FragmentControlBinding, ControlViewModel> {

    private FragmentControlBinding binding;
    private ControlViewModel controlViewModel;
    private final int[] position = new int[2];
    //菜单List
    private final List<String> menuList = new ArrayList<>();
    private static final String[] controlMenu = new String[]{
            "未选择", "道闸标志物", "LED显示标志物", "语音播报标志物", "无线充电标志物", "智能TFT显示标志物(A)", "智能TFT显示标志物(B)", "智能交通灯标志物(A)", "智能交通灯标志物(B)",
            "立体车库标志物(A)", "立体车库标志物(B)", "ETC系统标志物", "烽火台报警标志物", "智能路灯标志物", "立体显示标志物"};
    //一级分类List
    private final List<String> topList = new ArrayList<>();
    private static final String[][] TopItem = new String[][]{
            {"无内容",},
            {"未选择", "开启", "关闭", "车牌显示模式", "道闸初始角度调节", "请求返回道闸状态"},//道闸
            {"未选择", "数码管显示指定数据", "数码管显示计时模式", "数码管显示距离模式"},//LED
            {"未选择", "语音播报随机指令", "语音播报指定内容"},//语音
            {"未选择", "开"},//无线
            {"未选择", "图片显示模式", "车牌显示模式", "计时模式模式", "距离显示模式", "HEX显示模式", "交通标志显示模式"},//TFT - A
            {"未选择", "图片显示模式", "车牌显示模式", "计时模式模式", "距离显示模式", "HEX显示模式", "交通标志显示模式"},//TFT - B
            {"未选择", "进入识别模式", "识别结果为红色，请求确认", "识别结果为绿色，请求确认", "识别结果为黄色，请求确认"},//A
            {"未选择", "进入识别模式", "识别结果为红色，请求确认", "识别结果为绿色，请求确认", "识别结果为黄色，请求确认"},//B
            {"未选择", "复位（第一层）", "到达第二层", "到达第三层", "到达第四层", "请求返回立体车库当前层数", "请求返回立体车库前/后侧红外状态"},//A
            {"未选择", "复位（第一层）", "到达第二层", "到达第三层", "到达第四层", "请求返回立体车库当前层数", "请求返回立体车库前/后侧红外状态"},//B
            {"未选择", "左侧舵机调节", "右侧舵机调节"},//ETC
            {"未选择", "打开", "关闭"},//烽火台
            {"未选择", "光源挡位加一档", "光源挡位加二档", "光源挡位加三档"},//路灯
            {"未选择", "颜色信息显示模式", "图形信息显示模式", "距离信息显示模式", "车牌信息显示模式", "交通警示牌信息显示模式", "交通标志信息显示模式", "显示默认信息", "设置文字显示颜色"},//立体显示
    };
    //二级分类List
    private final List<String> secList = new ArrayList<>();

    public ControlFragment() {
        super(FragmentControlBinding::inflate, ControlViewModel.class, false);
    }

    @Override
    public void initFragment(@NonNull FragmentControlBinding binding, @Nullable ControlViewModel viewModel, @Nullable Bundle savedInstanceState) {
        this.binding = binding;
        this.controlViewModel = viewModel;
    }

    @Override
    public void init() {
        //设置TextView滚动
        binding.tvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
        /* 添加菜单选项 */
        menuList.addAll(Arrays.asList(controlMenu));
        /* 重置ViewModel */
        controlViewModel.getSelectImage().setValue(0);
        controlViewModel.getSelectName().setValue(menuList.get(0));
        /* 菜单适配器 */
        MenuAdapter menuAdapter = new MenuAdapter(getContext(), menuList);
        binding.leftMenu.setAdapter(menuAdapter);
        /* 一级分类适配器 */
        TopAdapter topAdapter = new TopAdapter(getContext(), topList, controlViewModel);
        binding.topCate.setAdapter(topAdapter);
        /* 二级分类适配器 */
        SecAdapter secAdapter = new SecAdapter(getContext(), secList);
        binding.secCate.setAdapter(secAdapter);
        /* ListView菜单项点击监听事件 */
        binding.leftMenu.setOnItemClickListener((parent, view, p, id) -> {
            /* ViewModel更新 */
            controlViewModel.getSelectImage().setValue(p);
            controlViewModel.getSelectName().setValue(menuList.get(p));
            /* 设置Adapter数据 */
            /* 菜单适配器 */
            position[0] = p;
            menuAdapter.setSelectItem(p);
            /* 一级列表适配器 */
            topAdapter.setSelectItem(0);
            topAdapter.setTopData(Arrays.asList(TopItem[p]));
            /* 更新Adapter */
            menuAdapter.notifyDataSetChanged();
            topAdapter.notifyDataSetChanged();
        });
        /* ListView一级分类项点击监听事件 */
        binding.topCate.setOnItemClickListener((parent, view, p, id) -> {
            /* 设置Adapter数据 */
            position[1] = p;
            topAdapter.setSelectItem(p);
            /* 设置点击事件 */
            topAdapter.showDialog(position);
        });
    }

    @Override
    public void observerDataStateUpdateAction() {
        controlViewModel.getShowMsg().setValue(null);
        controlViewModel.getSelectImage().observe(getViewLifecycleOwner(), i -> {
            ImageView iv = binding.ivControlChoose;
            switch (i) {
                case 1:
                    iv.setImageResource(R.mipmap.barrier_gate);
                    break;
                case 2:
                    iv.setImageResource(R.mipmap.nixie_tube);
                    break;
                case 3:
                    iv.setImageResource(R.mipmap.voice_broadcast);
                    break;
                case 4:
                    iv.setImageResource(R.mipmap.maglev);
                    break;
                case 5:
                case 6:
                    iv.setImageResource(R.mipmap.tft_lcd);
                    break;
                case 7:
                case 8:
                    iv.setImageResource(R.mipmap.traffic_light);
                    break;
                case 9:
                case 10:
                    iv.setImageResource(R.mipmap.cheku);
                    break;
                case 11:
                    iv.setImageResource(R.mipmap.etc_pic);
                    break;
                case 12:
                    iv.setImageResource(R.mipmap.alarm);
                    break;
                case 13:
                    iv.setImageResource(R.mipmap.gear_position);
                    break;
                case 14:
                    iv.setImageResource(R.mipmap.stereo_display);
                    break;
                default:
                    iv.setImageResource(R.mipmap.choose);
                    break;
            }
        });
        controlViewModel.getSelectName().observe(getViewLifecycleOwner(), s -> binding.tvControlChoose.setText(s));
        controlViewModel.getShowMsg().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.tvMsg.append(s + "\n");
        });
    }
}
