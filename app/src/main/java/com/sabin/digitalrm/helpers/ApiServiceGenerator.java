package com.sabin.digitalrm.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.sabin.digitalrm.LoginActivity;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiServiceGenerator {
    private static final String TAG = "X-LOG";
    public static final String sessionPrefsName = "SESSION_";
    public static final String settingPrefsName = "SETTINGS_";
    public static final String defaultAPIHost = "your IP";
    private static String apiHost;

    private static final int BASE_FTP_PORT = 21;
    private static final String USER_FTP = "digitalmr";
    private static final String PASSWORD_FTP = "Rsdk#DMR";

    private static OkHttpClient.Builder httpClient;

//    private static Retrofit.Builder builder =
//            new Retrofit.Builder()
//                    .baseUrl(BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create());


    public static <S> S createService(Context context, Class<S> serviceClass) {
        if(httpClient == null){
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

            Log.d(TAG, "createService: HTTP-CLIENT");

            httpClient =
                    new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(logging);
        }

        OkHttpClient client = httpClient.build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(getBaseUrl(context))
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(client).build();

        return retrofit.create(serviceClass);
    }

    public static String getBaseUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(settingPrefsName, 0);
        apiHost = prefs.getString("api_host", defaultAPIHost);
        return "http://" + apiHost + "/digitalmr-api/";
    }

    public static String getBaseFtp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(settingPrefsName, 0);
        apiHost = prefs.getString("api_host", defaultAPIHost);
        return apiHost;
    }

    public static String getUserFtp() {
        return USER_FTP;
    }

    public static String getPasswordFtp() {
        return PASSWORD_FTP;
    }

    public static int getBaseFtpPort() {
        return BASE_FTP_PORT;
    }
}
