package com.example.client.dto;

import java.util.ArrayList;
import java.util.List;

public class ResponseDTO<T> extends BaseResponse {
    private List<T> list;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public ResponseDTO() {
        list = new ArrayList<>();
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
