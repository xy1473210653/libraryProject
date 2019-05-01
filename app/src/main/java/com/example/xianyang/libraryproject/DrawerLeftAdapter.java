package com.example.xianyang.libraryproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//抽屉listview的自定义adapter
public class DrawerLeftAdapter extends BaseAdapter {
    private List<Map<String,Object>> datalist ;
    private Context context;
    public DrawerLeftAdapter (Context context,List<Map<String,Object>> datalist){
        this.context=context;
        this.datalist=datalist;
    }
    @Override
    public int getCount() {
        return datalist.size();
    }

    @Override
    public Object getItem(int position) {
        return datalist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder viewHoder=null;
        if (viewHoder==null)
        {
            viewHoder=new ViewHoder();
            convertView=LayoutInflater.from(context).inflate(R.layout.left_item,null);
            viewHoder.imageView=convertView.findViewById(R.id.left_item_image);
            viewHoder.textView=convertView.findViewById(R.id.left_item_text);
            convertView.setTag(viewHoder);
        }else
        {
            viewHoder= (ViewHoder) convertView.getTag();
        }
        viewHoder.textView.setText((String)datalist.get(position).get("text"));
        viewHoder.imageView.setImageResource((Integer) datalist.get(position).get("image"));
        return convertView;
    }
    class ViewHoder{
        ImageView imageView;
        TextView textView;
    }
}
