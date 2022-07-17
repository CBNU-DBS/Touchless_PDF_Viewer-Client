package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MypageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // After login, in "activity_mypage.xml"
        // user_name, user_mail, user_phone, user_google_drive_info shoud contain personal info and
        // another user login, then all info have to be changed

        // 구글 드라이브 버튼을 누르면 스마트폰에 다운로드 되어있는 "구글 드라이브" 앱이 실행됩니다.
        String googledrive = "com.google.android.apps.docs";
        Intent intentgoogledrive = getPackageManager().getLaunchIntentForPackage(googledrive);

        Button button = findViewById(R.id.btn_google_drive);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MypageActivity.this.startActivity(intentgoogledrive);
            }
        });

    }
}
