package com.example.client.dto;

import java.util.ArrayList;
import java.util.List;

public class ResponseDTO<T> {
    private int resultCode;
    private String resultMsg;
    private List<T> list;

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

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public ResponseDTO() {
    }

    public ResponseDTO(String resultMsg) {
        this.resultCode = 0;
        this.resultMsg = resultMsg;
        this.list = new ArrayList<>();
    }

    public ResponseDTO(int resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.list = new ArrayList<>();
    }

    public ResponseDTO(int resultCode, String resultMsg, List<T> list) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.list = list;
    }
}
