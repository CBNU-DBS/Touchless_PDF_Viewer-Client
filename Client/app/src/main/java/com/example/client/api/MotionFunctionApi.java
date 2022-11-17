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

/**
 * 모션 기능 설정 인터페이스
 */
public interface MotionFunctionApi {

    /**
     * 모션 기능 저장 API
     * @param motionFunctionDTOList 모션 기능 설정
     * @return
     */
    @POST("/motionfunction")
    Call<BaseResponse> saveMotionSetting(@Body List<MotionFunctionDTO> motionFunctionDTOList);

    /**
     * 모션 기능 설정 불러오기 API
     * @param userId 유저 아이디
     * @return
     */
    @GET("/motionfunction")
    Call<ResponseDTO<MotionFunctionDTO>> getMotionSetting(@Query("userId") Long userId);
}
