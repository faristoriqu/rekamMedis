package com.sabin.digitalrm.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sabin.digitalrm.models.ApiStatus;

import java.io.IOException;
import retrofit2.Response;

public class ApiError {
    public static ApiStatus parseError(Response<?> response) {
        Gson gson = new GsonBuilder().create();
        ApiStatus apiStatus = new ApiStatus();
        try{
            apiStatus = gson.fromJson(response.errorBody().string(), ApiStatus.class);
        }catch (IOException e){
            Log.d("X-LOG", "parseError: " + e.getLocalizedMessage());
        }

        return apiStatus;
    }
}
