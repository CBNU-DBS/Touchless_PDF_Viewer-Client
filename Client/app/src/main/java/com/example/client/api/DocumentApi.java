package com.example.client.api;

import com.example.client.dto.BaseResponse;
import com.example.client.dto.DocumentDTO;
import com.example.client.dto.ResponseDTO;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DocumentApi {
    @POST("/document")
    Call<BaseResponse> saveDocument(@Body DocumentDTO documentDTO);

    @GET("/document")
    Call<ResponseDTO<DocumentDTO>> getDocumentList(@Query("userId") Long userId);
}
