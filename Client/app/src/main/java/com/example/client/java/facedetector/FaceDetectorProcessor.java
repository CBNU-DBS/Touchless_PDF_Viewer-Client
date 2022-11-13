package com.example.client.java.facedetector;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.example.client.GraphicOverlay;
import com.example.client.java.VisionProcessorBase;
import com.example.client.preference.PreferenceUtils;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import java.util.List;
import java.util.Locale;

/**
 * MLKit FaceDetector 코드
 * FaceDetector로 입력 이미지에 대해 결과값을 확률로 생성
 */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {

    private static final String TAG = "FaceDetectorProcessor";
    private final FaceDetector detector;

    /**
     * FaceDetector 생성시 설정 옵션 불러오기
     * @param context
     */
    public FaceDetectorProcessor(Context context) {
        super(context);
        FaceDetectorOptions faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(context);
        Log.v(MANUAL_TESTING_LOG, "Face detector options: " + faceDetectorOptions);
        detector = FaceDetection.getClient(faceDetectorOptions);
    }

    /**
     * FaceDetector 종료시 실행 코드
     */
    @Override
    public void stop() {
        super.stop();
        detector.close();
    }

    /**
     * 동영상으로 받아오는 이미지를 detector 연산자로 분리하여 List 반환
     * @param image
     * @return
     */
    @Override
    protected Task<List<Face>> detectInImage(InputImage image) {
        return detector.process(image);
    }

    /**
     * detectInImage 함수 성공시 List<Face>를 입력받아 카메라에서의 좌표 구함
     * @param faces
     * @param graphicOverlay
     */
    @Override
    protected void onSuccess(@NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay) {
        for (Face face : faces) {
            graphicOverlay.add(new FaceGraphic(graphicOverlay, face));
            logExtrasForTesting(face);
        }
    }

    /**
     * 입력받은 Face를 기반으로 카메라에서의 좌표를 구하고, 모든 랜드마크에 대해 각 좌표를 생성.
     * 모든 랜드마크들에 대해서 각 행동들을 확률로 표현
     * @param face
     */
    private static void logExtrasForTesting(Face face) {
        if (face != null) {
            Log.v(MANUAL_TESTING_LOG, "face bounding box: " + face.getBoundingBox().flattenToString());
            Log.v(MANUAL_TESTING_LOG, "face Euler Angle X: " + face.getHeadEulerAngleX());
            Log.v(MANUAL_TESTING_LOG, "face Euler Angle Y: " + face.getHeadEulerAngleY());
            Log.v(MANUAL_TESTING_LOG, "face Euler Angle Z: " + face.getHeadEulerAngleZ());

            // 모든 랜드마크
            int[] landMarkTypes =
                    new int[] {
                            FaceLandmark.MOUTH_BOTTOM,
                            FaceLandmark.MOUTH_RIGHT,
                            FaceLandmark.MOUTH_LEFT,
                            FaceLandmark.RIGHT_EYE,
                            FaceLandmark.LEFT_EYE,
                            FaceLandmark.RIGHT_EAR,
                            FaceLandmark.LEFT_EAR,
                            FaceLandmark.RIGHT_CHEEK,
                            FaceLandmark.LEFT_CHEEK,
                            FaceLandmark.NOSE_BASE
                    };
            String[] landMarkTypesStrings =
                    new String[] {
                            "MOUTH_BOTTOM",
                            "MOUTH_RIGHT",
                            "MOUTH_LEFT",
                            "RIGHT_EYE",
                            "LEFT_EYE",
                            "RIGHT_EAR",
                            "LEFT_EAR",
                            "RIGHT_CHEEK",
                            "LEFT_CHEEK",
                            "NOSE_BASE"
                    };
            /**
             * 모든 랜드마크들에 대해 입력받은 Face 정보를 기반으로 확인을 위한 Log
             */
            for (int i = 0; i < landMarkTypes.length; i++) {
                FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
                if (landmark == null) {
                    Log.v(
                            MANUAL_TESTING_LOG,
                            "No landmark of type: " + landMarkTypesStrings[i] + " has been detected");
                } else {
                    PointF landmarkPosition = landmark.getPosition();
                    String landmarkPositionStr =
                            String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);
                    Log.v(
                            MANUAL_TESTING_LOG,
                            "Position for face landmark: "
                                    + landMarkTypesStrings[i]
                                    + " is :"
                                    + landmarkPositionStr);
                }
            }
            /**
             * 왼쪽, 오른쪽 눈 뜨고 있을 확률
             * 입력받은 Face가 웃고 있을 확률
             */
            Log.v(
                    MANUAL_TESTING_LOG,
                    "face left eye open probability: " + face.getLeftEyeOpenProbability());
            Log.v(
                    MANUAL_TESTING_LOG,
                    "face right eye open probability: " + face.getRightEyeOpenProbability());
            Log.v(MANUAL_TESTING_LOG, "face smiling probability: " + face.getSmilingProbability());
            Log.v(MANUAL_TESTING_LOG, "face tracking id: " + face.getTrackingId());
        }
    }

    /**
     * 인식 실패시의 예외처리
     * @param e
     */
    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
}

