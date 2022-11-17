package com.example.client.dto;

/**
 * 모션 기능 관련 DTO
 */
public class MotionFunctionDTO {
    private long userId;
    private String motion;
    private String function;

    public MotionFunctionDTO(long userId, String motion, String function) {
        this.userId = userId;
        this.motion = motion;
        this.function = function;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getMotion() {
        return motion;
    }

    public void setMotion(String motion) {
        this.motion = motion;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
