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

/**
 * 화면에 PDF 파일을 비트맵 이미지로 노출
 * FaceDetection과의 상호작용을 위한 시스템 interface 작성
 */

public class PDF_View_Activity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, OnItemSelectedListener, CompoundButton.OnCheckedChangeListener,
        ActivityCompat.OnRequestPermissionsResultCallback, OnPageErrorListener {


    private static final String FACE_DETECTION = "Face Detection";
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
            //사용자에게 파일 관리자가 작동하지 않음을 알림
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void openPdfFromAsset(String assetName) {
        Intent pdfintent = getIntent();
        pdfFileName = pdfintent.getStringExtra("pdfname");
        File PDFPath = new File(Localdir,pdfFileName);
        Log.d("check", String.valueOf(PDFPath));

        pdfView.fromFile(PDFPath)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // dp
                .onPageError(this)
                .load();
        setTitle(pdfFileName);
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
    // 왼쪽으로 스크롤 메소드
    public static void scrollleft() {
        float curOffsetX = pdfView.getCurrentXOffset();
        float curOffsetY = pdfView.getCurrentYOffset();
        pdfView.moveTo(curOffsetX + 200.0f, curOffsetY);
        pdfView.loadPages();
    }
    // 오른쪽로 스크롤 메소드
    public static void scrollright() {
        float curOffsetX = pdfView.getCurrentXOffset();
        float curOffsetY = pdfView.getCurrentYOffset();
        pdfView.moveTo(curOffsetX - 200.0f, curOffsetY);
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
        selectedModel = parent.getItemAtPosition(pos).toString();
        Log.d(TAG, "Selected model: " + selectedModel);
        preview.stop();
        createCameraSource(selectedModel);
        startCameraSource();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // 아무것도 않함
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

    /**
     * 기존 cameraSource가 없으면 cameraSource를 만듭니다.
     * @param model
     */
    private void createCameraSource(String model) {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
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
     * 카메라 소스가 있는 경우 해당 소스를 시작하거나 다시 시작합니다
     *  카메라 소스가 아직 존재하지 않는 경우(예: 카메라 소스가 생성되기 전에 다시 시작을 호출했기 때문에) 카메라 소스가 생성될 때 다시 호출됩니다.
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

    /** 카메라를 정지 */
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