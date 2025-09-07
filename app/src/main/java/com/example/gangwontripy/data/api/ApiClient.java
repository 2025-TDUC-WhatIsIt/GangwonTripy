package com.example.gangwontripy.data.api;

import com.example.gangwontripy.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static VisitApi visitApi;
    private static final String API_BASE = BuildConfig.API_BASE;
    private static Retrofit retrofit;

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE) // 빌드 설정/환경에 맞게
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AuthApi authApi() {
        return getRetrofit().create(AuthApi.class);
    }

    public static VisitApi visitApi(){
        if (visitApi == null){
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(log).build();
            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit r = new Retrofit.Builder()
                    .baseUrl(API_BASE)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            visitApi = r.create(VisitApi.class);
        }
        return visitApi;
    }
}
