package com.example.client.preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.example.client.R;

/** 정지 이미지 데모 설정을 구성합니다 */
public class StillImagePreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_still_image);
        FaceDetectionUtils.setUpFaceDetectionPreferences(this, /* isStreamMode = */false);
    }
}