package com.example.client;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.client.api.MotionFunctionApi;
import com.example.client.dto.MotionFunctionDTO;
import com.example.client.dto.BaseResponse;
import com.example.client.dto.ResponseDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MotionSettingActivity extends PreferenceFragment {
    SharedPreferences motionFunctionPrefs;

    ListPreference motionPreference1;
    ListPreference motionPreference2;
    ListPreference motionPreference3;
    ListPreference motionPreference4;
    ListPreference motionPreference5;
    ListPreference motionPreference6;
    ListPreference motionPreference7;
    Preference save_btn;
    HashMap<String, String> motionString;
    String motion1;
    String motion2;
    String motion3;
    String motion4;
    String motion5;
    String motion6;
    String motion7;
    SharedPreferences sp;
    Long userId;
    List<MotionFunctionDTO> motionFunctionDTOList = new ArrayList<>();
    MotionFunctionApi motionFunctionApi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.motion_settings_preference);
        setDefaultFunctionString();

        motionFunctionApi = RetrofitClient.getClient().create(MotionFunctionApi.class);

        motionPreference1 = (ListPreference)findPreference("Scroll_up");
        motionPreference2 = (ListPreference)findPreference("Scroll_down");
        motionPreference3 = (ListPreference)findPreference("Scroll_left");
        motionPreference4 = (ListPreference)findPreference("Scroll_right");
        motionPreference5 = (ListPreference)findPreference("Back");
        motionPreference6 = (ListPreference)findPreference("Zoom_in");
        motionPreference7 = (ListPreference)findPreference("Zoom_out");
        save_btn = (Preference)findPreference("save");
        sp = this.getActivity().getSharedPreferences("auto_login",MODE_PRIVATE);
        userId = sp.getLong("auto_id0",0L);
