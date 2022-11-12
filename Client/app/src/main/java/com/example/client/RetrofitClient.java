package com.example.client;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API 통신을 위한 클라이언트 클래스(싱글톤 패턴)
 */
public class RetrofitClient {
    private final static String BASE_URL = "http://52.79.244.120:8080";
    private static Retrofit retrofit = null;
    private static OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
    private static HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

    private RetrofitClient(){

    }

    /**
     * 싱글톤 패턴을 위한 인스턴스 불러오는 함수
     * @return
     */
    public static Retrofit getClient() {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(clientBuilder.build())
                        .build();
        }
        return retrofit;
    }

}
