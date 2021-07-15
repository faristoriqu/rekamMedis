package com.sabin.digitalrm.prm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.DoctorMainActivity;
import com.sabin.digitalrm.ProgressRequestBody;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.BookmarkAdapter;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.helpers.DefaultDialog;
import com.sabin.digitalrm.helpers.FileDownloader;
import com.sabin.digitalrm.helpers.Unzip;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.DMKBerkas;
import com.sabin.digitalrm.models.DMKBookmark;
import com.sabin.digitalrm.models.DMRPatient;
import com.sabin.digitalrm.models.GenTextBlanko;
import com.sabin.digitalrm.models.GenTextSetting;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingViewInterface;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectTextBox;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.engine.SpenView;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PoliBaruActivity extends BaseActivity implements ProgressRequestBody.UploadCallbacks {
    private static SpenNoteDoc mSpenNoteDoc;
    private static SpenPageDoc mSpenPageDoc;
    private static SpenView mSpenSurfaceView;
    private SpenView spenView;
    private SpenSettingPenLayout mPenSettingView;

    private static Context context;
    private LinearLayout subMenu;
    private LinearLayout bookmark;
    private RecyclerView recyclerView;
    private static Rect mScreenRect = new Rect();
    private Menu canvasMenu;
    private MenuItem bookmarkToggle;

    private BookmarkAdapter adapter;
    private static List<DMKBookmark> dataset;
    private File templateFile;
    private static APIService APIClient;

    String ftpHost, ftpUser, ftpPassword;
    int ftpPort;

    public static final String EXTRA_ID_BLANKO = "EX1";
    public static final String EXTRA_UNDMK = "EX2";
    public static final String EXTRA_NO_BRM = "EX3";
    public static final String EXTRA_DATASET_INDEX = "EX4";
    public static final String EXTRA_PATIENT_NAME = "EX5";
    public static final String EXTRA_DMR_NAME = "EX6";
    public static final String EXTRA_UNIT_CAT = "EX7";
    public static final String EXTRA_ID_DMR = "EX8";
    public static final String EXTRA_ID_SRV = "EX9";
    public static final String EXTRA_ID_UNIT = "EX10";

    public static final int REQUEST_POLI_BARU = 1;
    private final String TEMPLATE_PATH = "/templates/";
    private final int MAIN_STATE        = 0b001;
    private final int BOOKMARK_STATE    = 0b010;

    private boolean penEnable = false;
    private int backState = MAIN_STATE;
    private String noBRM;
    private Integer idBlanko;
    private Integer idSrv;
    private String undmk;
    private String dmrName;
    private Integer idUnit;
    private int datasetIndex;
    private Integer unitCat;
    private static int curentPage = 0;
    private static int totalPage = 0;
    private static int wizard = 0;
    private static Integer idDMR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poli_baru);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        context = this;
        wizard = 0;

        ftpHost = ApiServiceGenerator.getBaseFtp(getApplicationContext());
        ftpPort = ApiServiceGenerator.getBaseFtpPort();
        ftpUser = ApiServiceGenerator.getUserFtp();
        ftpPassword = ApiServiceGenerator.getPasswordFtp();

        String templateFPath = context.getFilesDir() + TEMPLATE_PATH;

        templateFile = new File(templateFPath);
        if (!templateFile.exists()) {
//            Toast.makeText(context, "Template Tidak Ditemukan, Mohon refresh daftar template anda!", Toast.LENGTH_SHORT).show();
            Baselog.d("Path "+templateFile+" not found!");
            if (!templateFile.mkdirs()) {
                Baselog.d("Path "+templateFile+" creation error!");
                finish();
            }
        }

        initSpen();
        initBookmark();
        initSpenView();
        initRetrofit();

        Bundle bundle = getIntent().getExtras();
        idBlanko = bundle.getInt(EXTRA_ID_BLANKO);
        noBRM = bundle.getString(EXTRA_NO_BRM);
        datasetIndex = bundle.getInt(EXTRA_DATASET_INDEX);
        undmk = bundle.getString(EXTRA_UNDMK);
        dmrName = bundle.getString(EXTRA_DMR_NAME);
        unitCat = bundle.getInt(EXTRA_UNIT_CAT);
        idSrv = bundle.getInt(EXTRA_ID_SRV);
        idUnit = bundle.getInt(EXTRA_ID_UNIT);

        String pasienName = bundle.getString(EXTRA_PATIENT_NAME);

        myToolbar.setTitle("DMR " + noBRM + " - " + pasienName);

