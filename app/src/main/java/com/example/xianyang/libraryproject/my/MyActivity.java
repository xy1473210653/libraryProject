package com.example.xianyang.libraryproject.my;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.xianyang.libraryproject.FristActivity;
import com.example.xianyang.libraryproject.MainActivity;
import com.example.xianyang.libraryproject.R;
import com.example.xianyang.libraryproject.socket.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 *
 *个人信息
 */
public class MyActivity extends AppCompatActivity {
    private ListView listView;
    private MyAdapter myAdapter;
    private Thread thread;
    private String[] list={"ID:","姓名:","性别:","电话:","工作:",
           "逾期次数:","信用积分:","注册时间:","已借书籍:","是否可借:"};
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x02)
            {
                Bundle bundle=msg.getData();
                SharedPreferences sf=getSharedPreferences("user",MODE_PRIVATE);
                SharedPreferences.Editor editor=sf.edit();
                editor.putString("name",bundle.getString("name"));
                editor.putString("sex",bundle.getString("sex"));
                editor.apply();
                list[0]+=bundle.getString("id");
                list[3]+=bundle.getString("tel");
                list[2]+=bundle.getString("sex");
                list[1]+=bundle.getString("name");
                list[4]+=bundle.getString("job");
                list[5]+=bundle.getString("ftimes");
                list[6]+=bundle.getString("crdscore");
                list[7]+=bundle.getString("regtime");
                list[9]+=bundle.getString("isbor");
                list[8]+=bundle.getString("books");
                myAdapter=new MyAdapter(MyActivity.this,list);
                listView.setAdapter(myAdapter);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("我的信息");
        listView=findViewById(R.id.my_message);
        //判断用户是否为登录状态，是则显示用户详细信息，否则显示登录界面
        SharedPreferences sf=getSharedPreferences("user",MODE_PRIVATE);
        if (sf.getBoolean("islogin",false))
        {
            getmessage();
        }else {
            startActivity(new Intent(MyActivity.this,MainActivity.class));
        }
    }
    /**
     * socket获取用户信息
     */
    private void getmessage() {
        thread=new Thread(new Runnable() {
            SharedPreferences sf=getSharedPreferences("user",MODE_PRIVATE);
            String id=sf.getString("id",null);
            String passwd=sf.getString("passwd",null);
            @Override
            public void run() {
                try {
                    Socket socket=new Socket(getResources().getString(R.string.service_ip),8080);
                    socket.setSoTimeout(10000);
                    if (socket!=null)
                    {
                        JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("aim","user_message");
                            jsonObject.put("id",id);
                            jsonObject.put("password",passwd);
                            String result=jsonObject.toString();
                            OutputStream os=socket.getOutputStream();
                            os.write(result.getBytes());
                            os.flush();
                            socket.shutdownOutput();

                            BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String res="";
                            String line="";
                            while ((line=br.readLine())!=null)
                            {
                                res+=line;
                            }
                            JSONObject object=new JSONObject(res);
                            // Log.d("socket", "run: "+object.getString("tel"));
                            Message message=new Message();
                            Bundle bundle=new Bundle();
                            bundle.putString("id",object.getString("id"));
                            bundle.putString("tel",object.getString("tel"));
                            bundle.putString("name",object.getString("name"));
                            bundle.putString("ftimes",object.getString("ftimes"));
                            bundle.putString("regtime",object.getString("regtime"));
                            bundle.putString("sex",object.getString("sex"));
                            bundle.putString("job",object.getString("job"));
                            bundle.putString("books",object.getString("books"));
                            bundle.putString("isbor",object.getString("isbor"));
                            bundle.putString("crdscore",object.getString("crdscore"));
                            //bundle.putString("faceid",object.getString("faceid"));
                            message.what=0x02;
                            message.setData(bundle);
                            handler.sendMessage(message);
                            Log.d("socket", "run: "+res);
                            br.close();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}
