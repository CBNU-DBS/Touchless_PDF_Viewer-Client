package com.example.client.preference;

import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.widget.Toast;
import androidx.annotation.StringRes;
import com.example.client.R;

/** 라이브 미리 보기 활동 및 정지 이미지 활동에 대한 얼굴 탐지를 설정하는 데 사용하는 Activity */
public class FaceDetectionUtils {
    public static void setUpFaceDetectionPreferences(
            PreferenceFragment preferenceFragment, boolean isStreamMode) {
        setUpListPreference(
                preferenceFragment, R.string.pref_key_live_preview_face_detection_landmark_mode);
        setUpListPreference(
                preferenceFragment, R.string.pref_key_live_preview_face_detection_contour_mode);
        setUpListPreference(
                preferenceFragment, R.string.pref_key_live_preview_face_detection_classification_mode);
        setUpListPreference(
                preferenceFragment, R.string.pref_key_live_preview_face_detection_performance_mode);
        if (isStreamMode) {
            EditTextPreference minFaceSizePreference =
                    (EditTextPreference)
                            preferenceFragment.findPreference(
                                    preferenceFragment.getString(
                                            R.string.pref_key_live_preview_face_detection_min_face_size));
            minFaceSizePreference.setSummary(minFaceSizePreference.getText());
            minFaceSizePreference.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        try {
                            float minFaceSize = Float.parseFloat((String) newValue);
                            if (minFaceSize >= 0.0f && minFaceSize <= 1.0f) {
                                minFaceSizePreference.setSummary((String) newValue);
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            // 고의로 실패
                        }

                        Toast.makeText(
                                preferenceFragment.getActivity(),
                                R.string.pref_toast_invalid_min_face_size,
                                Toast.LENGTH_LONG)
                                .show();
                        return false;
                    });
        }
    }

    private static void setUpListPreference(
            PreferenceFragment preferenceFragment, @StringRes int listPreferenceKeyId) {
        ListPreference listPreference =
                (ListPreference)
                        preferenceFragment.findPreference(preferenceFragment.getString(listPreferenceKeyId));
        listPreference.setSummary(listPreference.getEntry());
        listPreference.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    int index = listPreference.findIndexOfValue((String) newValue);
                    listPreference.setSummary(listPreference.getEntries()[index]);
                    return true;
                });
    }

    private FaceDetectionUtils() {}
}
