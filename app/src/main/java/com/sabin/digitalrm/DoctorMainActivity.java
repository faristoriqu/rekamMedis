package com.sabin.digitalrm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sabin.digitalrm.adapters.BookmarkAdapter;
import com.sabin.digitalrm.dialogs.DialogAddNoteAnalytic;
import com.sabin.digitalrm.fragments.RejectedBRMListFragmentDialog;
import com.sabin.digitalrm.helpers.PNGExport;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.BookmarkResponse;
import com.sabin.digitalrm.models.DMKBookmark;
import com.sabin.digitalrm.models.DetailVisitor;
import com.sabin.digitalrm.models.FtpKu;
import com.sabin.digitalrm.models.ProgressRequestBody;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.CompressUtils;
import com.sabin.digitalrm.utils.Logger;
import com.sabin.digitalrm.utils.SerializationUtil;
import com.sabin.digitalrm.utils.Timestamp;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingRemoverInfo;
import com.samsung.android.sdk.pen.SpenSettingViewInterface;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.engine.SpenView;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingRemoverLayout;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorMainActivity extends BaseActivity {
    private static final int DIALOG_REQUEST_CODE = 111;
    public static final int ACTION_MODE = 1;
    public static final int FIX_MODE = 2;
    public static  int Check = 0;
    public static String ARCHIVE_PATH, uid;
    public static File ARCHIVE_FILE;
    private final int MAIN_STATE        = 0b001;
    private final int BOOKMARK_STATE    = 0b010;
    
    private static SpenNoteDoc mSpenNoteDoc;
    private static SpenPageDoc mSpenPageDoc;
    private static SpenView mSpenSurfaceView;
    private SpenSettingPenLayout mPenSettingView;
    private SpenSettingRemoverLayout mEraserSettingView;
    private static Rect mScreenRect = new Rect();
    private static File noteFile;
    private static String notePath;
    private static FloatingActionButton fabNextPage, fabPrevPage;

    private LinearLayout bookmarkLayout;
    private RecyclerView bookmarkRecyvler;
    private MenuItem bookmarkToggle;
    private Menu canvasMenu;
    private TextView txtStatus;
    private static TextView txtPgIndicator;

    private static List<DMKBookmark> bookmarkList;
    private static int curentPage = 0;
    private static int totalPage = 0;
    private boolean penEnable = false;
    private boolean eraserEnable = false;
    private int backState = MAIN_STATE;
    private static String noBRM, idPoli, nama_file, kode_file, namapoli, pxName, nama_dir, spdFileName;
    private static String host, username, password;

    private static int servid = 0, brmid = 0, mode = 0, statusIntent=0, statusRM, port;

    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    private static Context mContext;
    private static View mView;
    private static APIService APIClient;
    private static BookmarkAdapter bookmarkAdapter;
    private int mToolType;
    private static boolean saveState = false, isSaveDialog = false, doneState = false, isUndoable = false, isRedoable = false;
    private static HashMap<Integer, Integer> resultFromDialog;
    private static RelativeLayout loadingPanel;

    public FTPClient mFTPClient = null;



    private static final Logger log = new Logger();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ARCHIVE_PATH = this.getFilesDir()+"/archives/";
        ARCHIVE_FILE = new File(ARCHIVE_PATH);

        setSupportActionBar(findViewById(R.id.my_toolbar));

        mContext = this;
        mView = getWindow().getDecorView().getRootView();

        //testing();

        host = ApiServiceGenerator.getBaseFtp(getApplicationContext());
        port = ApiServiceGenerator.getBaseFtpPort();
        username = ApiServiceGenerator.getUserFtp();
        password = ApiServiceGenerator.getPasswordFtp();

        fabNextPage = findViewById(R.id.fabNextPage);
        fabPrevPage = findViewById(R.id.fabPrevPage);
        txtPgIndicator = findViewById(R.id.txtPgIndicator);
        loadingPanel = findViewById(R.id.loadingPanel);

        pref = getSharedPreferences(ApiServiceGenerator.sessionPrefsName, 0);

        Bundle bundle = getIntent().getExtras();
        mode = Objects.requireNonNull(bundle).getInt("mode");
        servid = bundle.getInt("serv");
        uid = bundle.getString("uid");
        idPoli = bundle.getString("id");
        noBRM = bundle.getString("no_brm");
        nama_file = noBRM+"_"+idPoli+".spd";
        kode_file = noBRM+"_"+idPoli;
        namapoli = bundle.getString("nama_poli");
        brmid = bundle.getInt("id_berkas");
        pxName = bundle.getString("px_name");
        saveState = bundle.getBoolean("isSaved");
        statusIntent = bundle.getInt("statusIntent");

        Toast.makeText(mContext, "statusnya"+statusIntent, Toast.LENGTH_LONG).show();
        Log.e("statusnya", String.valueOf(statusIntent));

        String defaultTitle = "";

        if(mode == ACTION_MODE){
            defaultTitle += namapoli+" - ";
        }

        defaultTitle += noBRM;

        if(mode == FIX_MODE){
            defaultTitle += " [Mode Perbaikan]";
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle(defaultTitle);

        txtStatus = findViewById(R.id.txtStatus);

        initSpen();
        initSpenView();

        initRetrofit();
        initBookmark();

        if(!getFileCached(nama_file)){
            downloadBRM();
        }

        editor = pref.edit();
        editor.putString("activeBRM", noBRM);
        editor.putInt("activeBerkas", brmid);
        editor.putInt("activeServ", servid);
        editor.putString("activePx", pxName);
        editor.putInt("mode", mode);
        editor.apply();

        resultFromDialog = new HashMap<>();

        Log.e("[X-DEBUG]", "ECHO VAR: service_id:"+servid+", no_rm:"+noBRM+", user_id:"+uid+", unit_id:"+idPoli);

        fabNextPage.setOnClickListener(v -> {
            if((curentPage + 1) < totalPage){
//                String filePath = mContext.getFilesDir() + "/archives/" + nama_file;
//                new savePage(false, true, null).execute(filePath, String.valueOf(curentPage));
                loadingPanel.setVisibility(View.VISIBLE);

                int realPage;
                String filePath = mContext.getFilesDir() + "/archives/" + kode_file;
                File file = new File(filePath);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        Log.e("[X-DEBUG]", "Failed to creating dirs on " + filePath);
                        loadingPanel.setVisibility(View.GONE);

                        return;
                    }
                }
                String pngExp = null;
                try {
                    pngExp = new PNGExport(mContext, mView, mSpenSurfaceView, mSpenPageDoc, mSpenNoteDoc, uid, noBRM, totalPage, false, null).execute(filePath, nama_file, String.valueOf(curentPage)).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                if (Objects.requireNonNull(pngExp).equals("true")) {
                    Log.e("[X-DEBUG]", "PNG Saving Success");
                    realPage = curentPage+2;

                    mSpenPageDoc = mSpenNoteDoc.getPage((curentPage+1));
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    txtPgIndicator.setText(realPage + " of " + totalPage);
                } else {
                    Log.e("[X-DEBUG]", "PNG Saving Fail");
                    Toast.makeText(mContext, "Error while trying save document image", Toast.LENGTH_LONG).show();
                }

                loadingPanel.setVisibility(View.GONE);
                curentPage++;
            }else {
                Toast.makeText(mContext, "Last Page", Toast.LENGTH_SHORT).show();
            }
        });

        fabPrevPage.setOnClickListener(v -> {
            if(curentPage != 0){
//                String filePath = mContext.getFilesDir() + "/archives/" + nama_file;
//                new savePage(false, true, null).execute(filePath, String.valueOf(curentPage));
                loadingPanel.setVisibility(View.VISIBLE);

                int realPage;
                String filePath = mContext.getFilesDir() + "/archives/" + kode_file;
                File file = new File(filePath);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        Log.e("[X-DEBUG]", "Failed to creating dirs on " + filePath);
                        loadingPanel.setVisibility(View.GONE);

                        return;
                    }
                }
                String pngExp = null;
                try {
                    pngExp = new PNGExport(mContext, mView, mSpenSurfaceView, mSpenPageDoc, mSpenNoteDoc, uid, noBRM, totalPage, true, null).execute(filePath, nama_file, String.valueOf(curentPage)).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                if (Objects.requireNonNull(pngExp).equals("true")) {
                    Log.e("[X-DEBUG]", "PNG Saving Success");
                    realPage = curentPage;

                    mSpenPageDoc = mSpenNoteDoc.getPage((curentPage-1));
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    txtPgIndicator.setText(realPage + " of " + totalPage);
                } else {
                    Log.e("[X-DEBUG]", "PNG Saving Fail");
                    Toast.makeText(mContext, "Error while trying save document image", Toast.LENGTH_LONG).show();
                }

                loadingPanel.setVisibility(View.GONE);
                curentPage--;
            }else {
                Toast.makeText(mContext, "First Page", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private boolean getFileCached(String filename){
        File fileCached = new File(mContext.getFilesDir() + "/archives/", filename);
        if (!fileCached.exists()) {
            return false;
        }

        String saveFilePath = fileCached.getPath();
        notePath = saveFilePath;

        Log.e("[X-DEBUG]", "File: "+saveFilePath);

        try {
            SpenNoteDoc tmpSpenNoteDoc = new SpenNoteDoc(mContext, saveFilePath, mScreenRect.width(), SpenNoteDoc.MODE_WRITABLE, true);
            mSpenNoteDoc.close();
            mSpenNoteDoc = tmpSpenNoteDoc;
            mSpenPageDoc = mSpenNoteDoc.getPage(0);
            mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
            mSpenSurfaceView.update();
            curentPage = 0;
            totalPage = mSpenNoteDoc.getPageCount();

            txtPgIndicator.setText((curentPage+1)+" of "+totalPage);
        }catch (Exception ex){
            Toast.makeText(mContext, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            Log.e("[X-DEBUG]", ex.getLocalizedMessage());
        }

        return true;
    }

    public void downloadBRM(){
        BaseActivity.Baseprogress.showProgressDialog(mContext, "Initiating Download ...");

        Log.e("[X-DEBUG]", "UID:"+uid+" no.brm:"+noBRM+" id.poli:"+idPoli+" BRM.id:"+brmid);

        Call<ResponseBody> callDownload = APIClient.downloadBRMDokter(uid, brmid);
        callDownload.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
//                    ResponseBody responseBody = response.body();
                    if(response.code()==200){

                        new downloadBrmAsync().execute();
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

                        BaseActivity.Baseprogress.hideProgressDialog();
                        if(statuscode != null && message != null) {
                            Toast.makeText(mContext, "Response code: " + statuscode + ". " + message, Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(mContext, "Something went wrong. Unable to download document", Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }
                }else{
                    Toast.makeText(mContext, response.message()+": "+ ApiError.parseError(response).getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("[X-DEBUG]",response.message());
                    BaseActivity.Baseprogress.hideProgressDialog();
                }
                String check = response.headers().get("X-Filename");
                String[] woke = check.split("/");
                spdFileName = woke[1];
                nama_dir = woke[0];

                Log.e("[X-DEBUG]","downloadBRM() - onResponse Triggered");
                Log.e("FILENAME_CHECK", check);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.e("[X-DEBUG]",t.getLocalizedMessage());
                Log.e("[X-DEBUG]","downloadBRM() - onFailed Triggered");
                BaseActivity.Baseprogress.hideProgressDialog();
            }
        });
    }

    //TODO:APAKAH INI YANG MAU DI RUBAH?
    public class downloadBrmAsync extends AsyncTask<ResponseBody, Integer, String>{

        @Override
        protected String doInBackground(ResponseBody... responseBodies) {
            byte data[] = new byte[1024 * 4];

            FTPClient ftp = new FTPClient();
            boolean error = false;
            try {
                int reply;
                ftp.connect(host);
                ftp.login(username, password);

                Log.e("X-DEBUG", "Connected to " + host + ": "+ftp.getReplyString());

                reply = ftp.getReplyCode();

                if(!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();

                    Log.e("X-DEBUG", "FTP server refused connection.");
                }

                // transfer files
                ftp.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
                ftp.enterLocalPassiveMode();

                //masuk ke archives
                boolean cwd = ftp.changeWorkingDirectory("/archives/");
                Log.e("X-DEBUG", "CHANGE WORKING DIR TO '/archives/ : "+cwd);

                //masuk ke dir noBRM
                boolean cwdNoBrm = ftp.changeWorkingDirectory(noBRM);
                Log.e("X-DEBUG", "CHANGE WORKING DIR TO '/archives/"+noBRM+" : "+cwdNoBrm);

                //set file
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                //download
                InputStream bis = new BufferedInputStream(ftp.retrieveFileStream(spdFileName), 1024 * 8);
                Log.e("X-DEBUG", "DOWNLOAD BRM "+spdFileName);

                File outDir = new File(mContext.getFilesDir() + "/archives/");
                File outFile = new File(mContext.getFilesDir()+"/archives/", nama_file);

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
                noteFile = outFile;

                ftp.logout();
                ftp.disconnect();
            } catch(IOException e) {
                error = true;
                Log.e("X-DEBUG", "EXCEPTION: "+e.getLocalizedMessage());
                e.printStackTrace();
            }

            if(error) {
                Log.e("X-DEBUG", "OPERATION FAILED");
            }else{
                Log.e("X-DEBUG", "OPERATION SUCCESS");
            }
            File outFile = new File(mContext.getFilesDir()+"/archives/", nama_file);

            return outFile.getPath();

//            Log.e("[X-DEBUG]","downloadBRMAsync() - File size: "+fileSize);
//            try {
//
//                OutputStream output = new FileOutputStream(outputFile);
//                long total = 0;
//                long startTime = System.currentTimeMillis();
//                int timeCount = 1;
//                while ((count = bis.read(data)) != -1) {
//
//                    total += count;
//
//                    int progress = (int) ((total * 100) / fileSize);
//
//                    long currentTime = System.currentTimeMillis() - startTime;
//
//                    if (currentTime > 100 * timeCount) {
//                        Baselog.d("Progress " + progress);
//                        publishProgress(progress);
//                        timeCount++;
//                    }
//
//                    output.write(data, 0, count);
//                }
//                output.flush();
//                output.close();
//                bis.close();
//            }catch (Exception e){
//                Baselog.d("Error " + e.getLocalizedMessage());
//                endEditSession();
//            }



        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            BaseActivity.Baseprogress.setMessage("Download Progress: " + values[0] + "%");
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String path) {

            Log.e("[X-DEBUG]","downloadBRMAsync() - onPostExecute Triggered");
            BaseActivity.Baselog.d("Path " + path);

            try {
                notePath = path;
                SpenNoteDoc tmpSpenNoteDoc = new SpenNoteDoc(mContext, path, mScreenRect.width(), SpenNoteDoc.MODE_WRITABLE, true);
                mSpenNoteDoc.close();
                mSpenNoteDoc = tmpSpenNoteDoc;
                mSpenPageDoc = mSpenNoteDoc.getPage(0);
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                mSpenSurfaceView.update();
                curentPage = 0;
                totalPage = mSpenNoteDoc.getPageCount();
                BaseActivity.Baseprogress.setMessage("Loading ...");
//                renderPage();

                txtPgIndicator.setText((curentPage+1)+" of "+totalPage);

                if (mode == ACTION_MODE) {

                    if(statusIntent != 5){
                        setBRMData();
                    }else {
                        setBRMDataOnReject();
                    }

                }else if (mode == FIX_MODE){
                    Baseprogress.hideProgressDialog();
                }
            }catch (Exception e) {
                Baselog.d("Exception " + e.getMessage());
                Baseprogress.hideProgressDialog();
                Toast.makeText(mContext, "Gagal mendownload dokumen. File dokumen tidak ada atau korup", Toast.LENGTH_LONG).show();
                endEditSession();
                purgeAllFiles(ARCHIVE_FILE);

                if (mode == ACTION_MODE) {
                    restoreBRMdata();
                } else if (mode == FIX_MODE){
                    ((Activity)mContext).finish();
                }
            }
        }
    }

    private static void xrenderPage(){
        Log.e("[X-DEBUG]","renderPage() Triggered");
        byte [] dataByte;

        // Read all bookmark
        Baselog.d( "renderPage: RENDERING");

        bookmarkList.clear();

        for(int i = 0; i < totalPage; i++){
            dataByte = mSpenNoteDoc.getPage(i).getExtraDataByteArray("BOOKMARK");

            if(dataByte != null){
                try {
                    DMKBookmark bookmark = (DMKBookmark) SerializationUtil.deserialize(dataByte);
                    Baselog.d( "renderPage: indexPage " + i + "\non page " + bookmark.getPage());

                    bookmarkList.add(bookmark);
                }catch (Exception e){
                    Baselog.d( "renderPage: ERROR " + e.getLocalizedMessage());
                }
            }else{
                Baselog.d( "renderPage: NO BM on page " + i);
            }
        }
        bookmarkAdapter.notifyDataSetChanged();

        BaseActivity.Baselog.d( "renderPage: COMPLETE " + totalPage);
    }

    public static void setBRMData(){
        BaseActivity.Baseprogress.setMessage("Setting BRM Data ...");

        Call<ApiStatus> updateDataCall = APIClient.setStatusBRMOnHandle(uid, servid, 4, Integer.valueOf(uid), Timestamp.now());

        updateDataCall.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                if(response.isSuccessful()){
                    if (Objects.requireNonNull(response.body()).getStatuscode() == 400) {
                        BaseActivity.Baselog.d("Response error " + Objects.requireNonNull(response.body()).getMessage());
                        Log.e("[X-DEBUG]", "[-] set status data to 4 failed. Error: "+response.body().getMessage());
                    }else{
                        Log.e("[X-DEBUG]", "[-] set status data to 4. Result: "+response.body().getMessage());
                    }
                }else {
                    BaseActivity.Baselog.d("Response error " + response.message());
                    try {
                        Log.e("[X-DEBUG]", "[-] set status data to 4 isn't successful. Error: "+response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Log.e("[X-DEBUG]", "[-] set status data to 4 isn't successful. APIError: "+response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                BaseActivity.Baseprogress.hideProgressDialog();
            }

            @Override
            public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                BaseActivity.Baselog.d("Failure " + t.getLocalizedMessage());
                Log.e("[X-DEBUG]", "[-] set status dat to 4 onFailure thrown. Error: "+t.getLocalizedMessage());
                BaseActivity.Baseprogress.hideProgressDialog();
            }
        });

        Baselog.d( "renderPage: COMPLETE " + totalPage);
        BaseActivity.Baseprogress.hideProgressDialog();
    }
    public static void setBRMDataOnReject(){
        BaseActivity.Baseprogress.setMessage("Setting BRM Data ...");

        Call<ApiStatus> updateDataCall = APIClient.setStatusBRMOnHandle(uid, servid, 5, Integer.valueOf(uid), Timestamp.now());

        updateDataCall.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                if(response.isSuccessful()){
                    if (Objects.requireNonNull(response.body()).getStatuscode() == 400) {
                        BaseActivity.Baselog.d("Response error " + Objects.requireNonNull(response.body()).getMessage());
                        Log.e("[X-DEBUG]", "[-] set status data to 5 failed. Error: "+response.body().getMessage());
                    }else{
                        Log.e("[X-DEBUG]", "[-] set status data to 5. Result: "+response.body().getMessage());
                        statusRM = 5;
                        Log.e("xrm", String.valueOf(statusRM));
                    }
                }else {
                    BaseActivity.Baselog.d("Response error " + response.message());
                    try {
                        Log.e("[X-DEBUG]", "[-] set status data to 5 isn't successful. Error: "+response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Log.e("[X-DEBUG]", "[-] set status data to 4 isn't successful. APIError: "+response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                BaseActivity.Baseprogress.hideProgressDialog();
            }

            @Override
            public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                BaseActivity.Baselog.d("Failure " + t.getLocalizedMessage());
                Log.e("[X-DEBUG]", "[-] set status dat to 4 onFailure thrown. Error: "+t.getLocalizedMessage());
                BaseActivity.Baseprogress.hideProgressDialog();
            }
        });

        Baselog.d( "renderPage: COMPLETE " + totalPage);
        BaseActivity.Baseprogress.hideProgressDialog();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_doctor, menu);
        bookmarkToggle = menu.findItem(R.id.action_bookmark);
        MenuItem subMenu = menu.findItem(R.id.action_more);
        MenuItem pen = menu.findItem(R.id.action_spen);

        pen.getIcon().setTint(Color.WHITE);

        if(mode == ACTION_MODE) {
            getMenuInflater().inflate(R.menu.submenu_doctor, subMenu.getSubMenu());
        }else if(mode == FIX_MODE){
            getMenuInflater().inflate(R.menu.submenu_doctor_fixbrm, subMenu.getSubMenu());
        }

        canvasMenu = menu;

        txtStatus.setText("[Status] Selected tool: NONE");

        return true;
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_spen:{
                if(penEnable){
                    mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_NONE);
                    penEnable = false;
                    item.getIcon().setTint(Color.WHITE);

                    if (mPenSettingView.isShown()) {
                        mPenSettingView.setVisibility(View.GONE);
                    }
                }else{
                    item.getIcon().setTint(getResources().getColor(R.color.colorPrimaryDark));
                    penEnable = true;
                    mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_STROKE);

                    if (mPenSettingView.isShown()) {
                        mPenSettingView.setVisibility(View.GONE);
                    } else {
                        mPenSettingView.setVisibility(View.VISIBLE);
                    }

                    if(eraserEnable){
                        eraserEnable = false;
                        canvasMenu.findItem(R.id.action_eraser_tools).getIcon().setTint(Color.WHITE);
                    }
                }

                if(penEnable){
                    txtStatus.setText("[Status] Selected tool: PEN");
                }else if(eraserEnable){
                    txtStatus.setText("[Status] Selected tool: ERASER");
                }else{
                    txtStatus.setText("[Status] Selected tool: NONE");
                }

                break;
            }

            case R.id.action_eraser_tools:{
                if (mPenSettingView.isShown()) {
                    mPenSettingView.setVisibility(View.GONE);
                }

                if (eraserEnable) {
                    // If EraserSettingView is open, close it.
                    mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_NONE);
                    eraserEnable = false;
                    item.getIcon().setTint(Color.WHITE);
                    // If Spen is not in eraser mode, change it to eraser mode.
                } else {
                    mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE_REMOVER);
                    eraserEnable = true;
                    item.getIcon().setTint(getResources().getColor(R.color.colorPrimaryDark));

                    if(penEnable){
                        penEnable = false;
                        canvasMenu.findItem(R.id.action_spen).getIcon().setTint(Color.WHITE);
                    }
                }

                if(penEnable){
                    txtStatus.setText("[Status] Selected tool: PEN");
                }else if(eraserEnable){
                    txtStatus.setText("[Status] Selected tool: ERASER");
                }else{
                    txtStatus.setText("[Status] Selected tool: NONE");
                }

                break;
            }

            case R.id.action_bookmark:{
                bookmarkToggle = item;
                if((backState & BOOKMARK_STATE) != 0) {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_bookmark_list));
                    hideBookmark();
                }else {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_bookmark_fill_white));
                    showBookmark();
                    Baselog.d( "onClick: SHOW BOOKMARK");
                }
                break;
            }

