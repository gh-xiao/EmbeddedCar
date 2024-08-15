package com.xiao.embeddedcar.data.ViewAdapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiao.embeddedcar.R;

import java.util.List;

/**
 * 左侧菜单ListView的适配器
 *
 * @author Administrator
 */
public class MenuAdapter extends BaseAdapter {

    private final Context mContext;
    private int selectItem = 0;
    private final List<String> list;

    public MenuAdapter(Context context, List<String> list) {
        this.list = list;
        this.mContext = context;
    }

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        return list.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        /* 使用ViewHolder复用减少性能开销,以创建ListView */
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_menu, null);
            holder.tv_name = convertView.findViewById(R.id.item_menu_name);
            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag();
        /* 设置选择与未选择Item的样式 */
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
        private TextView tv_name;
    }
}
