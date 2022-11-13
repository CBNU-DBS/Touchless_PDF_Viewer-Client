package com.example.client.fragment;

import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.client.R;

/**
 * 모션 - 기능 을 설정할 수 있도록 recyclerview를 보여주는 Fragment Class
 */
public class SettingFragment extends Fragment {
    /**
     * 설정 화면 fragment 선언 및 예외처리
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try{
            v = inflater.inflate(R.layout.activity_setting, container, false);
        }catch (InflateException e){
            return v;
        }
        return v;
    }

    static View v;
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(v!=null){
            ViewGroup parent = (ViewGroup)v.getParent();
            if(parent!=null){
                parent.removeView(v);
            }
        }
    }
}
