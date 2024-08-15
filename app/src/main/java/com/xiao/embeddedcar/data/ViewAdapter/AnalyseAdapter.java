package com.xiao.embeddedcar.data.ViewAdapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiao.embeddedcar.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnalyseAdapter extends BaseAdapter {
    private final List<String> list = new ArrayList<>();
    private final Context mContext;
    private int selectItem = 0;

    public AnalyseAdapter(Context context) {
        this.mContext = context;
        loadImages();
    }

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
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
            convertView = View.inflate(mContext, R.layout.item_analyse, null);
            holder.tv_name = convertView.findViewById(R.id.item_analyse_name);
            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag();
        if (position == selectItem) {
            holder.tv_name.setBackgroundColor(mContext.getResources().getColor(R.color.teal_700));
            holder.tv_name.setTextColor(Color.WHITE);
        } else {
            holder.tv_name.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
            holder.tv_name.setTextColor(mContext.getResources().getColor(R.color.light_purple));
        }
        holder.tv_name.setText(list.get(position));
        return convertView;
    }

    private static class ViewHolder {
        public TextView tv_name;
    }

    /**
     * 加载图片名进入列表
     */
    private void loadImages() {
        list.clear();
        File[] files;
        File file = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Tess/");
        if (file.exists() && (files = file.listFiles()) != null) for (File pic : files) {
            if (pic.isFile()) list.add(pic.getName());
        }
    }
}
