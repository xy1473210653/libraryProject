package com.example.xianyang.libraryproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class LoseAdapter extends BaseAdapter {
    private List<Map<String,Object>> datalist ;
    private Context context;
    private List<Bitmap> bitmapList;
    private Thread thread;
    private ViewHoder finalViewHoder;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x00)
            {
                Toast.makeText(context,"申请领取成功",Toast.LENGTH_SHORT);
                finalViewHoder.isget.setText("已领取");
                finalViewHoder.isget.setTextColor(Color.GREEN);
            }
            else {
                if (msg.what==0x01)
                {
                    Toast.makeText(context,"申请领取失败",Toast.LENGTH_SHORT);
                }
            }
        }
    };
    public LoseAdapter(List<Map<String,Object>> datalist, Context context, List<Bitmap> bitmapList)
    {
        this.datalist=datalist;
        this.context=context;
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
        if(viewHoder==null)
        {
            viewHoder=new ViewHoder();
            convertView=LayoutInflater.from(context).inflate(R.layout.lose_item,null);
            viewHoder.isget=convertView.findViewById(R.id.lose_isget);
            viewHoder.text_place=convertView.findViewById(R.id.text_lose_seatID);
            viewHoder.lose_image=convertView.findViewById(R.id.lose_item_image);
            viewHoder.text_data=convertView.findViewById(R.id.lose_text_time);
            viewHoder.desc=convertView.findViewById(R.id.desc);
            convertView.setTag(viewHoder);
        }
        else {
            viewHoder= (ViewHoder) convertView.getTag();
        }
        viewHoder.lose_image.setImageBitmap(bitmapList.get(position));
        viewHoder.desc.setText((String) datalist.get(position).get("desc"));
        viewHoder.isget.setText((String) datalist.get(position).get("isget"));
        viewHoder.text_place.setText("失物在第"+(String)datalist.get(position).get("place")+"号格子"); ;
        viewHoder.text_data.setText ((String)datalist.get(position).get("data"));
         finalViewHoder = viewHoder;
        viewHoder.lose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,lose_image.class);
                intent.putExtra("logid",(String) datalist.get(position).get("logid"));
               context.startActivity(intent);
            }
        });
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog dialog=new AlertDialog.Builder(context)
                        .setMessage("申请第"+(position+1)+"个格子失物")
                        .setPositiveButton("取消",null)
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e("TAGjjjj", "run: "+"nihao" );
                                thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Socket socket = null;
                                        try {
                                            socket = new Socket(context.getResources().getString(R.string.service_ip), 8080);
                                            socket.setSoTimeout(10000);
                                            OutputStream os = socket.getOutputStream();
                                            JSONObject object = new JSONObject();
                                            object.put("id",context.getSharedPreferences("user",Context.MODE_PRIVATE).getString("id",null));
                                            object.put("aim", "get_article");
                                            object.put("logid",datalist.get(position).get("logid"));
                                            String result = object.toString();
                                            os.write(result.getBytes());
                                            os.flush();
                                            socket.shutdownOutput();

                                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                            String line = "";
                                            String res = "";
                                            while ((line = br.readLine()) != null) {
                                                res += line;
                                            }
                                            Log.e("TAGjjjj", "run: "+res );
                                            if (Boolean.parseBoolean(new JSONObject(res).getString("result")))
                                            {
                                                Message message = new Message();
                                                message.what = 0x00;
                                                handler.sendMessage(message);
                                            }else {
                                                Message message = new Message();
                                                message.what = 0x01;
                                                handler.sendMessage(message);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });
                                thread.start();
                            }
                        }).show();
                return false;
            }
        });

        return convertView;
    }
    class ViewHoder{
        ImageView lose_image;
        TextView isget;
        TextView text_place;
        TextView text_data;
        TextView desc;
    }
}
