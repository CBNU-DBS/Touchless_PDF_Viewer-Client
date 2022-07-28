package com.example.client.fragment;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.client.Adapter.PdfAdapter;
import com.example.client.PDF_View_Activity;
import com.example.client.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Locale;

public class DocumentFragment extends Fragment {
    public ViewGroup rootView;
    public File[] files;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 리사이클러뷰에 표시할 데이터 리스트 생성.
        super.onCreate(savedInstanceState);
        ArrayList<String> list = new ArrayList<>();
//        getFolderFileList();
        for (int i = 0; i < files.length; i++) {
            list.add(files[i].getName().toString());
        }
        RecyclerView recyclerView = getView().findViewById(R.id.PdfRecycler);
        Log.e("recyclerView",recyclerView+"");
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
        PdfAdapter adapter = new PdfAdapter(list);
        adapter.setOnItemClickListener(new PdfAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // TODO : 아이템 클릭 이벤트를 MainActivity에서 처리.
                Log.d("TAG", "onItemClick: "+position);
                Intent intent = new Intent(getActivity(),PDF_View_Activity.class);
                intent.putExtra("pdfname", files[position].getName());
                startActivity(intent);

            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.activity_select_pdf,container,false);
        // Inflate the layout for this fragment
        getFolderFileList();
        return inflater.inflate(R.layout.activity_select_pdf, container, false);
    }


    public void getFolderFileList() {
        //internal
        //File dir = new File(getFilesDir().getAbsolutePath(), "test");
//        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().toString());
//        Log.d("PDF",dir.getPath());
//        File[] files = dir.listFiles();
//        Log.d("PDF",files.length+"");
//        for(File f : files) {
//            Log.d("PDF"," f : "+f.getPath() +" , "+f.getPath());
//            Log.d("PDF"," f : "+f.getName() +" , "+f.getName());
//        }
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath().toString());
        Log.d("Files","dirPath : "+dir.getPath());
        files = dir.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
            Log.d("Files", "Filepath:" + files[i].getPath());
        }
    }
}
