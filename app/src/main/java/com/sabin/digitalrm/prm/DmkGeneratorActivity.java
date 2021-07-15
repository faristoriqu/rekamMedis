package com.sabin.digitalrm.prm;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.ProgressRequestBody;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.AsyncResult;
import com.sabin.digitalrm.models.DMK;
import com.sabin.digitalrm.models.InfoPoliResponse;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenView;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DmkGeneratorActivity extends BaseActivity{
    private final int REQUEST_OPEN_PDF = 1;
    public static final String ID_DMK = "ID_DMK";
    public static final String CODE_DMK = "CODE_DMK";
    public static final String STATUS_DMK = "STATUS";
    public static final String POS_DMK = "POS";
    public static final String TOTAL_PAGE = "PAGES";


    private final String TMP_PATH = "/tmp";

    private static Context mContext;
    private static SpenNoteDoc mSpenNoteDoc;
    private static SpenPageDoc mSpenPageDoc;
    private static SpenView mSpenSurfaceView;
    private SpenSettingPenLayout mPenSettingView;
    private static Rect mScreenRect = new Rect();
    private static RelativeLayout blankSurface;
    private static File tmpFile;

    private static ContentResolver resolver;
    private static PdfRenderer mPdfRenderer;
    private static APIService APIClient;

    static FloatingActionButton fab_nx, fab_pr, fabUpload;

    private static int curentPage = 0;
    private static int totalPage = 0;
    private static Integer idDmk;
    private int dmkPos;
    private int statusDMK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //masuk kak
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmk_generator);

        Toolbar toolbar = findViewById(R.id.prmExpToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fab_nx = (FloatingActionButton) findViewById(R.id.fab_next_pg);
        fab_pr = (FloatingActionButton) findViewById(R.id.fab_prev_pg);
        fabUpload = findViewById(R.id.fab_upload);

        fabUpload.setOnClickListener((view) -> uploadDMK());

        blankSurface = findViewById(R.id.surfaceBlank);

        mContext = this;
        APIClient = APIUtils.getAPIService(this);

        Bundle bundle = getIntent().getExtras();
        idDmk = bundle.getInt(ID_DMK);
        statusDMK = bundle.getInt(STATUS_DMK);
        dmkPos = bundle.getInt(POS_DMK);
        String code = bundle.getString(CODE_DMK);
        toolbar.setTitle("DMK" + code + " Generator");


        tmpFile = new File(getFilesDir() + TMP_PATH);

        if(!tmpFile.exists()){
            boolean dir = tmpFile.mkdir();

            if(!dir){
                toastErr(mContext, "Gagal membuat folder sementara!");
                finish();
            }
        }

        Log.d(TAG, "onCreate: LIST TMP");
        for (String file :
                tmpFile.list()) {
            Log.d(TAG, "onCreate: " + file);
        }

        resolver = getContentResolver();

        setFabListener();

        initSpenPackage();
        initSurfaceView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dmk_generator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_open_dmk: {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                startActivityForResult(intent, REQUEST_OPEN_PDF);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_OPEN_PDF:{
                if(resultCode == RESULT_OK){
                    new asyncGenerator().execute(data.getData());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File tmp = new File(tmpFile.getPath());
        Log.d(TAG, "onDestroy: LIST");

        for(String file : tmpFile.list()){
            File eraser = new File(tmpFile.getPath() + "/" + file);

            if(!eraser.delete()){
                Log.d(TAG, "onDestroy: FAILED ON SHRED");
            }
        }
    }

    private void uploadDMK(){
        if(statusDMK != DMK.STATUS_DMK_NEW){
            new AlertDialog.Builder(mContext)
                    .setTitle("DMK Akan Dirubah")
                    .setMessage("DMK ini telah telah tersedia sebelumnya, Jika anda melanjutkan maka akan akan diganti dan semua data dmk sebelumnya akan dihapus")
                    .setPositiveButton("Lanjutkan", (dialog, which) -> new saveAsync().execute())
                    .setNegativeButton("Batal", null)
                    .setCancelable(false)
                    .create()
                    .show();
        }else {
            new saveAsync().execute();
        }
    }

    private void setFabListener(){
        fab_nx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curentPage = curentPage + 1;

                if(curentPage > 0){
                    fab_pr.setVisibility(View.VISIBLE);
                }

                if(curentPage == totalPage-1){
                    fab_nx.setVisibility(View.GONE);
                }

                mSpenPageDoc = mSpenNoteDoc.getPage(curentPage);
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                mSpenSurfaceView.update();
            }
        });

        fab_pr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curentPage = curentPage - 1;

                if(curentPage < totalPage-1){
                    fab_nx.setVisibility(View.VISIBLE);
                }

                if(curentPage == 0){
                    fab_pr.setVisibility(View.GONE);
                }

                mSpenPageDoc = mSpenNoteDoc.getPage(curentPage);
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                mSpenSurfaceView.update();
            }
        });
    }

    private void dialogUploadOK(){
        new AlertDialog.Builder(mContext)
                .setTitle("DMK berhasil diupload")
                .setMessage("DMK untuk poli ini telah berhasil diupload, anda akan dikembalikan ke module Daftar Blenko")
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent data = new Intent();

                    data.putExtra(POS_DMK, dmkPos);
                    data.putExtra(TOTAL_PAGE, totalPage);
                    setResult(RESULT_OK, data);
                    finish();
                })
                .create()
                .show();
    }

    private void uploadNote(){
        Baseprogress.setMessage("Preparing File...");
        InputStream in;
        byte[] buf;
        Integer len = 0;
        try {
            in = new FileInputStream(new File(tmpFile.getPath() + "/note.tmp"));
        }catch (FileNotFoundException e){
            Log.d(TAG, "uploadNote: FILE NOT FOUND " + e.getLocalizedMessage());
            toastErr(mContext, "File Not Found!");
            Baseprogress.hideProgressDialog();
            return;
        }

        try {
            buf = new byte[in.available()];
            len = in.available();
            while (in.read(buf) != -1);
        }catch (IOException e){
            Log.d(TAG, "uploadNote: " + e.getLocalizedMessage());
            toastErr(mContext, "IO Error!");
            Baseprogress.hideProgressDialog();
            return;
        }

//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), buf);

        Log.d(TAG, "doInBackground: READY");

        //TODO: TAMBAH FUNGSI UPLOAD FTP

        Call<ApiStatus> call = APIClient.uploadDMK(idDmk, PetugasMainActivity.UID, totalPage);
        call.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse: UPLOAD SUCCESS");
                    toastInfo(mContext, "DMK Berhasil Di Upload");
                    dialogUploadOK();
                }else{
                    Baseprogress.hideProgressDialog();
                    Log.d(TAG, "onResponse: " + response.message());
                    ApiStatus apiStatus = ApiError.parseError(response);
                    toastErr(mContext, apiStatus.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiStatus> call, Throwable t) {
                Baseprogress.hideProgressDialog();
                toastErr(mContext, t.getLocalizedMessage());
            }
        });
    }

    private class saveAsync extends AsyncTask<String, String, AsyncResult> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Baseprogress.showProgressDialog(mContext, "Menyiapkan Uploader...");
        }

        @Override
        protected AsyncResult doInBackground(String... strings) {
            AsyncResult result = new AsyncResult();

            result.setStatus(AsyncResult.STATUS_OK);
            // Save Note File
            publishProgress("Menyimpan DMK...");
            try {
                mSpenNoteDoc.save(tmpFile.getPath() + "/note.tmp", false);
                Log.d(TAG, "doInBackground: NOTE SAVED");
            }catch (IOException e){
                Log.d(TAG, "doInBackground: IOE" + e.getLocalizedMessage());
                result.setStatus(AsyncResult.STATUS_CANCEL);
                result.setMsg(e.getLocalizedMessage());

                cancel(true);
            }

            return result;
        }

        @Override
        protected void onCancelled(AsyncResult asyncResult) {
            super.onCancelled(asyncResult);
            Baseprogress.hideProgressDialog();
            Toast.makeText(mContext, asyncResult.getMsg(), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(AsyncResult asyncResult) {
            super.onPostExecute(asyncResult);
            if(asyncResult.getStatus() != AsyncResult.STATUS_OK){
                toastErr(mContext, asyncResult.getMsg());
            }else{
                uploadNote();
            }
        }
    }

    private static class asyncGenerator extends AsyncTask<Uri, String, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Baseprogress.showProgressDialog(mContext, "Memulai Generator...");
        }

        @Override
        protected Void doInBackground(Uri... uris) {
            Uri uri = uris[0];

            publishProgress("Membuka DMK...");
            openPDF(uri);

            if(mPdfRenderer == null){
                cancel(true);
            }
            publishProgress("Memproses DMK...");

            final String path = tmpFile.getPath() + "/";

            int pages = mPdfRenderer.getPageCount();
            for(int page = 0; page < pages; page++){
                publishProgress("Memproses DMK " + (page + 1) + "/" + pages);
                PdfRenderer.Page pdfPage = mPdfRenderer.openPage(page);
                Bitmap bitmap = Bitmap.createBitmap(pdfPage.getWidth() * 3, pdfPage.getHeight() * 3,
                        Bitmap.Config.ARGB_8888);
                pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(path + ("p-" + page) + ".png" ));
                }catch (FileNotFoundException e){
                    cancel(true);
                }

                pdfPage.close();
            }

            publishProgress("Menyusun DMK ...");

            for(int page = 0; page < pages; page++){
                publishProgress("Menyusun DMK " + (page + 1) + "/" + pages);
                SpenPageDoc spage = mSpenNoteDoc.appendPage();
                spage.setBackgroundImage(path + ("p-" + page) + ".png");
                spage.setBackgroundImageMode(SpenPageDoc.BACKGROUND_IMAGE_MODE_FIT);
                Log.d(TAG, "doInBackground: " + path + ("p-" + page));
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            //Baseprogress.hideProgressDialog();
            Toast.makeText(mContext, "Terjadi Kesalahan!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Baseprogress.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Baseprogress.hideProgressDialog();
            mSpenPageDoc = mSpenNoteDoc.getPage(0);
            mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
            blankSurface.setVisibility(View.GONE);
            Toast.makeText(mContext, mSpenNoteDoc.getPageCount() + " halaman digenerate", Toast.LENGTH_SHORT).show();
            totalPage = mSpenNoteDoc.getPageCount();
            if (totalPage>1){
                fab_nx.setVisibility(View.VISIBLE);
            }else if (totalPage<=1){
                fab_nx.setVisibility(View.GONE);
            }
        }
    }

    private static void openPDF(Uri uri){
        Log.d(TAG, "openPDF: " + uri.toString());
        ParcelFileDescriptor parcelFileDescriptor = null;
        mPdfRenderer = null;

        try {
            parcelFileDescriptor =
                    resolver.openFileDescriptor(uri, "r");
            //FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            if(parcelFileDescriptor != null)
                mPdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSpenPackage(){
        // Initialize Spen
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(mContext);
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Spen Package.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }
    }

    private void initSurfaceView(){
        // Create Spen View
        RelativeLayout spenViewLayout = findViewById(R.id.spenViewLayout);

        mSpenSurfaceView = new SpenView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenView.", Toast.LENGTH_SHORT).show();
            finish();
        }

        mSpenSurfaceView.setToolTipEnabled(true);
        spenViewLayout.addView(mSpenSurfaceView);

        // Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        mScreenRect = new Rect();
        display.getRectSize(mScreenRect);
        // Create SpenNoteDoc
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, mScreenRect.width(), mScreenRect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc.",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

//        // Initialize Pen settings
//        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
//        mSpenSurfaceView.setPenSettingInfo(penInfo);
//        mPenSettingView.setInfo(penInfo);
//        mPenSettingView.loadPreferences();
//
//        // DISABLE SPEN
//        mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_NONE);
    }
}
