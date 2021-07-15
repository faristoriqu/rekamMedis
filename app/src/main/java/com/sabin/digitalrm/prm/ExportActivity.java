package com.sabin.digitalrm.prm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.FileProvider;import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.BuildConfig;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.helpers.DefaultDialog;
import com.sabin.digitalrm.helpers.FileDownloader;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.DMRExport;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.ClipboardUtils;
import com.sabin.digitalrm.utils.ShareUtils;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingViewInterface;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.document.SpenUnsupportedTypeException;
import com.samsung.android.sdk.pen.document.SpenUnsupportedVersionException;
import com.samsung.android.sdk.pen.engine.SpenView;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExportActivity extends BaseActivity {
    public static final String EXTRA_ID_DMR = "EX1";
    public static final String EXTRA_NORM = "EX2";
    public static final String EXTRA_PXNAME = "EX3";
    public static final String EXTRA_ID_UNIT = "EX4";

    private Context mContext;
    private View view;
    private static int curentPage = 0;
    private static int totalPage = 0;
    @SuppressLint("StaticFieldLeak")
    private static SpenNoteDoc mSpenNoteDoc;
    private static SpenPageDoc mSpenPageDoc;
    private static SpenView mSpenSurfaceView;
    private SpenSettingPenLayout mPenSettingView;
    private static Rect mScreenRect = new Rect();
    private static File noteFile;
    private static String noteFilename, notePath;
    private Menu expMenu;
    private boolean isSpenFeatureEnabled, isDocAttached = false;
    EditText inputRange;
    FloatingActionButton fab_nx, fab_pr;

    private APIService APIClient;

    private Integer idDMR;
    private String norm, uid, namaPasien;
    private Integer idUnit;
    private String pxName;
    private Boolean autoExport;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        setSupportActionBar(findViewById(R.id.prmExpToolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pref = getSharedPreferences("SESSION_", 0);
        uid = pref.getString("uid", null);

        fab_nx = findViewById(R.id.fab_next_pg);
        fab_pr = findViewById(R.id.fab_prev_pg);

        mContext = this;
        initSpen();
        initSurfaceView();

        APIClient = APIUtils.getAPIService(mContext);
        Bundle bundle = getIntent().getExtras();

        idDMR = bundle.getInt(EXTRA_ID_DMR);
        norm = bundle.getString(EXTRA_NORM);
        pxName = bundle.getString(EXTRA_PXNAME);
        idUnit = bundle.getInt(EXTRA_ID_UNIT);

        fab_nx.setOnClickListener(v -> {
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
        });

        fab_pr.setOnClickListener(v -> {
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
        });

        view = getWindow().getDecorView().getRootView();

        downloadFile();
    }

    private void initSurfaceView(){
        // Create Spen View
        FrameLayout spenViewContainer = findViewById(R.id.spenViewContainer);
        RelativeLayout spenViewLayout = findViewById(R.id.spenViewLayout);
        mPenSettingView = new SpenSettingPenLayout(mContext, "", spenViewLayout);
        spenViewContainer.addView(mPenSettingView);
        mSpenSurfaceView = new SpenView(mContext);

        mSpenSurfaceView.setToolTipEnabled(true);
        spenViewLayout.addView(mSpenSurfaceView);
        mPenSettingView.setCanvasView(mSpenSurfaceView);

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

        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.clearHistory();
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        // Initialize Pen settings
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        mSpenSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);
        mPenSettingView.loadPreferences();

        // DISABLE SPEN
        mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_NONE);
    }

    private void initSpen(){
        // Initialize Spen
        isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();

        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Spen.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.petugas_exp_menu, menu);
        expMenu = menu;

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_convert).setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_add:{
//                getFile();

                break;
            }

            case R.id.action_convert:{
                PDFExporter();

                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void downloadFile(){
        Call<ResponseBody> callDownload = APIClient.downloadDMR(idDMR, PetugasMainActivity.UID);

        FileDownloader downloader = new FileDownloader(mContext, callDownload, (norm + "_" + pxName + "_" + idUnit + "_" + idDMR));
        downloader.setOnFileDownloadListener(new FileDownloader.FileDownloadListener() {
            @Override
            public void onStart(String msg) {
                Baseprogress.showProgressDialog(mContext, msg);
            }

            @Override
            public void onProgress(String status) {
                Baseprogress.setMessage(status);
            }

            @Override
            public void onError(String msg) {
                toastErr(mContext, msg);
                Baseprogress.hideProgressDialog();
            }

            @Override
            public void onComplete(String filePath) {
                Baseprogress.hideProgressDialog();
                loadNoteFile(filePath);
            }
        });

        downloader.startDownload();
    }

    private void loadNoteFile(String filepath){
        notePath = filepath;
        if(notePath!=null) {
            noteFile = new File(notePath);
            noteFilename = noteFile.getName();

            try {
                SpenNoteDoc tmpSpenNoteDoc = new SpenNoteDoc(mContext, notePath, mScreenRect.width(), SpenNoteDoc.MODE_WRITABLE, true);
                mSpenNoteDoc.close();
                mSpenNoteDoc = tmpSpenNoteDoc;
                mSpenPageDoc = mSpenNoteDoc.getPage(0);
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                mSpenSurfaceView.update();
                curentPage = 0;
                totalPage = mSpenNoteDoc.getPageCount();
                expMenu.findItem(R.id.action_convert).setEnabled(true);
                expMenu.findItem(R.id.action_convert).setIcon(R.drawable.ic_pdf);
                fab_nx.setVisibility(View.VISIBLE);
                fab_pr.setVisibility(View.GONE);
                Toast.makeText(mContext, "Page count: "+(totalPage-1)+"; Filename: "+noteFilename, Toast.LENGTH_SHORT).show();
            }catch (IOException ex){
                Log.e("~Debug [LoadException1]", ex.getMessage()+"");
            }catch (SpenUnsupportedTypeException ex2){
                Log.e("~Debug [LoadException2]", ex2.getMessage()+"");
            }catch (SpenUnsupportedVersionException ex3){
                Log.e("~Debug [LoadException3]", ex3.getMessage()+"");
            }
        }
    }

    private void getFile() {
        final File localFile = new File(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)).getPath());
        final Uri localDocument = FileProvider.getUriForFile(ExportActivity.this, BuildConfig.APPLICATION_ID + ".provider", localFile);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, localDocument);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 100);
        }catch (Exception ex){
            Log.e("~Debug [getFile]", ex.getMessage());
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String allowedExtFile = "spd";
            isDocAttached = true;
            Uri uri = data.getData();
            String documentId = DocumentsContract.getDocumentId(uri);
            String uriAuthority = Objects.requireNonNull(uri).getAuthority();

            boolean isLocalDoc = false;
            String result = null;

            if("com.android.externalstorage.documents".equals(uriAuthority))
            {
                isLocalDoc = true;
            }

            if(isLocalDoc) {
                String idArr[] = documentId.split(":");
                if (idArr.length == 2) {
                    String type = idArr[0];
                    String realDocId = idArr[1];

                    String[] fileSplitter = realDocId.split("\\.");

                    if(fileSplitter[1].equals(allowedExtFile)) {
                        if ("primary".equalsIgnoreCase(type)) {
                            result = Environment.getExternalStorageDirectory() + "/" + realDocId;
                        }
                    }else{
                        Toast.makeText(mContext, "File extension not allowed", Toast.LENGTH_LONG).show();
                    }
                }
            }else{
                Toast.makeText(mContext, "Cannot accessing file", Toast.LENGTH_SHORT).show();
            }

            notePath = result;
            if(notePath!=null) {
                noteFile = new File(notePath);
                noteFilename = noteFile.getName();

                Log.e("~Debug [LoadDoc]", result+"");

                try {
                    SpenNoteDoc tmpSpenNoteDoc = new SpenNoteDoc(mContext, notePath, mScreenRect.width(), SpenNoteDoc.MODE_WRITABLE, true);
                    mSpenNoteDoc.close();
                    mSpenNoteDoc = tmpSpenNoteDoc;
                    mSpenPageDoc = mSpenNoteDoc.getPage(0);
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    mSpenSurfaceView.update();
                    curentPage = 0;
                    totalPage = mSpenNoteDoc.getPageCount();
                    expMenu.findItem(R.id.action_convert).setEnabled(true);
                    expMenu.findItem(R.id.action_convert).setIcon(R.drawable.ic_pdf);
                    fab_nx.setVisibility(View.VISIBLE);
                    fab_pr.setVisibility(View.GONE);
                    Toast.makeText(mContext, "Page count: "+(totalPage-1)+"; Filename: "+noteFilename, Toast.LENGTH_SHORT).show();
                }catch (IOException ex){
                    Log.e("~Debug [LoadException1]", ex.getMessage()+"");
                }catch (SpenUnsupportedTypeException ex2){
                    Log.e("~Debug [LoadException2]", ex2.getMessage()+"");
                }catch (SpenUnsupportedVersionException ex3){
                    Log.e("~Debug [LoadException3]", ex3.getMessage()+"");
                }
            }

        }else if(requestCode == 100 && resultCode == RESULT_CANCELED){
            Toast.makeText(this, "Action cancelled by the User", Toast.LENGTH_LONG).show();
            Log.e("~Debug [LoadDoc]", "Action cancelled by the User");
        }else{
            Toast.makeText(this, "Failed to open file", Toast.LENGTH_LONG).show();
            Log.e("~Debug [LoadDoc]", "Failed to open file");
        }
    }

    private void exportLink(DMRExport export){
        LayoutInflater inflater = getLayoutInflater();
        View container = inflater.inflate(R.layout.layout_export_link_action, null);

        String url = ApiServiceGenerator.getBaseUrl(getApplicationContext()) + "download/exports/" + export.getId() + "?fkey=" + export.getFileKey();

        EditText etLink = container.findViewById(R.id.export_link);
        ImageView btCopy = container.findViewById(R.id.img_copy);
        ImageView btShare = container.findViewById(R.id.img_share);
        ImageView btOpen = container.findViewById(R.id.img_open);

        etLink.setInputType(InputType.TYPE_NULL);
        etLink.setText(url);

        btShare.setOnClickListener(view -> {
            String data = "Link Export DMR " + export.getNoBRM();
            data += "\n" + export.getExportName();
            data += "\nURL: " + url;

            ShareUtils.shareText(mContext, data);
        });

        btCopy.setOnClickListener(view -> {
            ClipboardUtils.newClip(mContext, url);
            toastInfo(mContext, "Disalin ke clipboard");
        });

        btOpen.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        DefaultDialog dialog = new DefaultDialog(mContext);
        dialog.setView(container);
        dialog.setTitle("Export Berhasil");
        dialog.setIcon(mContext.getDrawable(R.drawable.ic_check));
        dialog.setPositiveButton("Selesai", null);

        dialog.show();

    }

    private void PDFExporter(){
//        final String storagePath = android.os.Environment.getExternalStorageDirectory().toString() + "/" + "DigitalMR" + "/" + "Exports";
        final String storagePath = mContext.getFilesDir().getAbsolutePath() + "/" + "exports";

        // Prompt Save File dialog to get the file name. getExternalFilesDir()
        // and get its save format option (note file or image).
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = Objects.requireNonNull(inflater).inflate(R.layout.export_dialog, findViewById(R.id.layout_root));
        AlertDialog.Builder builderSave = new AlertDialog.Builder(mContext);
        TextView pageCounter = layout.findViewById(R.id.txtPageCounter);
        inputRange = layout.findViewById(R.id.input_range);
        builderSave.setTitle("Export to PDF File");
        pageCounter.setText("Total Page: "+(totalPage-1));
        builderSave.setView(layout);
        inputRange.setText("");
        inputRange.setVisibility(View.GONE);
        final RadioGroup selectMethod = layout.findViewById(R.id.radioGroup);

        selectMethod.setOnCheckedChangeListener((group, checkedId) -> {
            int i = selectMethod.getCheckedRadioButtonId();

            if(i == R.id.radioAll){
                inputRange.setText("");
                inputRange.setVisibility(View.GONE);
            }else if(i == R.id.radioCurrent){
                inputRange.setText("");
                inputRange.setVisibility(View.GONE);
            }else if(i == R.id.radioRange){
                inputRange.setText("");
                inputRange.setVisibility(View.VISIBLE);
            }
        });

        builderSave.setCancelable(false);
        builderSave.setPositiveButton("Export", (dialog, which) -> {
            // Set the save directory for the file.
            String page;
            File filePath = new File(storagePath);
            if (!filePath.exists()) {
                if (!filePath.mkdirs()) {
                    Log.e("{X-DEBUG]", "Failed to creating dirs on "+filePath);
                    return;
                }
            }

            int checkedRadioButtonId = selectMethod.getCheckedRadioButtonId();

            switch (checkedRadioButtonId){
                case R.id.radioAll:
                    page = "1-" + totalPage;
                    break;

                case R.id.radioCurrent:
                    page = (curentPage + 1) + "-" + (curentPage + 1);
                    break;

                case R.id.radioRange:
                    page = inputRange.getText().toString();
                    break;

                default:
                    page = "";
                    break;
            }

            toastInfo(mContext, "Memproses Export...");
            Call<DMRExport> call = APIClient.exportDMR(PetugasMainActivity.UID, idDMR, page, ("Manual export, halaman: " + page));
            call.enqueue(new Callback<DMRExport>() {
                @Override
                public void onResponse(Call<DMRExport> call, @NonNull Response<DMRExport> response) {
                    if(response.isSuccessful()){
                        exportLink(response.body());
                    }else{
                        String msg = ApiError.parseError(response).getMessage();
                        Log.d(TAG, "onResponse: " + msg);
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<DMRExport> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                    Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        builderSave.setNegativeButton("Cancel", null);
        AlertDialog dlgSave = builderSave.create();
        dlgSave.show();
    }
}
