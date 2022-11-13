package com.example.client;

import android.app.Application;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;

/**
 * CameraX와 상호 작용하기 위한 모델 Activity Class.
 */
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class CameraXViewModel extends AndroidViewModel {

    private static final String TAG = "CameraXViewModel";
    private MutableLiveData<ProcessCameraProvider> cameraProviderLiveData;

    /**
     * 지정된 응용 프로그램 컨텍스트를 통해 카메라 서비스와 상호 작용하는 인스턴스를 만듭니다.
     */
    public CameraXViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<ProcessCameraProvider> getProcessCameraProvider() {
        if (cameraProviderLiveData == null) {
            cameraProviderLiveData = new MutableLiveData<>();

            ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                    ProcessCameraProvider.getInstance(getApplication());
            cameraProviderFuture.addListener(
                    () -> {
                        try {
                            cameraProviderLiveData.setValue(cameraProviderFuture.get());
                        } catch (ExecutionException | InterruptedException e) {
                            // 여기서 모든 오류(취소 포함)를 처리
                            Log.e(TAG, "Unhandled exception", e);
                        }
                    },
                    ContextCompat.getMainExecutor(getApplication()));
        }

        return cameraProviderLiveData;
    }
}

