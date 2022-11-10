package com.example.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import androidx.annotation.RequiresPermission;
import com.google.android.gms.common.images.Size;
import com.example.client.preference.PreferenceUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * 카메라를 관리하고 그 위에 UI 업데이트를 허용합니다
 * 카메라에서 지정된 속도로 미리 보기 프레임을 수신하여
 *  처리할 수 있는 한 빨리 자식 클래스의 디텍터/분류기로 프레임을 전송합니다.
 */
public class CameraSource {
    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_BACK = CameraInfo.CAMERA_FACING_BACK;

    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_FRONT = CameraInfo.CAMERA_FACING_FRONT;

    public static final int IMAGE_FORMAT = ImageFormat.NV21;
    public static final int DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH = 480;
    public static final int DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT = 360;

    private static final String TAG = "MIDemoApp:CameraSource";

    /**
     * OpenGL 컨텍스트를 절대 사용하지 않기 때문에 여기서 원하는 ID를 선택할 수 있습니다.
     * 카메라 팀이 미리 보기 없이 카메라를 사용하는 것
     */
    private static final int DUMMY_TEXTURE_NAME = 100;

    /**
     * 프리뷰 사이즈 애스펙트 비와 픽처 사이즈 애스펙트 비의 절대적인 차이가 이 공차보다 작으면,
     * 동일한 애스펙트 비로 간주한다.
     */
    private static final float ASPECT_RATIO_TOLERANCE = 0.01f;

    protected Activity activity;

    private Camera camera;

    /*PDF Viewer를 실행시키면 기본세팅으로 전면 카메라를 사용합니다*/
    private int facing = CAMERA_FACING_FRONT;

    /** 장치의 회전 및 장치에서 캡처된 관련 미리 보기 이미지 */
    private int rotationDegrees;

    private Size previewSize;

    private static final float REQUESTED_FPS = 30.0f;
    private static final boolean REQUESTED_AUTO_FOCUS = true;

    // 기본 리소스의 GC를 방지하려면 이 인스턴스를 보유해야 합니다.
    // 작성 방법 이외에는 사용되지 않지만 여전히 하드 참조가 유지되어야 합니다.
    private SurfaceTexture dummySurfaceTexture;

    private final GraphicOverlay graphicOverlay;

    /**
     * 카메라에서 프레임을 사용할 수 있게 되면 프레임으로 디텍터를 호출하는 데 사용할 수 있는 전용 스레드 및 관련 실행 가능.
     */
    private Thread processingThread;

    private final FrameProcessingRunnable processingRunnable;
    private final Object processorLock = new Object();

    private VisionImageProcessor frameProcessor;

    /**
     * 카메라에서 받은 바이트 배열과 관련 바이트 버퍼 간에 변환하는 맵
     * 나중에 네이티브 코드를 호출하는 데 더 효율적인 방법이기 때문에 내부적으로 바이트 버퍼를 사용
     */
    private final IdentityHashMap<byte[], ByteBuffer> bytesToByteBuffer = new IdentityHashMap<>();

    public CameraSource(Activity activity, GraphicOverlay overlay) {
        this.activity = activity;
        graphicOverlay = overlay;
        graphicOverlay.clear();
        processingRunnable = new FrameProcessingRunnable();
    }

    // ==============================================================================================
    // ==============================================================================================

    /** 카메라를 중지하고 카메라 및 기본 디텍터의 리소스를 해제합니다. */
    public void release() {
        synchronized (processorLock) {
            stop();
            cleanScreen();

            if (frameProcessor != null) {
                frameProcessor.stop();
            }
        }
    }

