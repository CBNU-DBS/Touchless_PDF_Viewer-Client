package com.example.client.api;

import com.example.client.dto.BaseResponse;
import com.example.client.dto.DocumentDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DocumentApi {
    @POST("/document")
    Call<BaseResponse> saveDocument(@Body DocumentDTO documentDTO);
}
