package com.example.client.fragment;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.SystemClock.sleep;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
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
import com.example.client.BuildConfig;
import com.example.client.PDF_View_Activity;
import com.example.client.R;
import com.example.client.RetrofitClient;
import com.example.client.api.DocumentApi;
import com.example.client.dto.BaseResponse;
import com.example.client.dto.DocumentDTO;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import com.blankj.utilcode.util.UriUtils;
import com.example.client.dto.MotionFunctionDTO;
import com.example.client.dto.ResponseDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentFragment extends Fragment {
    private static final int READ_REQUEST_CODE = 101;
    public ViewGroup rootView;
    public File[] files;                //Touchless_PDF-Client 앱의 로컬 폴더 내 파일들

    private File[] past_files;
    private String[] past_files_list;
    //음성인식 context 설정
    Context cThis;

    //음성 인식 Intent, Recognizer
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    // 음성인식 시작버튼, 결과출력 텍스트뷰
    ImageButton Btn_record_start;
    TextView STT_Result;
    // 동기화 버튼
    ImageButton Btn_synchronize;

    DocumentApi documentApi;
    Long userId;

    private File LocalDir;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 리사이클러뷰에 표시할 데이터 리스트 생성.
        super.onCreate(savedInstanceState);
        ArrayList<String> list = new ArrayList<>();
        documentApi = RetrofitClient.getClient().create(DocumentApi.class);
        SharedPreferences sharedPref_login = this.getActivity().getSharedPreferences("auto_login",MODE_PRIVATE);
        SharedPreferences.Editor editor_login = sharedPref_login.edit();
        userId = sharedPref_login.getLong("auto_id0",0L);

//        getFolderFileList();

        SharedPreferences Pref_search = getActivity().getSharedPreferences("pref_search",Context.MODE_PRIVATE);
        String voice_search0 = Pref_search.getString("voiceMsg","");
        SharedPreferences.Editor editor_search = Pref_search.edit();

        //음성인식 결과가 존재할 경우, 음성인식 결과가 포함된 이름의 pdf만 리스트에 저장 후, 출력
        if(voice_search0 != ""){
            for(int j = 0; j < files.length; j++){
                if(files[j].getName().contains(voice_search0)){
                    list.add(files[j].getName().toString());
                }
            }
            //음성인식 결과에 따른 문서 출력 후, 음성인식결과 삭제(다시 문서목록 출력 시, 모든 문서가 출력됨)
            editor_search.clear();
            editor_search.commit();
        } else {
            for (int i = 0; i < files.length; i++) {
                list.add(files[i].getName().toString());
            }
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
        ImageButton SAFUploadPdf = getView().findViewById(R.id.btn_SAFUploadPdf);
        SAFUploadPdf.bringToFront();
        SAFUploadPdf.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });
        recyclerView.setAdapter(adapter);

        /**
         * 구글드라이브 앱 실행 버튼
         * 유저의 기기에 설치되어 있는 구글드라이브 실행
         * 구글드라이브 실행을 기점으로 현재 Download 폴더의 과거 파일리스트 저장
         */
        ImageButton btn_google_drive = getView().findViewById(R.id.btn_googledrive);
        btn_google_drive.bringToFront();
        btn_google_drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //구글드라이브 진입 전, 현재 Download폴더의 pdf파일 이름 배열을 저장 후 Document Fragment로 전달
                File PastDir = new File("/storage/emulated/0/Download/");
                past_files = PastDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File pathname, String name) {
                        //"pdf"가 포함되어 있는 파일로만 이루어진 리스트 반환
                        return name.endsWith("pdf");
                    }
                });

                //과거 Download폴더의 pdf파일 리스트
                past_files_list = new String[past_files.length];
                for(int i = 0; i < past_files.length; i++){
                    past_files_list[i] = past_files[i].getPath();
                    Log.d("과거 pdf리스트",past_files_list[i]);
                }

                Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.google.android.apps.docs");
                startActivity(intent);
            }
        });

        // 음성인식 시작 버튼과 결과 출력 텍스트뷰
        Btn_record_start = getView().findViewById(R.id.btn_record_start);
        Btn_record_start.bringToFront();
        cThis = getActivity();  //context 설정

        //음성인식용 Intent 생성
        SttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getActivity().getPackageName()); //여분의 키
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR"); //한국어 사용

        /**
         * 음성인식 시작 버튼
         * Recognizer를 통해 사람의 음성을 인식하여 String으로 변환
         */
        Btn_record_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecognizer = SpeechRecognizer.createSpeechRecognizer(cThis);
                mRecognizer.setRecognitionListener(listener);
                mRecognizer.startListening(SttIntent);
            }
        });

        Btn_synchronize = getView().findViewById(R.id.btn_syncronize);
        Btn_synchronize.bringToFront();
        Btn_synchronize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDocumentList(userId);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 네이게이션바의 [문서] 버튼 클릭 시
        rootView = (ViewGroup) inflater.inflate(R.layout.activity_select_pdf,container,false);
        // Inflate the layout for this fragment
