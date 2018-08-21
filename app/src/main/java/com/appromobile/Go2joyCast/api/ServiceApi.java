package com.appromobile.Go2joyCast.api;

import com.appromobile.Go2joyCast.model.VideoSiteForm;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by Chau Huynh on 02/03/02017.
 */

public interface ServiceApi {
    @GET("/hotelapi/chromeCast/view/findLimitVideoSiteList")
    Call<List<VideoSiteForm>> findLimitVideoSiteList(@QueryMap Map<String, Object> params);

    @GET("/hotelapi/chromeCast/view/findLimitCastPlayerList")
    Call<List<String>> findLimitCastPlayerList();

}