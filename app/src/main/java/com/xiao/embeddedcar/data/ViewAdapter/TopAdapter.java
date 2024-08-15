package com.xiao.embeddedcar.data.ViewAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.xiao.embeddedcar.DataProcessingModule.ConnectTransport;
import com.xiao.embeddedcar.R;
import com.xiao.embeddedcar.data.ViewModel.ControlViewModel;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 右侧主界面ListView的适配器
 *
 * @author Administrator
 */
public class TopAdapter extends BaseAdapter {

    private final Context mContext;
    private final ControlViewModel vm;
    private int selectItem = 0;
    private List<String> topData;
    //传输数据
    private short[] data;

    /**
     * 构造器
     *
     * @param context Context
     * @param topData 一级分类
     */
    public TopAdapter(Context context, List<String> topData, ControlViewModel vm) {
        this.mContext = context;
        this.topData = topData;
        this.vm = vm;
    }

    private static class ViewHolder {
        private TextView topCate;
    }

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    public void setTopData(List<String> topData) {
        this.topData = topData;
    }

    @Override
    public int getCount() {
        return topData != null ? topData.size() : 10;
    }

    @Override
    public Object getItem(int position) {
        return topData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_top_cate, null);
            holder.topCate = convertView.findViewById(R.id.item_top_cate);
            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag();
        if (position == selectItem) {
            holder.topCate.setBackgroundColor(mContext.getResources().getColor(R.color.teal_700));
            holder.topCate.setTextColor(Color.WHITE);
        } else {
            holder.topCate.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
            holder.topCate.setTextColor(mContext.getResources().getColor(R.color.light_purple));
        }

