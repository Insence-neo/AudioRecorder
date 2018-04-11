package com.insence.audiorecorder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.insence.audiorecorder.FileViewerItem;
import com.insence.audiorecorder.OptionItem;
import com.insence.audiorecorder.R;

import java.util.List;

/**
 * Created by Insence on 2018/4/9.
 */

public class FileViewerItemAdapter extends BaseAdapter {

    private List<FileViewerItem> items;
    private LayoutInflater inflater;
    private ImageView image;
    private TextView option;

    public FileViewerItemAdapter(List<FileViewerItem> items, Context context) {
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            view=inflater.inflate(R.layout.fileviewer_item,null);
            image= (ImageView) view.findViewById(R.id.image_track);
            option= (TextView) view.findViewById(R.id.tv_option);
        }
        image.setImageResource(items.get(i).getImageId());
        option.setText(items.get(i).getOption());
        return view;
    }
}
