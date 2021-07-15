package com.sabin.digitalrm.prm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.dialogs.DialogAddNoteAnalytic;
import com.sabin.digitalrm.dialogs.DialogReviewNoteAnalytic;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.helpers.FileDownloader;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.utils.ApiError;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DMRAnalyticActivity extends BaseActivity {
    public static final String EXTRA_ID_DMR = "EX1";
    public static final String EXTRA_NORM = "EX2";

    private final int PG_NEXT = 2;
    private final int PG_PREV = 1;

    private Integer idDMR;

    private Context ctx;
    SpenView mSpenSurfaceView;
    Rect mScreenRect;
    SpenNoteDoc mSpenNoteDoc;
    SpenPageDoc mSpenPageDoc;
    SpenNoteDoc tmpSpenNoteDoc;

    APIService APIClient;

    String ftpHost, ftpUser, ftpPassword;
    int ftpPort;

    private Integer currentPage;
    private Integer totalPage;
    private String spdPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmranalytic);

        ctx = DMRAnalyticActivity.this;
        Toolbar tb = findViewById(R.id.tbarfldsel);

        setSupportActionBar(tb);
        Bundle bundle = getIntent().getExtras();

        ftpHost = ApiServiceGenerator.getBaseFtp(getApplicationContext());
        ftpPort = ApiServiceGenerator.getBaseFtpPort();
        ftpUser = ApiServiceGenerator.getUserFtp();
        ftpPassword = ApiServiceGenerator.getPasswordFtp();

        int idDMR = bundle.getInt(EXTRA_ID_DMR);
        this.idDMR = idDMR;
        String noRM = bundle.getString(EXTRA_NORM);
        tb.setTitle("Kelengkapan - " + noRM);

        APIClient = ApiServiceGenerator.createService(getApplicationContext(), APIService.class);
        initSpenView();

        //TODO: UBAH KE FTP