//        LocalDir = container.getContext().getFilesDir();
        LocalDir = new File("/data/data/com.example.client/files");
        Log.d("LocalDir", "onCreateView: "+LocalDir.toString());
        if(past_files_list != null) {
            GoogledriveUpdate();
        }
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

    /**
     * 구글드라이브 업데이트 함수
     * 구글드라이브에서 다운받은 새로 추가된 PDF 파일을 유저의 DB와 서버의 S3에 업로드 및 앱 로컬 폴더로 다운로드 진행
     */
    public void GoogledriveUpdate(){
        //스마트폰 기기의 Downlaod 파일 변수선언(구글드라이브로부터 pdf를 다운받은 후의 Download폴더)
        File CurrentDir = new File("/storage/emulated/0/Download/");
        File[] current_files = CurrentDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File pathname, String name) {
                return name.endsWith("pdf");
            }
        });
        Log.d("현재 다운로드 폴더 크기", String.valueOf(current_files.length));
        //과거와 현재의 Download폴더 pdf리스트 비교를 위한 현재 pdf리스트 저장용 String[] 선언
        String[] current_files_list = new String[current_files.length];
        for(int h = 0; h < current_files_list.length; h++){
            current_files_list[h] = current_files[h].getPath();
        }
        //앱을 처음 설치하여 파일을 받는 경우 Download 폴더의 모든 PDF파일을 앱 로컬폴더에 저장합니다.
        if(files.length == 0){
            for(int l=0; l<current_files_list.length; l++) {
                Log.d("빈 상태에서 새로추가된 pdf", current_files_list[l]);
                File new_file = new File(current_files_list[l]);
                String key = UUID.randomUUID().toString();
                uploadWithTransferUtility(key, new_file);
            }
        }else{
            // past_file_list와 구글드라이브로부터 다운로드 받은 후의 Download폴더의 pdf리스트를 비교합니다.
            for (int j = 0; j < current_files_list.length; j++) {
                Log.d("현재 pdf파일 리스트", current_files_list[j]);
                for (int k = 0; k < past_files_list.length; k++) {
                    Log.d("비교되는 과거 pdf파일 리스트",past_files_list[k]);
                    if (past_files_list[k].equals(current_files_list[j])) {
                        current_files_list[j] = "";
                        break;
                    }
                }
                //만약 새로 생긴 pdf파일인 경우 upload와 download를 실행합니다.
                if (current_files_list[j] != "") {
                    Log.d("새로추가된 pdf", current_files_list[j]);
                    File new_file = new File(current_files_list[j]);
                    String key = UUID.randomUUID().toString();
                    uploadWithTransferUtility(key, new_file);
                }
        }
        }
        past_files_list = null;
        Log.d("반복문","끝");
    }
    public void uploadWithTransferUtility(String key,File file) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(BuildConfig.AWS_ACCESS_KEY, BuildConfig.AWS_ACCESS_SECRET_KEY);    // IAM 생성하며 받은 것 입력
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2));
        String filenameAndKey = key+"_"+file.getName();
        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(getActivity().getApplicationContext()).build();
        TransferNetworkLossHandler.getInstance(getActivity().getApplicationContext());
        TransferObserver uploadObserver = transferUtility.upload("touchlesspdf", filenameAndKey, file);    // (bucket api, file이름, file객체)
        Toast.makeText(getContext(), "문서 업로드", Toast.LENGTH_SHORT).show();
        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    // Handle a completed upload
                    DocumentDTO documentDTO = new DocumentDTO(userId, key, file.getName());
                    documentApi.saveDocument(documentDTO).enqueue(new Callback<BaseResponse>() {
                        @Override
                        public void onResponse(Call<BaseResponse> call,
                                               Response<BaseResponse> response) {
                            if(response.isSuccessful()){
                                if(response.body().getResultCode() == 0){
//                                    Toast.makeText(getContext(), response.body().getResultMsg(), Toast.LENGTH_SHORT).show();
                                        downloadWithTransferUtility(key,file.getName());
                                } else {
//                                    Toast.makeText(getContext(), response.body().getResultMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<BaseResponse> call, Throwable t) {
                            Toast.makeText(getContext(), "문서 저장 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onProgressChanged(int id, long current, long total) {
                int done = (int) (((double) current / total) * 100.0);
                Log.d("MYTAG", "UPLOAD - - ID: "+id+", percent done = "+done);
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("MYTAG", "UPLOAD ERROR - - ID: $id - - EX:" + ex.toString());
            }
        });
    }
    public void downloadWithTransferUtility(String key, String filename) {
        Log.d("key : ",key+"");
        AWSCredentials awsCredentials = new BasicAWSCredentials(BuildConfig.AWS_ACCESS_KEY, BuildConfig.AWS_ACCESS_SECRET_KEY);    // IAM 생성하며 받은 것 입력
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(getActivity().getApplicationContext()).build();
        TransferNetworkLossHandler.getInstance(getActivity().getApplicationContext());
        if(new File(LocalDir.getPath()+"/"+filename).exists()){
            Log.d("downloadWithTransferUtility","중복 파일 존제"+filename);
            return;
        }
        TransferObserver downloadObserver = transferUtility.download("touchlesspdf",key+'_'+filename, new File(LocalDir.getPath()+"/"+filename));
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

    /**
     * 음성인식을 위한 RecognitionListener 선언 및 음성인식 기능
     */
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

        /**
         * 입력된 음성메세지를 String 형식으로 전환하는 함수
         * @param results
         */
        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            Toast.makeText(getActivity(), rs.toString(), Toast.LENGTH_SHORT).show();
//            STT_Result.setText(rs[0] + "\r\n" + STT_Result.getText());
//            Log.d("STT_Result",STT_Result.getText().toString());
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

    /**
     * 입력된 음성 메세지 확인 후 동작 처리 함수
     * @param VoiceMsg
     */
    private void FuncVoiceOrderCheck(String VoiceMsg){
        if(VoiceMsg.length() < 1) {
            return;
        }

        //음성인식 결과를 저장하기 위한 sharedPreferences 선언
        SharedPreferences Pref_search = getActivity().getSharedPreferences("pref_search",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_search = Pref_search.edit();

        //음성인식 결과의 공백제거
        VoiceMsg = VoiceMsg.replace(" ","");

        //음성인식 결과(value)를 [voiceMsg] key에 저장
        editor_search.putString("voiceMsg",VoiceMsg);
        Log.d("음성인식 결과(VoiceMsg) : ",VoiceMsg);
        editor_search.commit();

    }

    /**
     * 기능 실행 후, 음성인식이 종료되지 않아 계속 실행되는 경우를 막기위한 종료 함수
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mRecognizer != null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                Uri uri = data.getData();
                Log.e("uri", uri.toString());
                File file = UriUtils.uri2File(uri);
                String key = UUID.randomUUID().toString();
                uploadWithTransferUtility(key,file);

            }
        }
    }
    public void getDocumentList(Long userId){
        final List<DocumentDTO>[] result = new List[]{new ArrayList<>()};
        Response<ResponseDTO<DocumentDTO>> response;
        documentApi.getDocumentList(userId).enqueue(new Callback<ResponseDTO<DocumentDTO>>() {
            @Override
            public void onResponse(Call<ResponseDTO<DocumentDTO>> call,
                    Response<ResponseDTO<DocumentDTO>> response) {
                if(response.isSuccessful()){
                    Toast.makeText(getContext(), "문서 검색 성공_클라이언트", Toast.LENGTH_SHORT).show();
                    List<DocumentDTO> docs = response.body().getList();
                    for(DocumentDTO doc : docs){
                        String makeStr = doc.getKey()+'_'+doc.getTitle();
                        Log.d("getDocumentList",makeStr);
                        downloadWithTransferUtility(doc.getKey(),doc.getTitle());
                    }
                } else {
                    Toast.makeText(getContext(), "문서 동기화 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseDTO<DocumentDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
//        Log.d("getDocumentList",""+result[0].get(0).getTitle());
    }
}
