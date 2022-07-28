package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 로그인 버튼 이벤트 리스너
        Button btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class );
                startActivity(intent);
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



        // 잠시 테스트중
        Button btn_test1 = (Button) findViewById(R.id.btn_test1);
        btn_test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PDF_View_Activity.class);
                intent.putExtra("pdfname", "test1.pdf");
                startActivity(intent);
            }
        });
        Button btn_test2 = (Button) findViewById(R.id.btn_test2);
        btn_test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PDF_View_Activity.class);
                intent.putExtra("pdfname", "test2.pdf");
                startActivity(intent);
            }
        });

    }
}