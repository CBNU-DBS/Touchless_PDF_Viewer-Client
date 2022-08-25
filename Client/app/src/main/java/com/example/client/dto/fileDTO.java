package com.example.client.dto;

public class fileDTO {
    private long userId;
    private String s3URL;
    private String title;
    public fileDTO(long userId, String s3URL,String title) {
        this.userId = userId;
        this.s3URL = s3URL;
        this.title = title;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getS3URL() {
        return s3URL;
    }

    public void setS3URL(String s3URL) {
        this.s3URL = s3URL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = s3URL;
    }
}
