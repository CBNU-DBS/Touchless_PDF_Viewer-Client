package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.client.api.UserApi;
import com.example.client.dto.ResponseDTO;
import com.example.client.dto.ChangeDTO;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Personal_info_setting_Activity extends AppCompatActivity {

    UserApi userApi;

    SharedPreferences sharedPref_login;
    SharedPreferences.Editor editor_login;

    Long DBuser_id;
    String DBuser_pw;

    Button currentPW_Btn;
    Button changePW_Btn;

    EditText et_currentPW0;
    String currentPW;
    EditText et_newPW0;
    EditText et_confirm_newPW0;
    String newPW;
    String confirm_newPW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info_setting);

        userApi = RetrofitClient.getClient().create(UserApi.class);

        currentPW_Btn = (Button) findViewById(R.id.currentPW_btn);
        changePW_Btn = (Button) findViewById(R.id.changePW_btn);

        //현재 비밀번호를 확인하지 않으면 변경버튼이 보이지 않습니다.
        changePW_Btn.setVisibility(View.INVISIBLE);

        currentPW_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_currentPW0 = (EditText)findViewById(R.id.et_currentPW);
                currentPW = et_currentPW0.getText().toString();
                sharedPref_login = getSharedPreferences("auto_login",MODE_PRIVATE);
                DBuser_pw = sharedPref_login.getString("auto_pw0","");

                Log.e("기존 비밀번호",DBuser_pw);
                Log.e("현재 비밀번호 확인", currentPW);

                if(checkPassword(currentPW)) {
                    if (DBuser_pw.equals(currentPW)) {
                        Toast.makeText(getApplicationContext(), "비밀번호 확인 완료", Toast.LENGTH_SHORT).show();
                        changePW_Btn.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getApplicationContext(), "현재 비밀번호와 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                    //현재 저장되어있는 비밀번호를 확인합니다.
                }
            }
        });

        changePW_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_newPW0 = findViewById(R.id.et_newPW);
                et_confirm_newPW0 = findViewById(R.id.et_confirm_newPW);
                newPW = et_newPW0.getText().toString();
                confirm_newPW = et_confirm_newPW0.getText().toString();

                sharedPref_login = getSharedPreferences("auto_login",MODE_PRIVATE);
                DBuser_id = sharedPref_login.getLong("auto_id0",0);

                Log.e("새 비밀번호",newPW);
                Log.e("새 비밀번호 확인", confirm_newPW);
                Log.e("아이디 확인",Long.toString(DBuser_id));

                if(newPW.equals(confirm_newPW)){
                    if(checkPassword(confirm_newPW)) {
                        ChangeDTO change_pw = new ChangeDTO(DBuser_id, confirm_newPW);
                        Log.e("아이디 잘 저장됬는지",Long.toString(change_pw.getId()));
                        Log.e("비밀번호 잘 저장됬는지",change_pw.getPassword());

                        changePassword(change_pw);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "새 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                }
                //기존 비밀번호를 새로운 비밀번호로 변경합니다.
                //DB와 연동하여 비밀번호가 변경되면 토스트 메세지를 띄웁니다.
            }
        });


        Button back_to_mypage_Btn = (Button) findViewById(R.id.btn_back_to_mypage);
        back_to_mypage_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    private Boolean checkPassword(String password){
        // 숫자, 문자, 특수문자를 모두 포함한 8~15자리
        String passwordValidation = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&]).{8,15}.$";

        if(true == TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_confirm_newPW0.requestFocus();
            return false;
        }
        if(password.length() < 8){
            Toast.makeText(getApplicationContext(), "비밀번호를 8자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_confirm_newPW0.requestFocus();
            return false;
        }
        if(false == Pattern.matches(passwordValidation, password)){
            Toast.makeText(getApplicationContext(), "비밀번호는 숫자, 문자, 특수문자를 모두 포함한 8~15자리이여야 합니다.", Toast.LENGTH_SHORT).show();
            et_confirm_newPW0.requestFocus();
            return false;
        }
        return true;
    }

    private void changePassword(ChangeDTO change){
        userApi.changePW(change).enqueue(new Callback<ResponseDTO<ChangeDTO>>() {
            @Override
            public void onResponse(Call<ResponseDTO<ChangeDTO>> call,
                                   Response<ResponseDTO<ChangeDTO>> response) {
            if(response.isSuccessful()){
                Toast.makeText(getApplicationContext(), response.body().getResultMsg(),Toast.LENGTH_SHORT).show();
                Log.e("","비밀번호 변경 성공(Client)");
                finish();

                // 비밀번호 변경 시, 기존의 자동로그인 정보를 삭제합니다.
                sharedPref_login = getSharedPreferences("auto_login",MODE_PRIVATE);
                editor_login = sharedPref_login.edit();
                editor_login.clear();
                editor_login.commit();
                DBuser_pw = sharedPref_login.getString("auto_pw0","");

                Log.e("변경 후 초기화되어있어야 되는 비밀번호 값",DBuser_pw);

                Toast.makeText(getApplicationContext(), "비밀번호 변경 성공", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "다시 로그인 해주세요", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            } else {
                Log.e("","비밀번호 변경 실패");
            }
        }

            @Override
            public void onFailure(Call<ResponseDTO<ChangeDTO>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "비밀번호 변경 서버 통신 실패", Toast.LENGTH_SHORT).show();
                Log.e("Change Password Error", t.getMessage());
                t.printStackTrace();
            }
        });
    }

}