package com.example.xianyang.libraryproject.books;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xianyang.libraryproject.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String,Object>> datalist;
    private List<Bitmap> bitmapList;
    public BookAdapter(Context context, List<Map<String,Object>> datalist, List<Bitmap> bitmapList){
        this.context=context;
        this.datalist=datalist;
        this.bitmapList=bitmapList;
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
            convertView=LayoutInflater.from(context).inflate(R.layout.book_item,null);
            viewHoder.imageView=convertView.findViewById(R.id.book_image);
            viewHoder.bookBrief=convertView.findViewById(R.id.bookBrief);
            viewHoder.bookWriter=convertView.findViewById(R.id.bookWriter);
            viewHoder.bookName=convertView.findViewById(R.id.bookName);
            convertView.setTag(viewHoder);
        }else
        {
            viewHoder= (ViewHoder) convertView.getTag();
        }
        viewHoder.imageView.setImageBitmap(bitmapList.get(position));
        viewHoder.bookName.setText((String) datalist.get(position).get("name"));
        viewHoder.bookWriter.setText((String)datalist.get(position).get("writer"));
        viewHoder.bookBrief.setText((String)datalist.get(position).get("brief"));
        return convertView;
    }
    class ViewHoder{
        ImageView imageView;
        TextView bookName;
        TextView bookWriter;
        TextView bookBrief;
    }
}
