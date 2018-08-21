package com.appromobile.Go2joyCast.api;

import com.appromobile.Go2joyCast.model.VideoSiteForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by appro on 08/11/2017.
 */

public class HandleApi {
    private static HandleApi Instance = null;

    public static HandleApi getInstance() {
        if (Instance == null) {
            Instance = new HandleApi();
        }
        return Instance;
    }
    public void findLimitVideoSiteList(final ApiListCallBack apiListCallBack){
        Map<String, Object> params = new HashMap<>();
        params.put("type", 1);
        params.put("offset", 0);
        params.put("limit", 100);

        ControllerApi.serviceApi.findLimitVideoSiteList(params).enqueue(new Callback<List<VideoSiteForm>>() {
            @Override
            public void onResponse(Call<List<VideoSiteForm>> call, Response<List<VideoSiteForm>> response) {
                List<VideoSiteForm> list = response.body();
                if (list != null){
                    List<Object> list1 = new ArrayList<>();
                    list1.addAll(list);
                    apiListCallBack.resultApiList(list1);
                }
            }

            @Override
            public void onFailure(Call<List<VideoSiteForm>> call, Throwable t) {
                apiListCallBack.resultApiList(null);
            }
        });
    }

    public void findLimitCastPlayerList(final ApiListCallBack apiListCallBack){
        ControllerApi.serviceApi.findLimitCastPlayerList().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                List<String> list = response.body();
                if (list != null && list.size() > 0){
                    List<Object> list1 = new ArrayList<>();
                    list1.addAll(list);
                    apiListCallBack.resultApiList(list1);
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {

            }
        });
    }
}
