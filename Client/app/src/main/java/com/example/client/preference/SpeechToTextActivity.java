package com.example.client.preference;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.client.R;

import java.util.ArrayList;

public class SpeechToTextActivity extends AppCompatActivity {

    Context cThis;//context 설정

    //음성 인식용
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    // 화면 처리용
    Button Btn_record;
    TextView STT_Result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spech_to_text);

        cThis = this;

        //음성인식
        SttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getApplicationContext().getPackageName());
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR"); //한국어 사용
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener);

        //버튼설정
        Btn_record = (Button)findViewById(R.id.btn_record);
        Btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SpeechToTextActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {

                        mRecognizer.startListening(SttIntent);
                    }catch (SecurityException e){
                        e.printStackTrace();}
                }
            }
        });

        // 음성 인식 후, 인식된 내용을 출력해줄 EditText 설정
        STT_Result = findViewById(R.id.STT_result);

    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            // 말하기 시작할 준비가되면 호출
            Toast.makeText(getApplicationContext(),"음성을 인식하는 중입니다...",Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(),"음성이 인식되었습니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(int i) {
            // 네트워크 또는 인식 오류가 발생했을 때 호출
            String errormsg = "오류 발생";
            switch(i){
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: //음성을 너무 길게 인식시키면 오류 발생
                    errormsg = "음성 인식 시간 초과";
                    Toast.makeText(getApplicationContext(), "" + errormsg,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "" + errormsg,Toast.LENGTH_SHORT).show();
                    break;
            }


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
        if(VoiceMsg.length() < 1)return;

        VoiceMsg = VoiceMsg.replace(" ","");//공백제거

//        if(VoiceMsg.indexOf("카카오톡") > -1){
//            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.kakao.talk");
//            startActivity(launchIntent);
//            onDestroy();
//        } //음성인식으로 받은 이름의 문서를 여는 쪽으로 활용할 수 있을 것 같다.
    }

    //기능 실행 후, 음성인식이 종료되지 않아 계속 실행되는 경우를 막기위한 종료 함수
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mRecognizer != null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer = null;
        }
    }

}