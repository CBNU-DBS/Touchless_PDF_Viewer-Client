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
 * 연관된 면 위치, 윤곽선 및 랜드마크를 SharedPreference로 저장하여 전역으로 접근 가능하도록 설정
 * Graphic 랜더링의 경우 가시성 문제로 실행 X
 */


public class FaceGraphic extends Graphic {
    private static final float FACE_POSITION_RADIUS = 8.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 40.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private static final int NUM_COLORS = 10;
    private static final int[][] COLORS =
            new int[][] {
                    // {글자색, 배경색}
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

    //SharedPreferences 선언, 설정과 연동하여 사용
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    // 각 모션들의 확률
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

    /**
     *
     * @param overlay
     * @param face
     */
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

    /** Canvas를 사용하여 인식한 얼굴에 대한 그래픽 생성, 가시성 문제로 그래픽 랜더링은 X
     * 0.1초마다 얼굴 정보를 받아 사용자가 취한 모션을 확률로 저장하고, 모션을 인식받으면 PDF 리더기의 기능 실행
     * */
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

            /**
             * 1초마다 확률이 0.7 이상인 행동 감지
             */
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
                action("Head_up");
            }
            else if(facedown == 10) {
                facedownToast.show();
                action("Head_down");
            }
            else if(faceleft == 10) {
                faceleftToast.show();
                action("Head_left");
            }
            else if(faceright == 10) {
                facerightToast.show();
                action("Head_right");
            }
            else if(botheyecheck >= 8) {
                bothToast.show();
                action("Eyes_close");
            }
            else if(leftcheck == 10) {
                leftToast.show();
                action("Eye_close_left");
            }
            else if(rightcheck == 10) {
                rightToast.show();
                action("Eye_close_right");
            }

        }
    }

    /**
     * PDF 리더기에서 모션에 할당된 기능이 실행
     * @param str
     */
    public void action(String str) {
        if(prefs.getString("Scroll_up","").equals(str)){
            PDF_View_Activity.scrollUp();
        }
        else if(prefs.getString("Scroll_down","").equals(str)){
            PDF_View_Activity.scrollDown();
        }
        else if(prefs.getString("Scroll_left","").equals(str)){
            PDF_View_Activity.scrollleft();
        }
        else if(prefs.getString("Scroll_right","").equals(str)){
            PDF_View_Activity.scrollright();
        }
        else if(prefs.getString("Back","").equals(str)){
            PDF_View_Activity.pdffinish();
        }
        else if(prefs.getString("Zoom_in","").equals(str)){
            PDF_View_Activity.zoomIn();
        }
        else if(prefs.getString("Zoom_out","").equals(str)){
            PDF_View_Activity.zoomOut();
        }
    }
}
