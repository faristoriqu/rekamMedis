package com.sabin.digitalrm.helpers;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.utils.APIUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Updater {
    private Context mContext;
    private static final String DOWNLOADED_CLIENT_UPDATE_BASEDIR = "/DigitalMR/Packages/";
    private static final String DOWNLOADED_CLIENT_UPDATE_FILENAME = "dmr.apk";
    private APIService APIClient;
    private String uid;
    private long size;
    private static ProgressDialog progressDialog;
    private static AlertDialog dialog;
    private static AsyncTask assembler;

    public Updater(Context ctx, long size){
        mContext = ctx;
        this.size = size;

        init();
        updateDialog();
    }

    public static boolean isDialogShown() {
        return dialog != null && dialog.isShowing();
    }

    public static void closeDialog(){
        if(dialog!=null) {
            dialog.dismiss();
        }
    }

    private void init(){
        APIClient = APIUtils.getAPIService(mContext);

        SharedPreferences pref = mContext.getSharedPreferences("SESSION_", 0);
        uid = pref.getString("uid", null);

        Log.e("[X-DEBUG]", "UID:"+uid);

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setIcon(R.drawable.ic_update);
        progressDialog.setTitle("Pembaruan DigitalMedicalRecord");
        progressDialog.setMessage("Sedang mempersiapkan unduhan ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Batal", (dialog, which) -> {
            progressDialog.cancel();
            assembler.cancel(true);
        });
    }

    @SuppressLint("DefaultLocale")
    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void updateDialog(){
        dialog = new AlertDialog.Builder(mContext)
                .setTitle("Pembaruan Digital Medical Record Ditemukan")
                .setIcon(R.drawable.ic_update)
                .setMessage("Apakah anda ingin memperbarui Digital Medical Record sekarang? ("+humanReadableByteCount(size, true)+")")
                .setCancelable(false)
                .setPositiveButton("Ya", (dialogInterface, i) -> {
                    progressDialog.show();
                    download();
                })
                .setNegativeButton("Tidak Sekarang", null)
                .create();

        dialog.show();
    }

    private void download(){
        Call<ResponseBody> downloadCaller = APIClient.updateClient(uid);

        downloadCaller.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    if(!Objects.requireNonNull(response.headers().get("Content-Type")).equals("application/json")) {
                        assembler = new buildFile(mContext).execute(response.body());
                    }else {
                        try {
                            String resp = Objects.requireNonNull(response.body()).string();

                            JSONObject wrapperObject = new JSONObject(resp);
                            String statuscode = wrapperObject.getString("statuscode");
                            String message = wrapperObject.getString("message");

                            Toast.makeText(mContext, "Terjadi kesalahan!\nError Code:"+statuscode+" - Message:"+message, Toast.LENGTH_LONG).show();
                        }catch (Exception ex){
                            Log.e("[X-DEBUG]", "Exception: " + response.message());
                        }

                        progressDialog.dismiss();
                    }
                }else {
                    Log.e("[X-DEBUG]", "Response error: " + response.message());
                    progressDialog.dismiss();
                    Toast.makeText(mContext,  response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d("[X-DEBUG]", "Failure: " + t.getLocalizedMessage());
                Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private static class buildFile extends AsyncTask<ResponseBody, Integer, String>{
        @SuppressLint("StaticFieldLeak")
        private Context contx;

        private buildFile(Context ctx){
            contx = ctx;
        }

        @Override
        protected String doInBackground(ResponseBody... bodies) {
            Log.e("[X-DEBUG]", "buildFile - doInBackground is running");
            int count;
            byte data[] = new byte[1024 * 4];

            ResponseBody body = bodies[0];

            long fileSize = body.contentLength();
            progressDialog.setMax((int)fileSize);
            Log.e("[X-DEBUG]", "Download start: " + fileSize);

            InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
            File outputDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + DOWNLOADED_CLIENT_UPDATE_BASEDIR);
            File outputFile = new File(outputDir.getPath(), DOWNLOADED_CLIENT_UPDATE_FILENAME);

            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    Log.e("[X-DEBUG]", "Path " + outputDir + " Creation Error");
                    cancel(true);
                }
            }

            Log.e("[X-DEBUG]", "Client app builder - File size: "+fileSize);
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
                        Log.e("[X-DEBUG]", "Progress " + progress);
                        publishProgress((int)total);
                        timeCount++;
                    }

                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                bis.close();
            }catch (Exception e){
                Log.e("[X-DEBUG]", "Error " + e.getLocalizedMessage());
            }

            return outputFile.getPath();
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            progressDialog.setMessage("Mengunduh pembaruan Digital Medical Record ...");
            progressDialog.setVolumeControlStream(values[0]);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String str){
            Log.e("[X-DEBUG]", "buidFile - onPostExecute running");
            Log.e("[X-DEBUG]", "Path " + str);
            progressDialog.dismiss();

            try {
                installUpdates(str);
            }catch (Exception e){
                Log.e("[X-DEBUG]", "Exception " + e.getMessage());
                Toast.makeText(contx, "Terjadi kesalahan. "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        private void installUpdates(String PATH){
            final Uri uri = FileProvider.getUriForFile(contx, contx.getApplicationContext().getPackageName() + ".provider", new File(PATH));
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, "application/vnd.android.package-archive");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contx.startActivity(i);

//            File file = new File(Pa);
//
//            if (file.exists()) {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                String type = "application/vnd.android.package-archive";
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    Uri downloadedApk = FileProvider.getUriForFile(getContext(), "ir.greencode", file);
//                    intent.setDataAndType(downloadedApk, type);
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                } else {
//                    intent.setDataAndType(Uri.fromFile(file), type);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                }
//
//                getContext().startActivity(intent);
//            } else {
//                Toast.makeText(getContext(), "ّFile not found!", Toast.LENGTH_SHORT).show();
//            }
        }

    }
}
