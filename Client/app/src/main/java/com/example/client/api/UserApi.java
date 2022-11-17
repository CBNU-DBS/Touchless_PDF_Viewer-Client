package com.example.client.api;

import com.example.client.dto.ChangeDTO;
import com.example.client.dto.ResponseDTO;
import com.example.client.dto.UserDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * 사용자 관련 API 인터페이스
 */
public interface UserApi {

    /**
     * 회원가입 API
     * @param user 사용자 정보
     * @return
     */
    @Headers({"Content-Type: application/json"})
    @POST("/users")
    Call<ResponseDTO<UserDTO>> joinUser(@Body UserDTO user);

    /**
     * 로그인 API
     * @param user 사용자 로그인 정보
     * @return
     */
    @POST("/users/login")
    Call<ResponseDTO<UserDTO>> loginUser(@Body UserDTO user);

    /**
     * 비밀번호 변경 API
     * @param change 비밀번호 변경 정보
     * @return
     */
    @POST("/users/changepw")
    Call<ResponseDTO<ChangeDTO>> changePW(@Body ChangeDTO change);
}
