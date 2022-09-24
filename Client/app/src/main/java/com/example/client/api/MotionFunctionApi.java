package com.example.client.api;

import com.example.client.dto.MotionFunctionDTO;
import com.example.client.dto.BaseResponse;
import com.example.client.dto.ResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MotionFunctionApi {
    @POST("/motionfunction")
    Call<BaseResponse> saveMotionSetting(@Body List<MotionFunctionDTO> motionFunctionDTOList);

    @GET("/motionfunction")
    Call<ResponseDTO<MotionFunctionDTO>> getMotionSetting(@Query("userId") Long userId);
}
