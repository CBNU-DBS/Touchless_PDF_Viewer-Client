package com.example.client.fragment;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
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
        File file = new File("/storage/emulated/0/Download/sample.pdf");
        Button addPdf = getView().findViewById(R.id.btn_addPdf);
        addPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("STATE", "LOG in SERVER");
                uploadWithTransferUtilty(file.getName(),file);
                downloadWithTransferUtilty(file.getName(),file);
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
    public void uploadWithTransferUtilty(String fileName, File file) {

        AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAWHWHK5FXJ6RFB2MP", "GZwJwAQmcLe2au4ldR0kkmr5VYlNqyUq2sZxX1Cm");    // IAM 생성하며 받은 것 입력
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(getActivity().getApplicationContext()).build();
        TransferNetworkLossHandler.getInstance(getActivity().getApplicationContext());

        TransferObserver uploadObserver = transferUtility.upload("ai-image-bucket/pdf", fileName, file);    // (bucket api, file이름, file객체)

        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    // Handle a completed upload
                }
            }

            @Override
            public void onProgressChanged(int id, long current, long total) {
                int done = (int) (((double) current / total) * 100.0);
                Log.d("MYTAG", "UPLOAD - - ID: $id, percent done = $done");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("MYTAG", "UPLOAD ERROR - - ID: $id - - EX:" + ex.toString());
            }
        });
    }
    public void downloadWithTransferUtilty(String fileName, File file) {

        AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAWHWHK5FXJ6RFB2MP", "GZwJwAQmcLe2au4ldR0kkmr5VYlNqyUq2sZxX1Cm");    // IAM 생성하며 받은 것 입력
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(getActivity().getApplicationContext()).build();
        TransferNetworkLossHandler.getInstance(getActivity().getApplicationContext());

        TransferObserver downloadObserver = transferUtility.download("ai-image-bucket/pdf",fileName, new File(Environment.DIRECTORY_DOWNLOADS+"/"+fileName));

        downloadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    // Handle a completed upload

                }
            }

            @Override
            public void onProgressChanged(int id, long current, long total) {
                int done = (int) (((double) current / total) * 100.0);
                Log.d("MYTAG", "DONWLOAD - - ID: $id, percent done = $done");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("MYTAG", "DONALOAD ERROR - - ID: $id - - EX:" + ex.toString());
            }
        });
    }
}
