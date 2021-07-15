package com.sabin.digitalrm.helpers;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.Logger;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sabin.digitalrm.utils.StringHash.fileMD5;

public class PNGExport extends AsyncTask<String, String, String>{
    @SuppressLint("StaticFieldLeak")
    private Context ctx;

    @SuppressLint("StaticFieldLeak")
    private SpenView mSpenView;

    @SuppressLint("StaticFieldLeak")
    private View vw;

    private ProgressDialog pDlg;
    private SpenNoteDoc spenNoteDoc;
    private SpenPageDoc spenPageDoc;
    private String uid, no_rm;
    private int totalPage;
    private boolean withProgress;

    Logger log = new Logger();

    public PNGExport(Context ctx, View vw, SpenView ssv, SpenPageDoc spd, SpenNoteDoc snd, String uid, String brm, int totalPage, boolean withProgress, ProgressDialog pDlg) {
        this.ctx = ctx;
        this.vw = vw;
        this.mSpenView = ssv;
        this.spenPageDoc = spd;
        this.spenNoteDoc = snd;
        this.uid = uid;
        this.no_rm = brm;
        this.totalPage = totalPage;
        this.withProgress = withProgress;
        this.pDlg = pDlg;
    }

    @Override
    protected void onPreExecute(){
        log.x("{PNGExport} onPreExecute");
        Log.e("[X-DEBUG]", "[PNGExport] onPreExecute");

        if(pDlg==null){
            pDlg = new ProgressDialog(ctx);
        }

        if(withProgress) {
            pDlg.setMessage("Working ...");
            pDlg.setIndeterminate(true);
            pDlg.setCancelable(false);
            pDlg.show();
        }
    }

    @Override
    protected String doInBackground(String... args) {
        log.x("{PNGExport} doInBackground");
        Log.e("[X-DEBUG]", "[PNGExport] doInBackground");
        final String storagePath = args[0];
        final String noteFilename = args[1];
        final String strCurPage = args[2];

        try {
            String[] arrFilename = noteFilename.split("\\.");
            String defFilename = arrFilename[0];
            String imgExt = ".png";

            if(arrFilename.length >= 2){
                defFilename = arrFilename[0];
            }

            if(strCurPage!=null) {
                final int curPage = Integer.parseInt(strCurPage);
                final int realCurPage = (curPage+1);

                if (captureSpenSurfaceView(mSpenView, spenNoteDoc, storagePath + "/" + defFilename + "_" + realCurPage + imgExt, curPage)) {
                    Log.e("[X-DEBUG]", "[PNGExport] Document captured on " + storagePath + "/" + defFilename + "_" + curPage + imgExt);
                } else {
                    Log.e("[X-DEBUG]", "[PNGExport] Unable to capture the document on " + storagePath + "/" + defFilename + "_" + realCurPage + imgExt);
                }
            }else{
                for(int i=0; i<totalPage; i++){
                    if (captureSpenSurfaceView(mSpenView, spenNoteDoc, storagePath + "/" + defFilename + imgExt, i)) {
                        Log.e("[X-DEBUG]", "[PNGExport] Document captured");
                    } else {
                        Log.e("[X-DEBUG]", "[PNGExport] Unable to capture the document");
                    }
                }
            }

            Log.e("[X-DEBUG]",  "[PNGExport] file "+storagePath + "/" + defFilename + imgExt + " created successfully!");
        }catch (Exception ex){
            Log.e("~Debug [doInBgEx]", ex.getLocalizedMessage());

            return "false";
        }

        return "true";
    }

    @Override
    protected void onPostExecute(String result) {
        if(pDlg.isShowing()) {
            pDlg.dismiss();
        }
//        BaseActivity.Baseprogress.hideProgressDialog();
    }

    private boolean captureSpenSurfaceView(SpenView mSpenView, SpenNoteDoc mSpenNoteDoc, String strFileName, int page) {
        log.x("-> captureSpenSurfaceView");
        // Capture the view
        Bitmap imgBitmap = mSpenView.captureCurrentView(true);
        if (imgBitmap == null) {
            return false;
        }
        OutputStream out;
        try {
            // Create FileOutputStream and save the captured image.
            out = new FileOutputStream(strFileName);
            imgBitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
            // Save the note information.
            mSpenNoteDoc.save(out, false);
            out.close();
        } catch (Exception e) {
            File tmpFile = new File(strFileName);
            if (tmpFile.exists()) {
                if(!tmpFile.delete()){
                    Log.e("[X-DEBUG]", "Unable to delete "+tmpFile.getPath());
                }
            }

            e.printStackTrace();
            return false;
        }
        imgBitmap.recycle();

        return true;
    }
}
