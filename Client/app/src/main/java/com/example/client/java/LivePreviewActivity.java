package com.example.client.java;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.common.annotation.KeepName;
import com.example.client.CameraSource;
import com.example.client.CameraSourcePreview;
import com.example.client.GraphicOverlay;
import com.example.client.R;
import com.example.client.java.facedetector.FaceDetectorProcessor;
import com.example.client.preference.SettingsActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//수정중
import androidx.core.app.ActivityCompat;


/** ML Kit API에 대한 미리보기. */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
        implements OnItemSelectedListener, CompoundButton.OnCheckedChangeListener,ActivityCompat.OnRequestPermissionsResultCallback {
    //MLkit에서 지원하는 모든 기능들. 해당 앱에서는 Face Detection만 사용
    private static final String OBJECT_DETECTION = "Object Detection";
    private static final String OBJECT_DETECTION_CUSTOM = "Custom Object Detection";
    private static final String CUSTOM_AUTOML_OBJECT_DETECTION =
            "Custom AutoML Object Detection (Flower)";
    private static final String FACE_DETECTION = "Face Detection";
    private static final String BARCODE_SCANNING = "Barcode Scanning";
    private static final String IMAGE_LABELING = "Image Labeling";
    private static final String IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Birds)";
    private static final String CUSTOM_AUTOML_LABELING = "Custom AutoML Image Labeling (Flower)";
    private static final String POSE_DETECTION = "Pose Detection";
    private static final String SELFIE_SEGMENTATION = "Selfie Segmentation";
    private static final String TEXT_RECOGNITION_LATIN = "Text Recognition Latin";
    private static final String TEXT_RECOGNITION_CHINESE = "Text Recognition Chinese (Beta)";
    private static final String TEXT_RECOGNITION_DEVANAGARI = "Text Recognition Devanagari (Beta)";
    private static final String TEXT_RECOGNITION_JAPANESE = "Text Recognition Japanese (Beta)";
    private static final String TEXT_RECOGNITION_KOREAN = "Text Recognition Korean (Beta)";

    private static final String TAG = "LivePreviewActivity";

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = FACE_DETECTION;

    /**
     * 생성자, 카메라 미리보기 실행을 위한 CameraX API 확인
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_vision_live_preview);

        preview = findViewById(R.id.preview_view);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        //스피너 선언 및 기설정된 옵션을 가져와서 List
        Spinner spinner = findViewById(R.id.spinner);
        List<String> options = new ArrayList<>();
        options.add(OBJECT_DETECTION);
        options.add(OBJECT_DETECTION_CUSTOM);
        options.add(CUSTOM_AUTOML_OBJECT_DETECTION);
        options.add(FACE_DETECTION);
        options.add(BARCODE_SCANNING);
        options.add(IMAGE_LABELING);
        options.add(IMAGE_LABELING_CUSTOM);
        options.add(CUSTOM_AUTOML_LABELING);
        options.add(POSE_DETECTION);
        options.add(SELFIE_SEGMENTATION);
        options.add(TEXT_RECOGNITION_LATIN);
        options.add(TEXT_RECOGNITION_CHINESE);
        options.add(TEXT_RECOGNITION_DEVANAGARI);
        options.add(TEXT_RECOGNITION_JAPANESE);
        options.add(TEXT_RECOGNITION_KOREAN);

        // spinner를 위한 Adapter 선언
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_outer_style, options);
        // 드롭다운 레이아웃 스타일 - 라디오 단추가 있는 목록 보기
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너에 데이터 어댑터 연결
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);

        ToggleButton facingSwitch = findViewById(R.id.facing_switch);
        facingSwitch.setOnCheckedChangeListener(this);

        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    intent.putExtra(
                            SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.LIVE_PREVIEW);
                    startActivity(intent);
                });

        createCameraSource(selectedModel);
    }

    // 항목이 선택된다면 선택된 항목을 검색하고 해당 항목으로 실행
    // 해당 앱에서는 Face Detection으로 고정하여 사용
    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        selectedModel = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "Selected model: " + selectedModel);
        preview.stop();
        createCameraSource(selectedModel);
        startCameraSource();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // 아무것도 하지 않음
    }

    /**
     * CheckChanged 컨트롤의 CheckBox 이밴트 발생시 실행
     * 카메라 방향 확인, 해당 앱에서는 전면 카메라로 고정하여 사용
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }

    /**
     * 기존 카cameraSource가 없다면, 새로운 cameraSource 생성
     * @param model
     */
    private void createCameraSource(String model) {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        /*face_detection 만 실행되게 수정함*/
        try {

            Log.i(TAG, "Using Face Detector Processor");
            cameraSource.setMachineLearningFrameProcessor(new FaceDetectorProcessor(this));
        } catch (RuntimeException e) {
            Log.e(TAG, "Can not create image processor: " + model, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * cameraSource가 있는 경우 해당 소스를 시작하거나 다시 시작
     * cameraSource가 없는 경우 cameraSource가 생성될 때 다시 호출
     * cameraSource가 생성되기 전에 호출이 계속될 수 있기 때문에 작성
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    /** 카메라 다시 재생시 */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        createCameraSource(selectedModel);
        startCameraSource();
    }

    /** 카메라 일시 정지시 */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    /** 카메라 종료시 */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}
