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

/**
 * Document 서버 통신을 위한 인터페이스
 */
public interface DocumentApi {

    /**
     * 문서 저장 API
     * @param documentDTO 문서 정보
     * @return
     */
    @POST("/document")
    Call<BaseResponse> saveDocument(@Body DocumentDTO documentDTO);

    /**
     * 유저에 해당하는 문서 저장
     * @param userId 유저 아이디
     * @return
     */
    @GET("/document")
    Call<ResponseDTO<DocumentDTO>> getDocumentList(@Query("userId") Long userId);
}
