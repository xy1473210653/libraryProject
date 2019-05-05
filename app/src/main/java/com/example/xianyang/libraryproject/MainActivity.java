package com.example.xianyang.libraryproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.xianyang.libraryproject.socket.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 登录
 */
public class MainActivity extends AppCompatActivity {
    private Button login_button;
    private Button rigiter_button;
    private ImageButton show_hide_passwd;//显示展示密码按钮
    private EditText userID_editText;
    private EditText userPassWD_editText;
    private Thread thread;
    private Boolean isHide=true;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 0x00:
                    SharedPreferences sf=getSharedPreferences("user",MODE_PRIVATE);
                    SharedPreferences.Editor editor=sf.edit();
                    editor.putBoolean("islogin",true);//登录成功，标记登录
                    editor.putString("id",userID_editText.getText().toString());
                    editor.putString("passwd",userPassWD_editText.getText().toString());
                    editor.apply();
                    startActivity(new Intent(MainActivity.this,FristActivity.class));
                    break;
                case 0x01:
                    Toast.makeText(MainActivity.this,"账号密码错误",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarCompat.compat(this,0xFF99e2e2);
        getSupportActionBar().hide();
        login_button = findViewById(R.id.login);
        rigiter_button = findViewById(R.id.rigister);
        userID_editText=findViewById(R.id.userID);
        userPassWD_editText=findViewById(R.id.user_passWD);
        show_hide_passwd=findViewById(R.id.passwd_show_button);
        show_hide_passwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHide)
                {
                    userPassWD_editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    show_hide_passwd.setBackgroundResource(R.mipmap.show_passwd);
                    userPassWD_editText.setSelection(userPassWD_editText.getText().length());
                    isHide=false;
                }
                else {
                    isHide=true;
                    userPassWD_editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    show_hide_passwd.setBackgroundResource(R.mipmap.hide_passwd);
                    userPassWD_editText.setSelection(userPassWD_editText.getText().length());
                }
            }
        });
        //登录
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                login();
            }
        });
        rigiter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //注册
                Intent intent = new Intent(MainActivity.this, RigisiterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 检验用户名和密码
     */
    private void login() {
        Log.d("socket", "login: "+"up");
        String userID=userID_editText.getText().toString();
        String passWD=userPassWD_editText.getText().toString();
        User user=new User(userID,passWD);

        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket=new Socket("192.168.43.217",8080);
                    socket.setSoTimeout(10000);
                    if (socket!=null)
                    {
                        JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("aim","login");
                            jsonObject.put("id",user.getUsername());
                            jsonObject.put("password",user.getPassWd());
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
                            if(Boolean.parseBoolean(object.getString("result")))
                            {
                                handler.sendEmptyMessage(0x00);
                            }
                            else {
                                handler.sendEmptyMessage(0x01);
                            }
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

