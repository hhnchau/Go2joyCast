package com.appromobile.Go2joyCast.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Chau Huynh on 02/03/02017.
 */

public class RequestInterceptor implements Interceptor {
    private ControllerApi controllerApi;

    public RequestInterceptor(ControllerApi controllerApi) {
        this.controllerApi = controllerApi;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        return chain.proceed(builder.build());
    }
}
