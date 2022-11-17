package com.example.client;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.example.client.GraphicOverlay.Graphic;

/**
 * background 상태에서 카메라 이미지 생성 Activity Class.
 */
public class CameraImageGraphic extends Graphic {

    private final Bitmap bitmap;

    public CameraImageGraphic(GraphicOverlay overlay, Bitmap bitmap) {
        super(overlay);
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, getTransformationMatrix(), null);
    }
}
