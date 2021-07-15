package com.sabin.digitalrm.helpers;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;

import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by xkill on 13/11/18.
 */

public class FileDownloader{
    private final String START_MSG = "Memeriksa Berkas...";
    private static String BASE_DIR = "tmp";

    private Call<ResponseBody> mCall;
    private static Context mContext;
    private static String fileName;

    private static FileDownloadListener mListerner = null;

    private static Logger log = new Logger();

    private Headers headers;

    public interface FileDownloadListener{
        void onStart(String msg);
        void onProgress(String status);
        void onError(String msg);
        void onComplete(String filePath);
    }

    public FileDownloader(Context context, Call<ResponseBody> call, String outFileName) {
        mCall = call;
        mContext = context;
        fileName = outFileName;

        Log.d("X-LOG", "FileDownloader: " + call.request().toString());
    }

    public void setOnFileDownloadListener(FileDownloadListener listener){
        mListerner = listener;
    }

    public void setBaseDir(String dir) {
        BASE_DIR = dir;
    }

    public Headers getResponseHeader(){
        return headers;
    }

    public void startDownload(){
        mCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,@NonNull Response<ResponseBody> response) {
                log.x(response.raw().toString());
                headers = response.headers();

                if(response.isSuccessful()){
                    ResponseBody responseBody = response.body();
                    //TODO: UBAH KE FTP
                    new DownloadAsync().execute(responseBody);
                }else{
                    log.x("on download error");
                    ApiStatus apiStatus = ApiError.parseError(response);
                    if(apiStatus!=null){
                        if(apiStatus.getMessage()!=null){
                            onDownloadError(apiStatus.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,@NonNull Throwable t) {
                onDownloadError(t.getLocalizedMessage());
            }


        });
    }

    private static class DownloadAsync extends AsyncTask<ResponseBody, Integer, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onDownloadStart("Mendownload File...");
        }

        @Override
        protected String doInBackground(ResponseBody... responseBodies) {
            int count;
            byte data[] = new byte[1024 * 4];

            ResponseBody body = responseBodies[0];

            long fileSize = body.contentLength();

            InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
            File outputDir = new File(mContext.getFilesDir() + "/" + BASE_DIR + "/");
            File outputFile = new File(mContext.getFilesDir()+ "/" + BASE_DIR + "/", fileName);

            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    Log.d("X-LOG", "doInBackground: mkdir fail");
                    cancel(true);
                }
            }

            try {

                OutputStream output = new FileOutputStream(outputFile);
                long total = 0;
                long startTime = System.currentTimeMillis();
                int timeCount = 1;
                while ((count = bis.read(data)) != -1) {

                    total += count;

                    int progress = (int) ((total * 100) / fileSize);

                    long currentTime = System.currentTimeMillis() - startTime;

                    if (currentTime > 100 * timeCount) {
                        publishProgress(progress);
                        timeCount++;
                    }

                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                bis.close();
            }catch (Exception e){
                cancel(true);
                return e.getLocalizedMessage();
            }
            body.close();
            return outputFile.getPath();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            onDownloadProgress("Mendownload File " + values[0].toString() + "%");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onDownloadComplete(s);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            onDownloadError("Dibatalkan oleh system, " + s);
        }

    }

    private static void onDownloadStart(String msg){
        if(mListerner != null){
            mListerner.onStart(msg);
        }
    }

    private static void onDownloadError(String msg){
        if(mListerner != null){
            mListerner.onError(msg);
        }
    }

    private static void onDownloadProgress(String status){
        if(mListerner != null){
            mListerner.onProgress(status);
        }
    }

    private static void onDownloadComplete(String filePath){
        if(mListerner != null){
            mListerner.onComplete(filePath);
        }
    }
}
