package com.example.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.client.fragment.HomeFragment;
import com.example.client.fragment.SettingFragment;
import com.example.client.fragment.DocumentFragment;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {

    HomeFragment homeFragment;
    SettingFragment settingFragment;
    DocumentFragment documentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeFragment = new HomeFragment();
        settingFragment = new SettingFragment();
        documentFragment = new DocumentFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, homeFragment).commit();

        // 마이페이지 개인 정보 입력
        Intent info_intent = getIntent();
        String user_name = info_intent.getStringExtra("Name");
        String user_email = info_intent.getStringExtra("Email");
        String user_phone = info_intent.getStringExtra("Phone");

        //번들 선언 후, 데이터 담기
        Bundle bundle = new Bundle();
        bundle.putString("user_name",user_name);
        bundle.putString("user_email",user_email);
        bundle.putString("user_phone",user_phone);

        //프래그먼트에 데이터 넘기기
        homeFragment.setArguments((bundle));

        NavigationBarView navigationBarView = findViewById(R.id.bottom_navigation_view);
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d("test", "onNavigationItemSelected: "+item.getItemId());
                switch(item.getItemId()){
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, homeFragment).commit();
                        return true;
                    case R.id.document:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, documentFragment).commit();
                        return true;
                    case R.id.setting:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, settingFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }

}