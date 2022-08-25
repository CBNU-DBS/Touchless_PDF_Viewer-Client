package com.example.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.client.api.MotionFunctionApi;
import com.example.client.dto.MotionFunctionDTO;
import com.example.client.dto.BaseResponse;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;

public class MotionSettingActivity extends PreferenceFragment {
    SharedPreferences prefs;

    ListPreference motionPreference1;
    ListPreference motionPreference2;
    ListPreference motionPreference3;
    ListPreference motionPreference4;
    ListPreference motionPreference5;
    ListPreference motionPreference6;
    ListPreference motionPreference7;
//    ListPreference motionPreference8;
    Preference save_btn;

    String motion1;
    String motion2;
    String motion3;
    String motion4;
    String motion5;
    String motion6;
    String motion7;

    MotionFunctionApi motionFunctionApi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.motion_settings_preference);
        motionFunctionApi = RetrofitClient.getClient().create(MotionFunctionApi.class);

        motionPreference1 = (ListPreference)findPreference("mspms1");
        motionPreference2 = (ListPreference)findPreference("mspms2");
        motionPreference3 = (ListPreference)findPreference("mspms3");
        motionPreference4 = (ListPreference)findPreference("mspms4");
        motionPreference5 = (ListPreference)findPreference("mspms5");
        motionPreference6 = (ListPreference)findPreference("mspms6");
        motionPreference7 = (ListPreference)findPreference("mspms7");
        save_btn = (Preference)findPreference("save");

//        motionPreference8 = (ListPreference)findPreference("mspms8");

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editon = prefs.edit();

        if(!prefs.contains("mspms1")){
            editon.putString("mspms1", "머리 위로");
            editon.apply();
        }
        if(!prefs.contains("mspms2")){
            editon.putString("mspms2", "머리 아래로");
            editon.apply();
        }
        if(!prefs.contains("mspms3")){
            editon.putString("mspms3", "머리 왼쪽으로");
            editon.apply();
        }
        if(!prefs.contains("mspms4")){
            editon.putString("mspms4", "머리 오른쪽으로");
            editon.apply();
        }
        if(!prefs.contains("mspms5")){
            editon.putString("mspms5", "양쪽 눈 감기");
            editon.apply();
        }
        if(!prefs.contains("mspms6")){
            editon.putString("mspms6", "왼쪽 눈 감기");
            editon.apply();
        }
        if(!prefs.contains("mspms7")){
            editon.putString("mspms7", "오른쪽 눈 감기");
            editon.apply();
        }

        if(!prefs.getString("mspms1", "").equals("")){
            motionPreference1.setSummary(prefs.getString("mspms1", "머리 위로"));
        }
        if(!prefs.getString("mspms2", "").equals("")){
            motionPreference2.setSummary(prefs.getString("mspms2", "머리 아래로"));
        }
        if(!prefs.getString("mspms3", "").equals("")){
            motionPreference3.setSummary(prefs.getString("mspms3", "머리 왼쪽으로"));
        }
        if(!prefs.getString("mspms4", "").equals("")){
            motionPreference4.setSummary(prefs.getString("mspms4", "머리 오른쪽으로"));
        }
        if(!prefs.getString("mspms5", "").equals("")){
            motionPreference5.setSummary(prefs.getString("mspms5", "양쪽 눈 감기"));
        }
        if(!prefs.getString("mspms6", "").equals("")){
            motionPreference6.setSummary(prefs.getString("mspms6", "왼쪽 눈 감기"));
        }
        if(!prefs.getString("mspms7", "").equals("")){
            motionPreference7.setSummary(prefs.getString("mspms7", "오른쪽 눈 감기"));
        }
