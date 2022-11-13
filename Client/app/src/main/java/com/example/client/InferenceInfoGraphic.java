package com.example.client;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.Nullable;

/**
 * 오버레이 보기에서 추론 정보(대기 시간, FPS, 해상도)를 렌더링하기 위한 그래픽 인스턴스 Activity Class.
 * 해당 앱에서는 가시성 문제로 사용하지 않음.
 */
public class InferenceInfoGraphic extends GraphicOverlay.Graphic {

    private static final int TEXT_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 60.0f;

    private final Paint textPaint;
    private final GraphicOverlay overlay;
    private final long frameLatency;
    private final long detectorLatency;

    // 입력 이미지 스트림이 처리되는 경우에만 유효합니다. 단일 이미지 모드의 경우 null
    @Nullable private final Integer framesPerSecond;
    private boolean showLatencyInfo = true;

    public InferenceInfoGraphic(
            GraphicOverlay overlay,
            long frameLatency,
            long detectorLatency,
            @Nullable Integer framesPerSecond) {
        super(overlay);
        this.overlay = overlay;
        this.frameLatency = frameLatency;
        this.detectorLatency = detectorLatency;
        this.framesPerSecond = framesPerSecond;
        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK);
        postInvalidate();
    }

    public InferenceInfoGraphic(GraphicOverlay overlay) {
        this(overlay, 0, 0, null);
        showLatencyInfo = false;
    }

    @Override
    public synchronized void draw(Canvas canvas) {
        float x = TEXT_SIZE * 0.5f;
        float y = TEXT_SIZE * 1.5f;

        canvas.drawText(
                "InputImage size: " + overlay.getImageHeight() + "x" + overlay.getImageWidth(),
                x,
                y,
                textPaint);

        if (!showLatencyInfo) {
            return;
        }
        // FPS 표시 및 추론 대기 시간
        if (framesPerSecond != null) {
            canvas.drawText(
                    "FPS: " + framesPerSecond + ", Frame latency: " + frameLatency + " ms",
                    x,
                    y + TEXT_SIZE,
                    textPaint);
        } else {
            canvas.drawText("Frame latency: " + frameLatency + " ms", x, y + TEXT_SIZE, textPaint);
        }
        canvas.drawText(
                "Detector latency: " + detectorLatency + " ms", x, y + TEXT_SIZE * 2, textPaint);
    }
}
