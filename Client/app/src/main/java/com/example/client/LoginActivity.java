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
import com.example.client.dto.UserDTO;

import java.io.IOException;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText et_login_email;
    EditText et_login_password;

    UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userApi = RetrofitClient.getClient().create(UserApi.class);
        et_login_email = findViewById(R.id.text_input_id);
        et_login_password = findViewById(R.id.text_input_password);

        //첫 로그인 이후에 ID와 PW의 저장 유무 확인을 통한 자동로그인
        SharedPreferences sharedPref_login = getSharedPreferences("auto_login",MODE_PRIVATE);
        String auto_email1 = sharedPref_login.getString("auto_email0","");
        String auto_pw1 = sharedPref_login.getString("auto_pw0","");

        //로그인을 성공한 이후에는 자동로그인 실행
        if(auto_email1 != "" && auto_pw1 != ""){
            Toast.makeText(getApplicationContext(), "자동로그인", Toast.LENGTH_SHORT).show();
            UserDTO user = new UserDTO("", auto_email1, auto_pw1, "");
            login(user);
        }

        // 로그인 버튼 이벤트 리스너
        Button btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = et_login_email.getText().toString();
                String password = et_login_password.getText().toString();
                // 자동로그인을 위한 패스워드 저장
                SharedPreferences sharedPref_login = getSharedPreferences("auto_login",MODE_PRIVATE);
                SharedPreferences.Editor editor_login = sharedPref_login.edit();
                editor_login.putString("auto_pw0", password);
                editor_login.commit();
                Log.e("저장된 패스워드",sharedPref_login.getString("auto_pw0",""));


                if(false == checkEmail(email)){
                    return;
                }
                if(false == checkPassword(password)){
                    return;
                }


                UserDTO user = new UserDTO("", email, password, "");

                login(user);
            }
        });

        // 회원가입 버튼 이벤트 리스너
        Button btn_join = (Button) findViewById(R.id.btn_join);
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(intent);
            }
        });

    }

    private Boolean checkEmail(String email){
        if(true == TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(), "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_login_email.requestFocus();
            return false;
        }
        return true;
    }

    private Boolean checkPassword(String password){
        // 숫자, 문자, 특수문자를 모두 포함한 8~15자리
        String passwordValidation = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&]).{8,15}.$";

        if(true == TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_login_password.requestFocus();
            return false;
        }
        if(password.length() < 8){
            Toast.makeText(getApplicationContext(), "비밀번호를 8자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_login_password.requestFocus();
            return false;
        }
        if(false == Pattern.matches(passwordValidation, password)){
            Toast.makeText(getApplicationContext(), "비밀번호는 숫자, 문자, 특수문자를 모두 포함한 8~15자리이여야 합니다.", Toast.LENGTH_SHORT).show();
            et_login_password.requestFocus();
            return false;
        }
        return true;
    }

    private void login(UserDTO user){
        userApi.loginUser(user).enqueue(new Callback<ResponseDTO<UserDTO>>() {
            @Override
            public void onResponse(Call<ResponseDTO<UserDTO>> call,
                                   Response<ResponseDTO<UserDTO>> response) {
                ResponseDTO<UserDTO> result = response.body();
                if(response.isSuccessful()){
                    Toast.makeText(getApplicationContext(), result.getResultMsg(), Toast.LENGTH_SHORT).show();
                    Log.i("", "로그인 성공");
                    finish();

                    Intent logined_intent = new Intent(getApplicationContext(), HomeActivity.class);

                    //로그인 성공 후, 자동 로그인 설정
                    SharedPreferences sharedPref_login = getSharedPreferences("auto_login",MODE_PRIVATE);
                    SharedPreferences.Editor editor_login = sharedPref_login.edit();

                    //자동로그인을 위한 아이디(이메일) 저장
                    editor_login.putString("auto_email0",result.getList().get(0).getEmail());
                    //앱을 사용동안 사용될 유저 개인정보 저장
                    editor_login.putString("auto_name0",result.getList().get(0).getName());
                    editor_login.putLong("auto_id0",result.getList().get(0).getId());
                    editor_login.putString("auto_phone0",result.getList().get(0).getPhone());
                    editor_login.commit();

                    //로그인 성공하여 마이페이지로 이동
                    startActivity(logined_intent);
                } else {
                    Log.e("", "로그인 실패");
                    try {
                        String body = response.errorBody().string();
                        if(body.charAt(15) == 1){
                            Toast.makeText(getApplicationContext(), body.substring(30,44), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), body.substring(30,45), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseDTO<UserDTO>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "로그인 오류", Toast.LENGTH_SHORT).show();
                Log.e("Login Error", t.getMessage());
                t.printStackTrace();
            }
        });
    }
}