//        downloadFile();
        Baseprogress.showProgressDialog(context, "Initiating Download ...");
        downloadBlanko();
    }

    @Override
    public void onBackPressed() {
        Baselog.d( "onBackPressed: BACK" + backState);
        if((backState & BOOKMARK_STATE) != 0){
            hideBookmark();
        }else{
            //alertExit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_poli_baru, menu);
        bookmarkToggle = menu.findItem(R.id.action_bookmark);
        MenuItem subMenu = menu.findItem(R.id.action_more);
        getMenuInflater().inflate(R.menu.submenu_poli_baru, subMenu.getSubMenu());
        canvasMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_spen:{
                if(penEnable){
                    mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_NONE);
                    penEnable = false;
                    item.getIcon().setTint(Color.WHITE);
                }else{
                    item.getIcon().setTint(getResources().getColor(R.color.colorPrimaryDark));
                    penEnable = true;
                    mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_STROKE);
                }
                break;
            }

            case R.id.action_tools:{
                if (mPenSettingView.isShown()) {
                    mPenSettingView.setVisibility(View.GONE);
                } else {
                    mPenSettingView.setVisibility(View.VISIBLE);
                }

                break;
            }

            case R.id.action_bookmark:{
                bookmarkToggle = item;
                if((backState & BOOKMARK_STATE) != 0)
                    hideBookmark();
                else {
                    showBookmark();
                    Baselog.d( "onClick: SHOW BOOKMARK");
                }
                break;
            }

            case R.id.action_next:{
                if((curentPage + 1) < totalPage){
                    curentPage++;
                    mSpenPageDoc = mSpenNoteDoc.getPage(curentPage);
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    Toast.makeText(context, "Page " + (curentPage + 1), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, "Last Page", Toast.LENGTH_SHORT).show();
                }

                break;
            }

            case R.id.action_prev:{
                if(curentPage != 0){
                    curentPage--;
                    mSpenPageDoc = mSpenNoteDoc.getPage(curentPage);
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    Toast.makeText(context, "Page " + (curentPage + 1), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, "First Page", Toast.LENGTH_SHORT).show();
                }

                break;
            }

            case R.id.action_exit:{
                alertExit();
                break;
            }

            case R.id.action_save:{
                //TODO: ubah disini
                saveAndUpload();
//                Baseprogress.showProgressDialog(context, "Menambahkan Poli...");
//                saveAndUploadAsync saveSync = new saveAndUploadAsync();
//                saveSync.setOnCompleteListener((idDMR) -> {
//                    Intent intent = new Intent();
//                    intent.putExtra(EXTRA_DATASET_INDEX, datasetIndex);
//                    intent.putExtra(EXTRA_ID_DMR, idDMR);
//                    setResult(RESULT_OK, intent);
//                    finish();
//                });
//
//                saveSync.execute(noBRM, dmrName, unitCat.toString());
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

    private void initRetrofit(){
        Baselog.d("Initretrofit");
        APIClient = APIUtils.getAPIService(context);
    }

    private void initBookmark(){
        bookmark = findViewById(R.id.layout_bookmark);

        recyclerView = findViewById(R.id.recView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager mManager = new LinearLayoutManager(context);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);

        dataset = new ArrayList<>();

        adapter = new BookmarkAdapter(dataset) {
            @Override
            protected void onBindViewHolder(@NonNull BookmarkHolder holder, int position, @NonNull DMKBookmark model) {
                holder.itemView.setOnClickListener(view -> {
                    int page = model.getPage();
                    Toast.makeText(context, "Page: " + page, Toast.LENGTH_SHORT).show();
                    curentPage = page - 1;
                    mSpenPageDoc = mSpenNoteDoc.getPage(curentPage);
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    hideBookmark();
                });
                holder.bindInfo(model);
            }
        };

        recyclerView.setLayoutManager(mManager);
        recyclerView.setAdapter(adapter);
    }

    // SPEN METHOD
    private void initSpen(){
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        }catch (Exception e1) {
            Toast.makeText(context, "Tidak dapat menginisialisasi Spen",
                    Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }

        if(!isSpenFeatureEnabled){
            Toast.makeText(context, "Spen Tidak Ditemukan!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initSpenView(){
        // Create Spen View
        FrameLayout spenViewContainer = findViewById(R.id.spenViewContainer1);
        RelativeLayout spenViewLayout = findViewById(R.id.spenViewLayout1);

        mPenSettingView = new SpenSettingPenLayout(context, "", spenViewLayout);
        spenViewContainer.addView(mPenSettingView);
        mSpenSurfaceView = new SpenView(context);
        if (mSpenSurfaceView == null) {
            Toast.makeText(context, "Cannot create new SpenView.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        mSpenSurfaceView.setToolTipEnabled(true);
        spenViewLayout.addView(mSpenSurfaceView);
        mPenSettingView.setCanvasView(mSpenSurfaceView);

        // Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        mScreenRect = new Rect();
        display.getRectSize(mScreenRect);
        // Create SpenNoteDoc
        try {
            mSpenNoteDoc = new SpenNoteDoc(context, mScreenRect.width(), mScreenRect.height());
        } catch (IOException e) {
            Toast.makeText(context, "Cannot create new NoteDoc.",
                    Toast.LENGTH_SHORT).show();
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

        // DISABLE SPEN
        mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_NONE);
    }

    private void dlgGentext(){
        new AlertDialog.Builder(context)
                .setTitle("Auto Text Generator")
                .setMessage("Beberapa data yang ada pada blanko ini akan diisi secara otomatis berdasarkan format blanko yang telah dibuat, " +
                        "Yakin ingin melanjutkan ?")
                .setCancelable(false)
                .setPositiveButton("Lanjutkan", (dialog, which) -> generateText())
                .setNegativeButton("Batal", null)
                .create().show();
    }

    private void generateText(){
        Baseprogress.showProgressDialog(context, "Menyiapkan Text Generator...");
        //Log.d(TAG, "generateText: " + noBRM + " " + idPoli);
        /*
        //TODO: MAKE POLI GROUP
        Call <ListGenSettingResponse> call = APIClient.getListTextSetting(PetugasMainActivity.UID, noBRM, "2");

        call.enqueue(new Callback<ListGenSettingResponse>() {
            @Override
            public void onResponse(@NonNull Call<ListGenSettingResponse> call, Response<ListGenSettingResponse> response) {
                if(response.isSuccessful()){
                    ListGenSettingResponse genSettingResponse = response.body();
                    if(genSettingResponse.getStatus() == 200){
                        if(genSettingResponse.getGenTextSetting() != null)
                            new renderGenTextAsync().execute(genSettingResponse.getGenTextSetting());
                        else
                            toastInfo(context, "Koordinat belum diatur untuk blanko ini!");
                    }else{
                        Log.d(TAG, "onResponse: " + genSettingResponse.getMessage());
                    }
                }else {
                    Log.d(TAG, "onResponse: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ListGenSettingResponse> call, @NonNull Throwable t) {
                Baseprogress.hideProgressDialog();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
        */
    }

    private static class renderGenTextAsync extends AsyncTask <GenTextSetting, String, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Baseprogress.setMessage("Memulai Text Generator...");
        }

        @Override
        protected Void doInBackground(GenTextSetting... genTextSettings) {
            /*
            GenTextSetting genSetting = genTextSettings[0];
            int pageIndex = 0;
            for(Map<String, List<TextSettingGen>> pageSetting : genSetting.getListTextSetting()){
                List<String> pageKeys = genSetting.getPageKeys(pageIndex++);
                for(String key : pageKeys){
                    for(TextSettingGen textGen : pageSetting.get(key)){
                        publishProgress("Me-render hlm " + (pageIndex) + ": " + key);
                        SpenObjectTextBox textObj = new SpenObjectTextBox();

                        textObj.setRect(textGen.getPos(), true);
                        textObj.setText(genSetting.getContent(key));
                        textObj.setFontSize(textGen.getFontSize());
                        textObj.setMovable(false);
                        textObj.setTextReadOnlyEnabled(true);
                        textObj.setSelectable(false);
                        textObj.setRotatable(false);
                        textObj.setTextStyle(textGen.getFontStyle());

                        mSpenPageDoc.appendObject(textObj);
                        mSpenSurfaceView.update();
                    }
                }
                mSpenPageDoc = mSpenNoteDoc.getPage(pageIndex);
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
            }
            */
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String msg = values[0];
            Baseprogress.setMessage(msg);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            toastInfo(context, "Text Generator Selesai.");
            Baseprogress.hideProgressDialog();
            mSpenPageDoc = mSpenNoteDoc.getPage(0);
            mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
        }
    }

    private void downloadFile(){
        Call<ResponseBody> callDownload = APIClient.downloadBlanko(idBlanko, PetugasMainActivity.UID);

        FileDownloader downloader = new FileDownloader(context, callDownload, noBRM);
        downloader.setBaseDir("tmp");
        downloader.setOnFileDownloadListener(new FileDownloader.FileDownloadListener() {
            @Override
            public void onStart(String msg) {
                Baseprogress.hideProgressDialog();
                Baseprogress.showProgressDialog(context, msg);
            }

            @Override
            public void onProgress(String status) {
                Baseprogress.setMessage(status);
            }

            @Override
            public void onError(String msg) {
                Baseprogress.hideProgressDialog();

                DefaultDialog dialog = new DefaultDialog(context);

                dialog.setMessage(msg);
                dialog.setTitle("Download Error!");
                dialog.setIcon(context.getDrawable(R.drawable.ic_error));
                dialog.setPositiveButton("Cobalagi", view -> {
                    downloadFile();
                    dialog.dismiss();
                });
                dialog.show();
            }

            @Override
            public void onComplete(String filePath) {
//                Baseprogress.hideProgressDialog();
                prepareZip(filePath);
//                loadTemplate(filePath);
            }
        });

        downloader.startDownload();
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

            Log.e("~Debug", "Undoable: "+undoable);
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

            Log.e("~Debug", "Redoable: "+redoable);
        }
    };

    private SpenTouchListener onPreTouchSurfaceViewListener = new SpenTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    mPenSettingView.setVisibility(SpenSurfaceView.GONE);
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
                Log.e("~Debug","mPenSettingView isn't null");
                SpenSettingPenInfo penInfo = mPenSettingView.getInfo();
                penInfo.color = color;
                mPenSettingView.setInfo(penInfo);
                mPenSettingView.savePreferences();
            }else{
                Log.e("~Debug","mPenSettingView is null");
            }
        }
    };

    private void prepareZip(String filepath){
        Unzip unzip = new Unzip(context, filepath, idBlanko.toString());
        unzip.setOnUnzipListener(new Unzip.UnzipListener() {
            @Override
            public void onStart(String msg) {
                if(Baseprogress.isDialogShowing()) {
                    Baseprogress.setMessage(msg);
                }else{
                    Log.e("[X-DEBUG]", "Baseprogress IS NULL");
                }
            }

            @Override
            public void onProgress(String status) {
                if(Baseprogress.isDialogShowing()) {
                    Baseprogress.setMessage(status);
                }else{
                    Log.e("[X-DEBUG]", "Baseprogress IS NULL");
                }
            }

            @Override
            public void onError(String msg) {
                if(Baseprogress.isDialogShowing()) {
                    Baseprogress.hideProgressDialog();
                }else{
                    Log.e("[X-DEBUG]", "Baseprogress IS NULL");
                }
                toastErr(context, msg);
            }

            @Override
            public void onComplete(String filePath) {
                if(Baseprogress.isDialogShowing()) {
                    Baseprogress.setMessage("Menyusun Blanko...");
                }else{
                    Log.e("[X-DEBUG]", "Baseprogress IS NULL");
                }
                Log.d(TAG, "onComplete: POL" + filePath);
                //loadTemplate(filePath + "/7_1");
                //assemblePage(filePath);
                AssembleBlanko assembleBlanko = new AssembleBlanko();
                assembleBlanko.setOnCompleteListener(() -> refreshNote());
                assembleBlanko.execute(idBlanko.toString(), idSrv.toString(), undmk, filePath);
            }
        });
        unzip.startUnzip();
    }

    private static class AssembleBlanko extends AsyncTask<String, String, String>{
        public interface OnCompleteListener{
            void onComplete();
        }

        OnCompleteListener mListener;

        public void setOnCompleteListener(OnCompleteListener listener){
            mListener = listener;
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            if(Baseprogress.isDialogShowing()) {
                Baseprogress.hideProgressDialog();
            }else{
                Log.e("[X-DEBUG]", "Baseprogress IS NULL");
            }
            Log.d(TAG, "onCancelled: " + s);
        }

        @Override
        protected String doInBackground(String... strings) {
            Integer idBlanko = Integer.valueOf(strings[0]);
            Integer idSrv = Integer.valueOf(strings[1]);
            String undmk = strings[2];
            String path = strings[3];
            int pageIndex = 0;

            SpenNoteDoc tmpNote;
            List<GenTextBlanko> blankoList;
            try {
                publishProgress("Mengambil metadata blanko...");
                Call <List<GenTextBlanko>> call = APIClient.getGentextBlankoList(idBlanko,idSrv, PetugasMainActivity.UID, undmk);
                Log.d(TAG, "doInBackground: " + call.request().toString());
                Response<List<GenTextBlanko>> response = call.execute();
                if(response.isSuccessful()){
                    blankoList = response.body();
                }else{
                    cancel(true);
                    return ApiError.parseError(response).getMessage();
                }
            }catch (IOException e){
                cancel(true);
                return e.getLocalizedMessage();
            }

            try {
                tmpNote = new SpenNoteDoc(context, mScreenRect.width(), mScreenRect.height());
            }catch (IOException e){
                cancel(true);
                return e.getLocalizedMessage();
            }

            dataset.clear();
            publishProgress("Menyusun blanko...");
            for(GenTextBlanko dmk : blankoList){
                Log.d(TAG, "doInBackground: " + dmk.getName());
                String notePath = path + "/" + dmk.getFilename().split("/")[1];
                DMKBookmark bookmark = new DMKBookmark();
                bookmark.setDmk(dmk.getCode());
                bookmark.setId(dmk.getId());
                bookmark.setName(dmk.getName());
                bookmark.setPage(pageIndex+1);
                bookmark.setTotalPages(dmk.getTotalPage());
                dataset.add(bookmark);

                Map<String, List<GenTextSetting>> pagesSettings = dmk.getTextSettings();
                Map<String, String> contents = dmk.getContents();

                Log.d(TAG, "doInBackground: path " + notePath);
                publishProgress("Memproses DMK " + dmk.getCode() + "...");

                try{
                    SpenNoteDoc note = new SpenNoteDoc(context, notePath, mScreenRect.width(), SpenNoteDoc.MODE_READ_ONLY);
                    int cnt = note.getPageCount();
                    for (int i = 0; i < cnt; i++){
                        SpenPageDoc page = note.getPage(i);
                        if(pagesSettings != null) {
                            List<GenTextSetting> pageSettings = pagesSettings.get("page-" + i);
                            if(pageSettings == null)
                                pageSettings = new ArrayList<>();

                            Log.d(TAG, "doInBackground: pageSettings" + pagesSettings.size());
                            for (GenTextSetting setting : pageSettings) {
                                SpenObjectTextBox textObj = new SpenObjectTextBox();

                                String content = contents.get(setting.getField());
                                content = content == null ? "-" : content;

                                textObj.setRect(setting.getCoordPos(), true);
                                textObj.setText(content);
                                textObj.setFontSize(setting.getFontSize());
                                textObj.setMovable(false);
                                textObj.setTextReadOnlyEnabled(true);
                                textObj.setSelectable(false);
                                textObj.setRotatable(false);
                                textObj.setTextStyle(setting.getFontStyle());

                                page.appendObject(textObj);
                            }
                        }
                        tmpNote.copyPage(page, pageIndex++);
                    }

                    note.close();
                }catch (Exception e){
                    String res = e.getLocalizedMessage();
                    cancel(true);
                    return res;
                }
            }

            try {
                mSpenNoteDoc.close();
            }catch (IOException e){
                String res = e.getLocalizedMessage();
                cancel(true);
                return res;
            }
            mSpenNoteDoc = tmpNote;


            return "Success";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(Baseprogress.isDialogShowing()) {
                Baseprogress.setMessage(values[0]);
            }else{
                Log.e("[X-DEBUG]", "Baseprogress IS NULL");
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(Baseprogress.isDialogShowing()) {
                Baseprogress.hideProgressDialog();
            }else{
                Log.e("[X-DEBUG]", "Baseprogress IS NULL");
            }
            mListener.onComplete();
        }
    }

    //baru
    public void downloadBlanko(){
        Call<ResponseBody> callDownload = APIClient.downloadBlanko(idBlanko, PetugasMainActivity.UID);
        callDownload.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    if(response.code()==200){
                        String check = response.headers().get("X-Filename");
                        String[] woke = check.split("/");
                        String zipFileName = woke[1];
                        String nama_dir = woke[0];

                        Log.e("[X-DEBUG]","downloadBRM() - onResponse Triggered");
                        Log.e("FILENAME_CHECK", check);

                        new downloadBlankoAsync(zipFileName, nama_dir).execute();
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
                            Toast.makeText(context, "Response code: " + statuscode + ". " + message, Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(context, "Something went wrong. Unable to download document", Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }
                }else{
                    Toast.makeText(context, response.message()+": "+ ApiError.parseError(response).getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("[X-DEBUG]",response.message());
                    Baseprogress.hideProgressDialog();
                }

            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.e("[X-DEBUG]",t.getLocalizedMessage());
                Log.e("[X-DEBUG]","downloadBRM() - onFailed Triggered");
                Baseprogress.hideProgressDialog();
            }
        });
    }

     //TODO:APAKAH INI YANG MAU DI RUBAH?
     public class downloadBlankoAsync extends AsyncTask<ResponseBody, Integer, String>{
        String zipFilename, namaDir;

        public downloadBlankoAsync(String zipFilename, String namaDir){
            this.zipFilename = zipFilename;
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
                 boolean cwd = ftp.changeWorkingDirectory("/tmp/");
                 Log.e("X-DEBUG", "CHANGE WORKING DIR TO '/tmp/ : "+cwd);

                 //set file
                 ftp.setFileType(FTP.BINARY_FILE_TYPE);

                 //download
                 InputStream bis = new BufferedInputStream(ftp.retrieveFileStream(zipFilename), 1024 * 8);
                 Log.e("X-DEBUG", "DOWNLOAD ZipFilename "+zipFilename);

                 File outDir = new File(context.getFilesDir() + "/tmp/");
                 File outFile = new File(context.getFilesDir()+"/tmp/", zipFilename);

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
                     Toast.makeText(PoliBaruActivity.this, "Error: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                 });
             }

             if(error) {
                 Log.e("X-DEBUG", "OPERATION FAILED");

                 return null;
             }else{
                 Log.e("X-DEBUG", "OPERATION SUCCESS");
             }

             File outFile = new File(context.getFilesDir()+"/tmp/", zipFilename);

             return outFile.getPath();
         }

         @SuppressLint("SetTextI18n")
         @Override
         protected void onPostExecute(String path) {
            if(path != null) {
                prepareZip(path);
            }
         }
     }


     private static class saveAndUploadAsync extends AsyncTask<String, String, String>{
         String ftpHost, ftpUser, ftpPassword;
         int ftpPort;

         String dmrFilename;

         saveAndUploadAsync(String ftpHost, int ftpPort, String ftpUser, String ftpPassword) {
             this.ftpHost = ftpHost;
             this.ftpPort = ftpPort;
             this.ftpUser = ftpUser;
             this.ftpPassword = ftpPassword;
         }

        public interface OnTaskListener{
            void onComplete(int idDMR);
            void onError(String msg);
        }

        OnTaskListener mListener;

        void setOnTaskListener(OnTaskListener listener){
            mListener = listener;
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            Baseprogress.hideProgressDialog();
            mListener.onError(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            String noteFilePath = context.getFilesDir() + "/tmp/dmr.tmp";

            String noBRM = strings[0];
            String dmrName = strings[1];
            Integer unitCat = Integer.valueOf(strings[2]);
            Integer idUnit = Integer.valueOf(strings[3]);

            Log.d(TAG, "doInBackground: nobrm " + noBRM);
            Log.d(TAG, "doInBackground: dmrname " + dmrName);
            Log.d(TAG, "doInBackground: unitcat " + unitCat);
            Log.d(TAG, "doInBackground: unit " + idUnit);

            Integer len;
            byte[] buf;

            switch (wizard){
                case 0:{
                    try {
                        publishProgress("Menambah data DMR pasien...");
                        Call<DMRPatient> callDMR = APIClient.addDMR(PetugasMainActivity.UID, noBRM, dmrName, unitCat, idUnit);
                        Log.d(TAG, "doInBackground: " + callDMR.request().toString());
                        Response<DMRPatient> response = callDMR.execute();
                        if(response.isSuccessful()){
                            idDMR = response.body().getId();
                            Log.e(TAG, "doInBackground: get filename " + response.body().getFilename());
                            dmrFilename = response.body().getFilename();
                        }else{
                            cancel(true);
                            return ApiError.parseError(response).getMessage();
                        }
                    }catch (IOException e){
                        cancel(true);
                        return e.getLocalizedMessage();
                    }

                    wizard = 1;
                }

                case 1:{
                    publishProgress("Menyimpan DMR...");
                    try {
                        mSpenNoteDoc.save(noteFilePath, false);
                    }catch (IOException e){
                        String res = e.getLocalizedMessage();
                        Log.d(TAG, "doInBackground: " + res); res = e.getLocalizedMessage();
                        Log.d(TAG, "doInBackground: " + res);
                        cancel(true);
                        return "Gagal menyipan DMR ke penyimpanan sementara!, " + res;
                    }

//                    publishProgress("Menyiapkan File Uploader...");
//                    try {
//                        InputStream spdStream;
//                        spdStream = new FileInputStream(new File(noteFilePath));
//                        buf = new byte[spdStream.available()];
//                        len = spdStream.available();
//                        while (spdStream.read(buf) != -1){
//                            len += spdStream.available();
//                        }
//
//                    }catch (FileNotFoundException e){
//                        cancel(true);
//                        return "File DMR tidak ditemukan! " + e.getLocalizedMessage();
//                    }catch (IOException e){
//                        cancel(true);
//                        return "Gagal membaca file DMR " + e.getLocalizedMessage();
//                    }

                    publishProgress("Mengupload File...");

                    String check = dmrFilename;
                    String[] splitan = check.split("/");

                    String noRM = splitan[0];
                    String namafile = splitan[1];
                    //ftp

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

                        File asset = new File(noteFilePath);
                        BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(asset));

                        // transfer files
                        ftp.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
                        ftp.enterLocalPassiveMode();

                        //masuk ke archives
                        boolean cwd = ftp.changeWorkingDirectory("/archives/");
                        Log.e("X-DEBUG", "CHANGE WORKING DIR TO '/archives/ : "+cwd);

                        // buat dir noBRM
                        boolean status = ftp.makeDirectory(noRM);
                        Log.e("X-DEBUG", "Make DIR TO '/archives/ "+noRM+": "+status);

                        //masuk ke dir noBRM
                        boolean cwdNoBrm = ftp.changeWorkingDirectory(noRM);
                        Log.e("X-DEBUG", "CHANGE WORKING DIR TO '/archives/"+cwdNoBrm+" : "+cwdNoBrm);

                        //buat dir exports
                        boolean exports = ftp.makeDirectory("exportsftp");
                        Log.e("X-DEBUG", "Make DIR TO '/archives/ "+exports+": "+exports);

                        //buat dir old_drms
                        boolean old_dmrs = ftp.makeDirectory("old_dmrsfpt");
                        Log.e("X-DEBUG", "Make DIR TO '/archives/ "+old_dmrs+": "+old_dmrs);

                        //buat dir tmp
                        boolean tmp = ftp.makeDirectory("tmpftp");
                        Log.e("X-DEBUG", "Make DIR TO '/archives/ "+tmp+": "+tmp);

                        //set file
                        ftp.setFileType(FTP.BINARY_FILE_TYPE);

                        Log.e("X-DEBUG", "UPLOAD '" + noteFilePath + "' STREAM LENGTH: " + buffIn.available());

                        Log.e("X-DEBUG", "UPLOADING...");


                        boolean upStatus = ftp.storeFile(namafile, buffIn);
                        if(upStatus){

                            buffIn.close();
                        }

                        Log.e("X-DEBUG", "UPLOAD '"+noteFilePath+"' STATUS: "+upStatus);

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

//                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), buf);

//                    try{
//                        Call<ApiStatus> call = APIClient.uploadDMR(idDMR, PetugasMainActivity.UID, len, requestBody);
//                        Log.d("LOG-DR", "Ukuran File : "+requestBody.contentLength()+" Type : "+requestBody.contentType());
//                        Log.d(TAG, "doInBackground: " + call.request().toString());
//                        Response<?> response = call.execute();
//                        if(!response.isSuccessful()){
//                            cancel(true);
//                            return ApiError.parseError(response).getMessage();
//                        }
//                    }catch (IOException e){
//                        cancel(true);
//                        return e.getLocalizedMessage();
//                    }

                    wizard = 2;
                }

                case 2:{
                    publishProgress("Mengupload Bookmark...");
                    List<DMKBerkas> dmkBerkasList = new ArrayList<>();
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    for(DMKBookmark dmk : dataset){
                        DMKBerkas dmkBerkas = new DMKBerkas(idDMR, dmk.getId(), dmk.getPage(), dmk.getTotalPages());
                        dmkBerkasList.add(dmkBerkas);
                    }

                    Type dmkBerkasType = new TypeToken<List<DMKBerkas>>() {}.getType();
                    String json = gson.toJson(dmkBerkasList, dmkBerkasType);
                    Log.d(TAG, "doInBackground: " + json);

                    try{
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), json);
                        Call<ApiStatus> call = APIClient.addDMKsInDMR(PetugasMainActivity.UID, idDMR, requestBody);
                        Log.d(TAG, "doInBackground: " + call.request().toString());
                        Response<?> response = call.execute();
                        if(!response.isSuccessful()){
                            cancel(true);
                            return ApiError.parseError(response).getMessage();
                        }
                    }catch (IOException e){
                        cancel(true);
                        return e.getLocalizedMessage();
                    }

                    wizard = 3;
                }
            }

            return idDMR.toString();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Baseprogress.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Baseprogress.hideProgressDialog();

            toastInfo(context, "DMR Berhasil Ditambahkakn");
            mListener.onComplete(Integer.valueOf(s));
        }
    }

    private static class ftpMakeDir extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String saveFilePath = strings[0];

            return null;
        }
    }
    private void refreshNote(){
        adapter.notifyDataSetChanged();
        curentPage = 0;
        totalPage = mSpenNoteDoc.getPageCount();
        mSpenPageDoc = mSpenNoteDoc.getPage(0);
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
        mSpenSurfaceView.update();

        Toast.makeText(context, "Document loaded successfully.", Toast.LENGTH_SHORT).show();
//            renderPage();
    }

