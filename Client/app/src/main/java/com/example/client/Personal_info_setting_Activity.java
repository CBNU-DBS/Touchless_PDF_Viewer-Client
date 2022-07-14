package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Personal_info_setting_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info_setting);

        Button password_set_Btn = (Button) findViewById(R.id.password_set_btn);
        password_set_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //비밀번호를 변경합니다.
                //DB와 연동하여 비밀번호가 변경되면 토스트 메세지를 띄웁니다.
            }
        });

        Button email_set_Btn = (Button) findViewById(R.id.email_set_btn);
        email_set_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이메일을 변경합니다.
                //DB와 연동하여 이메일이 변경되면 토스트 메세지를 띄웁니다.
            }
        });

        Button phonenum_set_Btn = (Button) findViewById(R.id.phonenum_set_btn);
        phonenum_set_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //전화번호를 변경합니다.
                //DB와 연동하여 전화번호가 변경되면 토스트메세지를 띄웁니다.
            }
        });

        Button back_to_mypage_Btn = (Button) findViewById(R.id.btn_back_to_mypage);
        back_to_mypage_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}