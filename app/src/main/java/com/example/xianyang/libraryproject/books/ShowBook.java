package com.example.xianyang.libraryproject.books;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xianyang.libraryproject.MyLoader;
import com.example.xianyang.libraryproject.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ShowBook extends AppCompatActivity {
    private ImageView book_image;//书icon
    private TextView author_t;//作者
    private TextView bookname_t;//书名
    private TextView pubdate_t;//出版日期
    private TextView price_t;//价格
    private TextView introduct_t;//简介
    private TextView press_t;//出版社
    private TextView localtion_t;//存储位置
    private Thread thread;
    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x00)
            {
                book_image.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_book);
        book_image=findViewById(R.id.show_book_image);
        author_t=findViewById(R.id.show_book_Writer);
        bookname_t=findViewById(R.id.show_book_name);
        pubdate_t=findViewById(R.id.show_book_pubdate);
        price_t=findViewById(R.id.show_book_price);
        introduct_t=findViewById(R.id.show_book_introduct);
        press_t=findViewById(R.id.show_book_press);
        localtion_t=findViewById(R.id.show_book_localtion);
        Bundle bundle=getIntent().getExtras();
        String book=bundle.getString("book");
        try {
            JSONObject jsonObject=new JSONObject(book);
            getPhoto(jsonObject.getString("logid"));
            //book_image.setImageResource(R.mipmap.ic_launcher_round);
            author_t.setText(jsonObject.getString("author"));
            bookname_t.setText(jsonObject.getString("bookname"));
            pubdate_t.setText(jsonObject.getString("pubdate"));
            price_t.setText(jsonObject.getString("price")+"元");
            //rem_t.setText(jsonObject.getString("rem"));
            introduct_t.setText("\u3000\u3000"+jsonObject.getString("introduct"));
            press_t.setText(jsonObject.getString("Press"));
            String local=jsonObject.getString("location");
            localtion_t.setText("书所在位置："+local.charAt(1)+"楼"+local.charAt(2)+"区"+local.charAt(4)+"号书架"+local.charAt(5)+"层"+local.charAt(6)+"区");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void getPhoto(String logid)
    {
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                try {
                    //获取书id
                    socket = new Socket(getResources().getString(R.string.service_ip), 8080);
                    socket.setSoTimeout(10000);
                    OutputStream os = socket.getOutputStream();
                    JSONObject object = new JSONObject();
                    object.put("aim", "find_book_picture");
                    object.put("logid",logid);
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
                    byte[] array=Base64.decode(res,Base64.DEFAULT);
                    Bitmap bitmap =BitmapFactory.decodeByteArray(array,0,array.length);
                    Message message=new Message();
                    message.what=0x00;
                    message.obj= bitmap;
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
