package com.example.client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.client.Adapter.PdfAdapter;

import java.util.ArrayList;

/**
 * pdf를 recyclerview 방식으로 보여줄 수 있게 하는 Activity Class.
 */
public class SelectPdfActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 리사이클러뷰에 표시할 데이터 리스트 생성.
        super.onCreate(savedInstanceState);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(String.format("Title %d", i));
        }

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        setContentView(R.layout.activity_select_pdf);
        RecyclerView recyclerView = findViewById(R.id.PdfRecycler);
        Log.e("recyclerView",recyclerView+"");
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
        PdfAdapter adapter = new PdfAdapter(list);
        recyclerView.setAdapter(adapter);
    }
}
