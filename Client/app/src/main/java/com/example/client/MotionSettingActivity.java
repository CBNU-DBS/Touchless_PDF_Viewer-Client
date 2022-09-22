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
    Preference save_btn;

    String motion1;
    String motion2;
    String motion3;
    String motion4;
    String motion5;
    String motion6;

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
        save_btn = (Preference)findPreference("save");


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

        motion1 = prefs.getString("mspms1", "머리 위로");
        motion2 = prefs.getString("mspms2", "머리 아래로");
        motion3 = prefs.getString("mspms3", "머리 왼쪽으로");
        motion4 = prefs.getString("mspms4", "머리 오른쪽으로");
        motion5 = prefs.getString("mspms5", "양쪽 눈 감기");
        motion6 = prefs.getString("mspms6", "왼쪽 눈 감기");


        prefs.registerOnSharedPreferenceChangeListener(prefListener);
        if(save_btn != null){
            save_btn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    List<MotionFunctionDTO> motionFunctionDTOList = new ArrayList<>();
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms1","머리 위로"), "위로 스크롤"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms2","머리 아래로"), "아래로 스크롤"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms3","머리 왼쪽으로"), "화면 확대"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms4","머리 오른쪽으로"), "화면 축소"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms5","양쪽 눈 감기"), "뒤로 가기"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms6","왼쪽 눈 감기"), "음성 검색"));
//                    motionFunctionDTOList.add(new MotionFunctionDTO(1, prefs.getString("mspms7","오른쪽 눈 감기"), "삭제 예정"));
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
            Toast nothingToast = Toast.makeText(getActivity(),"없는 설정이 있습니다", Toast.LENGTH_SHORT);

            if(key.equals("mspms1")){
                if(prefs.getString("mspms1","").equals("없음")){
                    motionPreference1.setSummary(prefs.getString("mspms1",""));
                    motionPreference1.setValue(prefs.getString("mspms1",""));
                    motion1 = prefs.getString("mspms1","");
                }
                else if(prefs.getString("mspms1","").equals(prefs.getString("mspms2",""))
                        ||prefs.getString("mspms1","").equals(prefs.getString("mspms3",""))
                        ||prefs.getString("mspms1","").equals(prefs.getString("mspms4",""))
                        ||prefs.getString("mspms1","").equals(prefs.getString("mspms5",""))
                        ||prefs.getString("mspms1","").equals(prefs.getString("mspms6",""))){
                    changeduplication("mspms1",checkMotionduplication("mspms1"));
                    alertToast.show();
                }
                else{
                    motionPreference1.setSummary(prefs.getString("mspms1",""));
                    motion1 = prefs.getString("mspms1","");
                }
            }
            if(key.equals("mspms2")){
                if(prefs.getString("mspms2","").equals("없음")){
                    motionPreference2.setSummary(prefs.getString("mspms2",""));
                    motionPreference2.setValue(prefs.getString("mspms2",""));
                    motion2 = prefs.getString("mspms2","");
                }
                else if(prefs.getString("mspms2","").equals(prefs.getString("mspms1",""))
                        ||prefs.getString("mspms2","").equals(prefs.getString("mspms3",""))
                        ||prefs.getString("mspms2","").equals(prefs.getString("mspms4",""))
                        ||prefs.getString("mspms2","").equals(prefs.getString("mspms5",""))
                        ||prefs.getString("mspms2","").equals(prefs.getString("mspms6",""))){
                    changeduplication("mspms2",checkMotionduplication("mspms2"));
                    alertToast.show();
                }
                else{
                    motionPreference2.setSummary(prefs.getString("mspms2",""));
                    motion2 = prefs.getString("mspms2","");
                }
            }
            if(key.equals("mspms3")){
                if(prefs.getString("mspms3","").equals("없음")){
                    motionPreference3.setSummary(prefs.getString("mspms3",""));
                    motionPreference3.setValue(prefs.getString("mspms3",""));
                    motion3 = prefs.getString("mspms3","");
                }
                else if(prefs.getString("mspms3","").equals(prefs.getString("mspms1",""))
                        ||prefs.getString("mspms3","").equals(prefs.getString("mspms2",""))
                        ||prefs.getString("mspms3","").equals(prefs.getString("mspms4",""))
                        ||prefs.getString("mspms3","").equals(prefs.getString("mspms5",""))
                        ||prefs.getString("mspms3","").equals(prefs.getString("mspms6",""))){
                    changeduplication("mspms3",checkMotionduplication("mspms3"));
                    alertToast.show();
                }
                else{
                    motionPreference3.setSummary(prefs.getString("mspms3",""));
                    motion3 = prefs.getString("mspms3","");
                }
            }
            if(key.equals("mspms4")){
                if(prefs.getString("mspms4","").equals("없음")){
                    motionPreference4.setSummary(prefs.getString("mspms4",""));
                    motionPreference4.setValue(prefs.getString("mspms4",""));
                    motion4 = prefs.getString("mspms4","");
                }
                else if(prefs.getString("mspms4","").equals(prefs.getString("mspms1",""))
                        ||prefs.getString("mspms4","").equals(prefs.getString("mspms2",""))
                        ||prefs.getString("mspms4","").equals(prefs.getString("mspms3",""))
                        ||prefs.getString("mspms4","").equals(prefs.getString("mspms5",""))
                        ||prefs.getString("mspms4","").equals(prefs.getString("mspms6",""))){
                    changeduplication("mspms4",checkMotionduplication("mspms4"));
                    alertToast.show();
                }
                else{
                    motionPreference4.setSummary(prefs.getString("mspms4",""));
                    motion4 = prefs.getString("mspms4","");
                }
            }
            if(key.equals("mspms5")){
                if(prefs.getString("mspms5","").equals("없음")){
                    motionPreference5.setSummary(prefs.getString("mspms5",""));
                    motionPreference5.setValue(prefs.getString("mspms5",""));
                    motion5 = prefs.getString("mspms5","");
                }
                else if(prefs.getString("mspms5","").equals(prefs.getString("mspms1",""))
                        ||prefs.getString("mspms5","").equals(prefs.getString("mspms2",""))
                        ||prefs.getString("mspms5","").equals(prefs.getString("mspms3",""))
                        ||prefs.getString("mspms5","").equals(prefs.getString("mspms4",""))
                        ||prefs.getString("mspms5","").equals(prefs.getString("mspms6",""))){
                    changeduplication("mspms5",checkMotionduplication("mspms5"));
                    alertToast.show();
                }
                else{
                    motionPreference5.setSummary(prefs.getString("mspms5",""));
                    motion5 = prefs.getString("mspms5","");
                }
            }
            if(key.equals("mspms6")){
                if(prefs.getString("mspms6","").equals("없음")){
                    motionPreference6.setSummary(prefs.getString("mspms6",""));
                    motionPreference6.setValue(prefs.getString("mspms6",""));
                    motion6 = prefs.getString("mspms6","");
                }
                else if(prefs.getString("mspms6","").equals(prefs.getString("mspms1",""))
                        ||prefs.getString("mspms6","").equals(prefs.getString("mspms2",""))
                        ||prefs.getString("mspms6","").equals(prefs.getString("mspms3",""))
                        ||prefs.getString("mspms6","").equals(prefs.getString("mspms4",""))
                        ||prefs.getString("mspms6","").equals(prefs.getString("mspms5",""))){
                    changeduplication("mspms6",checkMotionduplication("mspms6"));
                    alertToast.show();
                }
                else{
                    motionPreference6.setSummary(prefs.getString("mspms6",""));
                    motion6 = prefs.getString("mspms6","");
                }
            }
