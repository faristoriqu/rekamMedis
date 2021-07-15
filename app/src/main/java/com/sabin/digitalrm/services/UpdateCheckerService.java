package com.sabin.digitalrm.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.utils.APIUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateCheckerService extends IntentService {
    private static final String DOWNLOADED_CLIENT_UPDATE_BASEDIR = "/DigitalMR/Packages/";
    private static final String DOWNLOADED_CLIENT_UPDATE_FILENAME = "dmr.apk";
    static final public String RESULT = "com.sabin.UpdateCheckerService.REQUEST_PROCESSED";
    static final public String SIZE = "com.sabin.UpdateCheckerService.SIZE";
    static final public String STATUS = "com.sabin.UpdateCheckerService.STATUS";
    private static final String JSON_APP_INFO_FILENAME = "application.json";
    private static APIService APIClient;
    private Handler mHandler;

    public UpdateCheckerService() {
        super("UpdateCheckerService");
    }

    @Override
    public int onStartCommand(Intent intent, int val1, int val2){
        Log.e("[X-DEBUG]", "Service's onStartCommand is running");
        APIClient = APIUtils.getAPIService(getApplicationContext());
        mHandler = new Handler();

        packagePurger();
        checkClientUpdates();

        return val1;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.e("[X-DEBUG]", "Service terminated");
    }

    public void sendResult(boolean update, long size) {
        Intent intent = new Intent(RESULT);
        intent.putExtra(STATUS, update);
        intent.putExtra(SIZE, size);
        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
    }

    private class ToastRunnable implements Runnable {
        String msg;

        private ToastRunnable(String msg) {
            this.msg = msg;
        }

        @Override
        public void run(){
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    }

    private void packagePurger(){
        File outputDir = new File(Environment.getExternalStorageDirectory() + DOWNLOADED_CLIENT_UPDATE_BASEDIR);
        File outputFile = new File(outputDir.getPath(), DOWNLOADED_CLIENT_UPDATE_FILENAME);

        if(outputFile.exists()){
            if (outputFile.delete()){
                Log.e("[X-DEBUG]", "Unused package update has been removed!");
            }else {
                Log.e("[X-DEBUG]", "Unable to remove package update!");
            }
        }
    }

    private void checkClientUpdates(){
        Call<ResponseBody> downloadCaller = APIClient.checkVersion();

        downloadCaller.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        String resp = Objects.requireNonNull(response.body()).string();

                        JSONObject wrapperObject = new JSONObject(resp);
                        String statuscode = wrapperObject.getString("statuscode");
                        String message = wrapperObject.getString("message");

                        if(Integer.valueOf(statuscode) == 200){
                            Log.e("[X-DEBUG]", "On update check statuscode = 200");
                            JSONObject resultData = wrapperObject.getJSONObject("result");
                            String ver = resultData.getString("version");
                            long size = (long)Integer.valueOf(resultData.getString("size"));
                            int verint = Integer.valueOf(ver.replace(".", ""));
                            if(versionCheck(verint)){
                                Log.e("[X-DEBUG]", "On update check version true");

                                sendResult(true, size);
                            }else{
                                Log.e("[X-DEBUG]", "On update check version false");
                            }
                        }else if(Integer.valueOf(statuscode) == 400){
                            Log.e("[X-DEBUG]", "On update check statuscode = 400");
                            mHandler.post(new ToastRunnable("Terjadi kesalahan. "+message));
                        }
                    }catch (Exception ex){
                        Log.e("[X-DEBUG]", "Exception: " + response.message());
                    }
                }else {
                    Log.e("[X-DEBUG]", "Response error: " + response.message());
                    mHandler.post(new ToastRunnable(response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d("[X-DEBUG]", "Failure: " + t.getLocalizedMessage());
                mHandler.post(new ToastRunnable(t.getLocalizedMessage()));
            }
        });
    }

    private boolean versionCheck(int latest_vers){
        String recent_ver = getAppInfo().get("version").toString();
        int ver = Integer.valueOf(recent_ver.replace(".", ""));

        return latest_vers > ver;
    }

    private HashMap getAppInfo(){
        HashMap<String, String> app_info = new HashMap<>();
        String jsonData;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open(JSON_APP_INFO_FILENAME)));

            jsonData = reader.readLine();

            JSONObject wrapperObject = new JSONObject(jsonData);
            JSONObject appinfo = wrapperObject.getJSONObject("app_info");
            app_info.put("namespace" ,appinfo.getString("namespace"));
            app_info.put("codename", appinfo.getString("codename"));
            app_info.put("client_id", appinfo.getString("client_id"));
            app_info.put("version", appinfo.getString("version"));
            app_info.put("release_date", appinfo.getString("release_date"));
            app_info.put("client_size" ,appinfo.getString("client_size"));

        } catch (Exception e) {
            Log.e("[X-EXCEPTION]", "Exception thrown while reading asset file. \nException message: "+e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("[X-EXCEPTION]", "Exception thrown while close the reader. \nException message: "+e.getMessage());
                }
            }
        }

        return app_info;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.e("[X-DEBUG]", "Service's onHandleIntent is running");
    }
}
