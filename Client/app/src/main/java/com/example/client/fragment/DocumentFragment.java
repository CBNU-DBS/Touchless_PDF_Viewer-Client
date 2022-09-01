package com.example.client.fragment;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import com.example.client.aws.*;
import java.util.UUID;

public class DocumentFragment extends Fragment {
    public ViewGroup rootView;
    public File[] files;
    //음성인식 context 설정
    Context cThis;

    //음성 인식 Intent, Recognizer
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    // 음성인식 시작버튼, 결과출력 텍스트뷰
    Button Btn_record_start;
    TextView STT_Result;

    private File LocalDir;
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
        // 흠
        Button addPdf = getView().findViewById(R.id.btn_uploadPdf);
        //String key = UUID.randomUUID().toString();
        String key = "tmpKey";
        addPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadWithTransferUtilty(key,file);
            }
        });
        // 흠
        Button downloadPdf = getView().findViewById(R.id.btn_downloadPdf);
        downloadPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadWithTransferUtilty(key,file.getName());
            }
        });

        recyclerView.setAdapter(adapter);

        // 음성인식 시작 버튼과 결과 출력 텍스트뷰
        Btn_record_start = getView().findViewById(R.id.btn_record_start);
        STT_Result = getView().findViewById(R.id.text_record_result);
        cThis = getActivity();  //context 설정

        //음성인식용 Intent 생성
        SttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getActivity().getPackageName()); //여분의 키
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR"); //한국어 사용


        Btn_record_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecognizer = SpeechRecognizer.createSpeechRecognizer(cThis);
                mRecognizer.setRecognitionListener(listener);
                mRecognizer.startListening(SttIntent);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 버튼 클릭 시
       rootView = (ViewGroup) inflater.inflate(R.layout.activity_select_pdf,container,false);
        // Inflate the layout for this fragment
        LocalDir = container.getContext().getFilesDir();
        Log.d("LocalDir", "onCreateView: "+LocalDir.toString());
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
        Log.d("Files","dirPath : "+LocalDir.getPath());
        files = LocalDir.listFiles();
        Log.d("files Length",files.length+"");
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
            Log.d("Files", "Filepath:" + files[i].getPath());
        }
    }
    public void uploadWithTransferUtilty(String key,File file) {
        awsAccess aws = new awsAccess();
        AWSCredentials awsCredentials = new BasicAWSCredentials(aws.getAccessKey(), aws.getAccessScretKey());    // IAM 생성하며 받은 것 입력
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(getActivity().getApplicationContext()).build();
        TransferNetworkLossHandler.getInstance(getActivity().getApplicationContext());
        TransferObserver uploadObserver = transferUtility.upload("touchlesspdf", key, file);    // (bucket api, file이름, file객체)
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
    public void downloadWithTransferUtilty(String key, String filename) {
        Log.d("key : ",key+"");
        awsAccess aws = new awsAccess();
        AWSCredentials awsCredentials = new BasicAWSCredentials(aws.getAccessKey(), aws.getAccessScretKey());    // IAM 생성하며 받은 것 입력
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(getActivity().getApplicationContext()).build();
        TransferNetworkLossHandler.getInstance(getActivity().getApplicationContext());

        TransferObserver downloadObserver = transferUtility.download("touchlesspdf",key, new File(LocalDir.getPath()+"/"+filename));
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
                Log.d("MYTAG", "DOWNLOAD - - ID: $id, percent done = $done");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("MYTAG", "DONALOAD ERROR - - ID: $id - - EX:" + ex.toString());
            }
        });
    }

    // 음성인식을 위한 RecognitionListener 선언
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            // 말하기 시작할 준비가되면 호출
            Toast.makeText(getActivity(),"가능한 구체적으로 말해주세요",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {
            // 말하기 시작했을 때 호출
        }

        @Override
        public void onRmsChanged(float v) {
            // 입력받는 소리의 크기를 알려줌

        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            // 말을 시작하고 인식이 된 단어를 buffer에 저장함
        }

        @Override
        public void onEndOfSpeech() {
            // 말하기를 중지하면 호출
            Toast.makeText(getActivity(),"음성이 인식되었습니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(int i) {
            // 네트워크 또는 인식 오류가 발생했을 때 호출
            String errormsg = "오류 발생";
            switch(i){
                case SpeechRecognizer.ERROR_AUDIO:
                    errormsg = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    errormsg = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errormsg = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    errormsg = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errormsg = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errormsg = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errormsg = "RECOGNIZER 가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    errormsg = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: //음성을 너무 길게 인식시키면 오류 발생
                    errormsg = "말하는 시간초과";
                    break;
                default:
                    errormsg = "알 수 없는 오류임";
                    break;
            }
            Toast.makeText(getActivity(), errormsg, Toast.LENGTH_SHORT).show();
            onDestroy();
        }

        // 입력된 음성메세지를 String 형식으로 전환하는 함수
        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            STT_Result.setText(rs[0] + "\r\n" + STT_Result.getText());
            Log.d("STT_Result",STT_Result.getText().toString());
            FuncVoiceOrderCheck(rs[0]); //입력된 음성에 따라 기능을 작동하도록 하는 함수
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            // 부분 인식 결과를 사용할 수 있을 때 호출
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            // 향후 이벤트를 추가하기 위해 예약 할 수 있음
        }
    };

    //입력된 음성 메세지 확인 후 동작 처리
    private void FuncVoiceOrderCheck(String VoiceMsg){
        if(VoiceMsg.length() < 1) {
            return;
        }

        VoiceMsg = VoiceMsg.replace(" ",""); //음성인식 결과의 공백제거
        Log.d("음성인식 결과",VoiceMsg);
        for(int i=0; i< files.length; i++){
            if(files[i].getName().contains(VoiceMsg)){
                Intent intent = new Intent(getActivity(),PDF_View_Activity.class);
                intent.putExtra("pdfname", files[i].getName());
                startActivity(intent);

                onDestroy();
            } //음성인식으로 받은 단어가 포함되어 있는 문서를 찾아서 내용을 확인한다.
            else{
                Toast.makeText(getActivity(),"검색된 문서가 없습니다.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //기능 실행 후, 음성인식이 종료되지 않아 계속 실행되는 경우를 막기위한 종료 함수
    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mRecognizer != null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer = null;
        }
    }

}
