package com.example.client.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.client.LoginActivity;
import com.example.client.Personal_info_setting_Activity;
import com.example.client.R;

import java.io.File;
import java.io.FilenameFilter;


public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_mypage, container, false);

        TextView us_name = view.findViewById(R.id.user_name);
        TextView us_mail = view.findViewById(R.id.user_mail);
        TextView us_phone = view.findViewById(R.id.user_phone);

        //프래그먼트에서 SharedPreference 선언
        SharedPreferences sharedPref_login = getActivity().getSharedPreferences("auto_login",MODE_PRIVATE);

        //SharedPreference로 저장되있는 key-value 형식의 유저정보 출력
        us_name.setText(sharedPref_login.getString("auto_name0","") + "님 환영합니다!" );
        us_mail.setText(sharedPref_login.getString("auto_email0",""));
        us_phone.setText(sharedPref_login.getString("auto_phone0",""));

        // 마이페이지 비밀번호 변경 버튼 이벤트 리스너
        Button btn_change_PW = (Button) view.findViewById(R.id.btn_change_pw);
        btn_change_PW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Personal_info_setting_Activity.class);
                startActivity(intent);
            }
        });

        // 로그아웃 버튼 이벤트 리스너
        Button btn_logout = (Button) view.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그아웃 시, 자동 로그인 정보를 삭제합니다.
                SharedPreferences sharedPref_login = getActivity().getSharedPreferences("auto_login", MODE_PRIVATE);
                SharedPreferences.Editor editor_login = sharedPref_login.edit();
                editor_login.clear();
                editor_login.commit();

                Log.e("로그아웃 후 아이디",sharedPref_login.getString("auto_id0",""));
                Log.e("로그아웃 후 패스워드",sharedPref_login.getString("auto_pw0",""));

                Toast.makeText(getContext(), "로그인을 다시 해주세요", Toast.LENGTH_SHORT).show();

                //개인 정보를 초기화 한 후, 앱을 재시작합니다. 직접 다시 로그인해야합니다.
                restart();

//                //다시 로그인화면으로 돌아갑니다. 앱 재실행 시, 다시 직접 로그인해야됩니다.
//                Intent intent = new Intent(view.getContext(), LoginActivity.class);
//                startActivity(intent);
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    public void restart(){
        Intent intent = getActivity().getPackageManager().
                getLaunchIntentForPackage(getActivity().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finishAffinity();
    }
//    static View view;
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if(view!=null){
//            ViewGroup parent = (ViewGroup)view.getParent();
//            if(parent!=null){
//                parent.removeView(view);
//            }
//        }
//    }
}