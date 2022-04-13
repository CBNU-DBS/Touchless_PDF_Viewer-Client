package com.example.client;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;

import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    PDFView pdfView = findViewById(R.id.pdfView);
    // pdf 페이지 수
    Integer pageNumber = 0;
    // pdf 파일 이름
    String pdfFileName;

    String sample = "sample.pdf";

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openPdfFromAsset(sample);

        Button prevBtn = (Button) findViewById(R.id.prevBtn);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevPage();
            }
        });
        Button nextBtn = (Button) findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage();
            }
        });
        Button zoomInBtn = (Button) findViewById(R.id.zoomInBtn);
        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomIn();
            }
        });
        Button zoomOutBtn = (Button) findViewById(R.id.zoomOutBtn);
        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomOut();
            }
        });

        Button scrollUpBtn = (Button) findViewById(R.id.scrollUpBtn);
        scrollUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollUp();
            }
        });
        Button scrollDownBtn = (Button) findViewById(R.id.scrollDownBtn);
        scrollDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollDown();
            }
        });
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
        pdfFileName = assetName;
        pdfView.fromAsset(assetName)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
        setTitle(pdfFileName);
    }

    // 아래로 스크롤 메소드
    private void scrollDown() {
        float curOffsetX = pdfView.getCurrentXOffset();
        float curOffsetY = pdfView.getCurrentYOffset();
        Toast.makeText(getApplicationContext(), "현재 오프셋"+curOffsetX +"   " + curOffsetY, Toast.LENGTH_SHORT).show();
        pdfView.moveTo(curOffsetX, curOffsetY - 500.0f);
        pdfView.loadPages();
    }
    // 위로 스크롤 메소드
    private void scrollUp() {
        float curOffsetX = pdfView.getCurrentXOffset();
        float curOffsetY = pdfView.getCurrentYOffset();
        Toast.makeText(getApplicationContext(), "현재 오프셋"+curOffsetX +"   " + curOffsetY, Toast.LENGTH_SHORT).show();
        pdfView.moveTo(curOffsetX, curOffsetY + 500.0f);
        pdfView.loadPages();
    }
    // 축소 메소드
    private void zoomOut() {
        Toast.makeText(getApplicationContext(), "축소!", Toast.LENGTH_SHORT).show();
        float curZoom = pdfView.getZoom();
        float nextZoom = curZoom - 0.5f;
        if(nextZoom >= pdfView.getMinZoom()){
            Toast.makeText(getApplicationContext(), "축소 실행", Toast.LENGTH_SHORT).show();
            PointF curpivot = new PointF(pdfView.getPivotX(),pdfView.getPivotY());
            pdfView.zoomCenteredTo(nextZoom, curpivot);
        } else {
            Toast.makeText(getApplicationContext(), "최고로 축소한 거임", Toast.LENGTH_SHORT).show();
        }
    }
    // 확대 메소드
    private void zoomIn() {
        Toast.makeText(getApplicationContext(), "확대!", Toast.LENGTH_SHORT).show();
        float curZoom = pdfView.getZoom();
        float nextZoom = curZoom + 0.5f;
        if(nextZoom <= pdfView.getMaxZoom()){
            Toast.makeText(getApplicationContext(), "확대 실행", Toast.LENGTH_SHORT).show();
            PointF curpivot = new PointF(pdfView.getPivotX(),pdfView.getPivotY());
            pdfView.zoomCenteredTo(nextZoom, curpivot);
        } else {
            Toast.makeText(getApplicationContext(), "최고로 확대한 거임", Toast.LENGTH_SHORT).show();
        }
    }
    // 다음 페이지 메소드
    private void nextPage() {
        Toast.makeText(getApplicationContext(), "다음 페이지!", Toast.LENGTH_SHORT).show();
        pdfView.jumpTo(pdfView.getCurrentPage()+1);
    }
    // 이전 페이지 메소드
    public void prevPage(){
        Toast.makeText(getApplicationContext(),"이전 페이지!",Toast.LENGTH_SHORT).show();
        pdfView.jumpTo(pdfView.getCurrentPage()-1);
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
}