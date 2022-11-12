package com.example.client.java;

import static java.lang.Math.max;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.common.annotation.KeepName;
import com.example.client.BitmapUtils;
import com.example.client.GraphicOverlay;
import com.example.client.R;
import com.example.client.VisionImageProcessor;
import com.example.client.java.facedetector.FaceDetectorProcessor;
import com.example.client.preference.SettingsActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 카메라의 정지된 이미지를 사용하여 다양한 이미지 검출기 기능 실행 */
@KeepName
public final class StillImageActivity extends AppCompatActivity {
    //MLkit에서 지원하는 모든 기능들. 해당 앱에서는 Face Detection만 사용
    private static final String TAG = "StillImageActivity";
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

    private static final String SIZE_SCREEN = "w:screen"; // 화면 너비 일치
    private static final String SIZE_1024_768 = "w:1024"; // ~1024*768 의 정상 비율
    private static final String SIZE_640_480 = "w:640"; // ~640*480 의 정상 비율
    private static final String SIZE_ORIGINAL = "w:original"; // 원본 이미지 크기

    private static final String KEY_IMAGE_URI = "com.google.mlkit.vision.demo.KEY_IMAGE_URI";
    private static final String KEY_SELECTED_SIZE = "com.google.mlkit.vision.demo.KEY_SELECTED_SIZE";

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_CHOOSE_IMAGE = 1002;

    private ImageView preview;
    private GraphicOverlay graphicOverlay;
    private String selectedMode = OBJECT_DETECTION;
    private String selectedSize = SIZE_SCREEN;

    boolean isLandScape;

