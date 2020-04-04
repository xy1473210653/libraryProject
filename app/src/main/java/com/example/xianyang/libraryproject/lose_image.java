package com.example.xianyang.libraryproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class lose_image extends AppCompatActivity {
    private ImageView imageView;
    private Thread thread;
    private ProgressBar process;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x00)
            {
                process.setVisibility(View.GONE);
                imageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lose_image_layout);
        String logid=getIntent().getStringExtra("logid");
        imageView=findViewById(R.id.lose_image);
        process=findViewById(R.id.lose_progressbar);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                try {
                    socket = new Socket(getResources().getString(R.string.service_ip), 8080);
                    socket.setSoTimeout(10000);
                    OutputStream os = socket.getOutputStream();
                    JSONObject object = new JSONObject();
                    object.put("aim", "find_big_picture_article");
                    object.put("logid",logid);//getIntent().getStringExtra("logid"
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

                    Log.d("socketpo", "run: " + res);
                    Message message = new Message();
                    message.what = 0x00;
                    byte[] array=Base64.decode(res,Base64.DEFAULT);
                    message.obj=BitmapFactory.decodeByteArray(array,0,array.length);
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }
}
