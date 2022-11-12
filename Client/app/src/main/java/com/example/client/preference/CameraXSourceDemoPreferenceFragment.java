package com.example.client.preference;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import androidx.annotation.RequiresApi;
import com.example.client.R;

/** CameraX API 라이브 이미지에 대한 설정 */
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class CameraXSourceDemoPreferenceFragment extends CameraXLivePreviewPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen preferenceScreen =
                (PreferenceScreen) findPreference(getResources().getString(R.string.pref_screen));

        PreferenceCategory preferenceCategory =
                (PreferenceCategory) findPreference(getString(R.string.pref_category_key_camera));

        preferenceCategory.removePreference(
                findPreference(getString(R.string.pref_key_camera_live_viewport)));
        // 카메라 탐지 정보를 숨기기 위한 기본 설정 범주를 제거합니다.
        preferenceScreen.removePreference(preferenceScreen.getPreference(1));

        // 마지막 세 가지 기본 설정 범주 제거
        preferenceScreen.removePreference(preferenceScreen.getPreference(2));
        preferenceScreen.removePreference(preferenceScreen.getPreference(2));
        preferenceScreen.removePreference(preferenceScreen.getPreference(2));
    }
}
