package com.example.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;
import static android.content.Context.MODE_PRIVATE;
import com.example.client.dto.ResponseDTO;
import java.util.HashMap;
import retrofit2.Response;

import com.example.client.api.MotionFunctionApi;
import com.example.client.dto.MotionFunctionDTO;
import com.example.client.dto.BaseResponse;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * 모션 설정 화면 Activity Class.
 * 기기에 저장된 모션 설정을 관리
 * 서버와의 통신을 통해 서버에 저장된 모션 설정을 기기로 가져옴
 */
public class MotionSettingActivity extends PreferenceFragment {
    /**
     * SharedPreferences를 통해 전역적으로 접근 가능
     */
    SharedPreferences finalmotionFunctionPrefs;
    SharedPreferences motionFunctionPrefs;

    // 모션 설정, 저장 버튼 Preference 선언
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

    /**
     * 생성자.
     * motionPreference를 통해 모션에 따른 각 행동들을 저장
     * 만약 앱을 처음 실행한다면 Default값을 저장
     * 설정에서 중복되는 값은 거부시킴
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultFunctionString();

        addPreferencesFromResource(R.xml.motion_settings_preference);
        motionFunctionApi = RetrofitClient.getClient().create(MotionFunctionApi.class);

        motionPreference1 = (ListPreference)findPreference("Scroll_up");
        motionPreference2 = (ListPreference)findPreference("Scroll_down");
        motionPreference3 = (ListPreference)findPreference("Scroll_left");
        motionPreference4 = (ListPreference)findPreference("Scroll_right");
        motionPreference5 = (ListPreference)findPreference("Back");
        motionPreference6 = (ListPreference)findPreference("Zoom_in");
        motionPreference7 = (ListPreference)findPreference("Zoom_out");
        save_btn = (Preference)findPreference("save");

        finalmotionFunctionPrefs = this.getActivity().getSharedPreferences("motionFunction",MODE_PRIVATE);
        motionFunctionPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editon = motionFunctionPrefs.edit();

        editon.putString("Scroll_up", finalmotionFunctionPrefs.getString("Scroll_up",""));
        editon.putString("Scroll_down", finalmotionFunctionPrefs.getString("Scroll_down",""));
        editon.putString("Scroll_left", finalmotionFunctionPrefs.getString("Scroll_left",""));
        editon.putString("Scroll_right", finalmotionFunctionPrefs.getString("Scroll_right",""));
        editon.putString("Back", finalmotionFunctionPrefs.getString("Back",""));
        editon.putString("Zoom_in", finalmotionFunctionPrefs.getString("Zoom_in",""));
        editon.putString("Zoom_out", finalmotionFunctionPrefs.getString("Zoom_out",""));
        editon.apply();

        motionPreference1.setValue(motionFunctionPrefs.getString("Scroll_up",""));
        motionPreference2.setValue(motionFunctionPrefs.getString("Scroll_down",""));
        motionPreference3.setValue(motionFunctionPrefs.getString("Scroll_left",""));
        motionPreference4.setValue(motionFunctionPrefs.getString("Scroll_right",""));
        motionPreference5.setValue(motionFunctionPrefs.getString("Back",""));
        motionPreference6.setValue(motionFunctionPrefs.getString("Zoom_in",""));
        motionPreference7.setValue(motionFunctionPrefs.getString("Zoom_out",""));

        sp = this.getActivity().getSharedPreferences("auto_login",MODE_PRIVATE);
        userId = sp.getLong("auto_id0",0L);
        motionFunctionPrefs.registerOnSharedPreferenceChangeListener(prefListener);

        Log.d("what", motionFunctionPrefs.getString("Scroll_up",""));
        Log.d("what", motionFunctionPrefs.getString("Scroll_down",""));
        Log.d("what", motionFunctionPrefs.getString("Scroll_left",""));
        Log.d("what", motionFunctionPrefs.getString("Scroll_right",""));
        Log.d("what", motionFunctionPrefs.getString("Back",""));
        Log.d("what", motionFunctionPrefs.getString("Zoom_in",""));
        Log.d("what", motionFunctionPrefs.getString("Zoom_out",""));
        Log.d("what", "========================================================");

        //앱 최초실행시 Default 값 저장
        if(!motionFunctionPrefs.contains("Scroll_up")){
            editon.putString("Scroll_up", "Head_up");
            editon.apply();
        }
        if(!motionFunctionPrefs.contains("Scroll_down")){
            editon.putString("Scroll_down", "Head_down");
            editon.apply();
        }
        if(!motionFunctionPrefs.contains("Scroll_left")){
            editon.putString("Scroll_left", "Head_left");
            editon.apply();
        }
        if(!motionFunctionPrefs.contains("Scroll_right")){
            editon.putString("Scroll_right", "Head_right");
            editon.apply();
        }
        if(!motionFunctionPrefs.contains("Back")){
            editon.putString("Back", "Eyes_close");
            editon.apply();
        }
        if(!motionFunctionPrefs.contains("Zoom_in")){
            editon.putString("Zoom_in", "Eye_close_left");
            editon.apply();
        }
        if(!motionFunctionPrefs.contains("Zoom_out")){
            editon.putString("Zoom_out", "Eye_close_right");
            editon.apply();
        }

        //각 MotionPreference들의 Summary 수정
        if(!motionFunctionPrefs.getString("Scroll_up", "").equals("")){
            motionPreference1.setSummary(change_language(motionFunctionPrefs.getString("Scroll_up", "")));
        }
        if(!motionFunctionPrefs.getString("Scroll_down", "").equals("")){
            motionPreference2.setSummary(change_language(motionFunctionPrefs.getString("Scroll_down", "")));
        }
        if(!motionFunctionPrefs.getString("Scroll_left", "").equals("")){
            motionPreference3.setSummary(change_language(motionFunctionPrefs.getString("Scroll_left", "")));
        }
        if(!motionFunctionPrefs.getString("Scroll_right", "").equals("")){
            motionPreference4.setSummary(change_language(motionFunctionPrefs.getString("Scroll_right", "")));
        }
        if(!motionFunctionPrefs.getString("Back", "").equals("")){
            motionPreference5.setSummary(change_language(motionFunctionPrefs.getString("Back", "")));
        }
        if(!motionFunctionPrefs.getString("Zoom_in", "").equals("")){
            motionPreference6.setSummary(change_language(motionFunctionPrefs.getString("Zoom_in", "")));
        }
        if(!motionFunctionPrefs.getString("Zoom_out", "").equals("")){
            motionPreference7.setSummary(change_language(motionFunctionPrefs.getString("Zoom_out", "")));
        }

        // 할당된 모션들을 저장
        motion1 = motionFunctionPrefs.getString("Scroll_up", "");
        motion2 = motionFunctionPrefs.getString("Scroll_down", "");
        motion3 = motionFunctionPrefs.getString("Scroll_left", "");
        motion4 = motionFunctionPrefs.getString("Scroll_right", "");
        motion5 = motionFunctionPrefs.getString("Back", "");
        motion6 = motionFunctionPrefs.getString("Zoom_in", "");
        motion6 = motionFunctionPrefs.getString("Zoom_out", "");


        motionFunctionPrefs.registerOnSharedPreferenceChangeListener(prefListener);
        if(save_btn != null){
            save_btn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                /**
                 * 서버에 모션-설정 저장을 위한 MotionFunctionDTOList를 생성하고 저장
                 * @param preference
                 * @return 성공 실패 여부
                 */
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    List<MotionFunctionDTO> motionFunctionDTOList = new ArrayList<>();
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motionFunctionPrefs.getString("Scroll_up","Head_up"), "Scroll_up"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motionFunctionPrefs.getString("Scroll_down","Head_down"), "Scroll_down"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motionFunctionPrefs.getString("Scroll_left","Head_left"), "Scroll_left"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motionFunctionPrefs.getString("Scroll_right","Head_right"), "Scroll_right"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motionFunctionPrefs.getString("Back","Eyes_close"), "Back"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motionFunctionPrefs.getString("Zoom_in","Eye_close_left"), "Zoom_in"));
                    motionFunctionDTOList.add(new MotionFunctionDTO(userId, motionFunctionPrefs.getString("Zoom_out","Eye_close_right"), "Zoom_out"));
                    saveMotionSetting(motionFunctionDTOList);
                    return true;
                }
            });
        }
    }//onCreate

    /**
     * 값이 변할경우 각 설정들에 대해 중복되는 값이 있는지 확인, 중복이 없다면 설정 변경
     */
    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            SharedPreferences.Editor editor = motionFunctionPrefs.edit();
