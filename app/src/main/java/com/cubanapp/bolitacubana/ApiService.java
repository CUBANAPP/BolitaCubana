/*
 * Copyright (c) CUBANAPP LLC 2019-2024 .
 */

package com.cubanapp.bolitacubana;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("chksvr_new.php")
    Call<ServerResponse> checkServer(@Body JSONObject apiKey);
}
