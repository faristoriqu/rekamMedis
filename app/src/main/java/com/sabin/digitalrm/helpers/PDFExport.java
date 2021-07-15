package com.sabin.digitalrm.helpers;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Environment;
import androidx.annotation.NonNull;import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.utils.APIUtils;
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

public class PDFExport extends AsyncTask<String, String, String>{
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
    private static APIService APIClient;
    private boolean isExport;

    public PDFExport(Context ctx, View vw, SpenView ssv, SpenPageDoc spd, SpenNoteDoc snd, String uid, String brm, boolean exp) {
        this.ctx = ctx;
        this.vw = vw;
        this.mSpenView = ssv;
        this.spenPageDoc = spd;
        this.spenNoteDoc = snd;
        this.uid = uid;
        this.no_rm = brm;
        this.isExport = exp;
    }

    @Override
    protected void onPreExecute(){
        Log.e("[X-DEBUG]", "[PDFExport] onPreExecute");
        APIClient = APIUtils.getAPIService(ctx);
        if(isExport) {
            pDlg = new ProgressDialog(ctx);
            pDlg.setMessage("Working ...");
            pDlg.setIndeterminate(true);
            pDlg.setCancelable(false);
            pDlg.show();
        }
    }

    @Override
    protected String doInBackground(String... args) {
        Log.e("[X-DEBUG]", "[PDFExport] doInBackground");
        final int checkedRadioButtonId = Integer.parseInt(args[0]);
        final String storagePath = args[1];
        final String noteFilename = args[2];
        final int totalPage = Integer.parseInt(args[3]);
        final String strRange = args[4];

        try {
            String[] arrFilename = noteFilename.split("\\.");
            String defFilename = arrFilename[0];
            String docExt = ".pdf";
            String imgExt = ".png";

            if(arrFilename.length >= 2){
                defFilename = arrFilename[0];
            }

            int pageCounter = 0;
            if (checkedRadioButtonId == R.id.radioRange) {
                Log.e("[X-DEBUG]", "On Radio Range");
                if (!strRange.equals("")) {
                    String[] pageRange = strRange.split("-");
                    int start = 0;
                    int end = 0;
                    int single = 0;
                    int [] rangeCount;

                    try {
                        if (pageRange.length > 1) {
                            start = Integer.parseInt(pageRange[0])-1;
                            end = Integer.parseInt(pageRange[pageRange.length - 1])-1;

                            pageCounter = end - start + 1;
                        } else {
                            single = Integer.parseInt(strRange);
                            pageCounter = 1;
                        }

                        if (single == 0) {
                            if (end > totalPage || start > totalPage || start < 0 || end < start) {
                                Snackbar.make(vw, "Invalid range format", Snackbar.LENGTH_LONG).show();
                            } else {
                                rangeCount = new int[(end-start)+1];
                                int ii = 0;

                                for(int i = start; i < end+1; i++) {
                                    rangeCount[ii++] = i;
                                }

                                Document document = new Document();
                                PdfWriter.getInstance(document, new FileOutputStream(storagePath + "/" + defFilename+docExt)); //  Change pdf's name.
                                document.open();

                                for (int aRangeCount : rangeCount) {
                                    if (captureSpenSurfaceView(mSpenView, spenNoteDoc, storagePath + "/" + defFilename + imgExt, aRangeCount, false)) {
                                        Image image = Image.getInstance(storagePath + "/" + defFilename + imgExt);  // Change image's name and extension.

                                        float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
                                        image.scalePercent(scaler);
                                        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

                                        document.add(image);
                                    } else {
                                        Log.e("~Debug [PDFExport]", "Unable to capture the document");
                                    }
                                }

                                document.close();
                                purgeFile(storagePath + "/" + defFilename+imgExt);
                            }
                        } else {
                            if (captureSpenSurfaceView(mSpenView, spenNoteDoc,storagePath + "/" + defFilename + imgExt, single-1, false)) {
                                if (PDFBuilder(defFilename + imgExt, storagePath, defFilename + docExt)) {
                                    Log.e("~Debug [PDFExport]", "PDF File Exported Successfully!");
                                } else {
                                    Log.e("~Debug [PDFExport]", "Unable to build pdf file");
                                }
                            } else {
                                Log.e("~Debug [PDFExport]", "Unable to capture the document");
                            }
                        }
                    } catch (Exception ex) {
                        Snackbar.make(vw, "Exception: "+ex.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(vw, "Insert selected page number or page range", Snackbar.LENGTH_LONG).show();
                }
            } else if (checkedRadioButtonId == R.id.radioCurrent) {
                pageCounter = 1;
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream( storagePath + "/" + defFilename+docExt)); //  Change pdf's name.
                document.open();

                if (captureSpenSurfaceView(mSpenView, spenNoteDoc, storagePath + "/" + defFilename + imgExt, 0, true)) {
                    Image image = Image.getInstance(storagePath + "/" + defFilename+imgExt);  // Change image's name and extension.

                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
                    image.scalePercent(scaler);
                    image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

                    document.add(image);
                } else {

                    Log.e("[X-DEBUG]", "[PDFExport] Unable to capture the document");
                }

                document.close();
                purgeFile(storagePath + "/" + defFilename+imgExt);
            }else{
                pageCounter = totalPage-1;
                Log.e("[X-DEBUG]", "On Radio All");
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(storagePath + "/" + defFilename+docExt)); //  Change pdf's name.
                document.open();

                for(int i = 0; i < totalPage-1; i++) {
                    if (captureSpenSurfaceView(mSpenView, spenNoteDoc, storagePath + "/" + defFilename + imgExt, i, false)) {
                        Image image = Image.getInstance(storagePath + "/" + defFilename+imgExt);  // Change image's name and extension.

                        float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
                        image.scalePercent(scaler);
                        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

                        document.add(image);
                    } else {

                        Log.e("[X-DEBUG]",  "Unable to capture the document");
                    }
                }

                document.close();
                purgeFile(storagePath + "/" + defFilename+imgExt);
            }

            if(isExport) {
                //TODO: UBAH KE FTP
                uploadExportedFile(storagePath, defFilename + docExt, pageCounter, isExport);
            }else{
                Log.e("[X-DEBUG]",  "[PDFExport] file "+storagePath + "/" + defFilename+docExt + " created successfully!");
                Snackbar.make(vw, "Document Saved!", Snackbar.LENGTH_LONG).show();
                if(isExport){
                    pDlg.dismiss();
                }
            }
        }catch (Exception ex){
            Log.e("~Debug [doInBgEx]", ex.getLocalizedMessage());
            Snackbar.make(vw, "Error: "+ex.getMessage(), Snackbar.LENGTH_LONG).show();
            if(isExport) {
                pDlg.dismiss();
            }
            return "false";
        }

        return "true";
    }

