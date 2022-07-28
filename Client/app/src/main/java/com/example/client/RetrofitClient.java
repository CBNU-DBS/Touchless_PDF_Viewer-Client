package com.example.client;

import com.example.client.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private final static String BASE_URL = "http://10.0.2.2:8080";
    private static Retrofit retrofit = null;

    private RetrofitClient(){

    }
    public static Retrofit getClient() {
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
        }
        return retrofit;
    }

}
