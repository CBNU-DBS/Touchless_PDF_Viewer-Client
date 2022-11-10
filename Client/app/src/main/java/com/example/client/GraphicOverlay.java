package com.example.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;

/**
 * 일련의 사용자 지정 그래픽을 관련 미리 보기(예: 카메라 미리 보기) 위에 오버레이하는 보기
 * 그래픽 객체를 추가하고 객체를 업데이트하고 제거하여 뷰 내에서 적절한 도면과 무효화를 트리거할 수 있습니다.
 */
public class GraphicOverlay extends View {
    private final Object lock = new Object();
    private final List<Graphic> graphics = new ArrayList<>();
    // 영상 좌표에서 오버레이 보기 좌표로 변환하기 위한 행렬입니다.
    private final Matrix transformationMatrix = new Matrix();

    private int imageWidth;
    private int imageHeight;
    // 이미지 크기에 대한 보기 크기를 오버레이하는 요인입니다.
    // 영상 좌표에 있는 모든 항목은 오버레이 보기 영역에 맞도록 이 양만큼 크기를 조정해야 합니다.
    private float scaleFactor = 1.0f;
    // 스케일링 후 오버레이 보기 영역에 이미지를 맞추기 위해 각 측면에서 잘라내야 하는 수평 픽셀 수
    private float postScaleWidthOffset;
    // 스케일링 후 오버레이 보기 영역에 이미지를 맞추기 위해 각 측면에서 잘라야 하는 수직 픽셀 수
    private float postScaleHeightOffset;
    private boolean isImageFlipped;
    private boolean needUpdateTransformation = true;

    /**
     * 그래픽 오버레이 내에서 렌더링할 사용자 지정 그래픽 객체의 기본 클래스
     */
    public abstract static class Graphic {
        private GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        /**
         * canvas에 그래픽을 그립니다. 도면은 다음 방법을 사용하여 그려진 그래픽에 대한 뷰 좌표로 변환해야 합니다.
         * @param canvas drawing canvas
         */
        public abstract void draw(Canvas canvas);

        /** 영상 축척에서 뷰 축척으로 제공된 값을 조정합니다. */
        public float scale(float imagePixel) {
            return imagePixel * overlay.scaleFactor;
        }

        /** 앱의 응용 프로그램 컨텍스트를 반환합니다. */
        public Context getApplicationContext() {
            return overlay.getContext().getApplicationContext();
        }

        public boolean isImageFlipped() {
            return overlay.isImageFlipped;
        }

        /**
         * 이미지의 좌표계에서 보기 좌표계로 x 좌표를 조정합니다.
         */
        public float translateX(float x) {
            if (overlay.isImageFlipped) {
                return overlay.getWidth() - (scale(x) - overlay.postScaleWidthOffset);
            } else {
                return scale(x) - overlay.postScaleWidthOffset;
            }
        }

        /**
         * 이미지의 좌표계에서 보기 좌표계로 좌표를 조정합니다.
         */
        public float translateY(float y) {
            return scale(y) - overlay.postScaleHeightOffset;
        }

        public Matrix getTransformationMatrix() {
            return overlay.transformationMatrix;
        }

        public void postInvalidate() {
            overlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        addOnLayoutChangeListener(
                (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                        needUpdateTransformation = true);
    }

    /** 오버레이에서 모든 그래픽을 제거합니다. */
    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }

    /** 오버레이에 그래픽을 추가합니다. */
    public void add(Graphic graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
    }

    /** 오버레이에서 그래픽을 제거합니다. */
    public void remove(Graphic graphic) {
        synchronized (lock) {
            graphics.remove(graphic);
        }
        postInvalidate();
    }

    /**
     * 이미지 좌표를 나중에 변환하는 방법을 알려주는 크기 및 반전 여부를 포함하여
     * 디텍터에서 처리 중인 이미지의 소스 정보를 설정합니다.
     *
     * @param imageWidth ML Kit 디텍터로 전송된 이미지의 너비
     * @param imageHeight ML Kit 디텍터로 전송된 이미지 높이
     * @param isFlipped 이미지가 반전되었는지 여부. 이미지가 전면 카메라에서 나온 경우 이 값을 true로 설정해야 합니다.
     */
    public void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped) {
        Preconditions.checkState(imageWidth > 0, "image width must be positive");
        Preconditions.checkState(imageHeight > 0, "image height must be positive");
        synchronized (lock) {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.isImageFlipped = isFlipped;
            needUpdateTransformation = true;
        }
        postInvalidate();
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    private void updateTransformationIfNeeded() {
        if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0) {
            return;
        }
        float viewAspectRatio = (float) getWidth() / getHeight();
        float imageAspectRatio = (float) imageWidth / imageHeight;
        postScaleWidthOffset = 0;
        postScaleHeightOffset = 0;
        if (viewAspectRatio > imageAspectRatio) {
            // 이 보기에 표시하려면 이미지를 수직으로 잘라야 합니다.
            scaleFactor = (float) getWidth() / imageWidth;
            postScaleHeightOffset = ((float) getWidth() / imageAspectRatio - getHeight()) / 2;
        } else {
            // 이 보기에 표시하려면 이미지를 수평으로 잘라야 합니다.
            scaleFactor = (float) getHeight() / imageHeight;
            postScaleWidthOffset = ((float) getHeight() * imageAspectRatio - getWidth()) / 2;
        }

        transformationMatrix.reset();
        transformationMatrix.setScale(scaleFactor, scaleFactor);
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset);

        if (isImageFlipped) {
            transformationMatrix.postScale(-1f, 1f, getWidth() / 2f, getHeight() / 2f);
        }

        needUpdateTransformation = false;
    }

    /** 연결된 그래픽 개체로 오버레이를 그립니다. */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            updateTransformationIfNeeded();

            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        }
    }
}
