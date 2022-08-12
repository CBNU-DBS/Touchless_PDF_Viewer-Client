package com.example.client.api;

import com.example.client.dto.MotionFunctionDTO;
import com.example.client.dto.Response;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MotionFunctionApi {
    @POST("/motionfunction")
    Call<Response> saveMotionSetting(@Body List<MotionFunctionDTO> motionFunctionDTOList);
}