    private void uploadExportedFile(final String path, final String filename, final int pages, boolean isExport){
        File f = new File(path+"/"+filename);
        InputStream in;
        byte[] buf;
        Integer len = 0;
        try {
            in = new FileInputStream(f);
        }catch (FileNotFoundException e){
            Log.e("[X-DEBUG]", "uploadNote: FILE NOT FOUND " + e.getLocalizedMessage());
            Toast.makeText(ctx, "File Not Found!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            buf = new byte[in.available()];
            len = in.available();
            while (in.read(buf) != -1);
        }catch (IOException e){
            Log.e("[X-DEBUG]", "uploadNote: " + e.getLocalizedMessage());
            Toast.makeText(ctx, "IO Error!", Toast.LENGTH_LONG).show();
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), buf);

        Call<ApiStatus> uploadResponseCall = APIClient.uploadExport(uid, fileMD5(f), filename, Integer.valueOf(no_rm), pages, requestBody);

        uploadResponseCall.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                if(isExport) {
                    pDlg.dismiss();
                }
                if(response.isSuccessful()){
                    File destDIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "DigitalMR" + "/" + "Exports" + "/");
                    File destFILE = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "DigitalMR" + "/" + "Exports" + "/" + filename);
                    if(!destDIR.exists()){
                        if(!destDIR.mkdir()){
                            Log.e("[X-DEBUG]", "Failed to create destination directory");
                        }
                    }