//        download(idDMR, noRM);
        Baseprogress.showProgressDialog(ctx, "Initiating Download ...");
        downloadSpd(idDMR, noRM);


        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(view -> {
            DialogAddNoteAnalytic dialog = DialogAddNoteAnalytic.newInstance(idDMR, PetugasMainActivity.UID);
            dialog.show(getSupportFragmentManager(), null);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dmr_analytic, menu);

        return true;
    }

    private void initSpenView(){
        // Create Spen View
        RelativeLayout spenViewLayout = findViewById(R.id.canvas_analytic);

        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        mSpenSurfaceView = new SpenView(ctx);
        if (mSpenSurfaceView == null) {
            Toast.makeText(ctx, "Cannot create new SpenView.", Toast.LENGTH_SHORT).show();
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
            mSpenNoteDoc = new SpenNoteDoc(ctx, mScreenRect.width(), mScreenRect.height());
        } catch (IOException e) {
            Toast.makeText(ctx, "Cannot create new NoteDoc.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.clearHistory();
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
    }

    //baru
    public void downloadSpd(int idBRM, String noRM){
        Call<ResponseBody> callDownload = APIClient.downloadDMR(idBRM, PetugasMainActivity.UID);
        callDownload.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    if(response.code()==200){
                        String check = response.headers().get("X-Filename");
                        String[] woke = check.split("/");
                        String spdFileName = woke[1];
                        String nama_dir = woke[0];

                        Log.e("[X-DEBUG]","downloadBRM() - onResponse Triggered");
                        Log.e("FILENAME_CHECK", check);

                        new downloadBlankoAsync(spdFileName, nama_dir).execute();
                    }else{
                        String statuscode  = null;
                        String message = null;

                        try {
                            JSONArray jar = new JSONArray(response.body());
                            for (int i = 0; i < jar.length(); i++) {
                                JSONObject jsonobject = jar.getJSONObject(i);
                                statuscode = jsonobject.getString("statuscode");
                                message = jsonobject.getString("message");
                            }
                        }catch (JSONException jex){
                            jex.printStackTrace();
                        }

                        Baseprogress.hideProgressDialog();
                        if(statuscode != null && message != null) {
                            Toast.makeText(ctx, "Response code: " + statuscode + ". " + message, Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(ctx, "Something went wrong. Unable to download document", Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }
                }else{
                    Toast.makeText(ctx, response.message()+": "+ ApiError.parseError(response).getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("[X-DEBUG]",response.message());
                    Baseprogress.hideProgressDialog();
                }

            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(ctx, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.e("[X-DEBUG]",t.getLocalizedMessage());
                Log.e("[X-DEBUG]","downloadBRM() - onFailed Triggered");
                Baseprogress.hideProgressDialog();
            }
        });
    }

    //TODO:APAKAH INI YANG MAU DI RUBAH?
    public class downloadBlankoAsync extends AsyncTask<ResponseBody, Integer, String> {
        String spdFilename, namaDir;

        public downloadBlankoAsync(String spdFilename, String namaDir){
            this.spdFilename = spdFilename;
            this.namaDir = namaDir;
        }

        @Override
        protected String doInBackground(ResponseBody... responseBodies) {
            byte data[] = new byte[1024 * 4];

            FTPClient ftp = new FTPClient();
            boolean error = false;
            try {
                int reply;

                ftp.connect(ftpHost);
                ftp.login(ftpUser, ftpPassword);

                Log.e("X-DEBUG", "Connected to " + ftpHost + ": "+ftp.getReplyString());

                reply = ftp.getReplyCode();

                if(!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();

                    Log.e("X-DEBUG", "FTP server refused connection.");
                }

                // transfer files
                ftp.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
                ftp.enterLocalPassiveMode();

                //masuk ke archives
                boolean cwd = ftp.changeWorkingDirectory("/archives/"+namaDir+"/");
                Log.e("X-DEBUG", "CHANGE WORKING DIR TO '/archives/"+namaDir+ ": "+cwd);

                //set file
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                //download
                InputStream bis = new BufferedInputStream(ftp.retrieveFileStream(spdFilename), 1024 * 8);
                Log.e("X-DEBUG", "DOWNLOAD spdFilename "+spdFilename);

                File outDir = new File(ctx.getFilesDir() + "/tmp/");
                File outFile = new File(ctx.getFilesDir()+"/tmp/", spdFilename+".spd");

                if(!outDir.exists()){
                    if(!outDir.mkdir()){
                        Log.e("X-DEBUG", "FAILED CREATE LOCAL PATH");
                    }
                }

                if(outFile.exists()){
                    if(!outFile.delete()){
                        Log.e("X-DEBUG", "FAILED DELETE EXISTING LOCAL FILE");
                    }
                }

                OutputStream output = new FileOutputStream(outFile);

                int count;
                while ((count = bis.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                bis.close();

                ftp.logout();
                ftp.disconnect();
            } catch(IOException e) {
                error = true;
                Baseprogress.hideProgressDialog();
                Log.e("X-DEBUG", "EXCEPTION: "+e.getLocalizedMessage());
                e.printStackTrace();

                runOnUiThread(() -> {
                    Toast.makeText(DMRAnalyticActivity.this, "Error: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }

            if(error) {
                Log.e("X-DEBUG", "OPERATION FAILED");

                return null;
            }else{
                Log.e("X-DEBUG", "OPERATION SUCCESS");
            }

            File outFile = new File(ctx.getFilesDir()+"/tmp/", spdFilename +".spd");

            return outFile.getPath();
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String path) {
            Baseprogress.hideProgressDialog();
            openDoc(path);
        }
    }

    private void download(int idBRM, String noRM){
        Call<ResponseBody> callDownload = APIClient.downloadDMR(idBRM, PetugasMainActivity.UID);
        FileDownloader downloader = new FileDownloader(ctx, callDownload, noRM);
        downloader.setBaseDir("tmp");
        downloader.setOnFileDownloadListener(new FileDownloader.FileDownloadListener() {
            @Override
            public void onStart(String msg) {
                Baseprogress.hideProgressDialog();
                Baseprogress.showProgressDialog(ctx, msg);
            }

            @Override
            public void onProgress(String status) {
                Baseprogress.setMessage(status);
            }

            @Override
            public void onError(String msg) {
                Baseprogress.hideProgressDialog();
                toastErr(ctx, msg);
            }

            @Override
            public void onComplete(String filePath) {
                Baseprogress.hideProgressDialog();
                try {
                    Headers headers = downloader.getResponseHeader();
                    File f = new File(filePath);
                    InputStream is = new FileInputStream(f);

                    BaseActivity.Baseprogress.hideProgressDialog();

                    Log.e("[X-DEBUG]", "Content-Length HEADER: "+headers.get("Content-Length")+"; FILE STREAM LENGTH: "+is.available());
                    if(Objects.equals(headers.get("Content-Length"), String.valueOf(is.available()))){
                        spdPath = filePath;
                        openDoc(spdPath);
                    }else{
                        Toast.makeText(DMRAnalyticActivity.this, "Download failed or file corrupt", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }catch (Exception e){
                    BaseActivity.Baselog.d("Exception " + e.getMessage());
                    Toast.makeText(ctx, "Terjadi kesalahan. File dokumen rusak", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        downloader.startDownload();
    }

    private void openDoc(String FilePath) {
        try {
            tmpSpenNoteDoc = new SpenNoteDoc(ctx, FilePath, mScreenRect.width(),
                    SpenNoteDoc.MODE_WRITABLE, true);
            mSpenNoteDoc.close();
            mSpenNoteDoc = tmpSpenNoteDoc;
            mSpenPageDoc = mSpenNoteDoc.getPage(0);
            mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
            mSpenSurfaceView.setZoomable(false);
//            mSpenSurfaceView.setPreTouchListener(onPreTouchSurfaceViewListener);
            mSpenSurfaceView.update();
            currentPage = 0;
            totalPage = mSpenNoteDoc.getPageCount();
        }catch (Exception ex){
            Log.e("[X-EXCEPTION]", "Error: "+ex.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.pg_nxt:{
                movePageIndex(PG_NEXT);

                break;
            }

            case R.id.pg_prv:{
                movePageIndex(PG_PREV);

                break;
            }

            case R.id.list_notes:{
                showRNoteLists();
                //generateJsonFieldData();

                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRNoteLists(){
        DialogReviewNoteAnalytic dialog = DialogReviewNoteAnalytic.newInstance(idDMR);
        dialog.show(getSupportFragmentManager(), null);
    }

    private void movePageIndex(int action){
        if(action==PG_NEXT){
            if(currentPage<totalPage-1) {
                currentPage = currentPage + 1;
            }else{
                Toast.makeText(ctx, "Last Page", Toast.LENGTH_SHORT).show();
            }
        }else if(action==PG_PREV){
            if(currentPage>0) {
                currentPage = currentPage - 1;
            }else{
                Toast.makeText(ctx, "First Page", Toast.LENGTH_SHORT).show();
            }
        }else{
            currentPage = 0;
        }

        mSpenPageDoc = mSpenNoteDoc.getPage(currentPage);
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
        mSpenSurfaceView.update();
    }
}
