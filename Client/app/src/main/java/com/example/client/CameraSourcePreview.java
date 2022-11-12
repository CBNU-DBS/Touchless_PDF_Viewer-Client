package com.example.client;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import com.google.android.gms.common.images.Size;
import com.example.client.preference.PreferenceUtils;
import java.io.IOException;

/** 화면에서 카메라 이미지 미리 보기 */
public class CameraSourcePreview extends ViewGroup {
    private static final String TAG = "MIDemoApp:Preview";

    private final Context context;
    private final SurfaceView surfaceView;
    private boolean startRequested;
    private boolean surfaceAvailable;
    private CameraSource cameraSource;

    private GraphicOverlay overlay;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        startRequested = false;
        surfaceAvailable = false;

        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(surfaceView);
    }

    private void start(CameraSource cameraSource) throws IOException {
        this.cameraSource = cameraSource;

        if (this.cameraSource != null) {
            startRequested = true;
            startIfReady();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        this.overlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (cameraSource != null) {
            cameraSource.stop();
        }
    }

    public void release() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
        surfaceView.getHolder().getSurface().release();
    }

    private void startIfReady() throws IOException, SecurityException {
        if (startRequested && surfaceAvailable) {
            if (PreferenceUtils.isCameraLiveViewportEnabled(context)) {
                cameraSource.start(surfaceView.getHolder());
            } else {
                cameraSource.start();
            }
            requestLayout();

            if (overlay != null) {
                Size size = cameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                boolean isImageFlipped = cameraSource.getCameraFacing() == CameraSource.CAMERA_FACING_FRONT;
                if (isPortraitMode()) {
                    // 세로 방향으로 90도 회전하므로 가로 및 높이 크기를 바꿉니다.
                    // 카메라 미리 보기와 처리 중인 이미지의 크기가 같습니다.
                    overlay.setImageSourceInfo(min, max, isImageFlipped);
                } else {
                    overlay.setImageSourceInfo(max, min, isImageFlipped);
                }
                overlay.clear();
            }
            startRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            surfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            surfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = 320;
        int height = 240;
        if (cameraSource != null) {
            Size size = cameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
            }
        }

        // 세로 방향으로 90도 회전하므로 가로 및 높이 크기를 바꿉니다.
        if (isPortraitMode()) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        float previewAspectRatio = (float) width / height;
        int layoutWidth = right - left;
        int layoutHeight = bottom - top;
        float layoutAspectRatio = (float) layoutWidth / layoutHeight;
        if (previewAspectRatio > layoutAspectRatio) {
            // 미리 보기 입력이 레이아웃 영역보다 넓다면 레이아웃 높이를 맞추고 가운데를 유지하면서 미리보기 입력을 가로로 자릅니다.
            int horizontalOffset = (int) (previewAspectRatio * layoutHeight - layoutWidth) / 2;
            surfaceView.layout(-horizontalOffset, 0, layoutWidth + horizontalOffset, layoutHeight);
        } else {
            // 미리 보기 입력이 레이아웃 영역보다 크다면 레이아웃 너비를 맞추고 가운데를 유지하면서 미리보기 입력을 수직으로 자릅니다.
            int verticalOffset = (int) (layoutWidth / previewAspectRatio - layoutHeight) / 2;
            surfaceView.layout(0, -verticalOffset, layoutWidth, layoutHeight + verticalOffset);
        }
    }

    private boolean isPortraitMode() {
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}
