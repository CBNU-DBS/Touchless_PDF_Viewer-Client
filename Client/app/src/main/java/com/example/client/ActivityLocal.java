package com.example.client;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 전역적으로 카메라 설정을 저장하기 위한 Activity.
 * 오직 FaceGraphic에서만 접근
 */
public class ActivityLocal extends Application {
    private static Context context;

    // 생성자
    public void onCreate(){
        super.onCreate();
        ActivityLocal.context = getApplicationContext();
    }

    long time1 = 1000;
    long time2 = 0;
    Queue<Float> leftqueue = new LinkedList<>();
    Queue<Float> rightqueue = new LinkedList<>();
    Queue<Float> eulerXqueue = new LinkedList<>();
    Queue<Float> eulerYqueue = new LinkedList<>();


    // 기본적으로 Set(), Get()만 가능
    public long gettime1() {
        return time1;
    }

    public void settime1( long id ) {
        this.time1 = id;
    }

    public long gettime2() {
        return time2;
    }

    public void settime2( long id ) {
        this.time2 = id;
    }

    public long gettimediv() {
        return time1 - time2;
    }

    public float getleft() {
        return leftqueue.poll();
    }

    public void setleft( float id ) {
        this.leftqueue.add(id);
    }

    public int getleftsize() {
        return leftqueue.size();
    }

    public float getright() {
        return rightqueue.poll();
    }

    public void setright( float id ) {
        this.rightqueue.add(id);
    }

    public int getrightsize() {
        return rightqueue.size();
    }

    public float geteulerX() {
        return eulerXqueue.poll();
    }

    public void seteulerX( float id ) {
        this.eulerXqueue.add(id);
    }

    public float geteulerY() {
        return eulerYqueue.poll();
    }

    public void seteulerY( float id ) {
        this.eulerYqueue.add(id);
    }

    public static Context getAppContext() {
        return ActivityLocal.context;
    }


}
