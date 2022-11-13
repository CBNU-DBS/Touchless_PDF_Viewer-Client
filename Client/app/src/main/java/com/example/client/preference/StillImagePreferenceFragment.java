package com.example.client.preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.example.client.R;

/** 정지 이미지 기본 설정을 구성 */
public class StillImagePreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_still_image);
        FaceDetectionUtils.setUpFaceDetectionPreferences(this, /* isStreamMode = */false);
    }
}