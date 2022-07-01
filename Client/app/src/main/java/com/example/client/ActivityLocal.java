package com.example.client;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.Queue;

public class ActivityLocal extends Application {
    long time1 = 1000;
    long time2 = 0;
    Queue<Float> leftqueue = new LinkedList<>();
    Queue<Float> rightqueue = new LinkedList<>();

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


}