        holder.topCate.setText(topData.get(position));
        return convertView;
    }

    /**
     * 点击的控件选项
     *
     * @param position 控件定位
     */
    public void showDialog(int[] position) {
        data = new short[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        /* 更新Adapter */
//        this.setSelectItem(0);
        this.notifyDataSetChanged();
        switch (position[0]) {
            case 1://道闸标志物
                switch (position[1]) {
                    case 1:
                        ConnectTransport.getInstance().gate(0x01, 0x01, 0x00, 0x00);
                        vm.getShowMsg().setValue("打开道闸标志物");
                        break;
                    case 2:
                        ConnectTransport.getInstance().gate(0x01, 0x02, 0x00, 0x00);
                        vm.getShowMsg().setValue("关闭道闸标志物");
                        break;
                    case 3://道闸显示车牌
                        gate_plate_number();
                        break;
                    case 4://道闸初始角度调节
                        gate_angle_number();
                        break;
                    case 5:
                        ConnectTransport.getInstance().gate(0x20, 0x01, 0x00, 0x00);
                        vm.getShowMsg().setValue("请求返回道闸标志物状态");
                        break;
                }
                break;
            case 2://LED显示标志物
                switch (position[1]) {
                    case 1://数码管显示指定数据
                        digitalController();
                        break;
                    case 2://数码管显示计时模式
                        digital_time();
                        break;
                    case 3://数码管显示距离模式
                        digital_dis();
                        break;
                }
                break;
            case 3://语音播报标志物
                if (position[1] == 1) {
                    ConnectTransport.getInstance().VoiceBroadcast();
                    vm.getShowMsg().setValue("控制语音播报标志物随机播报指令");
                }
                //语音播报指定内容
                else voiceSendContent();
                break;
            case 4://无线充电标志物
                if (position[1] == 1) {
                    ConnectTransport.getInstance().magnetic_suspension(0x01, 0x01, 0x00, 0x00);
                    vm.getShowMsg().setValue("开启无线充电标志物");
                }
                break;
            case 5:
            case 6://智能TFT显示标志物
                boolean TFT = position[0] == 5;
                switch (position[1]) {
                    case 1://图片显示模式
                        TFT_Image(TFT);
                        break;
                    case 2://车牌显示模式
                        TFT_plate_number(TFT);
                        break;
                    case 3://计时模式
                        TFT_Timer(TFT);
                        break;
                    case 4://距离显示模式
                        Distance(TFT);
                        break;
                    case 5://HEX显示模式
                        Hex_show(TFT);
                        break;
                    case 6://交通标志显示模式
                        TFT_traffic(TFT);
                        break;
                }
                break;
            case 7:
            case 8://智能交通灯标志物
                int traffic_type = position[0] == 7 ? 0x0E : 0x0F;
                if (position[1] == 1)
                    ConnectTransport.getInstance().traffic_control(traffic_type, 0x01, 0x00);
                if (position[1] != 0)
                    ConnectTransport.getInstance().traffic_control(traffic_type, 0x02, position[1] - 1);
                vm.getShowMsg().setValue("已向交通灯" + (position[0] == 7 ? "A" : "B") + "发送指令");
                break;
            case 9:
            case 10://立体车库标志物
                int garage_type = position[0] == 9 ? 0x0D : 0x05;
                if (0 < position[1] && position[1] < 5) {
                    ConnectTransport.getInstance().garage_control(garage_type, 0x01, position[1]);
                    vm.getShowMsg().setValue("已请求立体车库" + (position[0] == 9 ? "A" : "B") + "运行到" + position[1] + "层");
                }
                if (position[1] == 5) {
                    ConnectTransport.getInstance().garage_control(garage_type, 0x02, 0x01);
                    vm.getShowMsg().setValue("已请求立体车库" + (position[0] == 9 ? "A" : "B") + "返回当前层数");
                }
                if (position[1] == 6) {
                    ConnectTransport.getInstance().garage_control(garage_type, 0x02, 0x02);
                    vm.getShowMsg().setValue("已请求立体车库" + (position[0] == 9 ? "A" : "B") + "返回前/后侧红外状态");
                }
                break;
            case 11://ETC系统标志物
                etc_SteeringEngine_Adjust(position[1] == 1 ? 0 : 1);
                break;
            case 12://烽火台报警标志物
                if (position[1] == 1) {
                    ConnectTransport.getInstance().infrared((byte) 0x03, (byte) 0x05, (byte) 0x14,
                            (byte) 0x45, (byte) 0xDE, (byte) 0x92);
                    vm.getShowMsg().setValue("开启烽火台");
                } else {
                    ConnectTransport.getInstance().infrared((byte) 0x67, (byte) 0x34, (byte) 0x78,
                            (byte) 0xA2, (byte) 0xFD, (byte) 0x27);
                    vm.getShowMsg().setValue("关闭烽火台");
                }
                break;
            case 13://智能路灯标志物
                ConnectTransport.getInstance().gear(position[1]);
                vm.getShowMsg().setValue("智能路灯增加挡位: " + position[1]);
                break;
            case 14://立体显示标志物
                switch (position[1]) {
                    case 1://颜色信息显示模式
                        color();
                        break;
                    case 2://图形信息显示模式
                        shape();
                        break;
                    case 3://距离信息显示模式
                        dis();
                        break;
                    case 4://车牌信息显示模式
                        lic();
                        break;
                    case 5://交通警示牌信息显示模式
                        road();
                        break;
                    case 6://交通标志信息显示模式
                        traffic_flag();
                        break;
                    case 7://显示默认信息
                        data[0] = 0x16;
                        data[1] = 0x01;
                        ConnectTransport.getInstance().infrared_stereo(data);
                        vm.getShowMsg().setValue("已设置立体显示标志物显示默认信息");
                        break;
                    case 8://设置文字显示颜色
                        textColorSet();
                        break;
                }
                break;
        }
    }

    /**
     * 道闸显示车牌
     */
    private void gate_plate_number() {
        AlertDialog.Builder gate_plate_builder = new AlertDialog.Builder(mContext);
        gate_plate_builder.setTitle("道闸显示车牌");
        final String[] gate_Image_item = {"A123B4", "B567C8", "D910E1"};
        gate_plate_builder.setSingleChoiceItems(gate_Image_item, -1, (dialog, which) -> {
            switch (which) {
                case 0:
                    ConnectTransport.getInstance().gate(0x10, 'A', '1', '2');
                    ConnectTransport.getInstance().YanChi(500);
                    ConnectTransport.getInstance().gate(0x11, '3', 'B', '4');
                    break;
                case 1:
                    ConnectTransport.getInstance().gate(0x10, 'B', '5', '6');
                    ConnectTransport.getInstance().YanChi(500);
                    ConnectTransport.getInstance().gate(0x11, '7', 'C', '8');
                    break;
                case 2:
                    ConnectTransport.getInstance().gate(0x10, 'D', '9', '1');
                    ConnectTransport.getInstance().YanChi(500);
                    ConnectTransport.getInstance().gate(0x11, '0', 'E', '1');
                    break;
            }
            vm.getShowMsg().setValue("已设置道闸车牌: " + gate_Image_item[which]);
        });
        gate_plate_builder.create().show();
    }

    /**
     * 道闸初始角度调节
     */
    private void gate_angle_number() {
        AlertDialog.Builder gate_plate_builder = new AlertDialog.Builder(mContext);
        gate_plate_builder.setTitle("道闸初始角度调节");
        final String[] gate_Image_item = {"上升", "下降"};
        gate_plate_builder.setSingleChoiceItems(gate_Image_item, -1, (dialog, which) -> {
            ConnectTransport.getInstance().gate(0x09, which == 0 ? 0x01 : 0x02, 0, 0);
            vm.getShowMsg().setValue("已设置道闸初始角度: " + gate_Image_item[which]);
        });
        gate_plate_builder.create().show();
    }

    private int main, one, two, three;

    /**
     * 数码管显示指定数据
     */
    private void digitalController() {
        AlertDialog.Builder dg_Builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_digital, null);
        dg_Builder.setTitle("数码管显示指定数据");
        String[] items = {"第一行", "第二行"};
        dg_Builder.setView(view);
        //下拉列表
        Spinner spinner = view.findViewById(R.id.spinner);
        final EditText editText1 = view.findViewById(R.id.editText1);
        final EditText editText2 = view.findViewById(R.id.editText2);
        final EditText editText3 = view.findViewById(R.id.editText3);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, items);
        spinner.setAdapter(adapter);
        //下拉列表选择监听
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                main = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        dg_Builder.setPositiveButton("确定", (dialog, which) -> {
            String ones = editText1.getText().toString();
            String twos = editText2.getText().toString();
            String threes = editText3.getText().toString();
            //显示数据，一个文本编译框最多两个数据显示数目管中两个数据
            one = ones.isEmpty() ? 0x00 : Integer.parseInt(ones, 16);
            two = twos.isEmpty() ? 0x00 : Integer.parseInt(twos, 16);
            three = threes.isEmpty() ? 0x00 : Integer.parseInt(threes, 16);
            ConnectTransport.getInstance().digital(main, one, two, three);
            vm.getShowMsg().setValue("已设置数码管" + items[main - 1] + "显示指定数据: " + ones + twos + threes);
        });
        dg_Builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        dg_Builder.create().show();
    }

    /**
     * 数码管显示计时模式
     */
    private void digital_time() {
        AlertDialog.Builder dg_timeBuilder = new AlertDialog.Builder(mContext);
        dg_timeBuilder.setTitle("数码管显示计时模式");
        String[] dgtime_item = {"计时结束", "计时开始", "清零"};
        dg_timeBuilder.setSingleChoiceItems(dgtime_item, -1, (dialog, which) -> {
            switch (which) {
                case 0://计时结束
                    ConnectTransport.getInstance().digital_close();
                    break;
                case 1://计时开启
                    ConnectTransport.getInstance().digital_open();
                    break;
                case 2://计时清零
                    ConnectTransport.getInstance().digital_clear();
                    break;
            }
            vm.getShowMsg().setValue("已设置数码管计时模式: " + dgtime_item[which]);
        });
        dg_timeBuilder.create().show();
    }

    /**
     * 数码管显示距离模式
     */
    private void digital_dis() {
        AlertDialog.Builder dis_timeBuilder = new AlertDialog.Builder(mContext);
        dis_timeBuilder.setTitle("数码管显示距离模式");
        final String[] dis_item = {"100mm", "200mm", "400mm"};
        dis_timeBuilder.setSingleChoiceItems(dis_item, -1, (dialog, which) -> {
            ConnectTransport.getInstance().digital_dic(Integer.parseInt(dis_item[which].substring(0, 3)));
            vm.getShowMsg().setValue("已设置数码管显示距离: " + dis_item[which]);
        });
        dis_timeBuilder.create().show();
    }

    /**
     * 语音播报指定内容
     */
    private void voiceSendContent() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_car, null);
        TextView voiceText = view.findViewById(R.id.voiceText);
        AlertDialog.Builder voiceBuilder = new AlertDialog.Builder(mContext);
        voiceBuilder.setTitle("语音播报标志物");
        voiceBuilder.setView(view);
        voiceBuilder.setPositiveButton("播报", (dialog1, which1) -> {
            String src = voiceText.getText().toString();
            if (src.isEmpty()) src = "请输入你要播报的内容";
            try {
                byte[] sbyte = byteSend(src.getBytes("GBK"));
                ConnectTransport.getInstance().send_voice(sbyte);
                vm.getShowMsg().setValue("已设置指定播报内容: " + src);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            dialog1.cancel();
        });
        voiceBuilder.setNegativeButton("取消", null);
        voiceBuilder.create().show();
    }

    /**
     * TFT显示标志物 - 图片显示模式
     *
     * @param b A/B标志物控制指令
     */
    private void TFT_Image(boolean b) {
        int type = b ? 0x0B : 0x08;
        AlertDialog.Builder TFT_Image_builder = new AlertDialog.Builder(mContext);
        TFT_Image_builder.setTitle("图片显示模式");
        String[] TFT_Image_item = {"指定显示", "上翻一页", "下翻一页", "自动翻页"};
        TFT_Image_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            if (which != 0) {
                ConnectTransport.getInstance().TFT_LCD(type, 0x10, which, 0x00, 0x00);
                vm.getShowMsg().setValue("发送TFT指令: " + TFT_Image_item[which]);
            } else TFT_show(type);

        });
        TFT_Image_builder.create().show();
    }

    /**
     * TFT显示标志物 - 指定图片显示
     *
     * @param type A/B标志物控制指令
     */
    private void TFT_show(int type) {
        AlertDialog.Builder TFT_Image_builder = new AlertDialog.Builder(mContext);
        TFT_Image_builder.setTitle("指定图片显示");
        String[] TFT_Image_item = {"1", "2", "3", "4", "5"};
        TFT_Image_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            ConnectTransport.getInstance().TFT_LCD(type, 0x10, which + 0x01, 0x00, 0x00);
            vm.getShowMsg().setValue("指定TFT显示图片: " + TFT_Image_item[which]);
        });
        TFT_Image_builder.create().show();
    }

    /**
     * TFT显示标志物 - 车牌显示模式
     *
     * @param b A/B标志物控制指令
     */
    private void TFT_plate_number(boolean b) {
        int type = b ? 0x0B : 0x08;
        AlertDialog.Builder TFT_plate_builder = new AlertDialog.Builder(mContext);
        TFT_plate_builder.setTitle("车牌显示模式");
        final String[] TFT_Image_item = {"Z799C4", "B554H1", "D888B8"};
        TFT_plate_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            switch (which) {
                case 0:
                    ConnectTransport.getInstance().TFT_LCD(type, 0x20, 'Z', '7', '9');
                    ConnectTransport.getInstance().YanChi(500);
                    ConnectTransport.getInstance().TFT_LCD(type, 0x21, '9', 'C', '4');
                    break;
                case 1:
                    ConnectTransport.getInstance().TFT_LCD(type, 0x20, 'B', '5', '5');
                    ConnectTransport.getInstance().YanChi(500);
                    ConnectTransport.getInstance().TFT_LCD(type, 0x21, '4', 'H', '1');
                    break;
                case 2:
                    ConnectTransport.getInstance().TFT_LCD(type, 0x20, 'D', '8', '8');
                    ConnectTransport.getInstance().YanChi(500);
                    ConnectTransport.getInstance().TFT_LCD(type, 0x21, '8', 'B', '8');
                    break;
            }
            vm.getShowMsg().setValue("已设置TFT显示车牌: " + TFT_Image_item[which]);
        });
        TFT_plate_builder.create().show();
    }

    /**
     * TFT显示标志物 - 计时模式
     *
     * @param b A/B标志物控制指令
     */
    private void TFT_Timer(boolean b) {
        int type = b ? 0x0B : 0x08;
        AlertDialog.Builder TFT_Timer_builder = new AlertDialog.Builder(mContext);
        TFT_Timer_builder.setTitle("计时模式");
        String[] TFT_Image_item = {"开始", "关闭", "停止"};
        TFT_Timer_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            switch (which) {
                case 0:
                    ConnectTransport.getInstance().TFT_LCD(type, 0x30, 0x01, 0x00, 0x00);
                    break;
                case 1:
                    ConnectTransport.getInstance().TFT_LCD(type, 0x30, 0x02, 0x00, 0x00);
                    break;
                case 2:
                    ConnectTransport.getInstance().TFT_LCD(type, 0x30, 0x00, 0x00, 0x00);
                    break;
            }
            vm.getShowMsg().setValue("已设置TFT计时模式: " + TFT_Image_item[which]);
        });
        TFT_Timer_builder.create().show();
    }

    /**
     * TFT显示标志物 - 距离显示模式
     *
     * @param b A/B标志物控制指令
     */
    private void Distance(boolean b) {
        int type = b ? 0x0B : 0x08;
        AlertDialog.Builder TFT_Distance_builder = new AlertDialog.Builder(mContext);
        TFT_Distance_builder.setTitle("距离显示模式");
        String[] TFT_Image_item = {"400mm", "500mm", "600mm"};
        TFT_Distance_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            ConnectTransport.getInstance().TFT_LCD(type, 0x50, 0x00, which + 0x04, 0x00);
            vm.getShowMsg().setValue("已设置TFT显示距离: " + TFT_Image_item[which]);
        });
        TFT_Distance_builder.create().show();
    }

    /**
     * TFT显示标志物 - HEX显示模式
     *
     * @param b A/B标志物控制指令
     */
    private void Hex_show(boolean b) {
        int type = b ? 0x0B : 0x08;
        AlertDialog.Builder TFT_Hex_builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_hex, null);
        TFT_Hex_builder.setTitle("HEX显示模式");
        TFT_Hex_builder.setView(view);
        //下拉列表
        final EditText editText1 = view.findViewById(R.id.editText1);
        final EditText editText2 = view.findViewById(R.id.editText2);
        final EditText editText3 = view.findViewById(R.id.editText3);
        TFT_Hex_builder.setPositiveButton("确定", (dialog, which) -> {
            String ones = editText1.getText().toString();
            String twos = editText2.getText().toString();
            String threes = editText3.getText().toString();
            //显示数据，一个文本编译框最多两个数据显示数目管中两个数据
            one = ones.isEmpty() ? 0x00 : Integer.parseInt(ones, 16);
            two = twos.isEmpty() ? 0x00 : Integer.parseInt(twos, 16);
            three = threes.isEmpty() ? 0x00 : Integer.parseInt(threes, 16);
            ConnectTransport.getInstance().TFT_LCD(type, 0x40, one, two, three);
            vm.getShowMsg().setValue("已设置TFT显示hex: " + ones + twos + threes);
        });
        TFT_Hex_builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        TFT_Hex_builder.create().show();
    }

    /**
     * TFT显示标志物 - 交通标志显示模式
     *
     * @param b A/B标志物控制指令
     */
    private void TFT_traffic(boolean b) {
        int type = b ? 0x0B : 0x08;
        AlertDialog.Builder TFT_Items_builder = new AlertDialog.Builder(mContext);
        TFT_Items_builder.setTitle(b ? "TFT-A 交通标志显示模式" : "TFT-B 交通标志显示模式");
        String[] TFT_Image_item = {"直行", "左转", "右转", "掉头", "禁止直行", "禁止通行"};
        TFT_Items_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            ConnectTransport.getInstance().TFT_LCD(type, 0x60, which + 0x01, 0x00, 0x00);
            vm.getShowMsg().setValue("已设置TFT显示交通标志物: " + TFT_Image_item[which]);
        });
        TFT_Items_builder.create().show();
    }

    /**
     * ETC系统标志物舵机初始角度调节
     *
     * @param rudder 选择舵机，0为左侧，1为右侧
     */
    private void etc_SteeringEngine_Adjust(int rudder) {
        AlertDialog.Builder garage_builder = new AlertDialog.Builder(mContext);
        String[] ga = {"上升", "下降"};
        garage_builder.setTitle(rudder != 0 ? "右侧舵机" : "左侧舵机");
        garage_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            if (i == 0) {
                if (rudder != 0) ConnectTransport.getInstance().rudder_control(0x00, 0x01);
                else ConnectTransport.getInstance().rudder_control(0x01, 0x00);
                vm.getShowMsg().setValue("已设置ETC" + (rudder != 0 ? "右侧舵机" : "左侧舵机") + "上升");
            } else {
                if (rudder != 0) ConnectTransport.getInstance().rudder_control(0x00, 0x02);
                else ConnectTransport.getInstance().rudder_control(0x02, 0x00);
                vm.getShowMsg().setValue("已设置ETC" + (rudder != 0 ? "右侧舵机" : "左侧舵机") + "下降");
            }
        });
        garage_builder.create().show();
    }

    /**
     * 立体显示标志物 - 颜色信息显示模式
     */
    private void color() {
        AlertDialog.Builder colorBuilder = new AlertDialog.Builder(mContext);
        colorBuilder.setTitle("颜色信息显示模式");
        String[] lg_item = {"红色", "绿色", "蓝色", "黄色", "品色", "青色", "黑色", "白色"};
        colorBuilder.setSingleChoiceItems(lg_item, -1, (dialog, which) -> {
            data[0] = 0x13;
            data[1] = (short) (which + 0x01);
            ConnectTransport.getInstance().infrared_stereo(data);
            vm.getShowMsg().setValue("已设置立体显示标志物显示颜色: " + lg_item[which]);
        });
        colorBuilder.create().show();
    }

    /**
     * 立体显示标志物 - 图形信息显示模式
     */
    private void shape() {
        AlertDialog.Builder shapeBuilder = new AlertDialog.Builder(mContext);
        shapeBuilder.setTitle("图形信息显示模式");
        String[] shape_item = {"矩形", "圆形", "三角形", "菱形", "五角星"};
        shapeBuilder.setSingleChoiceItems(shape_item, -1, (dialog, which) -> {
            data[0] = 0x12;
            data[1] = (short) (which + 0x01);
            ConnectTransport.getInstance().infrared_stereo(data);
            vm.getShowMsg().setValue("已设置立体显示标志物显示图形: " + shape_item[which]);
        });
        shapeBuilder.create().show();
    }

    /**
     * 立体显示标志物 - 距离信息显示模式
     */
    private void dis() {
        AlertDialog.Builder disBuilder = new AlertDialog.Builder(mContext);
        disBuilder.setTitle("距离信息显示模式");
        final String[] road_item = {"10cm", "15cm", "20cm", "28cm", "39cm"};
        disBuilder.setSingleChoiceItems(road_item, -1, (dialog, which) -> {
            int disNum = Integer.parseInt(road_item[which].substring(0, 2));
            data[0] = 0x11;
            data[1] = (short) (disNum / 10 + 0x30);
            data[2] = (short) (disNum % 10 + 0x30);
            ConnectTransport.getInstance().infrared_stereo(data);
            vm.getShowMsg().setValue("已设置立体显示标志物显示距离: " + road_item[which]);
        });
        disBuilder.create().show();
    }

    /**
     * 立体显示标志物 - 车牌信息显示模式
     */
    private void lic() {
        AlertDialog.Builder licBuilder = new AlertDialog.Builder(mContext);
        licBuilder.setTitle("车牌信息显示模式");
        String[] lic_item = {"N300Y7A4", "N600H5B4", "N400Y6G6", "J888B8C8"};
        licBuilder.setSingleChoiceItems(lic_item, -1, (dialog, which) -> {
            short[] li = StringToBytes(lic_item[which]);
            data[0] = 0x20;
            data[1] = li[0];
            data[2] = li[1];
            data[3] = li[2];
            data[4] = li[3];
            ConnectTransport.getInstance().infrared_stereo(data);
            data[0] = 0x10;
            data[1] = li[4];
            data[2] = li[5];
            data[3] = li[6];
            data[4] = li[7];
            ConnectTransport.getInstance().infrared_stereo(data);
            vm.getShowMsg().setValue("已设置立体显示标志物显示车牌: " + lic_item[which]);
        });
        licBuilder.create().show();
    }

    /**
     * 立体显示标志物 - 交通警示牌信息显示模式
     */
    private void road() {
        AlertDialog.Builder roadBuilder = new AlertDialog.Builder(mContext);
        roadBuilder.setTitle("交通警示牌信息显示模式");
        String[] road_item = {"前方学校 减速慢行", "前方施工 禁止通行", "塌方路段 注意安全", "追尾危险 保持车距", "严禁 酒后驾车", "严禁 乱扔垃圾"};
        roadBuilder.setSingleChoiceItems(road_item, -1, (dialog, which) -> {
            data[0] = 0x14;
            data[1] = (short) (which + 0x01);
            ConnectTransport.getInstance().infrared_stereo(data);
            vm.getShowMsg().setValue("已设置立体显示标志物显示交通警示牌信息: " + road_item[which]);
        });
        roadBuilder.create().show();
    }

    /**
     * 立体显示标志物 - 交通标志信息显示模式
     */
    private void traffic_flag() {
        AlertDialog.Builder roadBuilder = new AlertDialog.Builder(mContext);
        roadBuilder.setTitle("交通标志信息显示模式");
        String[] road_item = {"直行", "左转", "右转", "掉头", "禁止直行", "禁止通行"};
        roadBuilder.setSingleChoiceItems(road_item, -1, (dialog, which) -> {
            data[0] = 0x15;
            data[1] = (short) (which + 0x01);
            ConnectTransport.getInstance().infrared_stereo(data);
            vm.getShowMsg().setValue("已设置立体显示标志物显示交通标志信息: " + road_item[which]);
        });
        roadBuilder.create().show();
    }

    /**
     * 立体显示标志物 - 设置文字显示颜色
     */
    private void textColorSet() {
        AlertDialog.Builder roadBuilder = new AlertDialog.Builder(mContext);
        roadBuilder.setTitle("设置文字显示颜色");
        String[] color_item = {"中国红", "绿色", "蓝色", "自定义颜色"};
        roadBuilder.setSingleChoiceItems(color_item, -1, (dialog, which) -> {
            data[0] = 0x17;
            data[1] = 0x01;
            switch (which) {
                case 0: //红
                    data[2] = 0xC8;
                    data[3] = 0x10;
                    data[4] = 0x2E;
                    break;
                case 1: //绿
                    data[2] = 0x00;
                    data[3] = 0xff;
                    data[4] = 0x00;
                    break;
                case 2: //蓝
                    data[2] = 0x00;
                    data[3] = 0x00;
                    data[4] = 0xff;
                    break;
                case 3:
                    customColorSend();
                    break;
            }
            ConnectTransport.getInstance().infrared_stereo(data);
            vm.getShowMsg().setValue("已设置立体显示标志物文字颜色: " + color_item[which]);
        });
        roadBuilder.create().show();
    }

    @SuppressLint("SetTextI18n")
    private void customColorSend() {
        AlertDialog.Builder TFT_Hex_builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_hex, null);
        TFT_Hex_builder.setTitle("自定义文字颜色");
        TFT_Hex_builder.setView(view);
        // 下拉列表
        final EditText editText1 = view.findViewById(R.id.editText1);
        final EditText editText2 = view.findViewById(R.id.editText2);
        final EditText editText3 = view.findViewById(R.id.editText3);
        editText1.setText("FF");
        editText2.setText("00");
        editText3.setText("FF");
        data[0] = 0x17;
        data[1] = 0x01;
        TFT_Hex_builder.setPositiveButton("确定", (dialog, which) -> {
            String ones = editText1.getText().toString();
            String twos = editText2.getText().toString();
            String threes = editText3.getText().toString();
            //显示数据，一个文本编译框最多两个数据显示数目管中两个数据
            data[2] = (short) (ones.isEmpty() ? 0x00 : Integer.parseInt(ones, 16));
            data[3] = (short) (twos.isEmpty() ? 0x00 : Integer.parseInt(twos, 16));
            data[4] = (short) (threes.isEmpty() ? 0x00 : Integer.parseInt(threes, 16));
            ConnectTransport.getInstance().infrared_stereo(data);
            vm.getShowMsg().setValue(ones + twos + threes);
        });
        TFT_Hex_builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        TFT_Hex_builder.create().show();
    }

    private byte[] byteSend(byte[] sByte) {
        byte[] textByte = new byte[sByte.length + 5];
        textByte[0] = (byte) 0xFD;
        textByte[1] = (byte) (((sByte.length + 2) >> 8) & 0xff);
        textByte[2] = (byte) ((sByte.length + 2) & 0xff);
        //合成语音命令
        textByte[3] = 0x01;
        //编码格式
        textByte[4] = (byte) 0x01;
        System.arraycopy(sByte, 0, textByte, 5, sByte.length);
        return textByte;
    }

    /**
     * 从string中得到short数据数组
     *
     * @param licString 车牌字符串
     * @return short数组的车牌字符串
     */
    private short[] StringToBytes(String licString) {
        if (licString == null || licString.isEmpty()) return null;
        licString = licString.toUpperCase();
        int length = licString.length();
        char[] hexChars = licString.toCharArray();
        short[] d = new short[length];
        for (int i = 0; i < length; i++) d[i] = (short) hexChars[i];
        return d;
    }
}
