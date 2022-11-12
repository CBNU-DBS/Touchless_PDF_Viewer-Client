package com.example.client;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.Image.Plane;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;
import androidx.exifinterface.media.ExifInterface;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/** 비트맵 변환을 위한 유틸리티 */
public class BitmapUtils {
    private static final String TAG = "BitmapUtils";

    /** NV21 형식 바이트 버퍼를 비트맵으로 변환합니다. */
    @Nullable
    public static Bitmap getBitmap(ByteBuffer data, FrameMetadata metadata) {
        data.rewind();
        byte[] imageInBuffer = new byte[data.limit()];
        data.get(imageInBuffer, 0, imageInBuffer.length);
        try {
            YuvImage image =
                    new YuvImage(
                            imageInBuffer, ImageFormat.NV21, metadata.getWidth(), metadata.getHeight(), null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, metadata.getWidth(), metadata.getHeight()), 80, stream);

            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

            stream.close();
            return rotateBitmap(bmp, metadata.getRotation(), false, false);
        } catch (Exception e) {
            Log.e("VisionProcessorBase", "Error: " + e.getMessage());
        }
        return null;
    }

    /** YUV_420_888 이미지를 CameraX API에서 비트맵으로 변환합니다. */
    @RequiresApi(VERSION_CODES.LOLLIPOP)
    @Nullable
    @ExperimentalGetImage
    public static Bitmap getBitmap(ImageProxy image) {
        FrameMetadata frameMetadata =
                new FrameMetadata.Builder()
                        .setWidth(image.getWidth())
                        .setHeight(image.getHeight())
                        .setRotation(image.getImageInfo().getRotationDegrees())
                        .build();

        ByteBuffer nv21Buffer =
                yuv420ThreePlanesToNV21(image.getImage().getPlanes(), image.getWidth(), image.getHeight());
        return getBitmap(nv21Buffer, frameMetadata);
    }

    /** 비트맵이 바이트 버퍼에서 변환된 경우 비트맵을 회전합니다. */
    private static Bitmap rotateBitmap(
            Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();

        // 이미지를 다시 바르게 회전합니다.
        matrix.postRotate(rotationDegrees);

        // X 또는 Y 축을 따라 이미지를 미러링합니다.
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // 이전 비트맵이 변경된 경우 해당 비트맵을 재활용합니다.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    @Nullable
    public static Bitmap getBitmapFromContentUri(ContentResolver contentResolver, Uri imageUri)
            throws IOException {
        Bitmap decodedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
        if (decodedBitmap == null) {
            return null;
        }
        int orientation = getExifOrientationTag(contentResolver, imageUri);

        int rotationDegrees = 0;
        boolean flipX = false;
        boolean flipY = false;
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                flipX = true;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                rotationDegrees = 90;
                flipX = true;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegrees = 180;
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                flipY = true;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegrees = -90;
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                rotationDegrees = -90;
                flipX = true;
                break;
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                // 이 경우에는 변환이 필요하지 않습니다.
        }

        return rotateBitmap(decodedBitmap, rotationDegrees, flipX, flipY);
    }

    private static int getExifOrientationTag(ContentResolver resolver, Uri imageUri) {
        // 장치의 로컬 파일에서 EXIF 방향 태그 구문 분석만을 지원
        if (!ContentResolver.SCHEME_CONTENT.equals(imageUri.getScheme())
                && !ContentResolver.SCHEME_FILE.equals(imageUri.getScheme())) {
            return 0;
        }

        ExifInterface exif;
        try (InputStream inputStream = resolver.openInputStream(imageUri)) {
            if (inputStream == null) {
                return 0;
            }

            exif = new ExifInterface(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "failed to open file to read rotation meta data: " + imageUri, e);
            return 0;
        }

        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }

    /**
     * YUV_420_888을 NV21 바이트 버퍼로 변환합니다.
     *
     * <p> NV21 형식은 Y, U 및 V 값을 포함하는 단일 바이트 배열로 구성됩니다. 크기가 S인 이미지의 경우
     * 배열의 첫 번째 S 위치에 모든 Y 값이 포함됩니다. 나머지 위치에는 인터리빙된 V 및 U 값이 포함됩니다.
     * U와 V는 두 차원 모두에서 2의 계수로 하위 샘플링되므로 S/4U 값과 S/4V 값이 있습니다
     *
     * <p> YUV_420_888은 U와 V가 두 차원 모두에서 2배만큼 하위 샘플링된 모든 YUV 이미지를 기술할 수 있는 일반적인 형식
     * Y 평면은 인터리브되지 않도록 보장되므로 NV21 어레이의 첫 번째 부분에 값을 복사할 수 있습니다.
     * U 및 V 평면은 이미 NV21 형식의 표현을 가지고 있을 수 있습니다.
     * 이 문제는 평면이 동일한 버퍼를 공유하고 V 버퍼가 U 버퍼 앞의 한 위치이고 평면의 픽셀 스트라이드가 2인 경우에 발생합니다.
     */
    @RequiresApi(VERSION_CODES.KITKAT)
    private static ByteBuffer yuv420ThreePlanesToNV21(
            Plane[] yuv420888planes, int width, int height) {
        int imageSize = width * height;
        byte[] out = new byte[imageSize + 2 * (imageSize / 4)];

        if (areUVPlanesNV21(yuv420888planes, width, height)) {
            // Y 값을 복사합니다.
            yuv420888planes[0].getBuffer().get(out, 0, imageSize);

            ByteBuffer uBuffer = yuv420888planes[1].getBuffer();
            ByteBuffer vBuffer = yuv420888planes[2].getBuffer();
            // U 버퍼에 V 값이 포함되어 있지 않으므로 V 버퍼에서 첫 번째 V 값을 가져옵니다.
            vBuffer.get(out, imageSize, 1);
            // U 버퍼에서 첫 번째 U 값과 나머지 VU 값을 복사합니다.
            uBuffer.get(out, imageSize + 1, 2 * imageSize / 4 - 1);
        } else {
            // UV 값을 하나씩 복사하는 것으로 되돌아가면 속도가 느리지만 작동하기도 합니다.
            // Unpack Y.
            unpackPlane(yuv420888planes[0], width, height, out, 0, 1);
            // Unpack U.
            unpackPlane(yuv420888planes[1], width, height, out, imageSize + 1, 2);
            // Unpack V.
            unpackPlane(yuv420888planes[2], width, height, out, imageSize, 2);
        }

        return ByteBuffer.wrap(out);
    }

    /** YUV_420_888 이미지의 UV 평면 버퍼가 NV21 형식인지 확인합니다. */
    @RequiresApi(VERSION_CODES.KITKAT)
    private static boolean areUVPlanesNV21(Plane[] planes, int width, int height) {
        int imageSize = width * height;

        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        // 백업 버퍼 속성.
        int vBufferPosition = vBuffer.position();
        int uBufferLimit = uBuffer.limit();

        // U 버퍼에 첫 번째 V 값이 포함되지 않으므로 V 버퍼를 1바이트 전진시킵니다.
        vBuffer.position(vBufferPosition + 1);
        // V 버퍼에 마지막 U 값이 포함되지 않으므로 U 버퍼의 마지막 바이트를 잘라냅니다.
        uBuffer.limit(uBufferLimit - 1);

        // 버퍼가 같고 예상되는 요소 수가 있는지 확인합니다.
        boolean areNV21 =
                (vBuffer.remaining() == (2 * imageSize / 4 - 2)) && (vBuffer.compareTo(uBuffer) == 0);

        // 버퍼를 초기 상태로 복원합니다.
        vBuffer.position(vBufferPosition);
        uBuffer.limit(uBufferLimit);

        return areNV21;
    }

    /**
     * 이미지 평면을 바이트 배열로 압축을 풉니다.
     */
    @TargetApi(VERSION_CODES.KITKAT)
    private static void unpackPlane(
            Plane plane, int width, int height, byte[] out, int offset, int pixelStride) {
        ByteBuffer buffer = plane.getBuffer();
        buffer.rewind();

        // 현재 평면의 크기를 계산합니다.
        // 원래 이미지와 같은 가로 세로 비율을 가지고 있다고 가정
        int numRow = (buffer.limit() + plane.getRowStride() - 1) / plane.getRowStride();
        if (numRow == 0) {
            return;
        }
        int scaleFactor = height / numRow;
        int numCol = width / scaleFactor;

        // 출력 버퍼에서 데이터를 추출
        int outputPos = offset;
        int rowStart = 0;
        for (int row = 0; row < numRow; row++) {
            int inputPos = rowStart;
            for (int col = 0; col < numCol; col++) {
                out[outputPos] = buffer.get(inputPos);
                outputPos += pixelStride;
                inputPos += plane.getPixelStride();
            }
            rowStart += plane.getRowStride();
        }
    }
}
