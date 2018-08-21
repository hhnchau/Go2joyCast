package com.appromobile.Go2joyCast.api;

import android.app.Application;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Chau Huynh on 02/03/02017.
 */

public class ControllerApi extends Application {
    private String TAG = "API_SERVICE";
    public Retrofit retrofit;
    public static ServiceApi serviceApi;

    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(new RequestInterceptor(this));
        builder.connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);
        OkHttpClient client = builder.build();

        try {
            retrofit = new Retrofit.Builder()
                    .baseUrl(UrlParams.url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            serviceApi = retrofit.create(ServiceApi.class);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Cannot connect to server!");
        }

    }
}
