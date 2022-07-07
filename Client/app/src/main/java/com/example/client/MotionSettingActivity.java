package com.example.client;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.widget.ArrayAdapter;

public class MotionSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_setting);

        // 어댑터 생성 (strings.xml에 배열과 spinner 연결)
        ArrayAdapter<CharSequence> adapter =
                        ArrayAdapter.createFromResource(this,R.array.motion_array, R.layout.spinner_style);
        // 드랍다운 뷰와 연결
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

    }
}