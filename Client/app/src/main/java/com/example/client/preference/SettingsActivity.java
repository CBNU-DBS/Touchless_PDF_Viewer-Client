package com.example.client.preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.client.R;

/**
 * 설정에서 지정된 작업에 대한 설정을 구성하는 기본 설정을 호스팅
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String EXTRA_LAUNCH_SOURCE = "extra_launch_source";

    /** 이 활동을 시작할 위치를 지정합니다. */
    @SuppressWarnings("NewApi") // CameraX는 API 21+에서만 사용할 수 있습니다.
    public enum LaunchSource {
        LIVE_PREVIEW(R.string.pref_screen_title_live_preview, LivePreviewPreferenceFragment.class),
        STILL_IMAGE(R.string.pref_screen_title_still_image, StillImagePreferenceFragment.class),
        CAMERAX_LIVE_PREVIEW(
                R.string.pref_screen_title_camerax_live_preview,
                CameraXLivePreviewPreferenceFragment.class),
        CAMERAXSOURCE_DEMO(
                R.string.pref_screen_title_cameraxsource_demo, CameraXSourceDemoPreferenceFragment.class);

        private final int titleResId;
        private final Class<? extends PreferenceFragment> prefFragmentClass;

        LaunchSource(int titleResId, Class<? extends PreferenceFragment> prefFragmentClass) {
            this.titleResId = titleResId;
            this.prefFragmentClass = prefFragmentClass;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        LaunchSource launchSource =
                (LaunchSource) getIntent().getSerializableExtra(EXTRA_LAUNCH_SOURCE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(launchSource.titleResId);
        }

        try {
            getFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.settings_container,
                            launchSource.prefFragmentClass.getDeclaredConstructor().newInstance())
                    .commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
