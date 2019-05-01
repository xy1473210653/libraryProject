package com.example.xianyang.libraryproject;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
/**
 * 注册活动
 */
public class RigisiterActivity extends AppCompatActivity {
    private Button rigister_Button;
    private EditText userID_ET;
    private EditText passWD_ET;
    private EditText passWD_new_ET;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rigister);
        rigister_Button=findViewById(R.id.rigister_button);
        userID_ET=findViewById(R.id.userID_rigister);
        passWD_ET=findViewById(R.id.user_passWD_rigister);
        passWD_new_ET=findViewById(R.id.user_passWD_new_rigister);
        //注册，检验密码是否一致
        rigister_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID=userID_ET.getText().toString();
                String passWD=passWD_ET.getText().toString();
                String passWD_new=passWD_new_ET.getText().toString();
                if(!userID.isEmpty())
                {
                    if (!passWD.isEmpty()&&!passWD_new.isEmpty()&&passWD.equals(passWD_new))
                    {

                    }
                    else if (passWD.isEmpty()||passWD_new.isEmpty())
                    {
                        Toast.makeText(RigisiterActivity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                    }else if (!passWD.equals(passWD_new))
                    {
                        Toast.makeText(RigisiterActivity.this,"密码不一致",Toast.LENGTH_SHORT).show();
                    }
                }else
                {
                    Toast.makeText(RigisiterActivity.this,"用户名不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
