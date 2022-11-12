package com.example.client.dto;

/**
 * API 기본 응답 클래스
 */
public class BaseResponse {
    protected int resultCode;
    protected String resultMsg;

    public BaseResponse(){
    }

    public BaseResponse(int resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }
    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }
}