//        motionPreference8 = (ListPreference)findPreference("mspms8");

        motionFunctionPrefs = this.getActivity().getSharedPreferences("motionFunction", MODE_PRIVATE);
        SharedPreferences.Editor motionFunctionEditor = motionFunctionPrefs.edit();
        setDefaultMotionSetting();
        getMotion();
        motionFunctionPrefs.registerOnSharedPreferenceChangeListener(prefListener);

        if(save_btn != null){
            save_btn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    motionFunctionDTOList = new ArrayList<>();
                    getMotion();
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motion1, "Scroll_up"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motion2, "Scroll_down"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motion3, "Scroll_left"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motion4, "Scroll_right"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motion5, "Back"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motion6, "Zoom_in"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motion7, "Zoom_out"));
                    saveMotionSetting(motionFunctionDTOList);
                    return true;
                }
            });
        }
    }//onCreate


    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            SharedPreferences.Editor editor = motionFunctionPrefs.edit();
            Toast alertToast = Toast.makeText(getActivity(),"중복된 설정이 있습니다", Toast.LENGTH_SHORT);
            Toast nothingToast = Toast.makeText(getActivity(),"없는 설정이 있습니다", Toast.LENGTH_SHORT);

            if(key.equals("Scroll_up")){
                if(motionFunctionPrefs.getString("Scroll_up","머리 위로").equals("없음")){
                    motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                    motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                }
                else if(motionFunctionPrefs.getString("Scroll_up","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_down","머리 아래로"))
                        || motionFunctionPrefs.getString("Scroll_up","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_left","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_up","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_right","머리 오른쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_up","머리 위로").equals(
                        motionFunctionPrefs.getString("Back","양쪽 눈 감기"))
                        || motionFunctionPrefs.getString("Scroll_up","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_in","왼쪽 눈 감기"))
                        || motionFunctionPrefs.getString("Scroll_up","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_out","오른쪽 눈 감기"))){
                    if(motion1.equals(motionFunctionPrefs.getString("Scroll_down","머리 아래로"))){
                        editor.putString("Scroll_down", "없음");
                        editor.apply();
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        alertToast.show();
                    }
                    else if(motion1.equals(motionFunctionPrefs.getString("Scroll_left","머리 아래로"))){
                        editor.putString("Scroll_left", "없음");
                        editor.apply();
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        alertToast.show();
                    }
                    if(motion1.equals(motionFunctionPrefs.getString("Scroll_right","머리 아래로"))){
                        editor.putString("Scroll_right", "없음");
                        editor.apply();
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        alertToast.show();
                    }
                    if(motion1.equals(motionFunctionPrefs.getString("Back","머리 아래로"))){
                        editor.putString("Back", "없음");
                        editor.apply();
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        alertToast.show();
                    }
                    if(motion1.equals(motionFunctionPrefs.getString("Zoom_in","머리 아래로"))){
                        editor.putString("Zoom_in", "없음");
                        editor.apply();
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        alertToast.show();
                    }
                    if(motion1.equals(motionFunctionPrefs.getString("Zoom_out","머리 아래로"))){
                        editor.putString("Zoom_out", "없음");
                        editor.apply();
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                    motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                }
            }
            if(key.equals("Scroll_down")){
                if(motionFunctionPrefs.getString("Scroll_down","머리 위로").equals("없음")){
                    motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                    motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                }
                else if(motionFunctionPrefs.getString("Scroll_down","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_up","머리 위로"))
                        || motionFunctionPrefs.getString("Scroll_down","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_left","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_down","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_right","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_down","머리 위로").equals(
                        motionFunctionPrefs.getString("Back","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_down","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_in","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_down","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_out","머리 왼쪽으로"))){
                    if(motion2.equals(motionFunctionPrefs.getString("Scroll_up","머리 아래로"))){
                        editor.putString("Scroll_up", "없음");
                        editor.apply();
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        alertToast.show();
                    }
                    if(motion2.equals(motionFunctionPrefs.getString("Scroll_left","머리 아래로"))){
                        editor.putString("Scroll_left", "없음");
                        editor.apply();
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        alertToast.show();
                    }
                    if(motion2.equals(motionFunctionPrefs.getString("Scroll_right","머리 아래로"))){
                        editor.putString("Scroll_right", "없음");
                        editor.apply();
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        alertToast.show();
                    }
                    if(motion2.equals(motionFunctionPrefs.getString("Back","머리 아래로"))){
                        editor.putString("Back", "없음");
                        editor.apply();
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        alertToast.show();
                    }
                    if(motion2.equals(motionFunctionPrefs.getString("Zoom_in","머리 아래로"))){
                        editor.putString("Zoom_in", "없음");
                        editor.apply();
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        alertToast.show();
                    }
                    if(motion2.equals(motionFunctionPrefs.getString("Zoom_out","머리 아래로"))){
                        editor.putString("Zoom_out", "없음");
                        editor.apply();
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                    motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                }
            }
            if(key.equals("Scroll_left")){
                if(motionFunctionPrefs.getString("Scroll_left","머리 위로").equals("없음")){
                    motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                    motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                }
                else if(motionFunctionPrefs.getString("Scroll_left","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_up","머리 아래로"))
                        || motionFunctionPrefs.getString("Scroll_left","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_down","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_left","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_right","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_left","머리 위로").equals(
                        motionFunctionPrefs.getString("Back","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_left","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_in","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_left","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_out","머리 왼쪽으로"))){
                    if(motion3.equals(motionFunctionPrefs.getString("Scroll_up","머리 아래로"))){
                        editor.putString("Scroll_up", "없음");
                        editor.apply();
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        alertToast.show();
                    }
                    if(motion3.equals(motionFunctionPrefs.getString("Scroll_down","머리 아래로"))){
                        editor.putString("Scroll_down", "없음");
                        editor.apply();
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        alertToast.show();
                    }
                    if(motion3.equals(motionFunctionPrefs.getString("Scroll_right","머리 아래로"))){
                        editor.putString("Scroll_right", "없음");
                        editor.apply();
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        alertToast.show();
                    }
                    if(motion3.equals(motionFunctionPrefs.getString("Back","머리 아래로"))){
                        editor.putString("Back", "없음");
                        editor.apply();
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        alertToast.show();
                    }
                    if(motion3.equals(motionFunctionPrefs.getString("Zoom_in","머리 아래로"))){
                        editor.putString("Zoom_in", "없음");
                        editor.apply();
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        alertToast.show();
                    }
                    if(motion3.equals(motionFunctionPrefs.getString("Zoom_out","머리 아래로"))){
                        editor.putString("Zoom_out", "없음");
                        editor.apply();
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                    motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                }
            }
            if(key.equals("Scroll_right")){
                if(motionFunctionPrefs.getString("Scroll_right","머리 위로").equals("없음")){
                    motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                    motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                }
                else if(motionFunctionPrefs.getString("Scroll_right","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_up","머리 아래로"))
                        || motionFunctionPrefs.getString("Scroll_right","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_down","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_right","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_left","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_right","머리 위로").equals(
                        motionFunctionPrefs.getString("Back","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_right","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_in","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Scroll_right","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_out","머리 왼쪽으로"))){
                    if(motion4.equals(motionFunctionPrefs.getString("Scroll_up","머리 아래로"))){
                        editor.putString("Scroll_up", "없음");
                        editor.apply();
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        alertToast.show();
                    }
                    if(motion4.equals(motionFunctionPrefs.getString("Scroll_down","머리 아래로"))){
                        editor.putString("Scroll_down", "없음");
                        editor.apply();
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        alertToast.show();
                    }
                    if(motion4.equals(motionFunctionPrefs.getString("Scroll_left","머리 아래로"))){
                        editor.putString("Scroll_left", "없음");
                        editor.apply();
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        alertToast.show();
                    }
                    if(motion4.equals(motionFunctionPrefs.getString("Back","머리 아래로"))){
                        editor.putString("Back", "없음");
                        editor.apply();
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        alertToast.show();
                    }
                    if(motion4.equals(motionFunctionPrefs.getString("Zoom_in","머리 아래로"))){
                        editor.putString("Zoom_in", "없음");
                        editor.apply();
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        alertToast.show();
                    }
                    if(motion4.equals(motionFunctionPrefs.getString("Zoom_out","머리 아래로"))){
                        editor.putString("Zoom_out", "없음");
                        editor.apply();
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                    motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                }
            }
            if(key.equals("Back")){
                if(motionFunctionPrefs.getString("Back","머리 위로").equals("없음")){
                    motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                    motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                }
                else if(motionFunctionPrefs.getString("Back","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_up","머리 아래로"))
                        || motionFunctionPrefs.getString("Back","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_down","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Back","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_left","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Back","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_right","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Back","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_in","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Back","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_out","머리 왼쪽으로"))){
                    if(motion5.equals(motionFunctionPrefs.getString("Scroll_up","머리 아래로"))){
                        editor.putString("Scroll_up", "없음");
                        editor.apply();
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        alertToast.show();
                    }
                    if(motion5.equals(motionFunctionPrefs.getString("Scroll_down","머리 아래로"))){
                        editor.putString("Scroll_down", "없음");
                        editor.apply();
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        alertToast.show();
                    }
                    if(motion5.equals(motionFunctionPrefs.getString("Scroll_left","머리 아래로"))){
                        editor.putString("Scroll_left", "없음");
                        editor.apply();
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        alertToast.show();
                    }
                    if(motion5.equals(motionFunctionPrefs.getString("Scroll_right","머리 아래로"))){
                        editor.putString("Scroll_right", "없음");
                        editor.apply();
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        alertToast.show();
                    }
                    if(motion5.equals(motionFunctionPrefs.getString("Zoom_in","머리 아래로"))){
                        editor.putString("Zoom_in", "없음");
                        editor.apply();
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        alertToast.show();
                    }
                    if(motion5.equals(motionFunctionPrefs.getString("Zoom_out","머리 아래로"))){
                        editor.putString("Zoom_out", "없음");
                        editor.apply();
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 "
                                + "위로"));
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                    motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                }
            }
            if(key.equals("Zoom_in")){
                if(motionFunctionPrefs.getString("Zoom_in","머리 위로").equals("없음")){
                    motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                    motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                }
                else if(motionFunctionPrefs.getString("Zoom_in","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_up","머리 아래로"))
                        || motionFunctionPrefs.getString("Zoom_in","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_down","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Zoom_in","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_left","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Zoom_in","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_right","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Zoom_in","머리 위로").equals(
                        motionFunctionPrefs.getString("Back","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Zoom_in","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_out","머리 왼쪽으로"))){
                    if(motion6.equals(motionFunctionPrefs.getString("Scroll_up","머리 아래로"))){
                        editor.putString("Scroll_up", "없음");
                        editor.apply();
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        alertToast.show();
                    }
                    if(motion6.equals(motionFunctionPrefs.getString("Scroll_down","머리 아래로"))){
                        editor.putString("Scroll_down", "없음");
                        editor.apply();
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        alertToast.show();
                    }
                    if(motion6.equals(motionFunctionPrefs.getString("Scroll_left","머리 아래로"))){
                        editor.putString("Scroll_left", "없음");
                        editor.apply();
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        alertToast.show();
                    }
                    if(motion6.equals(motionFunctionPrefs.getString("Scroll_right","머리 아래로"))){
                        editor.putString("Scroll_right", "없음");
                        editor.apply();
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        alertToast.show();
                    }
                    if(motion6.equals(motionFunctionPrefs.getString("Back","머리 아래로"))){
                        editor.putString("Back", "없음");
                        editor.apply();
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in",
                                "머리 위로"));
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        alertToast.show();
                    }
                    if(motion6.equals(motionFunctionPrefs.getString("Zoom_out","머리 아래로"))){
                        editor.putString("Zoom_out", "없음");
                        editor.apply();
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                    motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                }
            }
            if(key.equals("Zoom_out")){
                if(motionFunctionPrefs.getString("Zoom_out","머리 위로").equals("없음")){
                    motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                    motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                }
                else if(motionFunctionPrefs.getString("Zoom_out","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_up","머리 아래로"))
                        || motionFunctionPrefs.getString("Zoom_out","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_down","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Zoom_out","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_left","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Zoom_out","머리 위로").equals(
                        motionFunctionPrefs.getString("Scroll_right","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Zoom_out","머리 위로").equals(
                        motionFunctionPrefs.getString("Back","머리 왼쪽으로"))
                        || motionFunctionPrefs.getString("Zoom_out","머리 위로").equals(
                        motionFunctionPrefs.getString("Zoom_in","머리 왼쪽으로"))){
                    if(motion7.equals(motionFunctionPrefs.getString("Scroll_up","머리 아래로"))){
                        editor.putString("Scroll_up", "없음");
                        editor.apply();
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motion1 = motionFunctionPrefs.getString("Scroll_up","머리 위로");
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        motionPreference1.setSummary(motionFunctionPrefs.getString("Scroll_up","머리 위로"));
                        alertToast.show();
                    }
                    if(motion7.equals(motionFunctionPrefs.getString("Scroll_down","머리 아래로"))){
                        editor.putString("Scroll_down", "없음");
                        editor.apply();
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motion2 = motionFunctionPrefs.getString("Scroll_down","머리 위로");
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        motionPreference2.setSummary(motionFunctionPrefs.getString("Scroll_down","머리 위로"));
                        alertToast.show();
                    }
                    if(motion7.equals(motionFunctionPrefs.getString("Scroll_left","머리 아래로"))){
                        editor.putString("Scroll_left", "없음");
                        editor.apply();
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motion3 = motionFunctionPrefs.getString("Scroll_left","머리 위로");
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        motionPreference3.setSummary(motionFunctionPrefs.getString("Scroll_left","머리 위로"));
                        alertToast.show();
                    }
                    if(motion7.equals(motionFunctionPrefs.getString("Scroll_right","머리 아래로"))){
                        editor.putString("Scroll_right", "없음");
                        editor.apply();
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motion4 = motionFunctionPrefs.getString("Scroll_right","머리 위로");
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        motionPreference4.setSummary(motionFunctionPrefs.getString("Scroll_right","머리 위로"));
                        alertToast.show();
                    }
                    if(motion7.equals(motionFunctionPrefs.getString("Back","머리 아래로"))){
                        editor.putString("Back", "없음");
                        editor.apply();
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motion5 = motionFunctionPrefs.getString("Back","머리 위로");
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        motionPreference5.setSummary(motionFunctionPrefs.getString("Back","머리 위로"));
                        alertToast.show();
                    }
                    if(motion7.equals(motionFunctionPrefs.getString("Zoom_in","머리 아래로"))){
                        editor.putString("Zoom_in", "없음");
                        editor.apply();
                        motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                        motion6 = motionFunctionPrefs.getString("Zoom_in","머리 위로");
                        motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                        motionPreference6.setSummary(motionFunctionPrefs.getString("Zoom_in","머리 위로"));
                        alertToast.show();
                    }
                }
                else{
                    motionPreference7.setSummary(motionFunctionPrefs.getString("Zoom_out","머리 위로"));
                    motion7 = motionFunctionPrefs.getString("Zoom_out","머리 위로");
                }
            }
        }
    };

    private void setDefaultFunctionString(){
        motionString = new HashMap<>();
        motionString.put("Head_up", "머리 위로");
        motionString.put("Head_down", "머리 아래로");
        motionString.put("Head_left", "머리 왼쪽으로");
        motionString.put("Head_right", "머리 오른쪽으로");
        motionString.put("Eyes_close", "양쪽 눈 감기");
        motionString.put("Eyes_close_left", "왼쪽 눈 감기");
        motionString.put("Eyes_close_right", "오른쪽 눈 감기");
        motionString.put("머리 위로", "Head_up");
        motionString.put("머리 아래로", "Head_down");
        motionString.put("머리 왼쪽으로", "Head_left");
        motionString.put("머리 오른쪽으로", "Head_right");
        motionString.put("양쪽 눈 감기", "Eyes_close");
        motionString.put("왼쪽 눈 감기", "Eyes_close_left");
        motionString.put("오른쪽 눈 감기", "Eyes_close_right");
    }

    private void setDefaultMotionSetting(){
        motionPreference1.setSummary(
                motionString.get(motionFunctionPrefs.getString("Scroll_up", "Head_up")));
        motionPreference2.setSummary(
                motionString.get(motionFunctionPrefs.getString("Scroll_down", "Head_down")));
        motionPreference3.setSummary(
                motionString.get(motionFunctionPrefs.getString("Scroll_left", "Head_left")));
        motionPreference4.setSummary(
                motionString.get(motionFunctionPrefs.getString("Scroll_right", "Head_right")));
        motionPreference5.setSummary(
                motionString.get(motionFunctionPrefs.getString("Back", "Eyes_close")));
        motionPreference6.setSummary(
                motionString.get(motionFunctionPrefs.getString("Zoom_in", "Eyes_close_left")));
        motionPreference7.setSummary(
                motionString.get(motionFunctionPrefs.getString("Zoom_out", "Eyes_close_right")));
    }

    private void getMotion(){
        motion1 = motionString.get(String.valueOf(motionPreference1.getSummary()));
        motion2 = motionString.get(String.valueOf(motionPreference2.getSummary()));
        motion3 = motionString.get(String.valueOf(motionPreference3.getSummary()));
        motion4 = motionString.get(String.valueOf(motionPreference4.getSummary()));
        motion5 = motionString.get(String.valueOf(motionPreference5.getSummary()));
        motion6 = motionString.get(String.valueOf(motionPreference6.getSummary()));
        motion7 = motionString.get(String.valueOf(motionPreference7.getSummary()));
    }

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
    private List<MotionFunctionDTO> getMotionSetting(long userId){
        final List<MotionFunctionDTO>[] result = new List[]{new ArrayList<>()};
        motionFunctionApi.getMotionSetting(userId).enqueue(
                new Callback<ResponseDTO<MotionFunctionDTO>>() {
                    @Override
                    public void onResponse(Call<ResponseDTO<MotionFunctionDTO>> call,
                            Response<ResponseDTO<MotionFunctionDTO>> response) {
                        if(response.isSuccessful()){
                            result[0] = response.body().getList();
                        } else {
                            Toast.makeText(getActivity(), "불러오기 실패1", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseDTO<MotionFunctionDTO>> call, Throwable t) {
                        Toast.makeText(getActivity(), "불러오기 실패2", Toast.LENGTH_SHORT).show();
                        Log.e("Motion Setting Error", t.getMessage());
                        t.printStackTrace();
                    }
                });
        return result[0];
    }

    private String checkMotionduplication(String code){
        if(!code.equals("mspms1")){
            if(checkduplication("mspms1", code)) return "mspms1";
        }
        if(!code.equals("mspms2")){
            if(checkduplication("mspms2", code)) return "mspms2";
        }
        if(!code.equals("mspms3")){
            if(checkduplication("mspms3", code)) return "mspms3";
        }
        if(!code.equals("mspms4")){
            if(checkduplication("mspms4", code)) return "mspms4";
        }
        if(!code.equals("mspms5")){
            if(checkduplication("mspms5", code)) return "mspms5";
        }
        if(!code.equals("mspms6")){
            if(checkduplication("mspms6", code)) return "mspms6";
        }
        if(!code.equals("mspms7")){
            if(checkduplication("mspms7", code)) return "mspms7";
        }
        else return "nothing";
        return "nothing";
    }

    private Boolean checkduplication(String code1, String code2){
        if(prefs.getString(code1,"1").equals(prefs.getString(code2,"2"))){
            return true;
        }
        else return false;
    }

    private void changeduplication(String code1, String code2){ //code1: 사용자가 바꾼 모션 설정, code2: 바꾼 설정과 중복되는 모션
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(code2, "없음");
        editor.apply();
        //code1 이전 설정 변경
        if(code1.equals("mspms1")) {
            motion1 = prefs.getString(code1, "");
            motionPreference1.setSummary(prefs.getString(code1,""));
        }
        else if(code1.equals("mspms2")) {
            motion2 = prefs.getString(code1, "");
            motionPreference2.setSummary(prefs.getString(code1,""));
        }
        else if(code1.equals("mspms3")) {
            motion3 = prefs.getString(code1, "");
            motionPreference3.setSummary(prefs.getString(code1,""));
        }
        else if(code1.equals("mspms4")) {
            motion4 = prefs.getString(code1, "");
            motionPreference4.setSummary(prefs.getString(code1,""));
        }
        else if(code1.equals("mspms5")) {
            motion5 = prefs.getString(code1, "");
            motionPreference5.setSummary(prefs.getString(code1,""));
        }
        else if(code1.equals("mspms6")) {
            motion6 = prefs.getString(code1, "");
            motionPreference6.setSummary(prefs.getString(code1,""));
        }
        else if(code1.equals("mspms7")) {
            motion7 = prefs.getString(code1, "");
            motionPreference7.setSummary(prefs.getString(code1,""));
        }
        //code2 이전 설정 변경
        if(code2.equals("mspms1")) {
            motion1 = prefs.getString(code2, "");
            motionPreference1.setSummary(prefs.getString(code2,""));
        }
        else if(code2.equals("mspms2")) {
            motion2 = prefs.getString(code2, "");
            motionPreference2.setSummary(prefs.getString(code2,""));
        }
        else if(code2.equals("mspms3")) {
            motion3 = prefs.getString(code2, "");
            motionPreference3.setSummary(prefs.getString(code2,""));
        }
        else if(code2.equals("mspms4")) {
            motion4 = prefs.getString(code2, "");
            motionPreference4.setSummary(prefs.getString(code2,""));
        }
        else if(code2.equals("mspms5")) {
            motion5 = prefs.getString(code2, "");
            motionPreference5.setSummary(prefs.getString(code2,""));
        }
        else if(code2.equals("mspms6")) {
            motion6 = prefs.getString(code2, "");
            motionPreference6.setSummary(prefs.getString(code2,""));
        }
        else if(code2.equals("mspms7")) {
            motion7 = prefs.getString(code2, "");
            motionPreference7.setSummary(prefs.getString(code2,""));
        }
    }

    private Boolean checknothing(){
        if("없음".equals(prefs.getString("mspms1",""))) return true;
        if("없음".equals(prefs.getString("mspms2",""))) return true;
        if("없음".equals(prefs.getString("mspms3",""))) return true;
        if("없음".equals(prefs.getString("mspms4",""))) return true;
        if("없음".equals(prefs.getString("mspms5",""))) return true;
        if("없음".equals(prefs.getString("mspms6",""))) return true;
        if("없음".equals(prefs.getString("mspms7",""))) return true;
        return false;
    }

}