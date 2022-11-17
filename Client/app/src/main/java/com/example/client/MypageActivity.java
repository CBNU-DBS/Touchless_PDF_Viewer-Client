package com.example.client;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 마이페이지 Activity Class.
 * GUI로 하단 메뉴바를 사용하면서 HomeFragment에서 현재 액티비티를 대체함.
 */
public class MypageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
    }
}
