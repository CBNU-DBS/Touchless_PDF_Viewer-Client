package com.example.client.api;

import com.example.client.dto.ResponseDTO;
import com.example.client.dto.UserDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserApi {
    @Headers({"Content-Type: application/json"})
    @POST("/users")
    Call<ResponseDTO<UserDTO>> joinUser(@Body UserDTO user);

    @POST("/users/login")
    Call<ResponseDTO<UserDTO>> loginUser(@Body UserDTO user);

    @POST("/users/changepw")
    Call<ResponseDTO<UserDTO>> changePW(@Body UserDTO user);
}
