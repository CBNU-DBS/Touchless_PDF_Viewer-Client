package com.example.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ListMenuPresenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ResourceBundle;

import javax.annotation.Nullable;

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

    String before1;
    String before2;
    String before3;
    String before4;
    String before5;
    String before6;
    String before7;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.motion_settings_preference);
        motionPreference1 = (ListPreference)findPreference("mspms1");
        motionPreference2 = (ListPreference)findPreference("mspms2");
        motionPreference3 = (ListPreference)findPreference("mspms3");
        motionPreference4 = (ListPreference)findPreference("mspms4");
        motionPreference5 = (ListPreference)findPreference("mspms5");
        motionPreference6 = (ListPreference)findPreference("mspms6");
        motionPreference7 = (ListPreference)findPreference("mspms7");

//        motionPreference8 = (ListPreference)findPreference("mspms8");

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

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
//            motionPreference8.setSummary(prefs.getString("mspms8", "몰?루"));
//        }

        before1 = prefs.getString("mspms1", "머리 위로");
        before2 = prefs.getString("mspms2", "머리 위로");
        before3 = prefs.getString("mspms3", "머리 위로");
        before4 = prefs.getString("mspms4", "머리 위로");
        before5 = prefs.getString("mspms5", "머리 위로");
        before6 = prefs.getString("mspms6", "머리 위로");
        before7 = prefs.getString("mspms7", "머리 위로");


        prefs.registerOnSharedPreferenceChangeListener(prefListener);
    }//onCreate


    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            SharedPreferences.Editor editor = prefs.edit();
            Toast alertToast = Toast.makeText(getActivity(),"중복된 설정이 있습니다", Toast.LENGTH_SHORT);

            if(key.equals("mspms1")){
                if(prefs.getString("mspms1","머리 위로").equals("없음")){
                    motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                    before1 = prefs.getString("mspms1","머리 위로");
                }
                else if(prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms2","머리 아래로"))
                        ||prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))
                        ||prefs.getString("mspms1","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    editor.putString("mspms1", before1);
                    editor.apply();
                    before1 = prefs.getString("mspms1","머리 위로");
                    motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                    alertToast.show();
                }
                else{
                    motionPreference1.setSummary(prefs.getString("mspms1","머리 위로"));
                    before1 = prefs.getString("mspms1","머리 위로");
                }
            }
            if(key.equals("mspms2")){
                if(prefs.getString("mspms2","머리 위로").equals("없음")){
                    motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                    before2 = prefs.getString("mspms2","머리 위로");
                }
                else if(prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))
                        ||prefs.getString("mspms2","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    editor.putString("mspms2", before2);
                    editor.apply();
                    before2 = prefs.getString("mspms2","머리 위로");
                    motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                    alertToast.show();
                }
                else{
                    motionPreference2.setSummary(prefs.getString("mspms2","머리 위로"));
                    before2 = prefs.getString("mspms2","머리 위로");
                }
            }
            if(key.equals("mspms3")){
                if(prefs.getString("mspms3","머리 위로").equals("없음")){
                    motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                    before3 = prefs.getString("mspms3","머리 위로");
                }
                else if(prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
                        ||prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))
                        ||prefs.getString("mspms3","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    editor.putString("mspms3", before3);
                    editor.apply();
                    before3 = prefs.getString("mspms3","머리 위로");
                    motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                    alertToast.show();
                }
                else{
                    motionPreference3.setSummary(prefs.getString("mspms3","머리 위로"));
                    before3 = prefs.getString("mspms3","머리 위로");
                }
            }
            if(key.equals("mspms4")){
                if(prefs.getString("mspms4","머리 위로").equals("없음")){
                    motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                    before4 = prefs.getString("mspms4","머리 위로");
                }
                else if(prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
                        ||prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))
                        ||prefs.getString("mspms4","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    editor.putString("mspms4", before4);
                    editor.apply();
                    before4 = prefs.getString("mspms4","머리 위로");
                    motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                    alertToast.show();
                }
                else{
                    motionPreference4.setSummary(prefs.getString("mspms4","머리 위로"));
                    before4 = prefs.getString("mspms4","머리 위로");
                }
            }
            if(key.equals("mspms5")){
                if(prefs.getString("mspms5","머리 위로").equals("없음")){
                    motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                    before5 = prefs.getString("mspms5","머리 위로");
                }
                else if(prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
                        ||prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))
                        ||prefs.getString("mspms5","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    editor.putString("mspms5", before5);
                    editor.apply();
                    before5 = prefs.getString("mspms5","머리 위로");
                    motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                    alertToast.show();
                }
                else{
                    motionPreference5.setSummary(prefs.getString("mspms5","머리 위로"));
                    before5 = prefs.getString("mspms5","머리 위로");
                }
            }
            if(key.equals("mspms6")){
                if(prefs.getString("mspms6","머리 위로").equals("없음")){
                    motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                    before6 = prefs.getString("mspms6","머리 위로");
                }
                else if(prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
                        ||prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms6","머리 위로").equals(prefs.getString("mspms7","머리 왼쪽으로"))){
                    editor.putString("mspms6", before6);
                    editor.apply();
                    before6 = prefs.getString("mspms6","머리 위로");
                    motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                    alertToast.show();
                }
                else{
                    motionPreference6.setSummary(prefs.getString("mspms6","머리 위로"));
                    before6 = prefs.getString("mspms6","머리 위로");
                }
            }
            if(key.equals("mspms7")){
                if(prefs.getString("mspms7","머리 위로").equals("없음")){
                    motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                    before7 = prefs.getString("mspms7","머리 위로");
                }
                else if(prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms1","머리 아래로"))
                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms2","머리 왼쪽으로"))
                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms3","머리 왼쪽으로"))
                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms4","머리 왼쪽으로"))
                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms5","머리 왼쪽으로"))
                        ||prefs.getString("mspms7","머리 위로").equals(prefs.getString("mspms6","머리 왼쪽으로"))){
                    editor.putString("mspms7", before7);
                    editor.apply();
                    before7 = prefs.getString("mspms7","머리 위로");
                    motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                    alertToast.show();
                }
                else{
                    motionPreference7.setSummary(prefs.getString("mspms7","머리 위로"));
                    before7 = prefs.getString("mspms7","머리 위로");
                }
            }
//            if(key.equals("mspms8")){
//                motionPreference8.setSummary(prefs.getString("mspms8","몰?루"));
//            }
        }
    };

}