//            Toast alertToast = Toast.makeText(getActivity(),"중복된 설정이 있습니다", Toast.LENGTH_SHORT);
//            Toast nothingToast = Toast.makeText(getActivity(),"없는 설정이 있습니다", Toast.LENGTH_SHORT);

            Log.d("changeListener", motionFunctionPrefs.getString("Scroll_up",""));
            Log.d("changeListener", motionFunctionPrefs.getString("Scroll_down",""));
            Log.d("changeListener", motionFunctionPrefs.getString("Scroll_left",""));
            Log.d("changeListener", motionFunctionPrefs.getString("Scroll_right",""));
            Log.d("changeListener", motionFunctionPrefs.getString("Back",""));
            Log.d("changeListener", motionFunctionPrefs.getString("Zoom_in",""));
            Log.d("changeListener", motionFunctionPrefs.getString("Zoom_out",""));
            Log.d("changeListener", "========================================================");


            if(key.equals("Scroll_up")){
                if(motionFunctionPrefs.getString("Scroll_up","").equals("Nothing")){
                    motionPreference1.setSummary(change_language(motionFunctionPrefs.getString("Scroll_up","")));
                    motionPreference1.setValue(motionFunctionPrefs.getString("Scroll_up",""));
                    motion1 = motionFunctionPrefs.getString("Scroll_up","");
                }
                else if(motionFunctionPrefs.getString("Scroll_up","").equals(motionFunctionPrefs.getString("Scroll_down",""))
                        ||motionFunctionPrefs.getString("Scroll_up","").equals(motionFunctionPrefs.getString("Scroll_left",""))
                        ||motionFunctionPrefs.getString("Scroll_up","").equals(motionFunctionPrefs.getString("Scroll_right",""))
                        ||motionFunctionPrefs.getString("Scroll_up","").equals(motionFunctionPrefs.getString("Back",""))
                        ||motionFunctionPrefs.getString("Scroll_up","").equals(motionFunctionPrefs.getString("Zoom_in",""))
                        ||motionFunctionPrefs.getString("Scroll_up","").equals(motionFunctionPrefs.getString("Zoom_out",""))){
                    changeduplication("Scroll_up",checkMotionduplication("Scroll_up"));
  //                  alertToast.show();
                }
                else{
                    motionPreference1.setSummary(change_language(motionFunctionPrefs.getString("Scroll_up","")));
                    motion1 = motionFunctionPrefs.getString("Scroll_up","");
                }
            }
            if(key.equals("Scroll_down")){
                if(motionFunctionPrefs.getString("Scroll_down","").equals("Nothing")){
                    motionPreference2.setSummary(change_language(motionFunctionPrefs.getString("Scroll_down","")));
                    motionPreference2.setValue(motionFunctionPrefs.getString("Scroll_down",""));
                    motion2 = motionFunctionPrefs.getString("Scroll_down","");
                }
                else if(motionFunctionPrefs.getString("Scroll_down","").equals(motionFunctionPrefs.getString("Scroll_up",""))
                        ||motionFunctionPrefs.getString("Scroll_down","").equals(motionFunctionPrefs.getString("Scroll_left",""))
                        ||motionFunctionPrefs.getString("Scroll_down","").equals(motionFunctionPrefs.getString("Scroll_right",""))
                        ||motionFunctionPrefs.getString("Scroll_down","").equals(motionFunctionPrefs.getString("Back",""))
                        ||motionFunctionPrefs.getString("Scroll_down","").equals(motionFunctionPrefs.getString("Zoom_in",""))
                        ||motionFunctionPrefs.getString("Scroll_down","").equals(motionFunctionPrefs.getString("Zoom_out",""))){
                    changeduplication("Scroll_down",checkMotionduplication("Scroll_down"));
     //               alertToast.show();
                }
                else{
                    motionPreference2.setSummary(change_language(motionFunctionPrefs.getString("Scroll_down","")));
                    motion2 = motionFunctionPrefs.getString("Scroll_down","");
                }
            }
            if(key.equals("Scroll_left")){
                if(motionFunctionPrefs.getString("Scroll_left","").equals("Nothing")){
                    motionPreference3.setSummary(change_language(motionFunctionPrefs.getString("Scroll_left","")));
                    motionPreference3.setValue(motionFunctionPrefs.getString("Scroll_left",""));
                    motion3 = motionFunctionPrefs.getString("Scroll_left","");
                }
                else if(motionFunctionPrefs.getString("Scroll_left","").equals(motionFunctionPrefs.getString("Scroll_up",""))
                        ||motionFunctionPrefs.getString("Scroll_left","").equals(motionFunctionPrefs.getString("Scroll_down",""))
                        ||motionFunctionPrefs.getString("Scroll_left","").equals(motionFunctionPrefs.getString("Scroll_right",""))
                        ||motionFunctionPrefs.getString("Scroll_left","").equals(motionFunctionPrefs.getString("Back",""))
                        ||motionFunctionPrefs.getString("Scroll_left","").equals(motionFunctionPrefs.getString("Zoom_in",""))
                        ||motionFunctionPrefs.getString("Scroll_left","").equals(motionFunctionPrefs.getString("Zoom_out",""))){
                    changeduplication("Scroll_left",checkMotionduplication("Scroll_left"));
          //          alertToast.show();
                }
                else{
                    motionPreference3.setSummary(change_language(motionFunctionPrefs.getString("Scroll_left","")));
                    motion3 = motionFunctionPrefs.getString("Scroll_left","");
                }
            }
            if(key.equals("Scroll_right")){
                if(motionFunctionPrefs.getString("Scroll_right","").equals("Nothing")){
                    motionPreference4.setSummary(change_language(motionFunctionPrefs.getString("Scroll_right","")));
                    motionPreference4.setValue(motionFunctionPrefs.getString("Scroll_right",""));
                    motion4 = motionFunctionPrefs.getString("Scroll_right","");
                }
                else if(motionFunctionPrefs.getString("Scroll_right","").equals(motionFunctionPrefs.getString("Scroll_up",""))
                        ||motionFunctionPrefs.getString("Scroll_right","").equals(motionFunctionPrefs.getString("Scroll_down",""))
                        ||motionFunctionPrefs.getString("Scroll_right","").equals(motionFunctionPrefs.getString("Scroll_left",""))
                        ||motionFunctionPrefs.getString("Scroll_right","").equals(motionFunctionPrefs.getString("Back",""))
                        ||motionFunctionPrefs.getString("Scroll_right","").equals(motionFunctionPrefs.getString("Zoom_in",""))
                        ||motionFunctionPrefs.getString("Scroll_right","").equals(motionFunctionPrefs.getString("Zoom_out",""))){
                    changeduplication("Scroll_right",checkMotionduplication("Scroll_right"));
       //             alertToast.show();
                }
                else{
                    motionPreference4.setSummary(change_language(motionFunctionPrefs.getString("Scroll_right","")));
                    motion4 = motionFunctionPrefs.getString("Scroll_right","");
                }
            }
            if(key.equals("Back")){
                if(motionFunctionPrefs.getString("Back","").equals("Nothing")){
                    motionPreference5.setSummary(change_language(motionFunctionPrefs.getString("Back","")));
                    motionPreference5.setValue(motionFunctionPrefs.getString("Back",""));
                    motion5 = motionFunctionPrefs.getString("Back","");
                }
                else if(motionFunctionPrefs.getString("Back","").equals(motionFunctionPrefs.getString("Scroll_up",""))
                        ||motionFunctionPrefs.getString("Back","").equals(motionFunctionPrefs.getString("Scroll_down",""))
                        ||motionFunctionPrefs.getString("Back","").equals(motionFunctionPrefs.getString("Scroll_left",""))
                        ||motionFunctionPrefs.getString("Back","").equals(motionFunctionPrefs.getString("Scroll_right",""))
                        ||motionFunctionPrefs.getString("Back","").equals(motionFunctionPrefs.getString("Zoom_in",""))
                        ||motionFunctionPrefs.getString("Back","").equals(motionFunctionPrefs.getString("Zoom_out",""))){
                    changeduplication("Back",checkMotionduplication("Back"));
      //              alertToast.show();
                }
                else{
                    motionPreference5.setSummary(change_language(motionFunctionPrefs.getString("Back","")));
                    motion5 = motionFunctionPrefs.getString("Back","");
                }
            }
            if(key.equals("Zoom_in")){
                if(motionFunctionPrefs.getString("Zoom_in","").equals("Nothing")){
                    motionPreference6.setSummary(change_language(motionFunctionPrefs.getString("Zoom_in","")));
                    motionPreference6.setValue(motionFunctionPrefs.getString("Zoom_in",""));
                    motion6 = motionFunctionPrefs.getString("Zoom_in","");
                }
                else if(motionFunctionPrefs.getString("Zoom_in","").equals(motionFunctionPrefs.getString("Scroll_up",""))
                        ||motionFunctionPrefs.getString("Zoom_in","").equals(motionFunctionPrefs.getString("Scroll_down",""))
                        ||motionFunctionPrefs.getString("Zoom_in","").equals(motionFunctionPrefs.getString("Scroll_left",""))
                        ||motionFunctionPrefs.getString("Zoom_in","").equals(motionFunctionPrefs.getString("Scroll_right",""))
                        ||motionFunctionPrefs.getString("Zoom_in","").equals(motionFunctionPrefs.getString("Back",""))
                        ||motionFunctionPrefs.getString("Zoom_in","").equals(motionFunctionPrefs.getString("Zoom_out",""))){
                    changeduplication("Zoom_in",checkMotionduplication("Zoom_in"));
       //             alertToast.show();
                }
                else{
                    motionPreference6.setSummary(change_language(motionFunctionPrefs.getString("Zoom_in","")));
                    motion6 = motionFunctionPrefs.getString("Zoom_in","");
                }
            }
            if(key.equals("Zoom_out")){
                if(motionFunctionPrefs.getString("Zoom_out","").equals("Nothing")){
                    motionPreference7.setSummary(change_language(motionFunctionPrefs.getString("Zoom_out","")));
                    motionPreference7.setValue(motionFunctionPrefs.getString("Zoom_out",""));
                    motion7 = motionFunctionPrefs.getString("Zoom_out","");
                }
                else if(motionFunctionPrefs.getString("Zoom_out","").equals(motionFunctionPrefs.getString("Scroll_up",""))
                        ||motionFunctionPrefs.getString("Zoom_out","").equals(motionFunctionPrefs.getString("Scroll_down",""))
                        ||motionFunctionPrefs.getString("Zoom_out","").equals(motionFunctionPrefs.getString("Scroll_left",""))
                        ||motionFunctionPrefs.getString("Zoom_out","").equals(motionFunctionPrefs.getString("Scroll_right",""))
                        ||motionFunctionPrefs.getString("Zoom_out","").equals(motionFunctionPrefs.getString("Back",""))
                        ||motionFunctionPrefs.getString("Zoom_out","").equals(motionFunctionPrefs.getString("Zoom_in",""))){
                    changeduplication("Zoom_out",checkMotionduplication("Zoom_out"));
      //              alertToast.show();
                }
                else{
                    motionPreference7.setSummary(change_language(motionFunctionPrefs.getString("Zoom_out","")));
                    motion7 = motionFunctionPrefs.getString("Zoom_out","");
                }
            }
        }
    };

    /**
     * 저장된 MotionFunctionDTOList를 서버에 저장을 위해 요청을 보냄.
     * @param motionFunctionDTOList 모션-설정이 저장되어있는 MotionFunctionDTOList
     */
    private void saveMotionSetting(List<MotionFunctionDTO> motionFunctionDTOList){
        motionFunctionApi.saveMotionSetting(motionFunctionDTOList).enqueue(
                new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call,
                                           retrofit2.Response<BaseResponse> response) {
                        if(response.isSuccessful()){
                            Toast.makeText(getActivity(), response.body().getResultMsg(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "왜 실패죠", Toast.LENGTH_SHORT).show();
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

    /**
     * 모션 설정에서 중복이 있는지 검사
     * @param code 모션 String
     * @return 중복이 있다면 중복되는 String 반환, 없다면 nothing
     */
    private String checkMotionduplication(String code){
        if(!code.equals("Scroll_up")){
            if(checkduplication("Scroll_up", code)) return "Scroll_up";
        }
        if(!code.equals("Scroll_down")){
            if(checkduplication("Scroll_down", code)) return "Scroll_down";
        }
        if(!code.equals("Scroll_left")){
            if(checkduplication("Scroll_left", code)) return "Scroll_left";
        }
        if(!code.equals("Scroll_right")){
            if(checkduplication("Scroll_right", code)) return "Scroll_right";
        }
        if(!code.equals("Back")){
            if(checkduplication("Back", code)) return "Back";
        }
        if(!code.equals("Zoom_in")){
            if(checkduplication("Zoom_in", code)) return "Zoom_in";
        }
        if(!code.equals("Zoom_out")){
            if(checkduplication("Zoom_out", code)) return "Zoom_out";
        }
        else return "Nothing";
        return "Nothing";
    }

    private void setDefaultFunctionString(){
        motionString = new HashMap<>();
        motionString.put("Nothing", "없음");
        motionString.put("Head_up", "머리 위로");
        motionString.put("Head_down", "머리 아래로");
        motionString.put("Head_left", "머리 왼쪽으로");
        motionString.put("Head_right", "머리 오른쪽으로");
        motionString.put("Eyes_close", "양쪽 눈 감기");
        motionString.put("Eye_close_left", "왼쪽 눈 감기");
        motionString.put("Eye_close_right", "오른쪽 눈 감기");
        motionString.put("없음", "Nothing");
        motionString.put("머리 위로", "Head_up");
        motionString.put("머리 아래로", "Head_down");
        motionString.put("머리 왼쪽으로", "Head_left");
        motionString.put("머리 오른쪽으로", "Head_right");
        motionString.put("양쪽 눈 감기", "Eyes_close");
        motionString.put("왼쪽 눈 감기", "Eye_close_left");
        motionString.put("오른쪽 눈 감기", "Eye_close_right");
    }

    //HashMap으로 영어 - 한글 변환
    private String change_language(String str){
        return motionString.get(str);
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

    /**
     * 두 String 간의 중복 확인
     * @param code1 확인 대상 1
     * @param code2 확인 대상 2
     * @return 중복이라면 True, 아니라면 False
     */
    private Boolean checkduplication(String code1, String code2){
        if(motionFunctionPrefs.getString(code1,"1").equals(motionFunctionPrefs.getString(code2,"2"))){
            return true;
        }
        else return false;
    }
    //중복일 경우 변경사항을 이전으로 되돌리는 코드
    private void changeduplication(String code1, String code2){ //code1: 사용자가 바꾼 모션 설정, code2: 바꾼 설정과 중복되는 모션
        SharedPreferences.Editor editor = motionFunctionPrefs.edit();
        editor.putString(code2, "Nothing");
        editor.apply();
        //code1 이전 설정 변경
        if(code1.equals("Scroll_up")) {
            motion1 =motionFunctionPrefs.getString(code1, "");
            motionPreference1.setSummary(change_language(motionFunctionPrefs.getString(code1,"")));
        }
        else if(code1.equals("Scroll_down")) {
            motion2 = motionFunctionPrefs.getString(code1, "");
            motionPreference2.setSummary(change_language(motionFunctionPrefs.getString(code1,"")));
        }
        else if(code1.equals("Scroll_left")) {
            motion3 = motionFunctionPrefs.getString(code1, "");
            motionPreference3.setSummary(change_language(motionFunctionPrefs.getString(code1,"")));
        }
        else if(code1.equals("Scroll_right")) {
            motion4 = motionFunctionPrefs.getString(code1, "");
            motionPreference4.setSummary(change_language(motionFunctionPrefs.getString(code1,"")));
        }
        else if(code1.equals("Back")) {
            motion5 = motionFunctionPrefs.getString(code1, "");
            motionPreference5.setSummary(change_language(motionFunctionPrefs.getString(code1,"")));
        }
        else if(code1.equals("Zoom_in")) {
            motion6 = motionFunctionPrefs.getString(code1, "");
            motionPreference6.setSummary(change_language(motionFunctionPrefs.getString(code1,"")));
        }
        else if(code1.equals("Zoom_out")) {
            motion7 = motionFunctionPrefs.getString(code1, "");
            motionPreference7.setSummary(change_language(motionFunctionPrefs.getString(code1,"")));
        }
        //code2 이전 설정 변경
        if(code2.equals("Scroll_up")) {
            motion1 = motionFunctionPrefs.getString(code2, "");
            motionPreference1.setSummary(change_language(motionFunctionPrefs.getString(code2,"")));
        }
        else if(code2.equals("Scroll_down")) {
            motion2 = motionFunctionPrefs.getString(code2, "");
            motionPreference2.setSummary(change_language(motionFunctionPrefs.getString(code2,"")));
        }
        else if(code2.equals("Scroll_left")) {
            motion3 = motionFunctionPrefs.getString(code2, "");
            motionPreference3.setSummary(change_language(motionFunctionPrefs.getString(code2,"")));
        }
        else if(code2.equals("Scroll_right")) {
            motion4 = motionFunctionPrefs.getString(code2, "");
            motionPreference4.setSummary(change_language(motionFunctionPrefs.getString(code2,"")));
        }
        else if(code2.equals("Back")) {
            motion5 = motionFunctionPrefs.getString(code2, "");
            motionPreference5.setSummary(change_language(motionFunctionPrefs.getString(code2,"")));
        }
        else if(code2.equals("Zoom_in")) {
            motion6 = motionFunctionPrefs.getString(code2, "");
            motionPreference6.setSummary(change_language(motionFunctionPrefs.getString(code2,"")));
        }
        else if(code2.equals("Zoom_out")) {
            motion7 = motionFunctionPrefs.getString(code2, "");
            motionPreference7.setSummary(change_language(motionFunctionPrefs.getString(code2,"")));
        }
    }

}