    private Uri imageUri;
    private int imageMaxWidth;
    private int imageMaxHeight;
    private VisionImageProcessor imageProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_still_image);

        findViewById(R.id.select_image_button)
                .setOnClickListener(
                        view -> {
                            // 다음 중 하나를 선택하는 메뉴: a) 새 사진 찍기 b) 기존 중에서 선택.
                            // 해당 앱에서는 새 사진 찍기만을 사용
                            PopupMenu popup = new PopupMenu(StillImageActivity.this, view);
                            popup.setOnMenuItemClickListener(
                                    menuItem -> {
                                        int itemId = menuItem.getItemId();
                                        if (itemId == R.id.select_images_from_local) {
                                            startChooseImageIntentForResult();
                                            return true;
                                        } else if (itemId == R.id.take_photo_using_camera) {
                                            startCameraIntentForResult();
                                            return true;
                                        }
                                        return false;
                                    });
                            MenuInflater inflater = popup.getMenuInflater();
                            inflater.inflate(R.menu.camera_button_menu, popup.getMenu());
                            popup.show();
                        });
        preview = findViewById(R.id.preview);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        populateFeatureSelector();
        populateSizeSelector();

        isLandScape =
                (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI);
            selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE);
        }

        View rootView = findViewById(R.id.root);
        rootView
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                imageMaxWidth = rootView.getWidth();
                                imageMaxHeight = rootView.getHeight() - findViewById(R.id.control).getHeight();
                                if (SIZE_SCREEN.equals(selectedSize)) {
                                    tryReloadAndDetectInImage();
                                }
                            }
                        });

        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    intent.putExtra(
                            SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.STILL_IMAGE);
                    startActivity(intent);
                });
    }

    /**
     * 다시 실행, 일시정지, 소멸자
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        createImageProcessor();
        tryReloadAndDetectInImage();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    /**
     * 아래 코드는 MLkit 환경설정 관련 코드
     * 해당 앱에서는 Face Detection만을 사용하고, 앱 정상작동을 위해 설정화면 접근 불가
     */

    private void populateFeatureSelector() {
        //스피너 선언 및 기설정된 옵션을 가져와서 List
        Spinner featureSpinner = findViewById(R.id.feature_selector);
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
        featureSpinner.setAdapter(dataAdapter);
        featureSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                        selectedMode = parentView.getItemAtPosition(pos).toString();
                        createImageProcessor();
                        tryReloadAndDetectInImage();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {}
                });
    }

    private void populateSizeSelector() {
        //스피너 선언 및 화면 사이즈 관련 옵션 List
        Spinner sizeSpinner = findViewById(R.id.size_selector);
        List<String> options = new ArrayList<>();
        options.add(SIZE_SCREEN);
        options.add(SIZE_1024_768);
        options.add(SIZE_640_480);
        options.add(SIZE_ORIGINAL);

        // spinner를 위한 Adapter 선언
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_outer_style, options);
        // 드롭다운 레이아웃 스타일 - 라디오 단추가 있는 목록 보기
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너에 데이터 어댑터 연결
        sizeSpinner.setAdapter(dataAdapter);
        sizeSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                        selectedSize = parentView.getItemAtPosition(pos).toString();
                        tryReloadAndDetectInImage();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {}
                });
    }

    /**
     * Activity가 종료될 경우에도 데이터 저장
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_IMAGE_URI, imageUri);
        outState.putString(KEY_SELECTED_SIZE, selectedSize);
    }

    private void startCameraIntentForResult() {
        // 마지막(지난번) 이미지 삭제
        imageUri = null;
        preview.setImageBitmap(null);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void startChooseImageIntentForResult() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            tryReloadAndDetectInImage();
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            // 이 경우 선택자가 imageUri를 반환
            imageUri = data.getData();
            tryReloadAndDetectInImage();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void tryReloadAndDetectInImage() {
        Log.d(TAG, "Try reload and detect image");
        try {
            if (imageUri == null) {
                return;
            }

            if (SIZE_SCREEN.equals(selectedSize) && imageMaxWidth == 0) {
                // UI 레이아웃이 아직 완료되지 않을 경우, 준비가 되면 다시 로드.
                return;
            }

            Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(getContentResolver(), imageUri);
            if (imageBitmap == null) {
                return;
            }

            // 오버레이를 먼저 삭제
            graphicOverlay.clear();

            Bitmap resizedBitmap;
            if (selectedSize.equals(SIZE_ORIGINAL)) {
                resizedBitmap = imageBitmap;
            } else {
                // 이미지 뷰의 해상도 가져오기
                Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

                // 이미지를 얼마나 축소할지 결정
                float scaleFactor =
                        max(
                                (float) imageBitmap.getWidth() / (float) targetedSize.first,
                                (float) imageBitmap.getHeight() / (float) targetedSize.second);

                resizedBitmap =
                        Bitmap.createScaledBitmap(
                                imageBitmap,
                                (int) (imageBitmap.getWidth() / scaleFactor),
                                (int) (imageBitmap.getHeight() / scaleFactor),
                                true);
            }

            preview.setImageBitmap(resizedBitmap);

            if (imageProcessor != null) {
                graphicOverlay.setImageSourceInfo(
                        resizedBitmap.getWidth(), resizedBitmap.getHeight(), false);
                imageProcessor.processBitmap(resizedBitmap, graphicOverlay);
            } else {
                Log.e(TAG, "Null imageProcessor, please check adb logs for imageProcessor creation error");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error retrieving saved image");
            imageUri = null;
        }
    }

    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;

        switch (selectedSize) {
            case SIZE_SCREEN:
                targetWidth = imageMaxWidth;
                targetHeight = imageMaxHeight;
                break;
            case SIZE_640_480:
                targetWidth = isLandScape ? 640 : 480;
                targetHeight = isLandScape ? 480 : 640;
                break;
            case SIZE_1024_768:
                targetWidth = isLandScape ? 1024 : 768;
                targetHeight = isLandScape ? 768 : 1024;
                break;
            default:
                throw new IllegalStateException("Unknown size");
        }

        return new Pair<>(targetWidth, targetHeight);
    }

    private void createImageProcessor() {
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        try {
            switch (selectedMode) {
                case FACE_DETECTION:
                    Log.i(TAG, "Using Face Detector Processor");
                    imageProcessor = new FaceDetectorProcessor(this);
                    break;
                default:
                    Log.e(TAG, "Unknown selectedMode: " + selectedMode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + selectedMode, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
}
