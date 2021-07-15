package com.sabin.digitalrm.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.sabin.digitalrm.utils.Logger;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private static Retrofit retrofit;
    private static Logger log;

    public static Retrofit getRetrofit(String BASE_URL) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();

        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getRetrofit(String BASE_URL, Context context) {
        int chaceSize = 12 * 1024 * 1024; // 12 MB Cache

        OkHttpClient.Builder client = new OkHttpClient.Builder();

        try {
            Cache cache = new Cache(context.getCacheDir(), chaceSize);
            client.cache(cache);
        }catch (Exception e){
            Log.e("[X-DEBUG]", "Error occurred: "+e.getLocalizedMessage());
        }

        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client.build())
                    .build();
        }

        return retrofit;
    }
}