//            if(key.equals("mspms7")){
//                if(prefs.getString("mspms7","머리 위로").equals("없음")){
//                    motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
//                    motion7 = prefs.getString("mspms7","머리 위로");
//                }
//                else if(prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
//                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
//                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
//                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
//                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
//                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))){
//                    changeduplication("mspms7",checkMotionduplication("mspms7"));
//                    alertToast.show();
//                }
//                else{
//                    motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
//                    motion7 = prefs.getString("mspms7","머리 위로");
//                }
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
//        if(!code.equals("mspms7")){
//            if(checkduplication("mspms7", code)) return "mspms7";
//        }
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
//        else if(code1.equals("mspms7")) {
//            motion7 = prefs.getString(code1, "");
//            motionPreference7.setSummary(prefs.getString(code1,""));
//        }
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
//        else if(code2.equals("mspms7")) {
//            motion7 = prefs.getString(code2, "");
//            motionPreference7.setSummary(prefs.getString(code2,""));
//        }
    }

    private Boolean checknothing(){
        if("없음".equals(prefs.getString("mspms1",""))) return true;
        if("없음".equals(prefs.getString("mspms2",""))) return true;
        if("없음".equals(prefs.getString("mspms3",""))) return true;
        if("없음".equals(prefs.getString("mspms4",""))) return true;
        if("없음".equals(prefs.getString("mspms5",""))) return true;
        if("없음".equals(prefs.getString("mspms6",""))) return true;
//        if("없음".equals(prefs.getString("mspms7",""))) return true;
        return false;
    }

}