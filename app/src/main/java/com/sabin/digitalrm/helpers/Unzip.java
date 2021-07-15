package com.sabin.digitalrm.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Unzip {
    private final static String TAG = "X-LOG";
    private String outDir, zipFile;

    private static UnzipListener mListerner = null;

    public interface UnzipListener{
        void onStart(String msg);
        void onProgress(String status);
        void onError(String msg);
        void onComplete(String filePath);
    }

    public Unzip(Context context, String zipfile, String outdir) {
        zipFile = zipfile;
        outDir = context.getFilesDir() + "/tmp/" + outdir;
        Log.d(TAG, "Unzip:out " + outDir);
        Log.d(TAG, "Unzip:zip " + zipfile);
    }

    public void setOnUnzipListener(UnzipListener listener){
        mListerner = listener;
    }

    public void startUnzip(){
        new UnzipAsync().execute(zipFile, outDir);
    }

    private static class UnzipAsync extends AsyncTask<String, Integer, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onStart("Memulai Ekstraksi...");

        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            onError(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            String outdir = strings[1];
            String zipFile = strings[0];

            File f = new File(outdir);
            int zipSize, cnt = 0;

            Log.d(TAG, "doInBackground:out " + outdir);
            Log.d(TAG, "doInBackground: zip" + zipFile);

            if(!f.exists()){
                if(!f.mkdir()){
                    cancel(true);
                    return "Gagal membuat direktori ekstrak";
                }
            }

            try {
                ZipFile zif = new ZipFile(zipFile);
                zipSize = zif.size();

                ZipEntry ze;
                Enumeration<?> zes = zif.entries();

                publishProgress(0);
                while (zes.hasMoreElements()){
                    cnt++;
                    ze = (ZipEntry) zes.nextElement();
                    String path = outdir + "/" + ze.getName();

                    if(ze.isDirectory()){
                        cancel(true);
                        return "Zip tidak boleh mengandung folder!";
                    }

                    InputStream is = zif.getInputStream(ze);
                    FileOutputStream fout = new FileOutputStream(path, true);

                    byte[] buffer = new byte[2 * 1024]; // or other buffer size
                    int read;

                    while ((read = is.read(buffer)) != -1) {
                        fout.write(buffer, 0, read);
                    }

                    publishProgress((cnt * 100) / zipSize);
                    fout.close();
                    is.close();

                    Log.d(TAG, "doInBackground: " + path);
                }
            }catch (IOException e){
                cancel(true);
                return e.getLocalizedMessage();
            }

            return outdir;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            onProgress("Extracting " + values[0] + "%...");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onComplete(s);
        }
    }

    private static void onStart(String msg){
        Log.d(TAG, "onStart: " + msg);
        if(mListerner != null){
            mListerner.onStart(msg);
        }
    }

    private static void onProgress(String msg){
        Log.d(TAG, "onProgress: " + msg);
        if(mListerner != null){
            mListerner.onProgress(msg);
        }
    }

    private static void onError(String msg){
        Log.d(TAG, "onError: " + msg);
        if(mListerner != null){
            mListerner.onError(msg);
        }
    }

    private static void onComplete(String msg){
        Log.d(TAG, "onComplete: " + msg);
        if (mListerner != null){
            mListerner.onComplete(msg);
        }
    }

}
