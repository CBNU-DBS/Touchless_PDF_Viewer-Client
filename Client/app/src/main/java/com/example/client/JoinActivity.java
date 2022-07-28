package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import java.util.IllegalFormatException;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;

public class JoinActivity extends AppCompatActivity {
    EditText    et_join_name;
    EditText    et_join_password;
    EditText    et_join_confirmPw;
    EditText    et_join_email;
    EditText    et_join_phone;
    Button      btn_join_register;
    String emailValidation = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        userApi = RetrofitClient.getClient().create(UserApi.class);
        et_join_name        = findViewById(R.id.et_join_name);
        et_join_email       = findViewById(R.id.et_join_email);
        et_join_password    = findViewById(R.id.et_join_password);
        et_join_confirmPw   = findViewById(R.id.et_join_confirmPw);
        et_join_phone       = findViewById(R.id.et_join_phone);
        btn_join_register   = findViewById(R.id.btn_join_register);

        // 회원가입 버튼 이벤트 리스너
        btn_join_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalFormatException {
                String name         = et_join_name.getText().toString();
                String email        = et_join_email.getText().toString();
                String password     = et_join_password.getText().toString();
                String confirmPw    = et_join_confirmPw.getText().toString();
                String phone        = et_join_phone.getText().toString();
                if(false == checkName(name)){
                    return;
                }
                if(false == checkPassword(password, confirmPw)){
                    return;
                }
                if(false == checkEmail(email)){
                    return;
                }
                if(false == checkPhone(phone)){
                    return;
                }
                UserDTO user = new UserDTO(name,email,password, phone);

                join(user);

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private Boolean checkName(String name){
        if(true == TextUtils.isEmpty(name)){
            Toast.makeText(getApplicationContext(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_join_name.requestFocus();
            return false;
        }
        return true;
    }

    private Boolean checkPassword(String password, String confirmPw){
        // 숫자, 문자, 특수문자를 모두 포함한 8~15자리
        String passwordValidation = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&]).{8,15}.$";

        if(true == TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_join_password.requestFocus();
            return false;
        }
        if(password.length() < 8){
            Toast.makeText(getApplicationContext(), "비밀번호를 8자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_join_password.requestFocus();
            return false;
        }
        if(false == Pattern.matches(passwordValidation, password)){
            Toast.makeText(getApplicationContext(), "비밀번호는 숫자, 문자, 특수문자를 모두 포함한 8~15자리이여야 합니다.", Toast.LENGTH_SHORT).show();
            et_join_password.requestFocus();
            return false;
        }
        if(true == TextUtils.isEmpty(confirmPw)){
            Toast.makeText(getApplicationContext(), "비밀번호 확인을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_join_confirmPw.requestFocus();
            return false;
        }
        if(false == password.equals(confirmPw)){
            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            et_join_confirmPw.requestFocus();
            return false;
        }
        return true;
    }

    private Boolean checkEmail(String email){
        // 이메일 확인 정규식
        if(true == TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(), "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_join_email.requestFocus();
            return false;
        }
        if(false == Pattern.matches(emailValidation, email)){
            Toast.makeText(getApplicationContext(), "이메일 형식을 확인해수제요.", Toast.LENGTH_SHORT).show();
            et_join_email.requestFocus();
            return false;
        }
        return true;
    }

    private Boolean checkPhone(String phone){
        if(true == TextUtils.isEmpty(phone)){
            Toast.makeText(getApplicationContext(), "전화번호 입력해주세요.", Toast.LENGTH_SHORT).show();
            et_join_phone.requestFocus();
            return false;
        }
        return true;
    }

    private void join(UserDTO user){
        userApi.joinUser(user).enqueue(new Callback<ResponseDTO<UserDTO>>() {
            @Override
            public void onResponse(Call<ResponseDTO<UserDTO>> call,
                    retrofit2.Response<ResponseDTO<UserDTO>> response) {
                ResponseDTO<UserDTO> result = response.body();
                Toast.makeText(getApplicationContext(), result.getResultMsg(), Toast.LENGTH_SHORT).show();
                if(result.getResultCode() == 0){
                    finish();
                }

            }

            @Override
            public void onFailure(Call<ResponseDTO<UserDTO>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_SHORT).show();
                Log.e("Sign Up Error", t.getMessage());
                t.printStackTrace();
            }
        });
    }

}