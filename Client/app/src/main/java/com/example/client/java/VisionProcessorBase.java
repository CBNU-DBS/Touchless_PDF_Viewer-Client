package com.example.client.java;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build.VERSION_CODES;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.gms.tasks.Tasks;
import com.google.android.odml.image.BitmapMlImageBuilder;
import com.google.android.odml.image.ByteBufferMlImageBuilder;
import com.google.android.odml.image.MediaMlImageBuilder;
import com.google.android.odml.image.MlImage;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.example.client.BitmapUtils;
import com.example.client.CameraImageGraphic;
import com.example.client.FrameMetadata;
import com.example.client.GraphicOverlay;
import com.example.client.InferenceInfoGraphic;
import com.example.client.ScopedExecutor;
import com.example.client.TemperatureMonitor;
import com.example.client.VisionImageProcessor;
import com.example.client.preference.PreferenceUtils;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 탐지 결과와 함께 사용할 항목을 위한 비전 프레임 프로세서의 추상 기본 클래스입니다
 *
 * @param <T> 탐지된 기능의 유형
 */
public abstract class VisionProcessorBase<T> implements VisionImageProcessor {

    protected static final String MANUAL_TESTING_LOG = "LogTagForTest";
    private static final String TAG = "VisionProcessorBase";

    private final ActivityManager activityManager;
    private final Timer fpsTimer = new Timer();
    private final ScopedExecutor executor;
    private final TemperatureMonitor temperatureMonitor;

    // 이 프로세서가 이미 종료되었는지 여부
    private boolean isShutdown;

    // 동일한 스레드에서 실행되며 동기화가 필요 없는 지연 시간을 계산하는 데 사용
    private int numRuns = 0;
    private long totalFrameMs = 0;
    private long maxFrameMs = 0;
    private long minFrameMs = Long.MAX_VALUE;
    private long totalDetectorMs = 0;
    private long maxDetectorMs = 0;
    private long minDetectorMs = Long.MAX_VALUE;

    // FPS를 계산하기 위해 1초 간격으로 지금까지 처리된 프레임 수
    private int frameProcessedInOneSecondInterval = 0;
    private int framesPerSecond = 0;

    // 최신 이미지와 메타데이터를 보관
    @GuardedBy("this")
    private ByteBuffer latestImage;

    @GuardedBy("this")
    private FrameMetadata latestImageMetaData;
    // 이미지 및 메타데이터를 계속 처리
    @GuardedBy("this")
    private ByteBuffer processingImage;

    @GuardedBy("this")
    private FrameMetadata processingMetaData;

