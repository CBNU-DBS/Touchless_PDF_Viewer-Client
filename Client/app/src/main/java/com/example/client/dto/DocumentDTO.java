package com.example.client.dto;

public class DocumentDTO {
    private long userId;
    private String key;
    private String title;

    public DocumentDTO(long userId, String key, String title) {
        this.userId = userId;
        this.key = key;
        this.title = title;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
