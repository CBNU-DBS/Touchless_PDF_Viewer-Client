package com.example.client.java.facedetector;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.example.client.PDF_View_Activity;
import com.example.client.MotionSettingActivity;

import com.example.client.ActivityLocal;
import com.example.client.GraphicOverlay;
import com.example.client.GraphicOverlay.Graphic;
import com.google.mlkit.vision.face.Face;


/**
 * Graphic instance for rendering face position, contour, and landmarks within the associated
 * graphic overlay view.
 */


public class FaceGraphic extends Graphic {
    private static final float FACE_POSITION_RADIUS = 8.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 40.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private static final int NUM_COLORS = 10;
    private static final int[][] COLORS =
            new int[][] {
                    // {Text color, background color}
                    {Color.BLACK, Color.WHITE},
                    {Color.WHITE, Color.MAGENTA},
                    {Color.BLACK, Color.LTGRAY},
                    {Color.WHITE, Color.RED},
                    {Color.WHITE, Color.BLUE},
                    {Color.WHITE, Color.DKGRAY},
                    {Color.BLACK, Color.CYAN},
                    {Color.BLACK, Color.YELLOW},
                    {Color.WHITE, Color.BLACK},
                    {Color.BLACK, Color.GREEN}
            };

    private final Paint facePositionPaint;
    private final Paint[] idPaints;
    private final Paint[] boxPaints;
    private final Paint[] labelPaints;

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    private volatile Face face;
    int botheyecheck = 0;
    int leftcheck = 0;
    int rightcheck = 0;
    int faceup = 0;
    int facedown = 0;
    int faceleft = 0;
    int faceright = 0;
    float eulerXdegree = 0.0F;
    float eulerYdegree = 0.0F;
    float lefteye = 0.0F;
    float righteye = 0.0F;

    Toast leftToast = Toast.makeText(this.getApplicationContext(),"왼쪽눈 감기 확인", Toast.LENGTH_SHORT);
    Toast rightToast = Toast.makeText(this.getApplicationContext(),"오른쪽눈 감기 확인", Toast.LENGTH_SHORT);
    Toast bothToast = Toast.makeText(this.getApplicationContext(),"양눈 감기 확인", Toast.LENGTH_SHORT);
    Toast facerightToast = Toast.makeText(this.getApplicationContext(),"머리 오른쪽 확인", Toast.LENGTH_SHORT);
    Toast faceleftToast = Toast.makeText(this.getApplicationContext(),"머리 왼쪽 확인", Toast.LENGTH_SHORT);
    Toast faceupToast = Toast.makeText(this.getApplicationContext(),"고개 들기 확인", Toast.LENGTH_SHORT);
    Toast facedownToast = Toast.makeText(this.getApplicationContext(),"고개 숙이기 확인", Toast.LENGTH_SHORT);

//    Log.v(MANUAL_TESTING_LOG, "face Euler Angle X: " + face.getHeadEulerAngleX());
//    Log.v(MANUAL_TESTING_LOG, "face Euler Angle Y: " + face.getHeadEulerAngleY());
//    Log.v(MANUAL_TESTING_LOG, "face Euler Angle Z: " + face.getHeadEulerAngleZ());

    FaceGraphic(GraphicOverlay overlay, Face face) {
        super(overlay);

        this.face = face;
        final int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        int numColors = COLORS.length;
        idPaints = new Paint[numColors];
        boxPaints = new Paint[numColors];
        labelPaints = new Paint[numColors];
        for (int i = 0; i < numColors; i++) {
            idPaints[i] = new Paint();
            idPaints[i].setColor(COLORS[i][0] /* text color */);
            idPaints[i].setTextSize(ID_TEXT_SIZE);

            boxPaints[i] = new Paint();
            boxPaints[i].setColor(COLORS[i][1] /* background color */);
            boxPaints[i].setStyle(Paint.Style.STROKE);
            boxPaints[i].setStrokeWidth(BOX_STROKE_WIDTH);

            labelPaints[i] = new Paint();
            labelPaints[i].setColor(COLORS[i][1] /* background color */);
            labelPaints[i].setStyle(Paint.Style.FILL);
        }
    }