    protected VisionProcessorBase(Context context) {
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        executor = new ScopedExecutor(TaskExecutors.MAIN_THREAD);
        fpsTimer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        framesPerSecond = frameProcessedInOneSecondInterval;
                        frameProcessedInOneSecondInterval = 0;
                    }
                },
                /* 딜레이= */ 0,
                /* 주기= */ 1000);
        temperatureMonitor = new TemperatureMonitor(context);
    }

    // 단일 정지 영상 처리 코드
    @Override
    public void processBitmap(Bitmap bitmap, final GraphicOverlay graphicOverlay) {
        long frameStartMs = SystemClock.elapsedRealtime();

        if (isMlImageEnabled(graphicOverlay.getContext())) {
            MlImage mlImage = new BitmapMlImageBuilder(bitmap).build();
            requestDetectInImage(
                    mlImage,
                    graphicOverlay,
                    /* 원본 카메라 이미지 */ null,
                    /* FPS를 보일 필요가 없음 */ false,
                    frameStartMs);
            mlImage.close();

            return;
        }

        requestDetectInImage(
                InputImage.fromBitmap(bitmap, 0),
                graphicOverlay,
                /* 원본 카메라 이미지 */ null,
                /* FPS를 보일 필요가 없음 */ false,
                frameStartMs);
    }

    // Camera1 API에서 라이브 미리보기 프레임을 처리하기 위한 코드
    @Override
    public synchronized void processByteBuffer(
            ByteBuffer data, final FrameMetadata frameMetadata, final GraphicOverlay graphicOverlay) {
        latestImage = data;
        latestImageMetaData = frameMetadata;
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlay);
        }
    }

    private synchronized void processLatestImage(final GraphicOverlay graphicOverlay) {
        processingImage = latestImage;
        processingMetaData = latestImageMetaData;
        latestImage = null;
        latestImageMetaData = null;
        if (processingImage != null && processingMetaData != null && !isShutdown) {
            processImage(processingImage, processingMetaData, graphicOverlay);
        }
    }

    private void processImage(
            ByteBuffer data, final FrameMetadata frameMetadata, final GraphicOverlay graphicOverlay) {
        long frameStartMs = SystemClock.elapsedRealtime();

        // 라이브 뷰포트가 켜져 있으면(밑면 뷰가 카메라 미리보기 도면을 처리함)
        // 수동 미리보기 도면에 사용된 불필요한 비트맵 작성을 스킵
        Bitmap bitmap =
                PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay.getContext())
                        ? null
                        : BitmapUtils.getBitmap(data, frameMetadata);

        if (isMlImageEnabled(graphicOverlay.getContext())) {
            MlImage mlImage =
                    new ByteBufferMlImageBuilder(
                            data,
                            frameMetadata.getWidth(),
                            frameMetadata.getHeight(),
                            MlImage.IMAGE_FORMAT_NV21)
                            .setRotation(frameMetadata.getRotation())
                            .build();

            requestDetectInImage(mlImage, graphicOverlay, bitmap, true, frameStartMs)
                    .addOnSuccessListener(executor, results -> processLatestImage(graphicOverlay));

            // Java Garbage collection은 최종적으로 닫을 수 있음
            mlImage.close();
            return;
        }

        requestDetectInImage(
                InputImage.fromByteBuffer(
                        data,
                        frameMetadata.getWidth(),
                        frameMetadata.getHeight(),
                        frameMetadata.getRotation(),
                        InputImage.IMAGE_FORMAT_NV21),
                graphicOverlay,
                bitmap,
                 true,
                frameStartMs)
                .addOnSuccessListener(executor, results -> processLatestImage(graphicOverlay));
    }

    // CameraX API에서 라이브 미리보기 프레임을 처리하기 위한 코드
    @Override
    @RequiresApi(VERSION_CODES.LOLLIPOP)
    @ExperimentalGetImage
    public void processImageProxy(ImageProxy image, GraphicOverlay graphicOverlay) {
        long frameStartMs = SystemClock.elapsedRealtime();
        if (isShutdown) {
            image.close();
            return;
        }

        Bitmap bitmap = null;
        if (!PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay.getContext())) {
            bitmap = BitmapUtils.getBitmap(image);
        }

        if (isMlImageEnabled(graphicOverlay.getContext())) {
            MlImage mlImage =
                    new MediaMlImageBuilder(image.getImage())
                            .setRotation(image.getImageInfo().getRotationDegrees())
                            .build();

            requestDetectInImage(
                    mlImage,
                    graphicOverlay,
                     bitmap,
                    true,
                    frameStartMs)
                    // CameraX 분석 사용 사례에서 가져온 이미지인 경우, 이미지를 사용한 후 수신된 이미지에 대해 image.close()를 호출해야 함.
                    // 그렇지 않으면 새 이미지가 수신되지 않거나 카메라가 정지될 수 있음.
                    // 현재 MlImage는 ImageProxy를 직접 지원하지 않으므로 여기서 ImageProxy.close()를 호출해야 함.
                    .addOnCompleteListener(results -> image.close());
            return;
        }

        requestDetectInImage(
                InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees()),
                graphicOverlay,
                 bitmap,
                 true,
                frameStartMs)
                // CameraX 분석 사용 사례에서 가져온 이미지인 경우, 이미지를 사용한 후 수신된 이미지에 대해 image.close()를 호출해야 함
                // 그렇지 않으면 새 이미지가 수신되지 않거나 카메라가 정지될 수 있음
                .addOnCompleteListener(results -> image.close());
    }

    // 공통 processing logic
    private Task<T> requestDetectInImage(
            final InputImage image,
            final GraphicOverlay graphicOverlay,
            @Nullable final Bitmap originalCameraImage,
            boolean shouldShowFps,
            long frameStartMs) {
        return setUpListener(
                detectInImage(image), graphicOverlay, originalCameraImage, shouldShowFps, frameStartMs);
    }

    private Task<T> requestDetectInImage(
            final MlImage image,
            final GraphicOverlay graphicOverlay,
            @Nullable final Bitmap originalCameraImage,
            boolean shouldShowFps,
            long frameStartMs) {
        return setUpListener(
                detectInImage(image), graphicOverlay, originalCameraImage, shouldShowFps, frameStartMs);
    }

    private Task<T> setUpListener(
            Task<T> task,
            final GraphicOverlay graphicOverlay,
            @Nullable final Bitmap originalCameraImage,
            boolean shouldShowFps,
            long frameStartMs) {
        final long detectorStartMs = SystemClock.elapsedRealtime();
        return task.addOnSuccessListener(
                executor,
                results -> {
                    long endMs = SystemClock.elapsedRealtime();
                    long currentFrameLatencyMs = endMs - frameStartMs;
                    long currentDetectorLatencyMs = endMs - detectorStartMs;
                    if (numRuns >= 500) {
                        resetLatencyStats();
                    }
                    numRuns++;
                    frameProcessedInOneSecondInterval++;
                    totalFrameMs += currentFrameLatencyMs;
                    maxFrameMs = max(currentFrameLatencyMs, maxFrameMs);
                    minFrameMs = min(currentFrameLatencyMs, minFrameMs);
                    totalDetectorMs += currentDetectorLatencyMs;
                    maxDetectorMs = max(currentDetectorLatencyMs, maxDetectorMs);
                    minDetectorMs = min(currentDetectorLatencyMs, minDetectorMs);

                    // 초당 한 번만 추론 정보를 기록합니다.
                    // frameProcessedInOneSecondInterval이 1이면 현재 초 동안 처리된 첫 번째 프레임임을 의미합니다.
                    if (frameProcessedInOneSecondInterval == 1) {
                        Log.d(TAG, "Num of Runs: " + numRuns);
                        Log.d(
                                TAG,
                                "Frame latency: max="
                                        + maxFrameMs
                                        + ", min="
                                        + minFrameMs
                                        + ", avg="
                                        + totalFrameMs / numRuns);
                        Log.d(
                                TAG,
                                "Detector latency: max="
                                        + maxDetectorMs
                                        + ", min="
                                        + minDetectorMs
                                        + ", avg="
                                        + totalDetectorMs / numRuns);
                        MemoryInfo mi = new MemoryInfo();
                        activityManager.getMemoryInfo(mi);
                        long availableMegs = mi.availMem / 0x100000L;
                        Log.d(TAG, "Memory available in system: " + availableMegs + " MB");
                        temperatureMonitor.logTemperature();
                    }

                    graphicOverlay.clear();
                    if (originalCameraImage != null) {
                        graphicOverlay.add(new CameraImageGraphic(graphicOverlay, originalCameraImage));
                    }
                    VisionProcessorBase.this.onSuccess(results, graphicOverlay);
                    if (!PreferenceUtils.shouldHideDetectionInfo(graphicOverlay.getContext())) {
                        graphicOverlay.add(
                                new InferenceInfoGraphic(
                                        graphicOverlay,
                                        currentFrameLatencyMs,
                                        currentDetectorLatencyMs,
                                        shouldShowFps ? framesPerSecond : null));
                    }
                    graphicOverlay.postInvalidate();
                })
                .addOnFailureListener(
                        executor,
                        e -> {
                            graphicOverlay.clear();
                            graphicOverlay.postInvalidate();
                            String error = "Failed to process. Error: " + e.getLocalizedMessage();
                            Toast.makeText(
                                    graphicOverlay.getContext(),
                                    error + "\nCause: " + e.getCause(),
                                    Toast.LENGTH_SHORT)
                                    .show();
                            Log.d(TAG, error);
                            e.printStackTrace();
                            VisionProcessorBase.this.onFailure(e);
                        });
    }

    @Override
    public void stop() {
        executor.shutdown();
        isShutdown = true;
        resetLatencyStats();
        fpsTimer.cancel();
        temperatureMonitor.stop();
    }

    private void resetLatencyStats() {
        numRuns = 0;
        totalFrameMs = 0;
        maxFrameMs = 0;
        minFrameMs = Long.MAX_VALUE;
        totalDetectorMs = 0;
        maxDetectorMs = 0;
        minDetectorMs = Long.MAX_VALUE;
    }

    protected abstract Task<T> detectInImage(InputImage image);

    protected Task<T> detectInImage(MlImage image) {
        return Tasks.forException(
                new MlKitException(
                        "MlImage is currently not demonstrated for this feature",
                        MlKitException.INVALID_ARGUMENT));
    }

    protected abstract void onSuccess(@NonNull T results, @NonNull GraphicOverlay graphicOverlay);

    protected abstract void onFailure(@NonNull Exception e);

    protected boolean isMlImageEnabled(Context context) {
        return false;
    }
}
