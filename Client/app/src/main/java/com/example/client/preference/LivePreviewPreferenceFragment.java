package com.example.client.preference;

import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import androidx.annotation.StringRes;
import com.example.client.CameraSource;
import com.example.client.CameraSource.SizePair;
import com.example.client.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** CameraX API 라이브 이미지에 대한 설정 */
public class LivePreviewPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_live_preview_quickstart);
        setUpCameraPreferences();
        FaceDetectionUtils.setUpFaceDetectionPreferences(this,  true);
    }

    void setUpCameraPreferences() {
        PreferenceCategory cameraPreference =
                (PreferenceCategory) findPreference(getString(R.string.pref_category_key_camera));
        cameraPreference.removePreference(
                findPreference(getString(R.string.pref_key_camerax_rear_camera_target_resolution)));
        cameraPreference.removePreference(
                findPreference(getString(R.string.pref_key_camerax_front_camera_target_resolution)));
        setUpCameraPreviewSizePreference(
                R.string.pref_key_rear_camera_preview_size,
                R.string.pref_key_rear_camera_picture_size,
                CameraSource.CAMERA_FACING_BACK);
        setUpCameraPreviewSizePreference(
                R.string.pref_key_front_camera_preview_size,
                R.string.pref_key_front_camera_picture_size,
                CameraSource.CAMERA_FACING_FRONT);
    }

    private void setUpCameraPreviewSizePreference(
            @StringRes int previewSizePrefKeyId, @StringRes int pictureSizePrefKeyId, int cameraId) {
        ListPreference previewSizePreference =
                (ListPreference) findPreference(getString(previewSizePrefKeyId));

        Camera camera = null;
        try {
            camera = Camera.open(cameraId);

            List<SizePair> previewSizeList = CameraSource.generateValidPreviewSizeList(camera);
            String[] previewSizeStringValues = new String[previewSizeList.size()];
            Map<String, String> previewToPictureSizeStringMap = new HashMap<>();
            for (int i = 0; i < previewSizeList.size(); i++) {
                SizePair sizePair = previewSizeList.get(i);
                previewSizeStringValues[i] = sizePair.preview.toString();
                if (sizePair.picture != null) {
                    previewToPictureSizeStringMap.put(
                            sizePair.preview.toString(), sizePair.picture.toString());
                }
            }
            previewSizePreference.setEntries(previewSizeStringValues);
            previewSizePreference.setEntryValues(previewSizeStringValues);

            if (previewSizePreference.getEntry() == null) {
                // 설정 페이지를 처음 여는 경우
                SizePair sizePair =
                        CameraSource.selectSizePair(
                                camera,
                                CameraSource.DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH,
                                CameraSource.DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT);
                String previewSizeString = sizePair.preview.toString();
                previewSizePreference.setValue(previewSizeString);
                previewSizePreference.setSummary(previewSizeString);
                PreferenceUtils.saveString(
                        getActivity(),
                        pictureSizePrefKeyId,
                        sizePair.picture != null ? sizePair.picture.toString() : null);
            } else {
                previewSizePreference.setSummary(previewSizePreference.getEntry());
            }

            previewSizePreference.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        String newPreviewSizeStringValue = (String) newValue;
                        previewSizePreference.setSummary(newPreviewSizeStringValue);
                        PreferenceUtils.saveString(
                                getActivity(),
                                pictureSizePrefKeyId,
                                previewToPictureSizeStringMap.get(newPreviewSizeStringValue));
                        return true;
                    });
        } catch (RuntimeException e) {
            // 지정된 카메라 ID에 대한 카메라가 없으면 해당 기본 설정을 숨기기
            ((PreferenceCategory) findPreference(getString(R.string.pref_category_key_camera)))
                    .removePreference(previewSizePreference);
        } finally {
            if (camera != null) {
                camera.release();
            }
        }
    }
}