    /** Draws the face annotations for position on the supplied canvas. */
    @Override
    public void draw(Canvas canvas) {
        ( (ActivityLocal) getApplicationContext() ).settime1(System.currentTimeMillis());

        Face face = this.face;
        if (face == null) {
            return;
        }
        try {
            if (((ActivityLocal) getApplicationContext()).gettimediv() > 100) {
                ((ActivityLocal) getApplicationContext()).settime2(System.currentTimeMillis());
                if(face.getLeftEyeOpenProbability() != null && face.getRightEyeOpenProbability() != null) {
                    float left = face.getLeftEyeOpenProbability();
                    float right = face.getRightEyeOpenProbability();
                    float eulerX = face.getHeadEulerAngleX();
                    float eulerY = face.getHeadEulerAngleY();
                    ((ActivityLocal) getApplicationContext()).setleft(right);
                    ((ActivityLocal) getApplicationContext()).setright(left);
                    ((ActivityLocal) getApplicationContext()).seteulerX(eulerX);
                    ((ActivityLocal) getApplicationContext()).seteulerY(eulerY);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }

        if(( (ActivityLocal) getApplicationContext() ).getleftsize() >= 10){
            botheyecheck = 0;
            leftcheck = 0;
            rightcheck = 0;
            faceup = 0;
            facedown = 0;
            faceleft = 0;
            faceright = 0;

            for(int k = 0; k<10; k++){
                lefteye = ( (ActivityLocal) getApplicationContext() ).getleft();
                righteye = ( (ActivityLocal) getApplicationContext() ).getright();
                eulerXdegree = ( (ActivityLocal) getApplicationContext() ).geteulerX();
                eulerYdegree = ( (ActivityLocal) getApplicationContext() ).geteulerY();
                if(lefteye < 0.7 && righteye < 0.7){
                    botheyecheck += 1;
                }
                if(lefteye <0.7){
                    leftcheck += 1;
                }
                if(righteye <0.7){
                    rightcheck += 1;
                }
                if(eulerXdegree > 12){
                    faceup += 1;
                }
                if(eulerXdegree < -12){
                    facedown += 1;
                }
                if(eulerYdegree > 12){
                    faceleft += 1;
                }
                if(eulerYdegree < -12){
                    faceright += 1;
                }
            }

            if(faceup == 10) {
                faceupToast.show();
                action("머리 위로");
            }
            else if(facedown == 10) {
                facedownToast.show();
                action("머리 아래로");
            }
            else if(faceleft == 10) {
                faceleftToast.show();
                action("머리 왼쪽으로");
            }
            else if(faceright == 10) {
                facerightToast.show();
                action("머리 오른쪽으로");
            }
            else if(botheyecheck >= 8) {
                bothToast.show();
                action("양쪽 눈 감기");
            }
            else if(leftcheck == 10) {
                leftToast.show();
                action("왼쪽 눈 감기");
            }
            else if(rightcheck == 10) {
                rightToast.show();
                action("오른쪽 눈 감기");
            }

        }


//        // Draws a circle at the position of the detected face, with the face's track id below.
//        float x = translateX(face.getBoundingBox().centerX());
//        float y = translateY(face.getBoundingBox().centerY());
//        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);
//
//        // Calculate positions.
//        float left = x - scale(face.getBoundingBox().width() / 2.0f);
//        float top = y - scale(face.getBoundingBox().height() / 2.0f);
//        float right = x + scale(face.getBoundingBox().width() / 2.0f);
//        float bottom = y + scale(face.getBoundingBox().height() / 2.0f);
//        float lineHeight = ID_TEXT_SIZE + BOX_STROKE_WIDTH;
//        float yLabelOffset = (face.getTrackingId() == null) ? 0 : -lineHeight;

//        // Decide color based on face ID
//        int colorID = (face.getTrackingId() == null) ? 0 : Math.abs(face.getTrackingId() % NUM_COLORS);
//
//        // Calculate width and height of label box
//        float textWidth = idPaints[colorID].measureText("ID: " + face.getTrackingId());
//        if (face.getSmilingProbability() != null) {
//            yLabelOffset -= lineHeight;
//            textWidth =
//                    Math.max(
//                            textWidth,
//                            idPaints[colorID].measureText(
//                                    String.format(Locale.US, "Happiness: %.2f", face.getSmilingProbability())));
//        }
//        if (face.getLeftEyeOpenProbability() != null) {
//            yLabelOffset -= lineHeight;
//            textWidth =
//                    Math.max(
//                            textWidth,
//                            idPaints[colorID].measureText(
//                                    String.format(
//                                            Locale.US, "Left eye open: %.2f", face.getLeftEyeOpenProbability())));
//        }
//        if (face.getRightEyeOpenProbability() != null) {
//            yLabelOffset -= lineHeight;
//            textWidth =
//                    Math.max(
//                            textWidth,
//                            idPaints[colorID].measureText(
//                                    String.format(
//                                            Locale.US, "Right eye open: %.2f", face.getRightEyeOpenProbability())));
//        }
//
//        yLabelOffset = yLabelOffset - 3 * lineHeight;
//        textWidth =
//                Math.max(
//                        textWidth,
//                        idPaints[colorID].measureText(
//                                String.format(Locale.US, "EulerX: %.2f", face.getHeadEulerAngleX())));
//        textWidth =
//                Math.max(
//                        textWidth,
//                        idPaints[colorID].measureText(
//                                String.format(Locale.US, "EulerY: %.2f", face.getHeadEulerAngleY())));
//        textWidth =
//                Math.max(
//                        textWidth,
//                        idPaints[colorID].measureText(
//                                String.format(Locale.US, "EulerZ: %.2f", face.getHeadEulerAngleZ())));
//        // Draw labels
//        canvas.drawRect(
//                left - BOX_STROKE_WIDTH,
//                top + yLabelOffset,
//                left + textWidth + (2 * BOX_STROKE_WIDTH),
//                top,
//                labelPaints[colorID]);
//        yLabelOffset += ID_TEXT_SIZE;
//        canvas.drawRect(left, top, right, bottom, boxPaints[colorID]);
//        if (face.getTrackingId() != null) {
//            canvas.drawText("ID: " + face.getTrackingId(), left, top + yLabelOffset, idPaints[colorID]);
//            yLabelOffset += lineHeight;
//        }
//
//        // Draws all face contours.
//        for (FaceContour contour : face.getAllContours()) {
//            for (PointF point : contour.getPoints()) {
//                canvas.drawCircle(
//                        translateX(point.x), translateY(point.y), FACE_POSITION_RADIUS, facePositionPaint);
//            }
//        }
//
//        // Draws smiling and left/right eye open probabilities.
//        if (face.getSmilingProbability() != null) {
//            canvas.drawText(
//                    "Smiling: " + String.format(Locale.US, "%.2f", face.getSmilingProbability()),
//                    left,
//                    top + yLabelOffset,
//                    idPaints[colorID]);
//            yLabelOffset += lineHeight;
//        }
//
//        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
//        if (face.getLeftEyeOpenProbability() != null) {
//            canvas.drawText(
//                    "Left eye open: " + String.format(Locale.US, "%.2f", face.getLeftEyeOpenProbability()),
//                    left,
//                    top + yLabelOffset,
//                    idPaints[colorID]);
//            yLabelOffset += lineHeight;
//        }
//        if (leftEye != null) {
//            float leftEyeLeft =
//                    translateX(leftEye.getPosition().x) - idPaints[colorID].measureText("Left Eye") / 2.0f;
//            canvas.drawRect(
//                    leftEyeLeft - BOX_STROKE_WIDTH,
//                    translateY(leftEye.getPosition().y) + ID_Y_OFFSET - ID_TEXT_SIZE,
//                    leftEyeLeft + idPaints[colorID].measureText("Left Eye") + BOX_STROKE_WIDTH,
//                    translateY(leftEye.getPosition().y) + ID_Y_OFFSET + BOX_STROKE_WIDTH,
//                    labelPaints[colorID]);
//            canvas.drawText(
//                    "Left Eye",
//                    leftEyeLeft,
//                    translateY(leftEye.getPosition().y) + ID_Y_OFFSET,
//                    idPaints[colorID]);
//        }
//
//        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
//        if (face.getRightEyeOpenProbability() != null) {
//            canvas.drawText(
//                    "Right eye open: " + String.format(Locale.US, "%.2f", face.getRightEyeOpenProbability()),
//                    left,
//                    top + yLabelOffset,
//                    idPaints[colorID]);
//            yLabelOffset += lineHeight;
//        }
//        if (rightEye != null) {
//            float rightEyeLeft =
//                    translateX(rightEye.getPosition().x) - idPaints[colorID].measureText("Right Eye") / 2.0f;
//            canvas.drawRect(
//                    rightEyeLeft - BOX_STROKE_WIDTH,
//                    translateY(rightEye.getPosition().y) + ID_Y_OFFSET - ID_TEXT_SIZE,
//                    rightEyeLeft + idPaints[colorID].measureText("Right Eye") + BOX_STROKE_WIDTH,
//                    translateY(rightEye.getPosition().y) + ID_Y_OFFSET + BOX_STROKE_WIDTH,
//                    labelPaints[colorID]);
//            canvas.drawText(
//                    "Right Eye",
//                    rightEyeLeft,
//                    translateY(rightEye.getPosition().y) + ID_Y_OFFSET,
//                    idPaints[colorID]);
//        }
//
//        canvas.drawText(
//                "EulerX: " + face.getHeadEulerAngleX(), left, top + yLabelOffset, idPaints[colorID]);
//        yLabelOffset += lineHeight;
//        canvas.drawText(
//                "EulerY: " + face.getHeadEulerAngleY(), left, top + yLabelOffset, idPaints[colorID]);
//        yLabelOffset += lineHeight;
//        canvas.drawText(
//                "EulerZ: " + face.getHeadEulerAngleZ(), left, top + yLabelOffset, idPaints[colorID]);

        // Draw facial landmarks
//        drawFaceLandmark(canvas, FaceLandmark.LEFT_EYE);
//        drawFaceLandmark(canvas, FaceLandmark.RIGHT_EYE);
//        drawFaceLandmark(canvas, FaceLandmark.LEFT_CHEEK);
//        drawFaceLandmark(canvas, FaceLandmark.RIGHT_CHEEK);
    }

//    private void drawFaceLandmark(Canvas canvas, @LandmarkType int landmarkType) {
//        FaceLandmark faceLandmark = face.getLandmark(landmarkType);
//        if (faceLandmark != null) {
//            canvas.drawCircle(
//                    translateX(faceLandmark.getPosition().x),
//                    translateY(faceLandmark.getPosition().y),
//                    FACE_POSITION_RADIUS,
//                    facePositionPaint);
//        }
//    }

    public void action(String str) {
        if(prefs.getString("mspms1","").equals(str)){
            PDF_View_Activity.scrollUp();
        }
        else if(prefs.getString("mspms2","").equals(str)){
            PDF_View_Activity.scrollDown();
        }
        else if(prefs.getString("mspms3","").equals(str)){
            PDF_View_Activity.prevPage();
        }
        else if(prefs.getString("mspms4","").equals(str)){
            PDF_View_Activity.nextPage();
        }
        else if(prefs.getString("mspms5","").equals(str)){
            PDF_View_Activity.pdffinish();
        }
        else if(prefs.getString("mspms6","").equals(str)){
            PDF_View_Activity.zoomIn();
        }
        else if(prefs.getString("mspms7","").equals(str)){
            PDF_View_Activity.zoomOut();
        }
    }


}
