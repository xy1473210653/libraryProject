package com.example.xianyang.libraryproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class MyLoader extends AsyncTaskLoader<List<Bitmap>> {
    private Context context;
    private Thread thread;
    private String[] list;
    String aim;
    private List<Bitmap> bitmapList = new ArrayList<Bitmap>();
    public MyLoader(@NonNull Context context, String[] list,String aim) {
        super(context);
        this.context=context;
        this.list=list;
        this.aim=aim;
    }

    @Nullable
    @Override
    public List<Bitmap> loadInBackground() {
        for (int i=0;i<list.length;i++)
        {
            SharedPreferences sf=context.getSharedPreferences("user",MODE_PRIVATE);
            if(sf.getBoolean("islogin",false)) {
                String finalId = sf.getString("id", null);
                Socket socket = null;
                try {
                    //获取书id
                    String id=new JSONObject(list[i]).getString("logid");
                    socket = new Socket(context.getResources().getString(R.string.service_ip), 8080);
                    socket.setSoTimeout(10000);
                    OutputStream os = socket.getOutputStream();
                    JSONObject object = new JSONObject();
                    object.put("aim", aim);
                    Log.d("socketpoppppp", "loadInBackground: "+aim);
                    object.put("logid",id);
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
                    Log.d("socketpoppppp", "loadInBackground: "+res);
                    byte[] array=Base64.decode(res,Base64.DEFAULT);
                    Bitmap bitmap=BitmapFactory.decodeByteArray(array,0,array.length);
                    bitmapList.add(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        return bitmapList;
    }
}
