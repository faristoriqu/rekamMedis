package com.sabin.digitalrm.utils;

import android.content.Context;

import com.sabin.digitalrm.helpers.APIClient;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;

public class APIUtils {

    private APIUtils() {}

//    @Deprecated
//    public static APIService getAPIService() {
//        return APIClient.getRetrofit(BASE_URL).create(APIService.class);
//    }

    @Deprecated
    public static APIService getAPIService(Context context) {
        String BASE_URL = ApiServiceGenerator.getBaseUrl(context.getApplicationContext());
        return APIClient.getRetrofit(BASE_URL, context).create(APIService.class);
    }
}