//            case R.id.action_next:{
//                if((curentPage + 1) < totalPage){
//                    String filePath = mContext.getFilesDir() + "/archives/" + nama_file;
//                    new savePage(false).execute(filePath, String.valueOf(curentPage));
//                    curentPage++;
//                }else {
//                    Toast.makeText(mContext, "Last Page", Toast.LENGTH_SHORT).show();
//                }
//
//                break;
//            }
//
//            case R.id.action_prev:{
//                if(curentPage != 0){
//                    String filePath = mContext.getFilesDir() + "/archives/" + nama_file;
//                    new savePage(false).execute(filePath, String.valueOf(curentPage));
//                    curentPage--;
//                }else {
//                    Toast.makeText(mContext, "First Page", Toast.LENGTH_SHORT).show();
//                }
//
//                break;
//            }

            case R.id.action_exit:{
                Log.e("[X-DEBUG]", "saveState value: "+saveState);
                Log.e("[X-DEBUG]", "undoable value: "+isUndoable);
                Log.e("[X-DEBUG]", "redoable value: "+isRedoable);
                Log.e("ssstt", "sttttt "+statusIntent);




                if (mSpenPageDoc.isUndoable() || saveState) {
                    if (!doneState) {
                        alertExit(nama_file);
                    } else {
                        purgeAllFiles(ARCHIVE_FILE);
                        endEditSession();
                        finish();
                    }
                } else {
                    purgeAllFiles(ARCHIVE_FILE);
                    endEditSession();
                    if (mode == ACTION_MODE) {
                        if(statusRM != 5) {
                            restoreBRMdata();
                        }else {
                            restoreBRMdataOnReject();
                        }
                    } else if (mode == FIX_MODE) {
                        finish();
                    }
                }

                // SEPERTINYA PERLU KONDISI KETIKA STATUS RM "TIDAK LENGKAP",
                // KEMUDIAN MEMBUKA FILE/ BLANKO RM..
                // NAMUN TIDAK MELAKUKAN PERUBAHAN
                // LEBIH BAIK STATUS TETAP MENJADI "TIDAK LENGKAP"
                // BUKAN MENJADI "BELUM DITANGANI"
                break;
            }

            case R.id.action_save:{
                if(mode == ACTION_MODE) {
                    if (saveNoteDlgV2(mView, nama_file, false, false)) {
                        Log.e("[X-DEBUG]", "File " + nama_file + " saved!");
                    } else {
                        Log.e("[X-DEBUG]", "Couldn't save file " + nama_file);
                    }
                }else if(mode == FIX_MODE){
                    checklistRejectedBRM();
                }

                break;
            }
            case R.id.catattan:{
                DialogAddNoteAnalytic dialog = DialogAddNoteAnalytic.newInstance(brmid, uid);
                dialog.show(getSupportFragmentManager(), null);
                break;
            }
            case R.id.action_set_status:{
                if (!saveState){
                    Toast.makeText(mContext, "Simpan Data Terlebih Dahulu", Toast.LENGTH_LONG).show();
                }else {
                    //checkoutBRM();
                    Log.e("nama_file", kode_file);
                    checkOut(kode_file, true, false, false, null);
                }
//                checkoutBRM();
                break;
            }

            case R.id.action_rejected_checklist:{
                checklistRejectedBRM();
                break;
            }

            case R.id.action_rejected_checkout:{
                checkoutRejectedBRM();
                break;
            }

            case R.id.action_prev_pdf: {
                Intent intent = new Intent(this, OldBRMActivity.class);
                intent.putExtra("mode", PreviewActivity.PDF_MODE);
                intent.putExtra("uid", uid);
                intent.putExtra("norm", noBRM);
                intent.putExtra("px_name", pxName);
                startActivity(intent);
                break;
            }

            case R.id.action_other_poli: {
                Intent intent = new Intent(this, OtherUnitsActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("brm", noBRM);
                intent.putExtra("poli", idPoli);
                intent.putExtra("idBerkas", brmid);
                startActivity(intent);
                break;
            }

            case R.id.action_undo:{
                if (mSpenPageDoc.isUndoable()) {
                    SpenPageDoc.HistoryUpdateInfo[] userData = mSpenPageDoc.undo();
                    mSpenSurfaceView.updateUndo(userData);
                }
                break;
            }

            case R.id.action_redo:{
                if (mSpenPageDoc.isRedoable()) {
                    SpenPageDoc.HistoryUpdateInfo[] userData = mSpenPageDoc.redo();
                    mSpenSurfaceView.updateRedo(userData);
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void checklistRejectedBRM(){
        FragmentManager fm = getSupportFragmentManager();
        RejectedBRMListFragmentDialog checklistDialogFragment = RejectedBRMListFragmentDialog.newInstance(mContext, resultFromDialog, uid, String.valueOf(brmid));
        checklistDialogFragment.show(fm, DIALOG_REQUEST_CODE+"");
    }

    private void checkoutRejectedBRM(){
        if(isSaveDialog && !resultFromDialog.containsValue(0)) {
            AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle("Checkout BRM")
                    .setIcon(getResources().getDrawable(R.drawable.ic_check_primary))
                    .setMessage("Apakah anda yakin ingin Checkout BRM ini?\n\nPeringatan: Aksi ini tidak dapat dibatalkan.")
                    .setPositiveButton("Checkout", (dialogInterface, i) -> {
                        String filePath = mContext.getFilesDir() + "/archives/" + nama_file;
                        ProgressDialog progressDialog = new ProgressDialog(mContext);
                        new saveDocument(false, false, true, true, true, progressDialog).execute(filePath);
                    })
                    .setNegativeButton("Tidak", null)
                    .create();

            dialog.show();
        }else{
            AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle("Checkout BRM")
                    .setIcon(getResources().getDrawable(R.drawable.ic_check_primary))
                    .setMessage("Centang terlebih dahulu daftar penolakan BRM anda sebelum Checkout dokumen.\n\nIngin membuka daftar penolakan BRM?")
                    .setPositiveButton("Buka Daftar Penolakan BRM", (dialogInterface, i) -> checklistRejectedBRM())
                    .setNegativeButton("Batal", null)
                    .create();

            dialog.show();
        }
    }

    public void checkoutBRM(){
        if(saveState) {
            AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle("Checkout BRM")
                    .setIcon(getResources().getDrawable(R.drawable.ic_check_primary))
                    .setMessage("Apakah anda yakin ingin Checkout BRM ini?\n\nPeringatan: Aksi ini tidak dapat dibatalkan.")
                    .setPositiveButton("Checkout", (dialogInterface, i) -> {
//                        saveNoteDlgV2(mView, nama_file, false, true);

                        //TODO:buat menu lain untuk ini
                        //uploadFile(kode_file, true, false, false, null);
                        Log.e("filename_check", nama_file);
                        checkOut(nama_file, true, false, false, null);
                    })
                    .setNegativeButton("Tidak", null)
                    .create();

            dialog.show();
        }else{
            AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle("Checkout BRM")
                    .setIcon(getResources().getDrawable(R.drawable.ic_check_primary))
                    .setMessage("Simpan dahulu pekerjaan anda sebelum Checkout dokumen. Ingin simpan sekarang?")
                    .setPositiveButton("Simpan & Checkout", (dialogInterface, i) -> {
                        String filePath = mContext.getFilesDir() + "/archives/" + nama_file;
                        log.x("SAVE & CHECKOUT FILE PATH: "+filePath);
                        ProgressDialog progressDialog = new ProgressDialog(mContext);
                        new saveDocument(false, true, false, true, false, progressDialog).execute(filePath);
//                        saveNoteDlgV2(mView, nama_file, false, false);
                    })
                    .setNegativeButton("Tidak", null)
                    .create();

            dialog.show();
        }
    }

    private  void checkOut(final String filename, boolean isClose, boolean isRNoteStatusClose, boolean withRNoteStatusUpdate, ProgressDialog progressDialog){
        BaseActivity.Baseprogress.showProgressDialog(mContext, "Initiating Upload ...");


        //TODO: ini 1
        File fFilePath = new File(mContext.getFilesDir() + "/archives");
        if (!fFilePath.exists()) {
            if (!fFilePath.mkdirs()) {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        //TODO: ini 2
        final String saveFilePath = fFilePath.getPath() + '/' + filename + ".spd";

        Log.e("[X-DEBUG]", "upload file path: " + saveFilePath);
        Log.e("[X-DEBUG]", "Size upload file path: " + saveFilePath.length());
        long FileSizeInbyte = saveFilePath.length();
        long FileSizeInKB = FileSizeInbyte / 1024;
        long FileSizeInMB = FileSizeInKB / 2024;


        ProgressDialog pDlgs;

        if(progressDialog==null){
            pDlgs = new ProgressDialog(mContext);
        }else{
            pDlgs = progressDialog;
        }
        //TODO: ini 3
        File file = new File(saveFilePath);

        ProgressRequestBody reqFileSPD = new ProgressRequestBody(file, "*/*", new ProgressRequestBody.UploadCallbacks(){

            @Override
            public void onProgressUpdate(int percentage) {
                log.x("DOC UPLOAD PROGRESS -> "+percentage+"%");

                pDlgs.setIndeterminate(false);
                pDlgs.setCancelable(false);
                pDlgs.setMax(100);
                pDlgs.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDlgs.setMessage("Sedang mengupload Berkas RM... (1/2)");
                pDlgs.setProgress(percentage);
                pDlgs.show();
            }

            @Override
            public void onError() {
                pDlgs.dismiss();
                Toast.makeText(mContext, "Something went wrong while trying to upload file", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinish() {
                pDlgs.dismiss();
                Toast.makeText(mContext, "1 of 2 files uploaded successfully", Toast.LENGTH_LONG).show();
            }
        });
//
        final String zipSaveFilePath = fFilePath.getPath() + '/' + kode_file + "/compressed/" + filename + ".zip";
        File zipFile = new File(zipSaveFilePath);

        Log.e(TAG, zipSaveFilePath);
        if(mode == ACTION_MODE) {
            //TODO: ini 5

        }

        //ftp(saveFilePath,zipSaveFilePath);
//        new ftpApacheVersion().execute();

//        testing(saveFilePath);

        new ftpConnect().execute(saveFilePath);
    }

    //TODO: method ftp simple


    //TODO: NEW FTP FUNCTION
    private final class ftpConnect extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String saveFilePath = strings[0];

            FTPClient ftp = new FTPClient();
            boolean error = false;
            try {
                int reply;

                ftp.connect(host);
                ftp.login(username, password);

                Log.e("X-DEBUG", "Connected to " + host + ": "+ftp.getReplyString());

                reply = ftp.getReplyCode();

                if(!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();

                    Log.e("X-DEBUG", "FTP server refused connection.");
                }

                File asset = new File(saveFilePath);
                BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(asset));

                // transfer files
                ftp.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
                ftp.enterLocalPassiveMode();
                boolean cwd = ftp.changeWorkingDirectory("/archives/"+nama_dir);
                Log.e("X-DEBUG", "CHANGE WORKING DIR TO '/archives/"+nama_dir+"': "+cwd);

                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                if(ftp.rename(spdFileName, spdFileName+".old")){
                    Log.e("X-DEBUG", "BACKUPED BRM FILE");
                }

                Log.e("X-DEBUG", "UPLOAD '" + spdFileName + "' STREAM LENGTH: " + buffIn.available());

                Log.e("X-DEBUG", "UPLOADING...");
                boolean upStatus = ftp.storeFile(spdFileName, buffIn);

                Log.e("X-DEBUG", "UPLOAD '"+spdFileName+"' STATUS: "+upStatus);

                buffIn.close();

                if(upStatus){
                    ftp.deleteFile(spdFileName+".old");

                    Log.e("X-DEBUG", "OLD FILE DELETED");
                }else{
                    FTPFile[] files = ftp.listFiles();

                    if(files.length>0){
                        for (FTPFile file : files) {
                            String name = file.getName();

                            if (name.equals(spdFileName + ".old")) {
                                Log.e("X-DEBUG", "OLD FILE FOUND");
                                if (ftp.deleteFile(spdFileName)) {
                                    Log.e("X-DEBUG", "UNFINISHED UPLOAD FILE DELETED");
                                    if(ftp.rename(spdFileName + ".old", spdFileName)){
                                        Log.e("X-DEBUG", "BRM FILE RESTORED");
                                    }
                                }
                            }
                        }
                    }
                }

                ftp.logout();
                ftp.disconnect();
            } catch(IOException e) {
                error = true;
                Log.e("X-DEBUG", "EXCEPTION: "+e.getLocalizedMessage());
                e.printStackTrace();
            }

            if(error) {
                Log.e("X-DEBUG", "OPERATION FAILED");
            }else{
                setDocsStatus();
                Log.e("X-DEBUG", "OPERATION SUCCESS");
            }

            return null;
        }


    }


    private static void setDocsStatus() {
        APIClient.setStatusBRMOnFinishHandle(uid, servid, 5, Timestamp.now()).enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                if(response.isSuccessful()) {
                    Log.e("[X-DEBUG]", "Post data submitted to the API.");

                    if(Objects.requireNonNull(response.body()).getStatuscode() == 200) {
                        doneState = true;
                        Toast.makeText(mContext, "Dokumen berhasil di-Checkout", Toast.LENGTH_LONG).show();
                        Log.e("[X-DEBUG]", "[-] checkout brm result: "+response.body().getMessage());
                        ((Activity)mContext).finish();

                    }else{
                        Toast.makeText(mContext, "Dokumen gagal di-Checkout. ResponseCode:"+ApiError.parseError(response).getStatuscode()+". ResponseMessage:"+ApiError.parseError(response).getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("[X-DEBUG]", "[-] checkout brm failed. Message: "+response.raw().toString());
                    }
                }else{
                    Log.e("[X-DEBUG]", "[-] checkout brm isn't successful. ErrorCode:"+response.code()+" ErrorMessage:"+response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                Log.e("[X-DEBUG]", "Unable to submit post to the API. Error: "+t.getMessage());
                Log.e("[X-DEBUG]", "[-] checkout brm onFailure thrown. Error: "+t.getLocalizedMessage());
                Toast.makeText(mContext, "Gagal Checkout dokumen. Silahkan coba lagi", Toast.LENGTH_LONG).show();

                BaseActivity.Baseprogress.hideProgressDialog();


            }
        });
    }

    @Override
    public void onBackPressed() {

            Baselog.d("onBackPressed: BACK" + backState);
            if ((backState & BOOKMARK_STATE) != 0) {
                hideBookmark();
            } else {
                if (mSpenPageDoc.isUndoable()) {
                    alertExit(nama_file);
                } else {
                    finish();
                }
            }

    }

    private final SpenPageDoc.HistoryListener mHistoryListener = new SpenPageDoc.HistoryListener() {
        @Override
        public void onCommit(SpenPageDoc page) {

        }

        @Override
        public void onUndoable(SpenPageDoc page, boolean undoable) {
            // Enable or disable the button according to the availability of undo.
            canvasMenu.findItem(R.id.action_undo).setEnabled(undoable);
            if(!undoable){
                canvasMenu.findItem(R.id.action_undo).setIcon(R.drawable.ic_undo_disabled);
            }else{
                canvasMenu.findItem(R.id.action_undo).setIcon(R.drawable.ic_undo);
            }

            isUndoable = undoable;

            Log.e("[X-DEBUG]", "Undoable: "+undoable);
        }

        @Override
        public void onRedoable(SpenPageDoc page, boolean redoable) {
            // Enable or disable the button according to the availability of redo.
            canvasMenu.findItem(R.id.action_redo).setEnabled(redoable);
            if(!redoable){
                canvasMenu.findItem(R.id.action_redo).setIcon(R.drawable.ic_redo_disabled);
            }else{
                canvasMenu.findItem(R.id.action_redo).setIcon(R.drawable.ic_redo);
            }

            isRedoable = redoable;

            Log.e("[X-DEBUG]", "Redoable: "+redoable);
        }
    };

    private SpenTouchListener onPreTouchSurfaceViewListener = new SpenTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if(mPenSettingView.isShown()) {
                        mPenSettingView.setVisibility(SpenSurfaceView.GONE);
                    }

                    if(mEraserSettingView.isShown()) {
                        mEraserSettingView.setVisibility(SpenSurfaceView.GONE);

                        penEnable = false;

                        canvasMenu.findItem(R.id.action_eraser_tools).getIcon().setTint(Color.WHITE);
                    }

                    if(!penEnable && !eraserEnable){
                        Toast.makeText(mContext, "Pen dinonaktifkan. Nyalakan pen mode terlebih dahulu.", Toast.LENGTH_LONG).show();
                    }else{
                        saveState = false;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
            }
            return false;
        }
    };

    private final SpenColorPickerListener mColorPickerListener = new SpenColorPickerListener() {
        @Override
        public void onChanged(int color, int x, int y) {
            // Set the color from the Color Picker to the setting view.
            if (mPenSettingView != null) {
                Log.e("[X-DEBUG]","mPenSettingView isn't null");
                SpenSettingPenInfo penInfo = mPenSettingView.getInfo();
                penInfo.color = color;
                mPenSettingView.setInfo(penInfo);
                mPenSettingView.savePreferences();
            }else{
                Log.e("[X-DEBUG]","mPenSettingView is null");
            }
        }
    };

    private void initSpen(){
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        }catch (Exception e1) {
            Toast.makeText(mContext, "Tidak dapat menginisialisasi Spen",
                    Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
//            finish();
        }

        if(!isSpenFeatureEnabled){
            Toast.makeText(mContext, "Spen Tidak Ditemukan!", Toast.LENGTH_SHORT).show();
//            finish();
        }
    }

    private void initSpenView(){
        // Create Spen View
        FrameLayout spenViewContainer = findViewById(R.id.spenViewContainer1);
        RelativeLayout spenViewLayout = findViewById(R.id.spenViewLayout1);

        mPenSettingView = new SpenSettingPenLayout(mContext, "", spenViewLayout);
        mEraserSettingView = new SpenSettingRemoverLayout(getApplicationContext(), "", spenViewLayout);

        spenViewContainer.addView(mPenSettingView);
//        spenViewContainer.addView(mEraserSettingView);

        mSpenSurfaceView = new SpenView(mContext);

        mSpenSurfaceView.setToolTipEnabled(true);
        spenViewLayout.addView(mSpenSurfaceView);

        mPenSettingView.setCanvasView(mSpenSurfaceView);
        mEraserSettingView.setCanvasView(mSpenSurfaceView);

        // Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        mScreenRect = new Rect();
        display.getRectSize(mScreenRect);
        // Create SpenNoteDoc
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, mScreenRect.width(), mScreenRect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.clearHistory();
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        // Initialize Pen settings
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        mSpenSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);
        mPenSettingView.loadPreferences();
        mSpenSurfaceView.setColorPickerListener(mColorPickerListener);
        mSpenSurfaceView.setPreTouchListener(onPreTouchSurfaceViewListener);
        mSpenPageDoc.setHistoryListener(mHistoryListener);

        // Initialize Eraser settings
        SpenSettingRemoverInfo removerInfo = new SpenSettingRemoverInfo();
        removerInfo.size = 1;
        removerInfo.type = SpenSettingRemoverInfo.CUTTER_TYPE_CUT;
        mSpenSurfaceView.setRemoverSettingInfo(removerInfo);
        mEraserSettingView.setInfo(removerInfo);

        mToolType = SpenSettingViewInterface.TOOL_SPEN;

        // DISABLE SPEN
        mSpenSurfaceView.setToolTypeAction(mToolType, SpenSettingViewInterface.ACTION_NONE);
    }

    private void initBookmark(){
        bookmarkLayout = findViewById(R.id.layout_bookmark);

        bookmarkRecyvler = findViewById(R.id.recView);
        bookmarkRecyvler.setHasFixedSize(true);

        LinearLayoutManager mManager = new LinearLayoutManager(mContext);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);

        bookmarkList = new ArrayList<>();

        getBookmark();

        bookmarkAdapter = new BookmarkAdapter(bookmarkList) {
            @Override
            protected void onBindViewHolder(@NonNull BookmarkAdapter.BookmarkHolder holder, int position, @NonNull DMKBookmark model) {
                holder.itemView.setOnClickListener(view -> {
                    int page = model.getPage();
                    Toast.makeText(mContext, "Page: " + page, Toast.LENGTH_SHORT).show();
                    curentPage = page - 1;
                    mSpenPageDoc = mSpenNoteDoc.getPage(curentPage);
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    hideBookmark();
                });
                holder.bindInfo(model);
            }
        };

        bookmarkRecyvler.setLayoutManager(mManager);
        bookmarkRecyvler.setAdapter(bookmarkAdapter);
    }

    private void getBookmark(){
        Call<BookmarkResponse> bookmarkResponseCall = APIClient.getBookmark(uid, noBRM);

        bookmarkResponseCall.enqueue(new Callback<BookmarkResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookmarkResponse> call, @NonNull Response<BookmarkResponse> response) {
                if(response.isSuccessful()){
                    if(Objects.requireNonNull(response.body()).getStatuscode() == 200) {
                        if(Objects.requireNonNull(response.body()).getBookmarks().size() > 0) {
                            Log.e("[X-DEBUG]", "Bookmarks Found");
                            updateBookmarkList(Objects.requireNonNull(response.body()).getBookmarks());
                        }else {
                            Log.e("[X-DEBUG]", "Empty Bookmarks");
                            toastInfo(mContext, "Tidak ada bookmark ditemukan");
                            bookmarkList.clear();
                            bookmarkAdapter.notifyDataSetChanged();
                        }
                    }else {
                        BaseActivity.Baselog.d("Response Error " + Objects.requireNonNull(response.body()).getMessage());
                        toastErr(mContext, Objects.requireNonNull(response.body()).getMessage() );
                    }
                }else {
                    BaseActivity.Baselog.d("Response error " + response.message());
                    toastErr(mContext,  response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookmarkResponse> call, @NonNull Throwable t) {
                BaseActivity.Baselog.d("Failure " + t.getLocalizedMessage());
                toastErr(mContext, t.getLocalizedMessage());
            }
        });
    }

    private void updateBookmarkList(List<DMKBookmark> bookmarks){
        bookmarkList.clear();
        bookmarkList.addAll(bookmarks);
        bookmarkAdapter.notifyDataSetChanged();
    }

    private void initRetrofit(){
        Baselog.d("Initretrofit");
        APIClient = APIUtils.getAPIService(mContext);
    }

    private void hideBookmark(){
        bookmarkToggle.getIcon().setTint(Color.WHITE);
        backState ^= BOOKMARK_STATE;
        bookmarkLayout.animate()
                .translationY(-bookmarkLayout.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        bookmarkLayout.setVisibility(View.GONE);
                    }
                });
    }

    private void showBookmark(){
        backState |= BOOKMARK_STATE;
        bookmarkToggle.getIcon().setTint(getResources().getColor(R.color.colorPrimaryDark));
        bookmarkLayout.setVisibility(View.VISIBLE);
        // Start the animation
        bookmarkLayout.animate().translationY(0).setListener(null);

    }

    private static void updateRejectedNoteStatuses(boolean isClose) {
        ProgressDialog pDlgs;
        pDlgs = new ProgressDialog(mContext);
        pDlgs.setMessage("Sedang mengubah status dokumen ...");
        pDlgs.setIndeterminate(true);
        pDlgs.setCancelable(false);
        pDlgs.show();

        StringBuilder idNotes = new StringBuilder("[");

        int i = 0;
        for(Integer key : resultFromDialog.keySet()){
            idNotes.append(key);

            if(i++ == resultFromDialog.size()-1){
                idNotes.append("]");
            }else{
                idNotes.append(",");
            }
        }

        try {
            Call<ApiStatus> updateDataCall = APIClient.updateNoteRNoteStatusWithArray(uid, true, idNotes.toString(), 1);

            updateDataCall.enqueue(new Callback<ApiStatus>(){
                @Override
                public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                    if (response.isSuccessful()) {
                        Log.e("[X-DEBUG]", "Code:"+response.code()+" Result:"+response.message());
                        Toast.makeText(mContext, "BRM berhasil diperbaiki", Toast.LENGTH_SHORT).show();

                        pDlgs.dismiss();
                        if (isClose) {
                            ((Activity) mContext).finish();
                        }
                    } else {
                        pDlgs.dismiss();

                        try {
                            Log.e("[X-DEBUG]", "Cannot submit post data. Response:" + response.errorBody().string() + ". Raw response:" + response.raw().toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                    Log.e("[X-DEBUG]", "Unable to update Rejected Notes status. Error: " + t.getMessage());
                    Toast.makeText(mContext, "Gagal Checkout dokumen. Silahkan coba lagi", Toast.LENGTH_LONG).show();

                    pDlgs.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class saveDocument extends AsyncTask<String, Boolean, Boolean> {
        ProgressDialog pDlg;
        boolean isClose, isUploadClose, isRNoteStatusClose, withUpload, withRNoteStatusUpdate;

        private saveDocument(boolean isClose, boolean isUploadClose, boolean isRNoteStatusClose, boolean withUpload, boolean withRNoteStatusUpdate, ProgressDialog pDlg) {
            this.isClose = isClose;
            this.isUploadClose = isUploadClose;
            this.isRNoteStatusClose = isRNoteStatusClose;
            this.withUpload = withUpload;
            this.withRNoteStatusUpdate = withRNoteStatusUpdate;
            this.pDlg = pDlg;
        }

        @Override
        protected void onPreExecute() {
            BaseActivity.Baseprogress.showProgressDialog(mContext, "Initiating Save Document...");
            if(pDlg==null){
                pDlg = new ProgressDialog(mContext);
            }

            pDlg.setMessage("Sedang menyimpan dokumen ...");
            pDlg.setIndeterminate(true);
            pDlg.setCancelable(false);
            pDlg.show();
        }

        @Override
        protected Boolean doInBackground(String... args) {
            String saveFilePath = args[0];
            boolean rtrn;

            try {
                // Save NoteDoc
                mSpenNoteDoc.save(saveFilePath, false);

                File f = new File(saveFilePath);
                if(f.exists()) {
                    log.x("FILE DOCUMENT SAVED");
                    rtrn = true;
                }else{
                    log.x("FAILED TO SAVE DOCUMENT");
                    rtrn = false;
                }
            } catch (Exception e) {
                e.printStackTrace();

                rtrn = false;
            }

            return rtrn;
        }

        @Override
        protected void onPostExecute(Boolean arg){
            if (arg) {
                saveState = true;

                String pngOutPath = mContext.getFilesDir().getAbsolutePath() + "/archives/";
                String noteFilename = nama_file;

                //export to pdf
                File filePath = new File(pngOutPath);
                if (!filePath.exists()) {
                    if (!filePath.mkdirs()) {
                        Log.e("[X-DEBUG]", "Failed to creating dirs on "+pngOutPath);
                        pDlg.dismiss();
                        return;
                    }
                }

                try {
                    boolean x = new savePage(false, false, null).execute(pngOutPath+"/"+noteFilename, String.valueOf(curentPage)).get();

                    if (x) {
                        boolean comp = new compressImages(isClose, isUploadClose, isRNoteStatusClose, false, withRNoteStatusUpdate, false, false, null).execute(pngOutPath+"/"+kode_file+"/"+noteFilename).get();

                        if(comp){
                            pDlg.dismiss();
                            Log.e("[X-DEBUG]", "PNG Saving Success");
                            Baselog.d("PNG Saving Success");
//                            mSpenPageDoc = mSpenNoteDoc.getPage(curentPage);
//                            mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
//
                            if(withUpload) {
                                uploadFile(kode_file, isUploadClose, isRNoteStatusClose, withRNoteStatusUpdate, null);
                            }
                        }else{
                            Log.e("[X-DEBUG]", "PNG Saving Fail");
                            return;
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    pDlg.dismiss();
                    Log.e("[X-DEBUG]", "PNG Saving Exception Thrown. Error: "+e.getLocalizedMessage());
                    return;
                }

                if(isClose){
                    Intent resInt = new Intent();
                    resInt.putExtra("isSaved", true);
                    ((Activity)mContext).setResult(RESULT_OK, resInt);
                    ((Activity)mContext).finish();
                }
//                uploadFile(saveFilePath);
            } else {
                saveState = false;
                BaseActivity.Baseprogress.hideProgressDialog();
                Toast.makeText(mContext, "Gagal menyimpan dokumen. Silahkan coba lagi", Toast.LENGTH_LONG).show();
            }

//            Toast.makeText(mContext, "Documents Saved!", Toast.LENGTH_LONG).show();

        }

    }

    private static class savePage extends AsyncTask<String, Boolean, Boolean> {


        ProgressDialog pDlg;
        int curPage = 0;
        String pngExp = "";
        boolean isFileChecker, withProgress;

        Logger log = new Logger();

        savePage(boolean isFileChecker, boolean withProgress, ProgressDialog pDlg){
            this.isFileChecker = isFileChecker;
            this.pDlg = pDlg;
            this.withProgress = withProgress;
        }

        @Override
        protected void onPreExecute() {

            BaseActivity.Baseprogress.showProgressDialog(mContext, "Initiating Save ...");
            log.x("{savePage} onPreExecute");
            if(pDlg==null){
//                pDlg = new ProgressDialog(mContext);
            }

            if(!isFileChecker && withProgress){
//                pDlg.setMessage("Loading ...");
//                pDlg.setIndeterminate(true);
//                pDlg.setCancelable(false);
//                pDlg.show();

                loadingPanel.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(String... args) {
            log.x("{savePage} doInBackground");
            String saveFilePath = args[0];
            curPage = Integer.parseInt(args[1]);
            boolean rtrn = false;

            try {
                // Save NoteDoc
                if(!isFileChecker) {
                    mSpenNoteDoc.save(saveFilePath, false);
                }

                rtrn = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return rtrn;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Boolean args){
            log.x("{savePage} onPostExecute");

            if(args) {
                String pngOutPath = mContext.getFilesDir().getAbsolutePath() + "/" + "archives/" + kode_file;
                String noteFilename = kode_file + "_" + (curPage + 1) + ".spd";

                //export to pdf
                File filePath = new File(pngOutPath);
                if (!filePath.exists()) {
                    if (!filePath.mkdirs()) {
                        Log.e("[X-DEBUG]", "Failed to creating dirs on " + pngOutPath);
//                        if(pDlg.isShowing()){
//                            pDlg.dismiss();
//                        }

                        loadingPanel.setVisibility(View.GONE);

                        return;
                    }
                }

                try {
                    if(isFileChecker) {
                        mSpenPageDoc = mSpenNoteDoc.getPage(curPage);
                        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    }

                    pngExp = new PNGExport(mContext, mView, mSpenSurfaceView, mSpenPageDoc, mSpenNoteDoc, uid, noBRM, totalPage, withProgress, pDlg).execute(pngOutPath, noteFilename, String.valueOf(curPage)).get();
                } catch (ExecutionException | InterruptedException e) {
                    log.x("Error: "+e.getLocalizedMessage());
                }

                if (pngExp.equals("true")) {
                    Log.e("[X-DEBUG]", "PNG Saving Success");

                    if(!isFileChecker) {
                        mSpenPageDoc = mSpenNoteDoc.getPage(curentPage);
                        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    }
                } else {
                    Log.e("[X-DEBUG]", "PNG Saving Fail");
                    Toast.makeText(mContext, "Error while trying save document image", Toast.LENGTH_LONG).show();
                }

                Toast.makeText(mContext, "Page " + (curentPage + 1), Toast.LENGTH_SHORT).show();
                txtPgIndicator.setText((curentPage+1)+" of "+totalPage);
            }else{
                log.x("Error while trying save spen document");
                BaseActivity.Baseprogress.hideProgressDialog();
            }

//            if(pDlg.isShowing()){
//                pDlg.dismiss();
//            }

            loadingPanel.setVisibility(View.GONE);
        }
    }

    private static class compressImages extends AsyncTask<String, Boolean, Boolean> {
        ProgressDialog pDlg;
        boolean isClose, isUploadClose, isRNoteStatusClose, withUpload, withRNoteStatusUpdate, withSNoteSave, withProgress;

        Logger log = new Logger();

        private compressImages(boolean isClose, boolean isUploadClose, boolean isRNoteStatusClose, boolean withUpload, boolean withRNoteStatusUpdate, boolean withSNoteSave, boolean withProgress, ProgressDialog pDlg) {
            this.isClose = isClose;
            this.isUploadClose = isUploadClose;
            this.isRNoteStatusClose = isRNoteStatusClose;
            this.withUpload = withUpload;
            this.withRNoteStatusUpdate = withRNoteStatusUpdate;
            this.withSNoteSave = withSNoteSave;
            this.pDlg = pDlg;
            this.withProgress = withProgress;
        }

        @Override
        protected void onPreExecute() {
            log.x("{compressImages} onPreExecute");
            if(pDlg==null){
                pDlg = new ProgressDialog(mContext);
            }

            if(withProgress) {
                pDlg.setMessage("Sedang mengkrompres berkas konten ...");
                pDlg.setIndeterminate(true);
                pDlg.setCancelable(false);
                pDlg.show();
            }
        }

        @Override
        protected Boolean doInBackground(String... args) {
            log.x("{compressImages} doInBackground");
            String saveFilePath = args[0];
            boolean rtrn = false;

            try {
                if(withSNoteSave) {
                    mSpenNoteDoc.save(saveFilePath, false);
                }
                rtrn = true;
            } catch (IOException e) {
                log.x("Error: "+e.getLocalizedMessage());
            }

            return rtrn;
        }

        @Override
        protected void onPostExecute(Boolean arg){
            log.x("{compressImages} onPostExecute");
            if (arg) {
                saveState = true;

                String archiveOutPath = mContext.getFilesDir().getAbsolutePath() + "/archives/";
                String pngOutPath = archiveOutPath + kode_file;

                //compress files
                File filePath = new File(pngOutPath + "/compressed");
                if (!filePath.exists()) {
                    if (!filePath.mkdirs()) {
                        Log.e("[X-DEBUG]", "Failed to creating dirs on "+pngOutPath);
                        if(pDlg.isShowing()){
                            pDlg.dismiss();
                        }
                        return ;
                    }
                }

                CompressUtils fileCompress;

                try {
                    fileCompress = new CompressUtils();

                    File file = new File(pngOutPath);
                    String[] filesArray = file.list();
                    ArrayList<String> fullPathFiles = new ArrayList<>();

                    for(String imgFile : filesArray) {
                        fullPathFiles.add(pngOutPath + "/" + imgFile);
                    }

                    checkAllCapturedFiles(totalPage, archiveOutPath, kode_file);

                    if(fileCompress.zip(mContext, fullPathFiles, filePath.getAbsolutePath(), kode_file+".zip")){
                        Log.e("[X-DEBUG]", "Image Files Compressed Successfully");
                        BaseActivity.Baseprogress.hideProgressDialog();

                        if(withUpload) {
                            if(pDlg.isShowing()){
                                pDlg.dismiss();
                            }

                            uploadFile(kode_file, isUploadClose, isRNoteStatusClose, withRNoteStatusUpdate, pDlg);
                        }
                    }else{
                        saveState = false;
                        if(pDlg.isShowing()){
                            pDlg.dismiss();
                        }
                        Log.e("[X-DEBUG]", "Files Compression Failed");
                        return;
                    }
                } catch (Exception e) {
                    saveState = false;
                    if(pDlg.isShowing()){
                        pDlg.dismiss();
                    }
                    Log.e("[X-DEBUG]", "File Compression Exception Thrown. Error: "+e.getLocalizedMessage());
                    return;
                }

                Toast.makeText(mContext, "Dokumen berhasil disimpan", Toast.LENGTH_LONG).show();

                if(isClose){
                    Intent resInt = new Intent();
                    resInt.putExtra("isSaved", true);
                    ((Activity)mContext).setResult(RESULT_OK, resInt);
                    ((Activity)mContext).finish();
                }
//                uploadFile(saveFilePath);
            } else {
                saveState = false;
                Toast.makeText(mContext, "Gagal menyimpan dokumen. Silahkan coba lagi", Toast.LENGTH_LONG).show();
            }

//            Toast.makeText(mContext, "Documents Saved!", Toast.LENGTH_LONG).show();
            if(pDlg.isShowing()){
                pDlg.dismiss();
            }


        }

    }

    private static void checkAllCapturedFiles(int totalPage, String filePath, String kode_file) {
        ArrayList<String> fileArray = new ArrayList<>();

        log.x("FILE PATH: "+filePath+"/"+kode_file);
        log.x("TOTAL PAGE: "+totalPage);

        File files = new File(filePath+"/"+kode_file);
        String[] arrayFiles = files.list();

        for(String file: arrayFiles){
            File f = new File(filePath+"/"+kode_file+"/"+file);

            if(f.isFile()) {
                fileArray.add(file);
            }
        }

        log.printr(fileArray);

        for(int i=0; i<totalPage; i++){
            if(!fileArray.contains(kode_file+"_"+(i+1)+".png")){
                try {
                    boolean x = new savePage(true, false, null).execute(filePath, String.valueOf(i)).get();

                    if(x){
                        log.x("Page "+(i+1)+" captured successfully");
                    }else{
                        log.x("Error: Page image capture failed");
                    }
                } catch (ExecutionException | InterruptedException e) {
                    log.x("Error: "+e.getLocalizedMessage());
                }
            }else{
                log.x("File "+kode_file+"_"+(i+1)+".png"+" is exist");
            }

        }
    }

    private static void uploadFile(final String filename, boolean isClose, boolean isRNoteStatusClose, boolean withRNoteStatusUpdate, ProgressDialog progressDialog) {

        //TODO: upload file ke menu sync sebelum logout
        //TODO: ketika sync langsung upload ke server lalu delete
        //TODO: file protocol ftp
        //TODO: variable protocol http

        //TODO: ini 1
        File fFilePath = new File(mContext.getFilesDir() + "/archives");
        if (!fFilePath.exists()) {
            if (!fFilePath.mkdirs()) {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        //TODO: ini 2
        final String saveFilePath = fFilePath.getPath() + '/' + filename + ".spd";

        Log.e("[X-DEBUG]", "upload file path: " + saveFilePath);
        Log.e("[X-DEBUG]", "Size upload file path: " + saveFilePath.length());
        long FileSizeInbyte = saveFilePath.length();
        long FileSizeInKB = FileSizeInbyte / 1024;
        long FileSizeInMB = FileSizeInKB / 2024;


        ProgressDialog pDlgs;

        if(progressDialog==null){
            pDlgs = new ProgressDialog(mContext);
        }else{
            pDlgs = progressDialog;
        }
        //TODO: ini 3
        File file = new File(saveFilePath);

        ProgressRequestBody reqFileSPD = new ProgressRequestBody(file, "*/*", new ProgressRequestBody.UploadCallbacks(){

            @Override
            public void onProgressUpdate(int percentage) {
                log.x("DOC UPLOAD PROGRESS -> "+percentage+"%");

                pDlgs.setIndeterminate(false);
                pDlgs.setCancelable(false);
                pDlgs.setMax(100);
                pDlgs.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDlgs.setMessage("Sedang mengupload Berkas RM... (1/2)");
                pDlgs.setProgress(percentage);
                pDlgs.show();
            }

            @Override
            public void onError() {
                pDlgs.dismiss();
                Toast.makeText(mContext, "Something went wrong while trying to upload file", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinish() {
                pDlgs.dismiss();
                Toast.makeText(mContext, "1 of 2 files uploaded successfully", Toast.LENGTH_LONG).show();
            }
        });

        Log.e("params", "UID: " + uid + "; UnitForBRMDetail: " + idPoli + "; BRM: " + noBRM + "; File: " + filename + ".spd" + "; SaveFilePath: " + file.getAbsolutePath());

        try {
            APIClient.uploadDMR(brmid, uid, (int) reqFileSPD.contentLength(), reqFileSPD).enqueue(new Callback<ApiStatus>() {
                @Override
                public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                    if (response.isSuccessful()) {
                        Log.e("[X-DEBUG]", "Post data submitted to the API.");

                        if (Objects.requireNonNull(response.body()).getStatuscode() == 200) {
                            Log.e("[X-DEBUG]", "[-] Upload response code:" + response.body().getStatuscode() + "; response message:" + response.body().getMessage());

//                            if(mode == ACTION_MODE) {
//                                setDocsStatus();
//                            }

                            //UPLOAD ZIP FILE
                            //TODO: ini 4
                            final String zipSaveFilePath = fFilePath.getPath() + '/' + kode_file + "/compressed/" + filename + ".zip";
                            File zipFile = new File(zipSaveFilePath);

                            Log.e(TAG, "Tes Besar FIle: "+zipFile.getTotalSpace());

                            ProgressRequestBody reqFileZIP = new ProgressRequestBody(zipFile, "application/zip", new ProgressRequestBody.UploadCallbacks(){

                                @Override
                                public void onProgressUpdate(int percentage) {
                                    log.x("ZIP UPLOAD PROGRESS -> "+percentage+"%");

                                    pDlgs.setIndeterminate(false);
                                    pDlgs.setCancelable(false);
                                    pDlgs.setMax(100);
                                    pDlgs.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    pDlgs.setMessage("Sedang mengupload konten... (2/2)");
                                    pDlgs.setProgress(percentage);
                                    pDlgs.show();
                                }

                                @Override
                                public void onError() {
                                    pDlgs.dismiss();
                                    Toast.makeText(mContext, "Something went wrong while trying to upload file", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFinish() {
                                    pDlgs.dismiss();
                                    Toast.makeText(mContext, "2 of 2 files uploaded successfully", Toast.LENGTH_LONG).show();
                                }
                            });

                            Log.e("[X-DEBUG]", "UID: " + uid + "; UnitForBRMDetail: " + idPoli + "; BRM: " + noBRM + "; File: " + filename + ".zip" + "; SaveFilePath: " + zipFile.getAbsolutePath());

                            //TODO: dipindah ketika menu export di klik
                            try {
                                APIClient.uploadDMRZIP(brmid, uid, (int) reqFileZIP.contentLength(), 1, reqFileZIP).enqueue(new Callback<ApiStatus>() {
                                    @Override
                                    public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                                        if (response.isSuccessful()) {
                                            Log.e("[X-DEBUG]", "Post data submitted to the API.");

                                            if (Objects.requireNonNull(response.body()).getStatuscode() == 200) {
                                                Log.e("[X-DEBUG]", "[-] Upload response code:" + response.body().getStatuscode() + "; response message:" + response.body().getMessage());
                                                if(mode == ACTION_MODE) {
                                                    //TODO: ini 5
                                                    //setDocsStatus();
                                                }

                                                deleteTempFile(saveFilePath);

                                                if(mode == FIX_MODE && withRNoteStatusUpdate) {
                                                    BRMRejectStatusUpdate setRejectStatusSync = new BRMRejectStatusUpdate(mContext, DetailVisitor.DMR_REVIEWED_BY_DOCTOR);
                                                    setRejectStatusSync.setOnCompleteListener(s -> {
                                                        if (Objects.requireNonNull(s).getStatuscode() == 200) {
                                                            Log.e("[X-DEBUG]", "Status data patched to the API successfully.");
                                                        } else {
                                                            Log.e("[X-DEBUG]", "Failed to patch status data to the API. Code:" + s.getStatuscode() + " Error:" + s.getMessage());
                                                        }
                                                    });
                                                    setRejectStatusSync.execute();
                                                }

                                                pDlgs.dismiss();
                                                //TODO:close
                                                if (isClose) {
                                                    ((Activity) mContext).finish();
                                                }

                                                if(withRNoteStatusUpdate){
                                                    updateRejectedNoteStatuses(isRNoteStatusClose);
                                                }

                                                endEditSession();
                                                purgeAllFiles(ARCHIVE_FILE);
                                            } else {
                                                Log.e("[X-DEBUG]", "[-] Upload response code:" + response.body().getStatuscode() + "; response message:" + response.body().getMessage());
                                                pDlgs.dismiss();
                                                Toast.makeText(mContext, "Dokumen gagal di-Checkout. " + Objects.requireNonNull(response.body()).getMessage(), Toast.LENGTH_LONG).show();

//                                                endEditSession();
//                                                purgeAllFiles(ARCHIVE_FILE);
                                            }
                                        } else {
                                            pDlgs.dismiss();

//                                            endEditSession();
//                                            purgeAllFiles(ARCHIVE_FILE);
                                            try {
                                                Log.e("[X-DEBUG]", "Cannot submit post data. Response:" + response.errorBody().string() + ". Raw response:" + response.raw().toString());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                                        Log.e("[X-DEBUG]", "Unable to submit ZIP File to the API. Error: " + t.getMessage());
//                                        deleteTempFile(saveFilePath);
                                        Toast.makeText(mContext, "Gagal Checkout dokumen. Silahkan coba lagi", Toast.LENGTH_LONG).show();

                                        pDlgs.dismiss();

//                                        endEditSession();
//                                        purgeAllFiles(ARCHIVE_FILE);
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

//                            deleteTempFile(saveFilePath);
                        } else {
                            Log.e("[X-DEBUG]", "[-] Upload response code:" + response.body().getStatuscode() + "; response message:" + response.body().getMessage());
                            pDlgs.dismiss();
                            Toast.makeText(mContext, "Dokumen gagal di-Checkout. " + Objects.requireNonNull(response.body()).getMessage(), Toast.LENGTH_LONG).show();

//                            endEditSession();
//                            purgeAllFiles(ARCHIVE_FILE);
                        }
                    } else {
                        pDlgs.dismiss();

//                        endEditSession();
//                        purgeAllFiles(ARCHIVE_FILE);
                        try {
                            Log.e("[X-DEBUG]", "Cannot submit post data. Response:" + response.errorBody().string() + ". Raw response:" + response.raw().toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                    Log.e("[X-DEBUG]", "Unable to submit SPD File to the API. Error: " + t.getMessage());
                    deleteTempFile(saveFilePath);
                    Toast.makeText(mContext, "Gagal Checkout dokumen. Silahkan coba lagi", Toast.LENGTH_LONG).show();

                    pDlgs.dismiss();

//                    endEditSession();
//                    purgeAllFiles(ARCHIVE_FILE);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();

//            endEditSession();
//            purgeAllFiles(ARCHIVE_FILE);
        }
    }

    private static void deleteTempFile(String filePath) {
        File localFile = new File(filePath);

        if (localFile.delete()) {
            Log.v("[X-DEBUG]", "Local save file has been deleted");
            purgeAllFiles(new File(ARCHIVE_PATH+"/"+kode_file));
        } else {
            Log.e("[X-DEBUG]", "Cannot delete local save file");
        }
    }

    private static void purgeAllFiles(File rootParentDir){
//        File dir = new File(mContext.getFilesDir()+"/archives/");
        if(rootParentDir.exists()){
            if(rootParentDir.isDirectory()){
                String[] children = rootParentDir.list();

                if(Objects.requireNonNull(children).length>0) {
                    for (String child : children) {
                        File f = new File(rootParentDir, child);

                        if (f.isDirectory()) {
                            purgeAllFiles(f);
                        }else{
                            if (f.delete()) {
                                log.x("File on " + f.getAbsolutePath() + " has been purged");
                            } else {
                                log.x("Failed to purge file on " + f.getAbsolutePath());
                            }
                        }
                    }
                }
            }

            if (rootParentDir.delete()) {
                log.x("File on " + rootParentDir.getAbsolutePath() + " has been purged");
            } else {
                log.x("Failed to purge file on " + rootParentDir.getAbsolutePath());
            }
        }else{
            log.x("Failed to purge file on " + rootParentDir.getAbsolutePath()+". File not found.");
        }
    }

    private static void purgeFile(String filepath){
        File f = new File(filepath);
        if(f.isDirectory()) {
            String[] children = f.list();
            for (String aChildren : children) {
                boolean status = new File(f, aChildren).delete();
                if (status) {
                    Baselog.d("File " + aChildren + " has been purged");
                } else {
                    Baselog.d("Failed to shred file " + aChildren);
                }
            }

            if(f.delete()){
                Baselog.d("Parent directory '"+filepath+"' has been purged");
            }else{
                Baselog.d("Failed to delete parent directory '"+filepath+"'");
            }
        }else{
            if(f.delete()){
                Baselog.d("File '"+filepath+"' has been purged");
            }else{
                Baselog.d("Failed to delete file '"+filepath+"'");
            }
        }


    }

    private void alertExit(final String fileName){
        if(saveState) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext)
                    .setTitle("Konfirmasi Keluar")
                    .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                    .setMessage("Pekerjaan anda sudah disimpan, namun BRM masih belum di-Checkout. Yakin ingin keluar ?")
                    .setPositiveButton("Checkout BRM", (dialogInterface, i) -> {
                        // Set the save directory for the file.
                        File filePath = new File(mContext.getFilesDir() + "/archives/");
                        if (!filePath.exists()) {
                            if (!filePath.mkdirs()) {
                                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        if (!fileName.equals("")) {
                            uploadFile(kode_file, true, false, false, null);
//                            editor = pref.edit();
//                            editor.putString("activeBRM", null);
//                            editor.apply();
                        } else {
                            Toast.makeText(mContext, "Invalid filename !!!", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNeutralButton("Hapus Perubahan Dan Keluar", (dialogInterface, i) -> {
                        endEditSession();
                        purgeAllFiles(ARCHIVE_FILE);

                        if(mode == ACTION_MODE) {
                            restoreBRMdata();
                        }else if(mode == FIX_MODE){
                            finish();
                        }
                    });

            if(mode == ACTION_MODE) {
                dialog.setNegativeButton("Lanjutkan Nanti", (dialogInterface, i) -> {
                    endEditSession();
//                        purgeAllFiles();
//                        restoreBRMdata();
                    finish();
                });
            }

            dialog.create();
            dialog.show();
        }else{
            AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle("Konfirmasi Keluar")
                    .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                    .setMessage("Semua pekerjaan anda akan hilang jika belum disimpan, Yakin ingin keluar ?")
                    .setPositiveButton("Simpan", (dialogInterface, i) -> {
                        // Set the save directory for the file.
                        File filePath = new File(mContext.getFilesDir() + "/archives/");
                        if (!filePath.exists()) {
                            if (!filePath.mkdirs()) {
                                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        String saveFilePath = filePath.getPath() + '/';

                        if (!fileName.equals("")) {
                            saveFilePath += fileName;
                            new saveDocument(true, false, false, false, false, null).execute(saveFilePath);
//                            editor = pref.edit();
//                            editor.putString("activeBRM", noBRM);
//                            editor.apply();
                        } else {
                            Toast.makeText(mContext, "Invalid filename !!!", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNeutralButton("Batal", null)
                    .setNegativeButton("Keluar", (dialogInterface, i) -> {
                        endEditSession();
                        purgeAllFiles(ARCHIVE_FILE);
                        if(mode == ACTION_MODE) {
                            if(statusRM != 5) {
                                restoreBRMdata();
                            }else {
                                restoreBRMdataOnReject();
                            }
                        }else if(mode == FIX_MODE){
                            finish();
                        }
                    })
                    .create();

            dialog.show();
        }
    }

    private static void endEditSession() {
        editor = pref.edit();
        editor.putString("activeBRM", null);
        editor.putInt("activeBerkas", 0);
        editor.putInt("activeServ", 0);
        editor.putInt("mode", 0);
        editor.apply();
    }

    private static class BRMRejectStatusUpdate extends AsyncTask<String, String, ApiStatus>{
        private Context ctx;
        private int statusCode;

        public interface OnCompleteListener{
            void onComplete(ApiStatus s);
        }

        OnCompleteListener mListener;

        BRMRejectStatusUpdate(Context context, int code){
            this.ctx = context;
            this.statusCode = code;
        }

        void setOnCompleteListener(OnCompleteListener listener){
            mListener = listener;
        }

        @Override
        protected void onCancelled(ApiStatus s) {
            super.onCancelled(s);

            Log.e("[X-DEBUG]", "request update BRM rejected status canceled");
        }

        @Override
        protected void onPreExecute(){
            Log.e("[X-DEBUG]", "request update BRM rejected status onPreExe");
        }

        @Override
        protected ApiStatus doInBackground(String... strings) {
            Log.e("[X-DEBUG]", "request update BRM rejected status onDoInBg");
            ApiStatus apiStatus;
            try {
                publishProgress("Mengupdate Status BRM ...");
                Call<ApiStatus> callAPI = APIClient.setBRMRejectStatus(uid, servid, statusCode);
                Log.d(TAG, "doInBackground: " + callAPI.request().toString());
                Response<ApiStatus> response = callAPI.execute();

                if(response.isSuccessful()){
                    apiStatus = response.body();
                }else{
                    cancel(true);
                    Log.e("[X-DEBUG]", "request update BRM rejected status isn't successful");
                    return null;
                }
            }catch (IOException e){
                cancel(true);
                Log.e("[X-DEBUG]", "Response JSON error while updating BRM rejected status");
                return null;
            }

            return apiStatus;
        }

        @Override
        protected void onPostExecute(ApiStatus s) {
            super.onPostExecute(s);
            mListener.onComplete(s);
            Log.e("[X-DEBUG]", "request update BRM rejected status onPostExe");
        }
    }

    private static void restoreBRMdata(){
        Log.e("[X-DEBUG]", "restoreBRMdata function is running");

        Call<ApiStatus> updateDataCall = APIClient.resetStatusBRM(uid, servid, 3, 0, "NULL", "NULL");

        updateDataCall.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                if(response.isSuccessful()){
                    if (Objects.requireNonNull(response.body()).getStatuscode() == 400) {
                        Log.e("[X-DEBUG]", "[-] Restore response " + Objects.requireNonNull(response.body()).getMessage());
                    }else{
                        Log.e("[X-DEBUG]", "[-] Restore response code: " + Objects.requireNonNull(response.body()).getStatuscode() + "; response msg: " + Objects.requireNonNull(response.body()).getMessage());
                    }
                }else {
                    Log.e("[X-DEBUG]", "[-] Restore response isn't successful. Error: " + ApiError.parseError(response).getMessage());
                }
                BaseActivity.Baseprogress.hideProgressDialog();

                Intent returnIntent = new Intent();
                ((Activity)mContext).setResult(Activity.RESULT_CANCELED, returnIntent);
                ((Activity)mContext).finish();
            }

            @Override
            public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                Log.e("[X-DEBUG]", "[-] Restore response onFailure thrown " + t.getLocalizedMessage());
                BaseActivity.Baseprogress.hideProgressDialog();
                Toast.makeText(mContext, "Gagal mengembalikan data: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//                ((Activity)mContext).finish();
            }
        });
    }
    private static void restoreBRMdataOnReject(){
        Log.e("[X-DEBUG]", "restoreBRMdata function is running");

        Call<ApiStatus> updateDataCall = APIClient.resetStatusBRM(uid, servid, 5, 0, "NULL", "NULL");

        updateDataCall.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                if(response.isSuccessful()){
                    if (Objects.requireNonNull(response.body()).getStatuscode() == 400) {
                        Log.e("[X-DEBUG]", "[-] Restore response " + Objects.requireNonNull(response.body()).getMessage());
                    }else{
                        Log.e("[X-DEBUG]", "[-] Restore response code: " + Objects.requireNonNull(response.body()).getStatuscode() + "; response msg: " + Objects.requireNonNull(response.body()).getMessage());
                    }
                }else {
                    Log.e("[X-DEBUG]", "[-] Restore response isn't successful. Error: " + ApiError.parseError(response).getMessage());
                }
                BaseActivity.Baseprogress.hideProgressDialog();

                Intent returnIntent = new Intent();
                ((Activity)mContext).setResult(Activity.RESULT_CANCELED, returnIntent);
                ((Activity)mContext).finish();
            }

            @Override
            public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                Log.e("[X-DEBUG]", "[-] Restore response onFailure thrown " + t.getLocalizedMessage());
                BaseActivity.Baseprogress.hideProgressDialog();
                Toast.makeText(mContext, "Gagal mengembalikan data: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//                ((Activity)mContext).finish();
            }
        });
    }

    private boolean saveNoteDlg(View vw, final String fileName, final boolean isClose){
        // Prompt Save File dialog to get the file name
        // and get its save format option (note file or image).
        String btnPosTitle;

        if(isClose){
            btnPosTitle = "Simpan dan Keluar";
        }else{
            btnPosTitle = "Simpan";
        }

        AlertDialog.Builder builderSave = new AlertDialog.Builder(mContext);
        builderSave.setTitle("Simpan Dokumen");
        builderSave.setMessage("Anda yakin ingin menyimpan dokumen ini ?");
        builderSave.setPositiveButton(btnPosTitle, (dialog, which) -> {
            // Set the save directory for the file.
            File filePath = new File(mContext.getFilesDir() + "/archives/");
            if (!filePath.exists()) {
                if (!filePath.mkdirs()) {
                    Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            String saveFilePath = filePath.getPath() + '/';

            if (!fileName.equals("")) {
                saveFilePath += fileName;
                new saveDocument(isClose, false, false, false, false, null).execute(saveFilePath);
                endEditSession();
            } else {
                Toast.makeText(mContext, "Error. Invalid filename", Toast.LENGTH_LONG).show();
            }
        });
        builderSave.setNegativeButton("Batal", (dialog, which) -> {

        });
        AlertDialog dlgSave = builderSave.create();
        dlgSave.show();
        return true;
    }

    private boolean saveNoteDlgV2(View vw, final String fileName, final boolean isClose, boolean isSaved) {
        // Prompt Save File dialog to get the file name
        // and get its save format option (note file or image).
        String btnPosTitle;

        if (isClose) {
            btnPosTitle = "Simpan dan Keluar";
        } else {
            btnPosTitle = "Simpan";
        }

        if (!isSaved){
            AlertDialog.Builder builderSave = new AlertDialog.Builder(mContext);
            builderSave.setTitle("Simpan Dokumen");
            builderSave.setMessage("Anda yakin ingin menyimpan dokumen ini ?");
            builderSave.setPositiveButton(btnPosTitle, (dialog, which) -> {
                // Set the save directory for the file.
                File filePath = new File(mContext.getFilesDir() + "/archives");
                if (!filePath.exists()) {
                    if (!filePath.mkdirs()) {
                        Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                String saveFilePath = filePath.getPath() + '/';

                if (!fileName.equals("")) {
                    saveFilePath += fileName;
                    try {
                        boolean x = new savePage(false, false, null).execute(saveFilePath, String.valueOf(curentPage)).get();

                        if (x) {
                            new compressImages(isClose, false, false, false, false, true, false,null).execute(saveFilePath);
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        log.x("Error: " + e.getLocalizedMessage());
                    }
                    endEditSession();
                } else {
                    Toast.makeText(mContext, "Error. Invalid filename", Toast.LENGTH_LONG).show();
                }
            });
            builderSave.setNegativeButton("Batal", null);
            AlertDialog dlgSave = builderSave.create();
            dlgSave.show();
        }else{
            File filePath = new File(mContext.getFilesDir() + "/archives");
            if (!filePath.exists()) {
                if (!filePath.mkdirs()) {
                    Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            String saveFilePath = filePath.getPath() + '/';

            if (!fileName.equals("")) {
                saveFilePath += fileName;
                try {
                    boolean x = new savePage(false, false, null).execute(saveFilePath, String.valueOf(curentPage)).get();
                    Log.e("X-DEBUUG","save page");
                    if (x) {
                        new compressImages(isClose, false, false, false, false, true, false,null).execute(saveFilePath);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    log.x("Error: " + e.getLocalizedMessage());
                }
                endEditSession();
            } else {
                Toast.makeText(mContext, "Error. Invalid filename", Toast.LENGTH_LONG).show();
            }
        }
        Check ++;
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("[X-DEBUG]", "onDestroy triggered");

        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }

        if(mSpenNoteDoc != null) {
            try {
                mSpenNoteDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }

        resultFromDialog.clear();
    }

    public static void onFinishUserDialog(HashMap<Integer, Integer> data) {
        isSaveDialog = true;
        resultFromDialog = data;

        Log.e("[X-DEBUG]", "HashMap Size:"+resultFromDialog.size());
        Log.e("[X-DEBUG]", "HashMap content:"+resultFromDialog.toString());
    }

    private void showLoadingBlock(){
        loadingPanel.setVisibility(View.VISIBLE);
    }

    private void hideLoadingBlock(){
        loadingPanel.setVisibility(View.GONE);
    }
}
