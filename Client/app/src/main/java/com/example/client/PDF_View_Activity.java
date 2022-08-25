package com.example.client;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
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
import com.google.mlkit.common.model.LocalModel;
import com.example.client.CameraSource;
import com.example.client.CameraSourcePreview;
import com.example.client.GraphicOverlay;
import com.example.client.R;
import com.example.client.java.facedetector.FaceDetectorProcessor;
import com.example.client.preference.PreferenceUtils;
import com.example.client.preference.SettingsActivity;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.example.client.ActivityLocal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.example.client.java.LivePreviewActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;
import java.util.List;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;



public class PDF_View_Activity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, OnItemSelectedListener, CompoundButton.OnCheckedChangeListener,
        ActivityCompat.OnRequestPermissionsResultCallback, OnPageErrorListener {

    //private static final String TAG = MainActivity.class.getSimpleName();

    private static final String OBJECT_DETECTION = "Object Detection";
    private static final String OBJECT_DETECTION_CUSTOM = "Custom Object Detection";
    private static final String CUSTOM_AUTOML_OBJECT_DETECTION = "Custom AutoML Object Detection (Flower)";
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
    private static PDFView pdfView;

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = FACE_DETECTION;
    public static Activity activity;

    // pdf 페이지 수
    Integer pageNumber = 0;
    // pdf 파일 이름
    String pdfFileName;

    String sample = "sample.pdf";

    File Localdir;
    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        pdfView = findViewById(R.id.pdfView);
        preview = findViewById(R.id.preview_view);
        Localdir = pdfView.getContext().getFilesDir();
        activity = this;
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
        openPdfFromAsset(sample);
        createCameraSource(selectedModel);

        //설정버튼, 일단 주석처리
//        ImageView settingsButton = findViewById(R.id.settings_button);
//        settingsButton.setOnClickListener(
//                v -> {
//                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
//                    intent.putExtra(
//                            SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.LIVE_PREVIEW);
//                    startActivity(intent);
//                });
//        Button prevBtn = (Button) findViewById(R.id.prevBtn);
//        prevBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                prevPage();
//            }
//        });
//        Button nextBtn = (Button) findViewById(R.id.nextBtn);
//        nextBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                nextPage();
//            }
//        });
//        Button zoomInBtn = (Button) findViewById(R.id.zoomInBtn);
//        zoomInBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                zoomIn();
//            }
//        });
//        Button zoomOutBtn = (Button) findViewById(R.id.zoomOutBtn);
//        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                zoomOut();
//            }
//        });
//
//        Button scrollUpBtn = (Button) findViewById(R.id.scrollUpBtn);
//        scrollUpBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                scrollUp();
//            }
//        });
//        Button scrollDownBtn = (Button) findViewById(R.id.scrollDownBtn);
//        scrollDownBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                scrollDown();
//            }
//        });
//        Button testBtn = (Button) findViewById(R.id.scrollDownBtn);
//        scrollDownBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //
//            }
//        });
    }


    void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void openPdfFromAsset(String assetName) {
        Intent pdfintent = getIntent();
        pdfFileName = pdfintent.getStringExtra("pdfname");
        File PDFPath = new File(Localdir,pdfFileName);
        Log.d("check", String.valueOf(PDFPath));

//        File Mypdffile = new File(PDFPath, "test1.pdf");
//        Log.d("check", String.valueOf(Mypdffile));

        pdfView.fromFile(PDFPath)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
        setTitle(pdfFileName);

//        pdfView.fromAsset(assetName)
//                .defaultPage(pageNumber)
//                .onPageChange(this)
//                .enableAnnotationRendering(true)
//                .onLoad(this)
//                .scrollHandle(new DefaultScrollHandle(this))
//                .spacing(10) // in dp
//                .onPageError(this)
//                .load();
//        setTitle(pdfFileName);
    }

    // 아래로 스크롤 메소드
    public static void scrollDown() {
        float curOffsetX = pdfView.getCurrentXOffset();
        float curOffsetY = pdfView.getCurrentYOffset();
        pdfView.moveTo(curOffsetX, curOffsetY - 500.0f);
        pdfView.loadPages();
    }
    // 위로 스크롤 메소드
    public static void scrollUp() {
        float curOffsetX = pdfView.getCurrentXOffset();
        float curOffsetY = pdfView.getCurrentYOffset();
        pdfView.moveTo(curOffsetX, curOffsetY + 500.0f);
        pdfView.loadPages();
    }
    // 축소 메소드
    public static void zoomOut() {
        Toast.makeText(ActivityLocal.getAppContext(), "축소!", Toast.LENGTH_SHORT).show();
        float curZoom = pdfView.getZoom();
        float nextZoom = curZoom - 0.5f;
        if(nextZoom >= pdfView.getMinZoom()){
            Toast.makeText(ActivityLocal.getAppContext(), "축소 실행", Toast.LENGTH_SHORT).show();
            PointF curpivot = new PointF(pdfView.getPivotX(),pdfView.getPivotY());
            pdfView.zoomCenteredTo(nextZoom, curpivot);
        } else {
            Toast.makeText(ActivityLocal.getAppContext(), "최고로 축소한 거임", Toast.LENGTH_SHORT).show();
        }
    }
    // 확대 메소드
    public static void zoomIn() {
        Toast.makeText(ActivityLocal.getAppContext(), "확대!", Toast.LENGTH_SHORT).show();
        float curZoom = pdfView.getZoom();
        float nextZoom = curZoom + 0.5f;
        if(nextZoom <= pdfView.getMaxZoom()){
            Toast.makeText(ActivityLocal.getAppContext(), "확대 실행", Toast.LENGTH_SHORT).show();
            PointF curpivot = new PointF(pdfView.getPivotX(),pdfView.getPivotY());
            pdfView.zoomCenteredTo(nextZoom, curpivot);
        } else {
            Toast.makeText(ActivityLocal.getAppContext(), "최고로 확대한 거임", Toast.LENGTH_SHORT).show();
        }
    }
    // 다음 페이지 메소드
    public static void nextPage() {
        Toast.makeText(ActivityLocal.getAppContext(), "다음 페이지!", Toast.LENGTH_SHORT).show();
        pdfView.jumpTo(pdfView.getCurrentPage()+1);
    }
    // 이전 페이지 메소드
    public static void prevPage(){
        Toast.makeText(ActivityLocal.getAppContext(),"이전 페이지!",Toast.LENGTH_SHORT).show();
        pdfView.jumpTo(pdfView.getCurrentPage()-1);
    }

    // 이전 페이지 메소드
    public static void pdffinish(){
        Toast.makeText(ActivityLocal.getAppContext(),"뒤로 가기!",Toast.LENGTH_SHORT).show();
        activity.finish();
    }


    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");
    }
    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }



    //카메라 부분
    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "Selected model: " + selectedModel);
        preview.stop();
        createCameraSource(selectedModel);
        startCameraSource();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

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

    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        try {

            Log.i(TAG, "Using Face Detector Processor");
            cameraSource.setMachineLearningFrameProcessor(new FaceDetectorProcessor(this));

      /*switch (model) {
        case OBJECT_DETECTION:
          Log.i(TAG, "Using Object Detector Processor");
          ObjectDetectorOptions objectDetectorOptions =
              PreferenceUtils.getObjectDetectorOptionsForLivePreview(this);
          cameraSource.setMachineLearningFrameProcessor(
              new ObjectDetectorProcessor(this, objectDetectorOptions));
          break;
        case OBJECT_DETECTION_CUSTOM:
          Log.i(TAG, "Using Custom Object Detector Processor");
          LocalModel localModel =
              new LocalModel.Builder()
                  .setAssetFilePath("custom_models/object_labeler.tflite")
                  .build();
          CustomObjectDetectorOptions customObjectDetectorOptions =
              PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(this, localModel);
          cameraSource.setMachineLearningFrameProcessor(
              new ObjectDetectorProcessor(this, customObjectDetectorOptions));
          break;
        case CUSTOM_AUTOML_OBJECT_DETECTION:
          Log.i(TAG, "Using Custom AutoML Object Detector Processor");
          LocalModel customAutoMLODTLocalModel =
              new LocalModel.Builder().setAssetManifestFilePath("automl/manifest.json").build();
          CustomObjectDetectorOptions customAutoMLODTOptions =
              PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(
                  this, customAutoMLODTLocalModel);
          cameraSource.setMachineLearningFrameProcessor(
              new ObjectDetectorProcessor(this, customAutoMLODTOptions));
          break;
        case TEXT_RECOGNITION_LATIN:
          Log.i(TAG, "Using on-device Text recognition Processor for Latin.");
          cameraSource.setMachineLearningFrameProcessor(
              new TextRecognitionProcessor(this, new TextRecognizerOptions.Builder().build()));
          break;
        case TEXT_RECOGNITION_CHINESE:
          Log.i(TAG, "Using on-device Text recognition Processor for Latin and Chinese.");
          cameraSource.setMachineLearningFrameProcessor(
              new TextRecognitionProcessor(
                  this, new ChineseTextRecognizerOptions.Builder().build()));
          break;
        case TEXT_RECOGNITION_DEVANAGARI:
          Log.i(TAG, "Using on-device Text recognition Processor for Latin and Devanagari.");
          cameraSource.setMachineLearningFrameProcessor(
              new TextRecognitionProcessor(
                  this, new DevanagariTextRecognizerOptions.Builder().build()));
          break;
        case TEXT_RECOGNITION_JAPANESE:
          Log.i(TAG, "Using on-device Text recognition Processor for Latin and Japanese.");
          cameraSource.setMachineLearningFrameProcessor(
              new TextRecognitionProcessor(
                  this, new JapaneseTextRecognizerOptions.Builder().build()));
          break;
        case TEXT_RECOGNITION_KOREAN:
          Log.i(TAG, "Using on-device Text recognition Processor for Latin and Korean.");
          cameraSource.setMachineLearningFrameProcessor(
              new TextRecognitionProcessor(
                  this, new KoreanTextRecognizerOptions.Builder().build()));
          break;
        case FACE_DETECTION:
          Log.i(TAG, "Using Face Detector Processor");
          cameraSource.setMachineLearningFrameProcessor(new FaceDetectorProcessor(this));
          break;
        case BARCODE_SCANNING:
          Log.i(TAG, "Using Barcode Detector Processor");
          cameraSource.setMachineLearningFrameProcessor(new BarcodeScannerProcessor(this));
          break;
        case IMAGE_LABELING:
          Log.i(TAG, "Using Image Label Detector Processor");
          cameraSource.setMachineLearningFrameProcessor(
              new LabelDetectorProcessor(this, ImageLabelerOptions.DEFAULT_OPTIONS));
          break;
        case IMAGE_LABELING_CUSTOM:
          Log.i(TAG, "Using Custom Image Label Detector Processor");
          LocalModel localClassifier =
              new LocalModel.Builder()
                  .setAssetFilePath("custom_models/bird_classifier.tflite")
                  .build();
          CustomImageLabelerOptions customImageLabelerOptions =
              new CustomImageLabelerOptions.Builder(localClassifier).build();
          cameraSource.setMachineLearningFrameProcessor(
              new LabelDetectorProcessor(this, customImageLabelerOptions));
          break;
        case CUSTOM_AUTOML_LABELING:
          Log.i(TAG, "Using Custom AutoML Image Label Detector Processor");
          LocalModel customAutoMLLabelLocalModel =
              new LocalModel.Builder().setAssetManifestFilePath("automl/manifest.json").build();
          CustomImageLabelerOptions customAutoMLLabelOptions =
              new CustomImageLabelerOptions.Builder(customAutoMLLabelLocalModel)
                  .setConfidenceThreshold(0)
                  .build();
          cameraSource.setMachineLearningFrameProcessor(
              new LabelDetectorProcessor(this, customAutoMLLabelOptions));
          break;
        case POSE_DETECTION:
          PoseDetectorOptionsBase poseDetectorOptions =
              PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
          Log.i(TAG, "Using Pose Detector with options " + poseDetectorOptions);
          boolean shouldShowInFrameLikelihood =
              PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
          boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
          boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
          boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);
          cameraSource.setMachineLearningFrameProcessor(
              new PoseDetectorProcessor(
                  this,
                  poseDetectorOptions,
                  shouldShowInFrameLikelihood,
                  visualizeZ,
                  rescaleZ,
                  runClassification,
                  *//* isStreamMode = *//* true));
          break;
        case SELFIE_SEGMENTATION:
          cameraSource.setMachineLearningFrameProcessor(new SegmenterProcessor(this));
          break;
        default:
          Log.e(TAG, "Unknown model: " + model);
      }*/
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
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        createCameraSource(selectedModel);
        startCameraSource();
    }

    /** Stops the camera. */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}