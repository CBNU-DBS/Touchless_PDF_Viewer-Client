package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MotionSettingActivity extends AppCompatActivity {

    private Spinner spinner_ms_scrollDown;
    private Spinner spinner_ms_scrollUp;
    private Spinner spinner_ms_nextPg;
    private Spinner spinner_ms_prevPg;
    private Spinner spinner_ms_back;
    private Spinner spinner_ms_search;
    private Spinner spinner_ms_zoomIn;
    private Spinner spinner_ms_zoomOut;

    private Button btn_ms_save;

    private List<String> spinnerItems = new ArrayList<>();
    private MotionSettingSpinnerAdapter spinnerScrollDownAdapter;
    private MotionSettingSpinnerAdapter spinnerScrollUpAdapter;
    private MotionSettingSpinnerAdapter spinnerNextPgAdapter;
    private MotionSettingSpinnerAdapter spinnerPrevPgAdapter;
    private MotionSettingSpinnerAdapter spinnerBackAdapter;
    private MotionSettingSpinnerAdapter spinnerSearchAdapter;
    private MotionSettingSpinnerAdapter spinnerZoomInAdapter;
    private MotionSettingSpinnerAdapter spinnerZoomOutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_setting);

        spinner_ms_scrollDown = findViewById(R.id.spinner_ms_scrollDown);
        spinner_ms_scrollUp = findViewById(R.id.spinner_ms_scrollUp);
        spinner_ms_nextPg = findViewById(R.id.spinner_ms_nextPg);
        spinner_ms_prevPg = findViewById(R.id.spinner_ms_prevPg);
        spinner_ms_back = findViewById(R.id.spinner_ms_back);
        spinner_ms_search = findViewById(R.id.spinner_ms_search);
        spinner_ms_zoomIn = findViewById(R.id.spinner_ms_zoomIn);
        spinner_ms_zoomOut = findViewById(R.id.spinner_ms_zoomOut);
        btn_ms_save = findViewById(R.id.btn_ms_save);

        setSpinnerAdapter();

        btn_ms_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkDuplicateMotion() == false){
                    Toast.makeText(getApplicationContext(), "모션이 중복됩니다.", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "성공", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setSpinnerAdapter(){
        try{
            spinnerItems = Arrays.asList(getResources().getStringArray(R.array.motion_array));
            spinnerScrollDownAdapter = new MotionSettingSpinnerAdapter(this, spinnerItems);
            spinnerScrollUpAdapter = new MotionSettingSpinnerAdapter(this, spinnerItems);
            spinnerNextPgAdapter = new MotionSettingSpinnerAdapter(this, spinnerItems);
            spinnerPrevPgAdapter = new MotionSettingSpinnerAdapter(this, spinnerItems);
            spinnerBackAdapter = new MotionSettingSpinnerAdapter(this, spinnerItems);
            spinnerSearchAdapter = new MotionSettingSpinnerAdapter(this, spinnerItems);
            spinnerZoomInAdapter = new MotionSettingSpinnerAdapter(this, spinnerItems);
            spinnerZoomOutAdapter = new MotionSettingSpinnerAdapter(this, spinnerItems);

            spinner_ms_scrollDown.setAdapter(spinnerScrollDownAdapter);
            spinner_ms_scrollUp.setAdapter(spinnerScrollUpAdapter);
            spinner_ms_nextPg.setAdapter(spinnerNextPgAdapter);
            spinner_ms_prevPg.setAdapter(spinnerPrevPgAdapter);
            spinner_ms_back.setAdapter(spinnerBackAdapter);
            spinner_ms_search.setAdapter(spinnerSearchAdapter);
            spinner_ms_zoomIn.setAdapter(spinnerZoomInAdapter);
            spinner_ms_zoomOut.setAdapter(spinnerZoomOutAdapter);
        } catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }

    }

    private boolean checkDuplicateMotion(){
        Set<String> motionSet = new HashSet<>();
        motionSet.add(spinner_ms_scrollDown.getSelectedItem().toString());
        motionSet.add(spinner_ms_scrollUp.getSelectedItem().toString());
        motionSet.add(spinner_ms_nextPg.getSelectedItem().toString());
        motionSet.add(spinner_ms_prevPg.getSelectedItem().toString());
        motionSet.add(spinner_ms_back.getSelectedItem().toString());
        motionSet.add(spinner_ms_search.getSelectedItem().toString());
        motionSet.add(spinner_ms_zoomIn.getSelectedItem().toString());
        motionSet.add(spinner_ms_zoomOut.getSelectedItem().toString());
        if(motionSet.size() == 8){
            return true;
        }
        return false;
    }
}