package com.example.client.java;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.client.BuildConfig;
import com.example.client.R;

/**
 * 해당 앱에서는 접근 불가능한 FaceDetection 설정 선택 도구
 * 앱의 정상적인 구동을 위해 FaceDetection 설정은 수정 불가
 * */
public final class ChooserActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {
    private static final String TAG = "ChooserActivity";

    @SuppressWarnings("NewApi") // CameraX 는 API 21 이상에서만 구동
    private static final Class<?>[] CLASSES =
            VERSION.SDK_INT < VERSION_CODES.LOLLIPOP
                    ? new Class<?>[] {
                    LivePreviewActivity.class, StillImageActivity.class,
            }
                    : new Class<?>[] {
                    LivePreviewActivity.class,
                    StillImageActivity.class,
                    CameraXLivePreviewActivity.class,
                    CameraXSourceDemoActivity.class,
            };

    private static final int[] DESCRIPTION_IDS =
            VERSION.SDK_INT < VERSION_CODES.LOLLIPOP
                    ? new int[] {
                    R.string.desc_camera_source_activity, R.string.desc_still_image_activity,
            }
                    : new int[] {
                    R.string.desc_camera_source_activity,
                    R.string.desc_still_image_activity,
                    R.string.desc_camerax_live_preview_activity,
                    R.string.desc_cameraxsource_demo_activity,
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(
                    new StrictMode.VmPolicy.Builder()
                            .detectLeakedSqlLiteObjects()
                            .detectLeakedClosableObjects()
                            .penaltyLog()
                            .build());
        }
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_chooser);

        // 목록 보기 및 어댑터 설정
        ListView listView = findViewById(R.id.test_activity_list_view);

        MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES);
        adapter.setDescriptionIds(DESCRIPTION_IDS);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    /**
     * 아이템 선택시 실행
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Class<?> clicked = CLASSES[position];
        startActivity(new Intent(this, clicked));
    }

    private static class MyArrayAdapter extends ArrayAdapter<Class<?>> {

        private final Context context;
        private final Class<?>[] classes;
        private int[] descriptionIds;

        MyArrayAdapter(Context context, int resource, Class<?>[] objects) {
            super(context, resource, objects);

            this.context = context;
            classes = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater =
                        (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2, null);
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(classes[position].getSimpleName());
            ((TextView) view.findViewById(android.R.id.text2)).setText(descriptionIds[position]);

            return view;
        }

        void setDescriptionIds(int[] descriptionIds) {
            this.descriptionIds = descriptionIds;
        }
    }
}