//    private void loadTemplate(String notepath){
//        Log.d(TAG, "loadTemplate: " + notepath);
//        try {
//            SpenObjectTextBox.setInitialCursorPos(SpenObjectTextBox.CURSOR_POS_END);
//            // Create NoteDoc with the selected file.
//            SpenNoteDoc tmpSpenNoteDoc = new SpenNoteDoc(context, notepath, mScreenRect.width(),
//                    SpenNoteDoc.MODE_WRITABLE, true);
//            mSpenNoteDoc.close();
//            mSpenNoteDoc = tmpSpenNoteDoc;
//            if (mSpenNoteDoc.getPageCount() == 0) {
//                mSpenPageDoc = mSpenNoteDoc.appendPage();
//            } else {
//                mSpenPageDoc = mSpenNoteDoc.getPage(0);
//            }
//            curentPage = 0;
//            totalPage = mSpenNoteDoc.getPageCount();
//            mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
//            mSpenSurfaceView.update();
//
//            Toast.makeText(context, "Document loaded successfully.", Toast.LENGTH_SHORT).show();
////            renderPage();
//        } catch (IOException e) {
//            Toast.makeText(context, "Cannot open this file.", Toast.LENGTH_LONG).show();
//        } catch (SpenUnsupportedTypeException e) {
//            Toast.makeText(context, "This file is not supported.", Toast.LENGTH_LONG).show();
//        } catch (SpenInvalidPasswordException e) {
//            Toast.makeText(context, "This file is locked by a password.", Toast.LENGTH_LONG).show();
//        } catch (SpenUnsupportedVersionException e) {
//            Toast.makeText(context, "This file is the version that does not support.",
//                    Toast.LENGTH_LONG).show();
//        } catch (Exception e) {
//            Toast.makeText(context, "Failed to load document.", Toast.LENGTH_LONG).show();
//        }
//    }

    private void hideBookmark(){
        bookmarkToggle.getIcon().setTint(Color.WHITE);
        backState ^= BOOKMARK_STATE;
        bookmark.animate()
                .translationY(-bookmark.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        bookmark.setVisibility(View.GONE);
                    }
                });
    }

    private void showBookmark(){
        backState |= BOOKMARK_STATE;
        bookmarkToggle.getIcon().setTint(getResources().getColor(R.color.colorPrimaryDark));
        bookmark.setVisibility(View.VISIBLE);
        // Start the animation
        bookmark.animate().translationY(0).setListener(null);
    }

