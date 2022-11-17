package com.example.client.java;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.RequiresApi;
import androidx.camera.view.PreviewView;
import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.camera.CameraSourceConfig;
import com.google.mlkit.vision.camera.CameraXSource;
import com.google.mlkit.vision.camera.DetectionTaskCallback;
import com.example.client.GraphicOverlay;
import com.example.client.InferenceInfoGraphic;
import com.example.client.R;
import com.example.client.preference.PreferenceUtils;
import com.example.client.preference.SettingsActivity;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import java.util.List;
import java.util.Objects;

/**
 * CameraX를 이용한 ML키트 API용 라이브 미리보기
 */
@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class CameraXSourceDemoActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "CameraXSourceDemo";

    private static final LocalModel localModel =
            new LocalModel.Builder().setAssetFilePath("custom_models/object_labeler.tflite").build();

    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;

    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private int lensFacing = CameraSourceConfig.CAMERA_FACING_BACK;
    private DetectionTaskCallback<List<DetectedObject>> detectionTaskCallback;
    private CameraXSource cameraXSource;
    private CustomObjectDetectorOptions customObjectDetectorOptions;
    private Size targetResolution;

    /**
     * 생성자, 사용자 기기 카메라 활성화 및 FaceDetection 기설정된 옵션 가져오기.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_vision_cameraxsource_demo);
        previewView = findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        ToggleButton facingSwitch = findViewById(R.id.facing_switch);
        facingSwitch.setOnCheckedChangeListener(this);

        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    intent.putExtra(
                            SettingsActivity.EXTRA_LAUNCH_SOURCE,
                            SettingsActivity.LaunchSource.CAMERAXSOURCE_DEMO);
                    startActivity(intent);
                });
        detectionTaskCallback =
                detectionTask ->
                        detectionTask
                                .addOnSuccessListener(this::onDetectionTaskSuccess)
                                .addOnFailureListener(this::onDetectionTaskFailure);
    }

    /**
     * CheckChanged 컨트롤의 CheckBox 이밴트 발생시 실행
     * 카메라 방향 확인, 해당 앱에서는 전면 카메라로 고정하여 사용
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        lensFacing =
                lensFacing == CameraSourceConfig.CAMERA_FACING_FRONT
                        ? CameraSourceConfig.CAMERA_FACING_BACK
                        : CameraSourceConfig.CAMERA_FACING_FRONT;

        createThenStartCameraXSource();
    }

    /**
     * Activity 일시중지시 실행할 행동 정의
     */
    @Override
    public void onResume() {
        super.onResume();
        if (cameraXSource != null
                && PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(this, localModel)
                .equals(customObjectDetectorOptions)
                && PreferenceUtils.getCameraXTargetResolution(getApplicationContext(), lensFacing) != null
                && Objects.requireNonNull(
                PreferenceUtils.getCameraXTargetResolution(getApplicationContext(), lensFacing))
                .equals(targetResolution)) {
            cameraXSource.start();
        } else {
            createThenStartCameraXSource();
        }
    }

    /**
     * 실행중에 다른 Activity가 올 경우 실행할 행동 정의
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (cameraXSource != null) {
            cameraXSource.stop();
        }
    }

    /**
     * Activity 소멸시 실행할 행동 정의
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraXSource != null) {
            cameraXSource.close();
        }
    }

    private void createThenStartCameraXSource() {
        if (cameraXSource != null) {
            cameraXSource.close();
        }
        customObjectDetectorOptions =
                PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(
                        getApplicationContext(), localModel);
        ObjectDetector objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);
        CameraSourceConfig.Builder builder =
                new CameraSourceConfig.Builder(
                        getApplicationContext(), objectDetector, detectionTaskCallback)
                        .setFacing(lensFacing);
        targetResolution =
                PreferenceUtils.getCameraXTargetResolution(getApplicationContext(), lensFacing);
        if (targetResolution != null) {
            builder.setRequestedPreviewSize(targetResolution.getWidth(), targetResolution.getHeight());
        }
        cameraXSource = new CameraXSource(builder.build(), previewView);
        needUpdateGraphicOverlayImageSourceInfo = true;
        cameraXSource.start();
    }

    /**
     * 감지가 성공일 경우 실행, 카메라 사이즈 확인 및 처리중인 이미지 수정
     * @param results
     */
    private void onDetectionTaskSuccess(List<DetectedObject> results) {
        graphicOverlay.clear();
        if (needUpdateGraphicOverlayImageSourceInfo) {
            Size size = cameraXSource.getPreviewSize();
            if (size != null) {
                Log.d(TAG, "preview width: " + size.getWidth());
                Log.d(TAG, "preview height: " + size.getHeight());
                boolean isImageFlipped =
                        cameraXSource.getCameraFacing() == CameraSourceConfig.CAMERA_FACING_FRONT;
                if (isPortraitMode()) {
                    // 세로 방향으로 90도 회전하므로 가로 및 높이값을 변경,
                    // 카메라 미리 보기와 처리 중인 이미지의 크기가 같도록
                    graphicOverlay.setImageSourceInfo(size.getHeight(), size.getWidth(), isImageFlipped);
                } else {
                    graphicOverlay.setImageSourceInfo(size.getWidth(), size.getHeight(), isImageFlipped);
                }
                needUpdateGraphicOverlayImageSourceInfo = false;
            } else {
                Log.d(TAG, "previewsize is null");
            }
        }
        Log.v(TAG, "Number of object been detected: " + results.size());
        graphicOverlay.add(new InferenceInfoGraphic(graphicOverlay));
        graphicOverlay.postInvalidate();
    }

    /**
     * 감지가 실패할 경우 실행, 예외처리
     * @param e
     */
    private void onDetectionTaskFailure(Exception e) {
        graphicOverlay.clear();
        graphicOverlay.postInvalidate();
        String error = "Failed to process. Error: " + e.getLocalizedMessage();
        Toast.makeText(
                graphicOverlay.getContext(), error + "\nCause: " + e.getCause(), Toast.LENGTH_SHORT)
                .show();
        Log.d(TAG, error);
    }

    private boolean isPortraitMode() {
        return getApplicationContext().getResources().getConfiguration().orientation
                != Configuration.ORIENTATION_LANDSCAPE;
    }
}
