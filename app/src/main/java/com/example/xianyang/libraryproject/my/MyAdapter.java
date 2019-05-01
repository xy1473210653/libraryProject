package com.example.xianyang.libraryproject.my;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.xianyang.libraryproject.R;
import com.example.xianyang.libraryproject.socket.User;

import java.util.List;

/**
 * 用户信息列表自定义adapter
 */
public class MyAdapter extends BaseAdapter {
    private Context context;
    private String[] list;

    public MyAdapter(Context context, String[] list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.length;
    }

    @Override
    public Object getItem(int position) {
        return list[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder viewHoder = null;
        if (viewHoder == null) {
            viewHoder = new ViewHoder();
            convertView = LayoutInflater.from(context).inflate(R.layout.mylist_item, null);
            viewHoder.textView=convertView.findViewById(R.id.mylist_item_text);
            convertView.setTag(viewHoder);
        }else {
            viewHoder= (ViewHoder) convertView.getTag();
        }
        viewHoder.textView.setText(list[position]);
        return convertView;
    }

    class ViewHoder{
        TextView textView;
    }
}
