package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.client.api.UserApi;
import com.example.client.dto.ResponseDTO;
import com.example.client.dto.UserDTO;

import java.util.regex.Pattern;

import retrofit2.Callback;

public class Personal_info_setting_Activity extends AppCompatActivity {

    UserApi userApi;

    SharedPreferences sharedPref_login;
    Long DBuser_id;
    String DBuser_email;
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

        sharedPref_login = getSharedPreferences("auto_login",MODE_PRIVATE);
        DBuser_pw = sharedPref_login.getString("auto_pw0","");
        DBuser_email = sharedPref_login.getString("auto_email0","");
        DBuser_id = sharedPref_login.getLong("auto_id0",0);

        currentPW_Btn = (Button) findViewById(R.id.currentPW_btn);
        changePW_Btn = (Button) findViewById(R.id.changePW_btn);

        et_currentPW0 = findViewById(R.id.et_currentPW);
        currentPW = et_currentPW0.getText().toString();

        //현재 비밀번호를 확인하지 않으면 변경버튼이 보이지 않습니다.
        changePW_Btn.setVisibility(View.INVISIBLE);

        currentPW_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DBuser_pw == currentPW){
                    Toast.makeText(getApplicationContext(), "비밀번호 확인 완료", Toast.LENGTH_SHORT).show();
                    changePW_Btn.setVisibility(View.VISIBLE);
                }
                //현재 저장되어있는 비밀번호를 확인합니다.
            }
        });

        et_newPW0 = findViewById(R.id.et_newPW);
        et_confirm_newPW0 = findViewById(R.id.et_confirm_newPW);
        newPW = et_newPW0.getText().toString();
        confirm_newPW = et_confirm_newPW0.getText().toString();

        changePW_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(newPW != confirm_newPW){
                    Toast.makeText(getApplicationContext(), "새 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                }else if(checkPassword(confirm_newPW)){
                    UserDTO change_pw = new UserDTO(DBuser_id, DBuser_email, DBuser_pw);
                    //changePassword(change_pw);
                    //여기에 서버를 통해서 비밀번호 변경하는 코드가 들어오면 된다.
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

//    private void changePassword(UserDTO user){
//        userApi.changePW(user).enqueue(new Callback<ResponseDTO<UserDTO>>() {
//        @Override
//            public void onResponse()
//        });
//    }

}