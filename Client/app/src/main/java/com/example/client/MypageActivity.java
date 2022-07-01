package com.example.client;

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

        Button account_set_Btn = (Button) findViewById(R.id.account_set_btn);
        account_set_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //move to account setting page
            }
        });
        Button logout_Btn = (Button) findViewById(R.id.logout_btn);
        logout_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //move to login page with logout(It needs to be changed to log in again)
            }
        });
    }
}