    /**
     * 카메라를 열고 기본 디텍터로 미리 보기 프레임을 보내기 시작. 미리 보기 프레임이 표시되지 않습니다.
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    public synchronized CameraSource start() throws IOException {
        if (camera != null) {
            return this;
        }

        camera = createCamera();
        dummySurfaceTexture = new SurfaceTexture(DUMMY_TEXTURE_NAME);
        camera.setPreviewTexture(dummySurfaceTexture);
        camera.startPreview();

        processingThread = new Thread(processingRunnable);
        processingRunnable.setActive(true);
        processingThread.start();
        return this;
    }

    /**
     * 카메라를 열고 기본 디텍터로 미리 보기 프레임을 보내기 시작합니다.
     * 제공된 표면 홀더는 미리보기에 사용되어 프레임이 사용자에게 표시될 수 있습니다.
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    public synchronized CameraSource start(SurfaceHolder surfaceHolder) throws IOException {
        if (camera != null) {
            return this;
        }

        camera = createCamera();
        camera.setPreviewDisplay(surfaceHolder);
        camera.startPreview();

        processingThread = new Thread(processingRunnable);
        processingRunnable.setActive(true);
        processingThread.start();
        return this;
    }

    /**
     * 카메라를 닫고 기본 프레임 디텍터로 프레임 전송을 중지합니다.
     */
    public synchronized void stop() {
        processingRunnable.setActive(false);
        if (processingThread != null) {
            try {
                // 스레드가 완료될 때까지 기다려 여러 스레드를 동시에 실행할 수 없습니다
                // 즉, 중지 후 너무 빨리 시작을 호출할 경우 발생합니다
                processingThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "Frame processing thread interrupted on release.");
            }
            processingThread = null;
        }

        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallbackWithBuffer(null);
            try {
                camera.setPreviewTexture(null);
                dummySurfaceTexture = null;
                camera.setPreviewDisplay(null);
            } catch (Exception e) {
                Log.e(TAG, "Failed to clear camera preview: " + e);
            }
            camera.release();
            camera = null;
        }

        // 이미지 버퍼는 더 이상 사용되지 않으므로 이미지 버퍼에 대한 참조를 해제합니다.
        bytesToByteBuffer.clear();
    }

    /** 카메라의 면을 변경합니다. 해당 앱에서는 사용하지 않습니다. */
    public synchronized void setFacing(int facing) {
        if ((facing != CAMERA_FACING_BACK) && (facing != CAMERA_FACING_FRONT)) {
            throw new IllegalArgumentException("Invalid camera: " + facing);
        }
        this.facing = facing;
    }

    /** 기본 카메라에서 현재 사용 중인 미리 보기 크기를 반환합니다. */
    public Size getPreviewSize() {
        return previewSize;
    }

    /**
     * 선택한 카메라를 반환합니다.
     */
    public int getCameraFacing() {
        return facing;
    }

    /**
     * 카메라를 열고 사용자 설정을 적용합니다.
     */
    @SuppressLint("InlinedApi")
    private Camera createCamera() throws IOException {
        int requestedCameraId = getIdForRequestedCamera(facing);
        if (requestedCameraId == -1) {
            throw new IOException("Could not find requested camera.");
        }
        Camera camera = Camera.open(requestedCameraId);

        SizePair sizePair = PreferenceUtils.getCameraPreviewSizePair(activity, requestedCameraId);
        if (sizePair == null) {
            sizePair =
                    selectSizePair(
                            camera,
                            DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH,
                            DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT);
        }

        if (sizePair == null) {
            throw new IOException("Could not find suitable preview size.");
        }

        previewSize = sizePair.preview;
        Log.v(TAG, "Camera preview size: " + previewSize);

        int[] previewFpsRange = selectPreviewFpsRange(camera, REQUESTED_FPS);
        if (previewFpsRange == null) {
            throw new IOException("Could not find suitable preview frames per second range.");
        }

        Camera.Parameters parameters = camera.getParameters();

        Size pictureSize = sizePair.picture;
        if (pictureSize != null) {
            Log.v(TAG, "Camera picture size: " + pictureSize);
            parameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
        }
        parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
        parameters.setPreviewFpsRange(
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        // YV12를 사용하여 OCR 검출을 위해 YV12->NV21 자동 변환 로직을 연습할 수 있습니다.
        parameters.setPreviewFormat(IMAGE_FORMAT);

        setRotation(camera, parameters, requestedCameraId);

        if (REQUESTED_AUTO_FOCUS) {
            if (parameters
                    .getSupportedFocusModes()
                    .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else {
                Log.i(TAG, "Camera auto focus is not supported on this device.");
            }
        }

        camera.setParameters(parameters);

        // 카메라로 작업하려면 4개의 프레임 버퍼가 필요합니다.
        //
        //   탐지 수행 시 현재 실행 중인 프레임에 대해 1개
        //   다음 보류 중인 프레임이 탐지를 완료하면 즉시 처리할 수 있는 프레임에 대해 1개
        //   카메라가 향후 미리 보기 이미지를 채우는 데 사용하는 프레임에 대해 2개
        //
        // 버퍼가 세 개만 사용되는 경우 탐지 시간이 그리 중요하지 않을 때 카메라는 수천 개의 경고 메시지를 방출합니다.

        camera.setPreviewCallbackWithBuffer(new CameraPreviewCallback());
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));

        return camera;
    }

    /**
     * 카메라가 향하는 방향으로 지정된 카메라에 대한 ID를 가져옵니다. 없는 경우 -1을 반환합니다.
     *
     * @param facing 원하는 카메라를 향하여(정면 카메라 고정)
     */
    private static int getIdForRequestedCamera(int facing) {
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 원하는 너비 및 높이가 지정된 가장 적합한 미리 보기 및 사진 크기를 선택합니다.
     *
     * @param camera 미리 보기 크기를 선택할 카메라
     * @param desiredWidth 카메라 프리뷰 프레임의 원하는 폭
     * @param desiredHeight 카메라 프리뷰 프레임의 원하는 높이
     * @return 선택한 미리 보기 및 사진 크기 쌍
     */
    public static SizePair selectSizePair(Camera camera, int desiredWidth, int desiredHeight) {
        List<SizePair> validPreviewSizes = generateValidPreviewSizeList(camera);

        // 최적의 크기를 선택하는 방법은 원하는 값과 너비 및 높이에 대한 실제 값 사이의 차이 합계를 최소화하는 것
        // 최상의 크기를 선택하는 유일한 방법은 아니지만, 가장 가까운 가로 세로 비율을 사용하는 것과
        // 가장 가까운 픽셀 영역을 사용하는 것 사이의 적절한 균형을 제공
        SizePair selectedPair = null;
        int minDiff = Integer.MAX_VALUE;
        for (SizePair sizePair : validPreviewSizes) {
            Size size = sizePair.preview;
            int diff =
                    Math.abs(size.getWidth() - desiredWidth) + Math.abs(size.getHeight() - desiredHeight);
            if (diff < minDiff) {
                selectedPair = sizePair;
                minDiff = diff;
            }
        }

        return selectedPair;
    }

    /**
     * 미리 보기 크기와 해당 동일한 가로 세로 비율의 사진 크기를 저장합니다.
     * 일부 장치에서 미리 보기 이미지가 왜곡되지 않도록 하려면 사진 크기를 미리 보기 크기와 동일한 가로 세로 비율로 설정해야 합니다.
     * 잘못 설정한다면 미리 보기가 왜곡될 수 있습니다.
     */
    public static class SizePair {
        public final Size preview;
        @Nullable public final Size picture;

        SizePair(Camera.Size previewSize, @Nullable Camera.Size pictureSize) {
            preview = new Size(previewSize.width, previewSize.height);
            picture = pictureSize != null ? new Size(pictureSize.width, pictureSize.height) : null;
        }

        public SizePair(Size previewSize, @Nullable Size pictureSize) {
            preview = previewSize;
            picture = pictureSize;
        }
    }

    /**
     * 허용되는 미리 보기 크기 목록을 생성합니다.
     * 동일한 가로 세로 비율의 해당 사진 크기가 없으면 미리 보기 크기가 허용되지 않습니다.
     * 동일한 가로 세로 비율의 해당 사진 크기가 있는 경우 사진 크기는 미리 보기 크기와 쌍을 이룸
     */
    public static List<SizePair> generateValidPreviewSizeList(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        List<SizePair> validPreviewSizes = new ArrayList<>();
        for (Camera.Size previewSize : supportedPreviewSizes) {
            float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;

            // 사진 크기를 순서대로 반복함으로써, 더 높은 해상도를 선호
            // 나중에 전체 해상도 사진을 찍을 수 있도록 가장 높은 해상도를 선택
            for (Camera.Size pictureSize : supportedPictureSizes) {
                float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                    validPreviewSizes.add(new SizePair(previewSize, pictureSize));
                    break;
                }
            }
        }

        // 미리 보기 크기와 동일한 가로 세로 비율을 가진 사진 크기가 없는 경우 모든 미리 보기 크기를 허용
        if (validPreviewSizes.size() == 0) {
            Log.w(TAG, "No preview sizes have a corresponding same-aspect-ratio picture size");
            for (Camera.Size previewSize : supportedPreviewSizes) {
                // The null picture size will let us know that we shouldn't set a picture size.
                validPreviewSizes.add(new SizePair(previewSize, null));
            }
        }

        return validPreviewSizes;
    }

    /**
     * 하는 초당 프레임이 지정된 초당 가장 적합한 미리 보기 프레임을 선택
     *
     * @param camera 초당 프레임 범위를 선택할 카메라
     * @param desiredPreviewFps 카메라 프리뷰 프레임에 필요한 초당 프레임 수
     * @return 선택한 초당 미리 보기 프레임 수
     */
    @SuppressLint("InlinedApi")
    private static int[] selectPreviewFpsRange(Camera camera, float desiredPreviewFps) {
        // 카메라 API는 부동소수점 프레임 속도 대신 1000 배수로 스케일링된 정수를 사용
        int desiredPreviewFpsScaled = (int) (desiredPreviewFps * 1000.0f);

        // 상한은 원하는 fps에 최대한 가깝고 하한은 가능한 작은 범위를 선택하여 낮은 조명 조건에서 프레임을 적절하게 노출
        int[] selectedFpsRange = null;
        int minUpperBoundDiff = Integer.MAX_VALUE;
        int minLowerBound = Integer.MAX_VALUE;
        List<int[]> previewFpsRangeList = camera.getParameters().getSupportedPreviewFpsRange();
        for (int[] range : previewFpsRangeList) {
            int upperBoundDiff =
                    Math.abs(desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
            int lowerBound = range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
            if (upperBoundDiff <= minUpperBoundDiff && lowerBound <= minLowerBound) {
                selectedFpsRange = range;
                minUpperBoundDiff = upperBoundDiff;
                minLowerBound = lowerBound;
            }
        }
        return selectedFpsRange;
    }

    /**
     * 지정된 카메라 ID에 대한 올바른 회전을 계산하고 파라미터의 회전을 설정합니다
     * 카메라의 디스플레이 방향과 회전을 설정
     *
     * @param parameters 회전을 설정할 카메라 파라미터
     * @param cameraId 카메라 ID를 기반으로 회전을 설정
     */
    private void setRotation(Camera camera, Camera.Parameters parameters, int cameraId) {
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        int degrees = 0;
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                Log.e(TAG, "Bad rotation value: " + rotation);
        }

        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int displayAngle;
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            this.rotationDegrees = (cameraInfo.orientation + degrees) % 360;
            displayAngle = (360 - this.rotationDegrees) % 360; // 반전된 카메라 화면 보정
        } else { // 역향
            this.rotationDegrees = (cameraInfo.orientation - degrees + 360) % 360;
            displayAngle = this.rotationDegrees;
        }
        Log.d(TAG, "Display rotation is: " + rotation);
        Log.d(TAG, "Camera face is: " + cameraInfo.facing);
        Log.d(TAG, "Camera rotation is: " + cameraInfo.orientation);
        // 이 값은 ImageMetadata가 허용하는 정도(0, 90, 180 또는 270) 중 하나여야 합니다.
        Log.d(TAG, "RotationDegrees is: " + this.rotationDegrees);

        camera.setDisplayOrientation(displayAngle);
        parameters.setRotation(this.rotationDegrees);
    }

    /**
     * 카메라 미리 보기 콜백을 위한 하나의 버퍼를 만듭니다.
     * 버퍼 크기는 카메라 미리 보기 크기와 카메라 이미지 형식을 기반으로 합니다.
     *
     * @return 현재 카메라 설정에 적합한 크기의 새 미리 보기 버퍼
     */
    @SuppressLint("InlinedApi")
    private byte[] createPreviewBuffer(Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(IMAGE_FORMAT);
        long sizeInBits = (long) previewSize.getHeight() * previewSize.getWidth() * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;

        // .allocate()를 사용하는 것과 달리, 바이트 배열을 이런 방식으로 생성하고 래핑하면
        // 작업할 배열이 있다는 것을 보장할 수 있습니다.
        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }

        bytesToByteBuffer.put(byteArray, buffer);
        return byteArray;
    }

    // ==============================================================================================
    // ==============================================================================================

    /** 카메라에 새 미리 보기 프레임이 있을 때 호출됩니다 */
    private class CameraPreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            processingRunnable.setNextFrame(data, camera);
        }
    }

    public void setMachineLearningFrameProcessor(VisionImageProcessor processor) {
        synchronized (processorLock) {
            cleanScreen();
            if (frameProcessor != null) {
                frameProcessor.stop();
            }
            frameProcessor = processor;
        }
    }

    /**
     * 기본 수신기에 대한 액세스를 제어하여 카메라에서 사용할 수 있을 때 프레임을 처리하도록 호출
     * 프레임에 대한 탐지를 최대한 빠르게 실행하도록 설계
     */
    private class FrameProcessingRunnable implements Runnable {

        // 아래의 모든 멤버 변수를 private 로 선언.
        private final Object lock = new Object();
        private boolean active = true;

        // 이러한 보류 중인 변수는 처리 대기 중인 새 프레임과 연관된 상태를 유지
        private ByteBuffer pendingFrameData;

        FrameProcessingRunnable() {}

        /** 실행 테이블을 활성/비활성으로 표시합니다. 차단된 스레드가 계속 진행되도록 신호 */
        void setActive(boolean active) {
            synchronized (lock) {
                this.active = active;
                lock.notifyAll();
            }
        }

        /**
         * 카메라로부터 수신한 프레임 데이터를 설정합니다.
         * 이전에 사용하지 않은 프레임 버퍼(있는 경우)가 다시 카메라에 추가되고 나중에 사용할 수 있도록 프레임 데이터에 대한 보류 중인 참조가 유지
         */
        @SuppressWarnings("ByteBufferBackingArray")
        void setNextFrame(byte[] data, Camera camera) {
            synchronized (lock) {
                if (pendingFrameData != null) {
                    camera.addCallbackBuffer(pendingFrameData.array());
                    pendingFrameData = null;
                }

                if (!bytesToByteBuffer.containsKey(data)) {
                    Log.d(
                            TAG,
                            "Skipping frame. Could not find ByteBuffer associated with the image "
                                    + "data from the camera.");
                    return;
                }

                pendingFrameData = bytesToByteBuffer.get(data);

                // 프로세서 스레드가 다음 프레임에서 대기 중인 경우 이를 알립니다
                lock.notifyAll();
            }
        }

        /**
         * 처리 스레드가 활성 상태인 한, 이것은 프레임에 대한 탐지를 연속적으로 실행
         * 보류 중인 프레임은 즉시 사용 가능하거나 아직 수신되지 않은 상태
         * 사용 가능한 경우 프레임 정보를 로컬 변수로 전송하고 해당 프레임에 대한 탐지를 실행
         * 다음 프레임에 대해 일시 중지 없이 즉시 루프백
         *
         */
        @SuppressLint("InlinedApi")
        @SuppressWarnings({"GuardedBy", "ByteBufferBackingArray"})
        @Override
        public void run() {
            ByteBuffer data;

            while (true) {
                synchronized (lock) {
                    while (active && (pendingFrameData == null)) {
                        try {
                            // 카메라에서 다음 프레임이 수신될 때까지 기다림
                            lock.wait();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "Frame processing loop terminated.", e);
                            return;
                        }
                    }

                    if (!active) {
                        // 이 카메라 소스가 중지되거나 해제되면 루프를 종료합니다.
                        // 위의 wait() 직후에 setActive(false)가 호출되어 이 루프의 종료를 트리거한 경우를 처리하기 위해 여기서 이것을 확인
                        return;
                    }

                    // 프레임 데이터를 로컬에서 보관하면 아래 탐지용으로 사용할 수 있습니다.
                    // 데이터를 사용하기 전에 이 버퍼가 카메라로 다시 재생되지 않도록 보류 중인 FrameData를 지우기
                    data = pendingFrameData;
                    pendingFrameData = null;
                }

                // 현재 프레임에서 탐지를 실행하는 동안 카메라가 보류 중인 프레임을 추가할 수 있으므로 동기화 밖에서 실행

                try {
                    synchronized (processorLock) {
                        frameProcessor.processByteBuffer(
                                data,
                                new FrameMetadata.Builder()
                                        .setWidth(previewSize.getWidth())
                                        .setHeight(previewSize.getHeight())
                                        .setRotation(rotationDegrees)
                                        .build(),
                                graphicOverlay);
                    }
                } catch (Exception t) {
                    Log.e(TAG, "Exception thrown from receiver.", t);
                } finally {
                    camera.addCallbackBuffer(data.array());
                }
            }
        }
    }

    /** graphicOverlay 하위 클래스도 정리를 수행. */
    private void cleanScreen() {
        graphicOverlay.clear();
    }
}
