package com.example.client;

import android.graphics.Bitmap;
import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import androidx.camera.core.ImageProxy;
import com.google.mlkit.common.MlKitException;
import java.nio.ByteBuffer;

/**
 * 서로 다른 비전 디텍터 및 사용자 지정 이미지 모델을 사용하여 이미지를 처리하는 인터페이스.
 */
public interface VisionImageProcessor {

    /** 비트맵 이미지를 처리합니다. */
    void processBitmap(Bitmap bitmap, GraphicOverlay graphicOverlay);

    /** Camera1 Live Preview 케이스에 사용되는 ByteBuffer 이미지 데이터를 처리합니다. */
    void processByteBuffer(
            ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay)
            throws MlKitException;

    /** CameraX Live Preview 케이스에 사용되는 ImageProxy 이미지 데이터를 처리합니다.. */
    @RequiresApi(VERSION_CODES.KITKAT)
    void processImageProxy(ImageProxy image, GraphicOverlay graphicOverlay) throws MlKitException;

    void stop();
}