                    boolean isCopied = false;
                    String path = null;

                    if(fileCoppier(f, destFILE)){
                        isCopied = true;
                        path = destFILE.getPath();
                    }

                    successInfo(path, isCopied);

                }else {
                    Log.e("[X-DEBUG]", "Response error " + response.message());
                    Toast.makeText(ctx, response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                if(isExport) {
                    pDlg.dismiss();
                }
                Log.e("[X-DEBUG]", "Failure " + t.getLocalizedMessage());
                Toast.makeText(ctx, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean fileCoppier(final File from, final File dest){
        try {
            InputStream in = new FileInputStream(from);
            OutputStream out = new FileOutputStream(dest);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                out.close();

                if(from.exists()){
                    if(!from.delete()){
                        Log.e("[X-DEBUG]", "Failure occurred while trying to delete temporary file");
                    }
                }
            } catch(Exception ex){
                Log.e("[X-DEBUG]", "Failure occurred while copying the file. " + ex.getMessage());
                return false;
            }

            in.close();
        } catch(Exception ex){
            Log.e("[X-DEBUG]", "Failure occurred while reading the file. " + ex.getMessage());
            return false;
        }

        return true;
    }

    private void successInfo(final String path, final boolean success){
        String msg, act;

        if(success){
            msg = "Document exported successfully to: " + path;
            act = "Open PDF file";
        }else{
            msg = "Document exported successfully but something went wrong";
            act = "Close";
        }

        Snackbar.make(vw, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction(act, v -> {
                    if(success) {
                        final Uri uri = FileProvider.getUriForFile(ctx, ctx.getApplicationContext().getPackageName() + ".provider", new File(path));
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setDataAndType(uri, "application/pdf");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        ctx.startActivity(i);
                    }
                })
                .show();
    }

    private boolean captureSpenSurfaceView(SpenView mSpenView, SpenNoteDoc mSpenNoteDoc, String strFileName, int page, boolean isCurrent) {
        if(!isCurrent) {
            //set view to page
            spenPageDoc = mSpenNoteDoc.getPage(page);
            mSpenView.setPageDoc(spenPageDoc, true);
            mSpenView.update();
        }

        // Capture the view
        Bitmap imgBitmap = mSpenView.captureCurrentView(true);
        if (imgBitmap == null) {
            Snackbar.make(vw, "Capture failed" + strFileName, Snackbar.LENGTH_LONG).show();
            return false;
        }
        OutputStream out;
        try {
            // Create FileOutputStream and save the captured image.
            out = new FileOutputStream(strFileName);
            imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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
            Snackbar.make(vw, "Failed to save the file", Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
        imgBitmap.recycle();

        return true;
    }

    private boolean PDFBuilder(String imgFilename, final String directoryPath, final String defFilename){
        try {
            Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(directoryPath + "/" + defFilename)); //  Change pdf's name.

            document.open();

            Image image = Image.getInstance(directoryPath + "/" + imgFilename);  // Change image's name and extension.

            float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
            image.scalePercent(scaler);
            image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

            document.add(image);
            document.close();

            purgeFile(directoryPath + "/" + imgFilename);
            return true;
        }catch (Exception ex){
            Log.e("~Debug [PDFBuildEx]", "Error: "+ex.getMessage());
            purgeFile(directoryPath + "/" + imgFilename);
        }

        return false;
    }

    private void purgeFile(String file){
        File fl = new File(file);
        if (fl.exists()) {
            if(fl.delete()){
                Log.e("~Debug [purgeFile]", "File deleted");
            }else{
                Log.e("~Debug [purgeFile]", "Unable to delete the file");
            }
        }
    }
}
