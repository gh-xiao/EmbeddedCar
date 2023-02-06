package com.xiao.embeddedcar.ViewAdapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiao.embeddedcar.R;

import org.json.JSONArray;
import org.json.JSONObject;

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
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
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

        private ViewHolder() {
        }
    }

    private void loadImages() {
        list.clear();
        getImages(list);
        Log.i("list.size(): ", String.valueOf(list.size()));
    }

    private void getImages(List<String> ImgNameList) {
        File[] files;
        File file = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Tess/");
        if (file.exists() && (files = file.listFiles()) != null) {
            for (File value : files) if (value.isFile()) ImgNameList.add(value.getName());
        }
    }

    public JSONArray getAllFiles(String dirPath, String _type) {
        File[] files;
        JSONArray fileList = new JSONArray();
        File f = new File(String.valueOf(mContext.getExternalFilesDir(dirPath)));
        if (!f.exists() || (files = f.listFiles()) == null) {
            return null;
        }
        for (File _file : files) {
            if (_file.isFile() && _file.getName().endsWith(_type)) {
                String filePath = _file.getAbsolutePath();
                String fileName = _file.getName();
                Log.e("LOGCAT", "fileName:" + fileName);
                Log.e("LOGCAT", "filePath:" + filePath);
                try {
                    JSONObject _fInfo = new JSONObject();
                    _fInfo.put("name", fileName);
                    _fInfo.put("path", filePath);
                    fileList.put(_fInfo);
                } catch (Exception ignored) {
                }
            } else if (_file.isDirectory()) getAllFiles(_file.getAbsolutePath(), _type);
        }
        return fileList;
    }
}
