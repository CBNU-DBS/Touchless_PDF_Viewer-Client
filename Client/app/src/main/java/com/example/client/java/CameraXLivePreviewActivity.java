package com.example.client.java;

import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;
import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.MlKitException;
import com.example.client.CameraXViewModel;
import com.example.client.GraphicOverlay;
import com.example.client.R;
import com.example.client.VisionImageProcessor;
import com.example.client.java.facedetector.FaceDetectorProcessor;
import com.example.client.preference.PreferenceUtils;
import com.example.client.preference.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * CameraX를 이용한 ML키트 API용 라이브 미리보기
 */
@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class CameraXLivePreviewActivity extends AppCompatActivity
        implements OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {
    //MLkit에서 전채적으로 지원하는 기능들, 해당 앱에서는 FaceDetection 활용
    private static final String TAG = "CameraXLivePreview";
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
    private static final String STATE_SELECTED_MODEL = "selected_model";

    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;

    @Nullable private ProcessCameraProvider cameraProvider;
    @Nullable private Preview previewUseCase;
    @Nullable private ImageAnalysis analysisUseCase;
    @Nullable private VisionImageProcessor imageProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private String selectedModel = OBJECT_DETECTION;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraSelector cameraSelector;

    /**
     * 생성자, 사용자 기기 카메라 활성화 및 FaceDetection 기설정된 옵션 가져오기.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (savedInstanceState != null) {
            selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, OBJECT_DETECTION);
        }
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        setContentView(R.layout.activity_vision_camerax_live_preview);
        previewView = findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        Spinner spinner = findViewById(R.id.spinner);
        //기설정된 FaceDetection옵션들. 앱상에서 수정 불가능
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

        // spinner용 Adapter 생성
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_outer_style, options);
        // 드롭다운 레이아웃 스타일 - 라디오 단추가 있는 목록 보기
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // spinner에 데이터 Adapter 연결
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);

        ToggleButton facingSwitch = findViewById(R.id.facing_switch);
        facingSwitch.setOnCheckedChangeListener(this);

        new ViewModelProvider(this, AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        this,
                        provider -> {
                            cameraProvider = provider;
                            bindAllCameraUseCases();
                        });

        //Face Detection 설정 관련 코드, 설정 변경시 앱 정상 구동이 힘든 관계로 사용 X
        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    intent.putExtra(
                            SettingsActivity.EXTRA_LAUNCH_SOURCE,
                            SettingsActivity.LaunchSource.CAMERAX_LIVE_PREVIEW);
                    startActivity(intent);
                });
    }

    /**
     * Activity 종료시에도 데이터를 저장하여 정상적인 구동이 가능하도록 onSaveInstanceState 활용
     * @param bundle
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(STATE_SELECTED_MODEL, selectedModel);
    }

    /**
     * 호출될 Callback에 대한 인터페이스 정의
     * @param parent
     * @param view
     * @param pos
     * @param id
     */
    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // 항목이 선택된다면 선택된 항목을 검색하고 해당 항목으로 실행
        // 해당 앱에서는 Face Detection으로 고정하여 사용
        selectedModel = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "Selected model: " + selectedModel);
        bindAnalysisUseCase();
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
        if (cameraProvider == null) {
            return;
        }
        int newLensFacing =
                lensFacing == CameraSelector.LENS_FACING_FRONT
                        ? CameraSelector.LENS_FACING_BACK
                        : CameraSelector.LENS_FACING_FRONT;
        CameraSelector newCameraSelector =
                new CameraSelector.Builder().requireLensFacing(newLensFacing).build();
        try {
            if (cameraProvider.hasCamera(newCameraSelector)) {
                Log.d(TAG, "Set facing to " + newLensFacing);
                lensFacing = newLensFacing;
                cameraSelector = newCameraSelector;
                bindAllCameraUseCases();
                return;
            }
        } catch (CameraInfoUnavailableException e) {
            // Falls through
        }
        Toast.makeText(
                getApplicationContext(),
                "This device does not have lens with facing: " + newLensFacing,
                Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Activity 일시중지시 실행할 행동 정의
     */
    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();
    }

    /**
     * 실행중에 다른 Activity가 올 경우 실행할 행동 정의
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    /**
     * Activity 소멸시 실행할 행동 정의
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    /**
     * CameraX API에서 요구하는 대로 모든 사용 사례를 바인딩 해제한 후 다시 바인딩
     */
    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    /**
     * CameraX API에서 요구하는 대로 모든 사용 사례를 바인딩 해제한 후 다시 바인딩
     */
    private void bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
            return;
        }
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }
        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle( this, cameraSelector, previewUseCase);
    }

    /**
     * CameraX API에서 요구하는 대로 모든 사용 사례를 바인딩 해제한 후 다시 바인딩
     */
    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }

        try {
            switch (selectedModel) {
                case FACE_DETECTION:
                    Log.i(TAG, "Using Face Detector Processor");
                    imageProcessor = new FaceDetectorProcessor(this);
                    break;
                default:
                    throw new IllegalStateException("Invalid model name");
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + selectedModel, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getLocalizedMessage(),
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }
        analysisUseCase = builder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy 가 다른 스레드를 사용하여 아래 코드에서 탐지를 실행.
                // 메인 스레드에서 분석기를 실행할 수 있음
                ContextCompat.getMainExecutor(this),
                imageProxy -> {
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }
                    try {
                        imageProcessor.processImageProxy(imageProxy, graphicOverlay);
                    } catch (MlKitException e) {
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        cameraProvider.bindToLifecycle( this, cameraSelector, analysisUseCase);
    }
}
