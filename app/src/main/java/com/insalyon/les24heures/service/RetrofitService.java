package com.insalyon.les24heures.service;

import com.insalyon.les24heures.DTO.AssomakerDTO;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by remi on 27/12/14.
 */
public interface RetrofitService {

    @Deprecated
    @GET("/anim/animations.json")
//    @GET("/artist/artists.json")
    void getResources(Callback<AssomakerDTO> cb);

    @GET("/")
    void getResources(@Query("version") String dataVersion, Callback<AssomakerDTO> cb);




}