//    private void fetDmkBlanko(Integer id, String undmk){
//        Call<List<DMKBlanko>> call = APIClient.getDMKinBlanko(id, PetugasMainActivity.UID, undmk);
//        call.enqueue(new Callback<List<DMKBlanko>>() {
//            @Override
//            public void onResponse(Call<List<DMKBlanko>> call, Response<List<DMKBlanko>> response) {
//                if(response.isSuccessful()){
//                    Log.d(TAG, "onResponse: " + response.body().get(0).getName());
//                }else{
//                    Log.d(TAG, "onResponse: " + ApiError.parseError(response).getMessage());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<DMKBlanko>> call, Throwable t) {
//                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
//            }
//        });
//    }

//    private void xrenderPage(){
//        byte [] dataByte;
//
//        // Read all bookmark
//        Baselog.d( "renderPage: RENDIRING");
//
//        dataset.clear();
//
//        for(int i = 0; i < totalPage; i++){
//            dataByte = mSpenNoteDoc.getPage(i).getExtraDataByteArray("BOOKMARK");
//
//            if(dataByte != null){
//                try {
//                    DMKBookmark bookmark = (DMKBookmark) SerializationUtil.deserialize(dataByte);
//                    Baselog.d( "renderPage: indexPage " + i + "\non page " + bookmark.getPage());
//
//                    dataset.add(bookmark);
//                }catch (Exception e){
//                    Baselog.d( "renderPage: ERROR " + e.getLocalizedMessage());
//                }
//            }else{
//                Baselog.d( "renderPage: NO BM on page " + i);
//            }
//        }
//        adapter.notifyDataSetChanged();
//        Baselog.d( "renderPage: COMPLETE " + totalPage);
//
//    }

    private void saveAndUpload(){
        Baseprogress.showProgressDialog(context, "Menambahkan Poli...");
        saveAndUploadAsync saveSync = new saveAndUploadAsync(ftpHost, ftpPort, ftpUser, ftpPassword);

        saveSync.setOnTaskListener(new saveAndUploadAsync.OnTaskListener() {
            @Override
            public void onComplete(int idDMR) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DATASET_INDEX, datasetIndex);
                intent.putExtra(EXTRA_ID_DMR, idDMR);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onError(String msg) {
                DefaultDialog dialog = new DefaultDialog(context);
                dialog.setMessage(msg);
                dialog.setTitle("Save & Upload Gagal!");
                dialog.setIcon(context.getDrawable(R.drawable.ic_error));
                dialog.setPositiveButton("Cobalagi", view -> {
                    saveAndUpload();
                    dialog.dismiss();
                });
                dialog.show();
            }
        });

        saveSync.execute(noBRM, dmrName, unitCat.toString(), idUnit.toString());
    }



    private void alertExit(){
        DefaultDialog dialog = new DefaultDialog(context);
        dialog.setTitle("Konfirmasi Keluar");
        dialog.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
        dialog.setMessage("Semua pekerjaan anda akan hilang jika belum disimpan. Yakin ingin keluar ?");
        dialog.setNormalButton("Batal", null);
        dialog.setPositiveButton("Keluar", view -> finish());
        dialog.show();
    }

    @Override
    public void onProgressUpdate(int percentage) {
        Baseprogress.setMessage("Uploading " + percentage + "%");
    }

    @Override
    public void onError() {
        Baseprogress.hideProgressDialog();
        toastErr(context, "Terjadi Galat");
    }

    @Override
    public void onFinish() {
        Baseprogress.hideProgressDialog();
    }
}