//        if(!prefs.getString("mspms8", "").equals("")){
//            motionPreference8.setSummary(prefs.getString("mspms8", "예정"));
//        }

        motion1 = prefs.getString("mspms1", "머리 위로");
        motion2 = prefs.getString("mspms2", "머리 아래로");
        motion3 = prefs.getString("mspms3", "머리 왼쪽으로");
        motion4 = prefs.getString("mspms4", "머리 오른쪽으로");
        motion5 = prefs.getString("mspms5", "양쪽 눈 감기");
        motion6 = prefs.getString("mspms6", "왼쪽 눈 감기");
        motion7 = prefs.getString("mspms7", "오른쪽 눈 감기");


        prefs.registerOnSharedPreferenceChangeListener(prefListener);
        if(save_btn != null){
            save_btn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    List<MotionFunctionDTO> motionFunctionDTOList = new ArrayList<>();
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms1","머리 위로"), "위로 스크롤"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms2","머리 아래로"), "아래로 스크롤"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms3","머리 왼쪽으로"), "이전 페이지"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms4","머리 오른쪽으로"), "다음 페이지"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms5","양쪽 눈 감기"), "뒤로 가기"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms6","왼쪽 눈 감기"), "화면 확대"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms7","오른쪽 눈 감기"), "화면 축소"));
                    saveMotionSetting(motionFunctionDTOList);
                    return true;
                }
            });
        }
    }//onCreate


    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            SharedPreferences.Editor editor = prefs.edit();
            Toast alertToast = Toast.makeText(getActivity(),"중복된 설정이 있습니다", Toast.LENGTH_SHORT);

            if(key.equals("mspms1")){
                if(prefs.getString("mspms1","머리 위로").equals("없음")){
                    motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                    motion1 = prefs.getString("mspms1","머리 위로");
                }
                else if(prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms2","머리 아래로"))
                        ||prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms4","머리 오른쪽으로"))
                        ||prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms5","양쪽 눈 감기"))
                        ||prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms6","왼쪽 눈 감기"))
                        ||prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms7","오른쪽 눈 감기"))){
                    if(motion1.equals(prefs.getString("mspms2","머리 아래로"))){
                        editor.putString("mspms2", "없음");
                        editor.apply();
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        alertToast.show();
                    }
                    else if(motion1.equals(prefs.getString("mspms3","머리 아래로"))){
                        editor.putString("mspms3", "없음");
                        editor.apply();
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        alertToast.show();
                    }
                    if(motion1.equals(prefs.getString("mspms4","머리 아래로"))){
                        editor.putString("mspms4", "없음");
                        editor.apply();
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        alertToast.show();
                    }
                    if(motion1.equals(prefs.getString("mspms5","머리 아래로"))){
                        editor.putString("mspms5", "없음");
                        editor.apply();
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        alertToast.show();
                    }
                    if(motion1.equals(prefs.getString("mspms6","머리 아래로"))){
                        editor.putString("mspms6", "없음");
                        editor.apply();
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        alertToast.show();
                    }
                    if(motion1.equals(prefs.getString("mspms7","머리 아래로"))){
                        editor.putString("mspms7", "없음");
                        editor.apply();
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                    motion1 = prefs.getString("mspms1","머리 위로");
                }
            }
            if(key.equals("mspms2")){
                if(prefs.getString("mspms2","머리 위로").equals("없음")){
                    motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                    motion2 = prefs.getString("mspms2","머리 위로");
                }
                else if(prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms1","머리 위로"))
                        ||prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))
                        ||prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    if(motion2.equals(prefs.getString("mspms1","머리 아래로"))){
                        editor.putString("mspms1", "없음");
                        editor.apply();
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        alertToast.show();
                    }
                    if(motion2.equals(prefs.getString("mspms3","머리 아래로"))){
                        editor.putString("mspms3", "없음");
                        editor.apply();
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        alertToast.show();
                    }
                    if(motion2.equals(prefs.getString("mspms4","머리 아래로"))){
                        editor.putString("mspms4", "없음");
                        editor.apply();
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        alertToast.show();
                    }
                    if(motion2.equals(prefs.getString("mspms5","머리 아래로"))){
                        editor.putString("mspms5", "없음");
                        editor.apply();
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        alertToast.show();
                    }
                    if(motion2.equals(prefs.getString("mspms6","머리 아래로"))){
                        editor.putString("mspms6", "없음");
                        editor.apply();
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        alertToast.show();
                    }
                    if(motion2.equals(prefs.getString("mspms7","머리 아래로"))){
                        editor.putString("mspms7", "없음");
                        editor.apply();
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                    motion2 = prefs.getString("mspms2","머리 위로");
                }
            }
            if(key.equals("mspms3")){
                if(prefs.getString("mspms3","머리 위로").equals("없음")){
                    motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                    motion3 = prefs.getString("mspms3","머리 위로");
                }
                else if(prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
                        ||prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))
                        ||prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    if(motion3.equals(prefs.getString("mspms1","머리 아래로"))){
                        editor.putString("mspms1", "없음");
                        editor.apply();
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        alertToast.show();
                    }
                    if(motion3.equals(prefs.getString("mspms2","머리 아래로"))){
                        editor.putString("mspms2", "없음");
                        editor.apply();
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        alertToast.show();
                    }
                    if(motion3.equals(prefs.getString("mspms4","머리 아래로"))){
                        editor.putString("mspms4", "없음");
                        editor.apply();
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        alertToast.show();
                    }
                    if(motion3.equals(prefs.getString("mspms5","머리 아래로"))){
                        editor.putString("mspms5", "없음");
                        editor.apply();
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        alertToast.show();
                    }
                    if(motion3.equals(prefs.getString("mspms6","머리 아래로"))){
                        editor.putString("mspms6", "없음");
                        editor.apply();
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        alertToast.show();
                    }
                    if(motion3.equals(prefs.getString("mspms7","머리 아래로"))){
                        editor.putString("mspms7", "없음");
                        editor.apply();
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                    motion3 = prefs.getString("mspms3","머리 위로");
                }
            }
            if(key.equals("mspms4")){
                if(prefs.getString("mspms4","머리 위로").equals("없음")){
                    motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                    motion4 = prefs.getString("mspms4","머리 위로");
                }
                else if(prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
                        ||prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))
                        ||prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    if(motion4.equals(prefs.getString("mspms1","머리 아래로"))){
                        editor.putString("mspms1", "없음");
                        editor.apply();
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        alertToast.show();
                    }
                    if(motion4.equals(prefs.getString("mspms2","머리 아래로"))){
                        editor.putString("mspms2", "없음");
                        editor.apply();
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        alertToast.show();
                    }
                    if(motion4.equals(prefs.getString("mspms3","머리 아래로"))){
                        editor.putString("mspms3", "없음");
                        editor.apply();
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        alertToast.show();
                    }
                    if(motion4.equals(prefs.getString("mspms5","머리 아래로"))){
                        editor.putString("mspms5", "없음");
                        editor.apply();
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        alertToast.show();
                    }
                    if(motion4.equals(prefs.getString("mspms6","머리 아래로"))){
                        editor.putString("mspms6", "없음");
                        editor.apply();
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        alertToast.show();
                    }
                    if(motion4.equals(prefs.getString("mspms7","머리 아래로"))){
                        editor.putString("mspms7", "없음");
                        editor.apply();
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                    motion4 = prefs.getString("mspms4","머리 위로");
                }
            }
            if(key.equals("mspms5")){
                if(prefs.getString("mspms5","머리 위로").equals("없음")){
                    motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                    motion5 = prefs.getString("mspms5","머리 위로");
                }
                else if(prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
                        ||prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))
                        ||prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    if(motion5.equals(prefs.getString("mspms1","머리 아래로"))){
                        editor.putString("mspms1", "없음");
                        editor.apply();
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        alertToast.show();
                    }
                    if(motion5.equals(prefs.getString("mspms2","머리 아래로"))){
                        editor.putString("mspms2", "없음");
                        editor.apply();
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        alertToast.show();
                    }
                    if(motion5.equals(prefs.getString("mspms3","머리 아래로"))){
                        editor.putString("mspms3", "없음");
                        editor.apply();
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        alertToast.show();
                    }
                    if(motion5.equals(prefs.getString("mspms4","머리 아래로"))){
                        editor.putString("mspms4", "없음");
                        editor.apply();
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        alertToast.show();
                    }
                    if(motion5.equals(prefs.getString("mspms6","머리 아래로"))){
                        editor.putString("mspms6", "없음");
                        editor.apply();
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        alertToast.show();
                    }
                    if(motion5.equals(prefs.getString("mspms7","머리 아래로"))){
                        editor.putString("mspms7", "없음");
                        editor.apply();
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                    motion5 = prefs.getString("mspms5","머리 위로");
                }
            }
            if(key.equals("mspms6")){
                if(prefs.getString("mspms6","머리 위로").equals("없음")){
                    motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                    motion6 = prefs.getString("mspms6","머리 위로");
                }
                else if(prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
                        ||prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    if(motion6.equals(prefs.getString("mspms1","머리 아래로"))){
                        editor.putString("mspms1", "없음");
                        editor.apply();
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        alertToast.show();
                    }
                    if(motion6.equals(prefs.getString("mspms2","머리 아래로"))){
                        editor.putString("mspms2", "없음");
                        editor.apply();
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        alertToast.show();
                    }
                    if(motion6.equals(prefs.getString("mspms3","머리 아래로"))){
                        editor.putString("mspms3", "없음");
                        editor.apply();
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        alertToast.show();
                    }
                    if(motion6.equals(prefs.getString("mspms4","머리 아래로"))){
                        editor.putString("mspms4", "없음");
                        editor.apply();
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        alertToast.show();
                    }
                    if(motion6.equals(prefs.getString("mspms5","머리 아래로"))){
                        editor.putString("mspms5", "없음");
                        editor.apply();
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        alertToast.show();
                    }
                    if(motion6.equals(prefs.getString("mspms7","머리 아래로"))){
                        editor.putString("mspms7", "없음");
                        editor.apply();
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                    motion6 = prefs.getString("mspms6","머리 위로");
                }
            }
            if(key.equals("mspms7")){
                if(prefs.getString("mspms7","머리 위로").equals("없음")){
                    motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                    motion7 = prefs.getString("mspms7","머리 위로");
                }
                else if(prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))){
                    if(motion7.equals(prefs.getString("mspms1","머리 아래로"))){
                        editor.putString("mspms1", "없음");
                        editor.apply();
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motion1 = prefs.getString("mspms1","머리 위로");
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                        alertToast.show();
                    }
                    if(motion7.equals(prefs.getString("mspms2","머리 아래로"))){
                        editor.putString("mspms2", "없음");
                        editor.apply();
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motion2 = prefs.getString("mspms2","머리 위로");
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                        alertToast.show();
                    }
                    if(motion7.equals(prefs.getString("mspms3","머리 아래로"))){
                        editor.putString("mspms3", "없음");
                        editor.apply();
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motion3 = prefs.getString("mspms3","머리 위로");
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                        alertToast.show();
                    }
                    if(motion7.equals(prefs.getString("mspms4","머리 아래로"))){
                        editor.putString("mspms4", "없음");
                        editor.apply();
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motion4 = prefs.getString("mspms4","머리 위로");
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                        alertToast.show();
                    }
                    if(motion7.equals(prefs.getString("mspms5","머리 아래로"))){
                        editor.putString("mspms5", "없음");
                        editor.apply();
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motion5 = prefs.getString("mspms5","머리 위로");
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                        alertToast.show();
                    }
                    if(motion7.equals(prefs.getString("mspms6","머리 아래로"))){
                        editor.putString("mspms6", "없음");
                        editor.apply();
                        motion7 = prefs.getString("mspms7","머리 위로");
                        motion6 = prefs.getString("mspms6","머리 위로");
                        motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                        motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                    motion7 = prefs.getString("mspms7","머리 위로");
                }
            }
//            if(key.equals("mspms8")){
//                motionPreference8.setSummary(prefs.getString("mspms8","몰?루"));
//            }
        }
    };

    private void saveMotionSetting(List<MotionFunctionDTO> motionFunctionDTOList){
        motionFunctionApi.saveMotionSetting(motionFunctionDTOList).enqueue(
                new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call,
                            retrofit2.Response<BaseResponse> response) {
                        if(response.isSuccessful()){
                            Toast.makeText(getActivity(), response.body().getResultMsg(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "저장 실패2", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        Toast.makeText(getActivity(), "저장 실패1", Toast.LENGTH_SHORT).show();
                        Log.e("Motion Setting Error", t.getMessage());
                        t.printStackTrace();
                    }
                });
    }

}