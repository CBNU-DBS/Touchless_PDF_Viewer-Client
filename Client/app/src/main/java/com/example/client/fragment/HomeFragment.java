package com.example.client.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

        // 구글 드라이브 실행 이벤트 리스너
        Button btn_google_drive = (Button) view.findViewById(R.id.btn_google_drive);
        btn_google_drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //구글드라이브 진입 전, 현재 Download폴더의 pdf파일 이름 배열을 저장 후 Document Fragment로 전달
                File PastDir = new File("/storage/emulated/0/Download/");
                File[] past_files = PastDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File pathname, String name) {
                        return name.endsWith("pdf");
                    }
                });
                //과거 Download폴더의 pdf파일 리스트
                String[] past_file_list = new String[past_files.length];

                for(int i = 0; i < past_files.length; i++){
                    past_file_list[i] = past_files[i].getPath();
                }

                Bundle result = new Bundle();
                result.putStringArray("past_file_list",past_file_list);
                getParentFragmentManager().setFragmentResult("requestKey",result);

                Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.google.android.apps.docs");
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

                //다시 로그인화면으로 돌아갑니다. 앱 재실행 시, 다시 직접 로그인해야됩니다.
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });


        // Inflate the layout for this fragment
        return view;
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