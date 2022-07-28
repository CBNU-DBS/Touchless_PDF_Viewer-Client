package com.example.client;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MypageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // After login, in "activity_mypage.xml"
        // user_name, user_mail, user_phone, user_google_drive_info shoud contain personal info and
        // another user login, then all info have to be changed
    }
}
