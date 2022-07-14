package com.example.client.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.client.LoginActivity;
import com.example.client.Personal_info_setting_Activity;
import com.example.client.R;


public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_mypage, container, false);

        // 마이페이지 설정 버튼 이벤트 리스너
        Button btn_account_set = (Button) view.findViewById(R.id.btn_account_set);
        btn_account_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //move to account setting page
                Intent intent = new Intent(view.getContext(), Personal_info_setting_Activity.class);
                startActivity(intent);
            }
        });
        // 로그아웃 버튼 이벤트 리스너
        Button btn_logout = (Button) view.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //move to login page with logout(It needs to be changed to log in again)
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });


        // Inflate the layout for this fragment
        return view;
    }
}