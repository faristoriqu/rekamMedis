package com.sabin.digitalrm.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.BookmarkAdapter;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.helpers.FileDownloader;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.BookmarkResponse;
import com.sabin.digitalrm.models.DMKBookmark;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.Logger;
import com.sabin.digitalrm.utils.Timestamp;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingRemoverInfo;
import com.samsung.android.sdk.pen.SpenSettingViewInterface;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.document.SpenUnsupportedTypeException;
import com.samsung.android.sdk.pen.document.SpenUnsupportedVersionException;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
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
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sabin.digitalrm.fragments.BaseFragment.toastErr;

public class PoliSurfaceFragment extends Fragment {
    private final int MAIN_STATE = 0b001;
    private final int BOOKMARK_STATE = 0b010;
    private static boolean saveState = false, isSaveDialog = false, doneState = false, isUndoable = false, isRedoable = false;
    private boolean spenOn = false;
    private boolean penEnable = false;
    private boolean eraserEnable = false;
    private MenuItem bookmarkToggle;
    private int backState = MAIN_STATE;
    public static File ARCHIVE_FILE;
    public static String ARCHIVE_PATH;
    private Menu canvasMenu;
    private SpenSettingPenLayout mPenSettingView;
    private SpenSettingRemoverLayout mEraserSettingView;
    private int mToolType;
    private LinearLayout bookmarkLayout;
    private RecyclerView bookmarkRecyvler;
    private static List<DMKBookmark> bookmarkList;
    private static BookmarkAdapter bookmarkAdapter;

    String ftpHost, ftpUser, ftpPassword;
    int ftpPort;
    String userid, servid, brmid, nobrm, idpoli, namapoli;
    Integer idBerkas;
    APIService APIClient;
    private static int totalPage, currentPage;
    String notePath, noteFilename, spdFileName, nama_dir;
    File noteFile;
    SpenNoteDoc mSpenNoteDoc;
    SpenSurfaceView mSpenSurfaceView;
    SpenPageDoc mSpenPageDoc;
    View rootView;
    FloatingActionButton fabnxt, fabprv;
    TextView txtPageIdx;
    boolean isCoding = false;

    public static PoliSurfaceFragment newInstance(boolean spen, String uid, String brm, String poli, String namapoli, int idBerkas, boolean isCoding) {
        PoliSurfaceFragment fragment = new PoliSurfaceFragment();
        Bundle args = new Bundle();
        args.putBoolean("spen", spen);
        args.putString("uid", uid);
        args.putString("brm", brm);
        args.putString("poli", poli);
        args.putString("namapoli", namapoli);
        args.putInt("idBerkas", idBerkas);
        args.putBoolean("isCoding", isCoding);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        spenOn = getArguments().getBoolean("spen");
        userid = getArguments().getString("uid");
        servid = getArguments().getString("srv_id");
        brmid = getArguments().getString("brm_id");
        nobrm = getArguments().getString("brm");
        idpoli = getArguments().getString("poli");
        namapoli = getArguments().getString("namapoli");
        idBerkas = getArguments().getInt("idBerkas");
        isCoding = getArguments().getBoolean("isCoding", false);
        noteFilename = nobrm + "_" + idpoli + "_" + idBerkas + ".spd";
        ARCHIVE_PATH = getContext().getFilesDir() + "/archives/";
        ARCHIVE_FILE = new File(ARCHIVE_PATH);

        ftpHost = ApiServiceGenerator.getBaseFtp(requireActivity().getApplicationContext());
        ftpPort = ApiServiceGenerator.getBaseFtpPort();
        ftpUser = ApiServiceGenerator.getUserFtp();
        ftpPassword = ApiServiceGenerator.getPasswordFtp();

        Log.e("X-DEBUG","spenOn : "+spenOn+",userid : "+userid+
                ",servid : "+servid+",brmid : "+brmid+",nobrm : "+nobrm+",idpoli : "+idpoli+
                ",namapoli : "+namapoli+",idBerkas : "+idBerkas+",noteFile : "+noteFile);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_poli_surface, container, false);
        rootView = view;
        Toolbar toolb = rootView.findViewById(R.id.toolBar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolb);
        fabnxt = rootView.findViewById(R.id.fabnxt);
        fabprv = rootView.findViewById(R.id.fabprv);
        txtPageIdx = rootView.findViewById(R.id.pageidx);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("BRM " + nobrm + " - " + namapoli);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        fabnxt.setVisibility(View.GONE);
        fabprv.setVisibility(View.GONE);
        txtPageIdx.setVisibility(View.GONE);
        txtPageIdx.setText("");

        initSpen();
        initRetrofit();
        initBookmark();
        BaseActivity.Baseprogress.showProgressDialog(getActivity(), "Initiating Download ...");
        return view;
    }

    private void initBookmark() {
        bookmarkLayout = rootView.findViewById(R.id.layout_bookmark);

        bookmarkRecyvler = rootView.findViewById(R.id.recView);
        bookmarkRecyvler.setHasFixedSize(true);

        LinearLayoutManager mManager = new LinearLayoutManager(getContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);

        bookmarkList = new ArrayList<>();

        getBookmark();

        bookmarkAdapter = new BookmarkAdapter(bookmarkList) {
            @Override
            protected void onBindViewHolder(@NonNull BookmarkAdapter.BookmarkHolder holder, int position, @NonNull DMKBookmark model) {
                holder.itemView.setOnClickListener(view -> {
                    int page = model.getPage();
                    Toast.makeText(getContext(), "Page: " + page, Toast.LENGTH_SHORT).show();
                    currentPage = page - 1;
                    mSpenPageDoc = mSpenNoteDoc.getPage(currentPage);
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    hideBookmark();
                });
                holder.bindInfo(model);
            }
        };

        bookmarkRecyvler.setLayoutManager(mManager);
        bookmarkRecyvler.setAdapter(bookmarkAdapter);
    }

    private void updateBookmarkList(List<DMKBookmark> bookmarks) {
        bookmarkList.clear();
        bookmarkList.addAll(bookmarks);
        bookmarkAdapter.notifyDataSetChanged();
    }

    private void initRetrofit() {
        APIClient = APIUtils.getAPIService(getContext());
    }

    private void getBookmark() {
        Call<BookmarkResponse> bookmarkResponseCall = APIClient.getBookmark(userid, nobrm);

        bookmarkResponseCall.enqueue(new Callback<BookmarkResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookmarkResponse> call, @NonNull Response<BookmarkResponse> response) {
                if (response.isSuccessful()) {
                    if (Objects.requireNonNull(response.body()).getStatuscode() == 200) {
                        if (Objects.requireNonNull(response.body()).getBookmarks().size() > 0) {
                            updateBookmarkList(Objects.requireNonNull(response.body()).getBookmarks());
                        } else {
                            bookmarkList.clear();
                            bookmarkAdapter.notifyDataSetChanged();
                        }
                    } else {
                        toastErr(getContext(), Objects.requireNonNull(response.body()).getMessage());
                    }
                } else {
                    toastErr(getContext(), response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookmarkResponse> call, @NonNull Throwable t) {
                toastErr(getContext(), t.getLocalizedMessage());
            }
        });
    }

    //baru
    public void downloadSpd(){
        Call<ResponseBody> callDownload = APIClient.downloadBRMDokter(userid, idBerkas);
        callDownload.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    if(response.code()==200){
                        String check = response.headers().get("X-Filename");
                        String[] woke = check.split("/");
                        spdFileName = woke[1];
                        nama_dir = woke[0];

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

                        BaseActivity.Baseprogress.hideProgressDialog();
                        if(statuscode != null && message != null) {
                            Toast.makeText(getContext(), "Response code: " + statuscode + ". " + message, Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getContext(), "Something went wrong. Unable to download document", Toast.LENGTH_LONG).show();
                        }
                        getActivity().finish();
                    }
                }else{
                    Toast.makeText(getContext(), response.message()+": "+ ApiError.parseError(response).getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("[X-DEBUG]",response.message());
                    BaseActivity.Baseprogress.hideProgressDialog();
                }

            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.e("[X-DEBUG]",t.getLocalizedMessage());
                Log.e("[X-DEBUG]","downloadBRM() - onFailed Triggered");
                BaseActivity.Baseprogress.hideProgressDialog();
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

                File outDir = new File(getContext().getFilesDir() + "/archives/");
                File outFile = new File(getContext().getFilesDir()+"/archives/", nobrm+"_"+idpoli+".spd");

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
                BaseActivity.Baseprogress.hideProgressDialog();
                Log.e("X-DEBUG", "EXCEPTION: "+e.getLocalizedMessage());
                e.printStackTrace();

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }

            if(error) {
                Log.e("X-DEBUG", "OPERATION FAILED");

                return null;
            }else{
                Log.e("X-DEBUG", "OPERATION SUCCESS");
            }

//            File outFile = new File(getContext().getFilesDir()+"/tmp/", spdFilename +".spd");
            File outFile = new File(getContext().getFilesDir()+"/archives/", nobrm+"_"+idpoli+".spd");

            return outFile.getPath();
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String path) {
            BaseActivity.Baseprogress.hideProgressDialog();
            loadNoteFile(path);
        }
    }


    private void downloader(String filename) {
        APIClient = APIUtils.getAPIService(getActivity());
        Call<ResponseBody> listPoliResponseCall = APIClient.downloadBRMDokter(userid, idBerkas);
        FileDownloader fd = new FileDownloader(getActivity(), listPoliResponseCall, filename);
        fd.setOnFileDownloadListener(new FileDownloader.FileDownloadListener() {
            @Override
            public void onStart(String msg) {
                BaseActivity.Baseprogress.hideProgressDialog();
                BaseActivity.Baseprogress.showProgressDialog(getActivity(), msg);
            }

            @Override
            public void onProgress(String status) {
                BaseActivity.Baseprogress.setMessage(status);
            }

            @Override
            public void onError(String msg) {
                BaseActivity.Baseprogress.hideProgressDialog();
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete(String filePath) {
                BaseActivity.Baseprogress.hideProgressDialog();
                loadNoteFile(filePath);
            }
        });
        fd.startDownload();
    }

    private void loadNoteFile(String filePath) {
        notePath = filePath;
        if (notePath != null) {
            noteFile = new File(notePath);
            noteFilename = noteFile.getName();

            try {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Rect mScreenRect = new Rect();
                display.getRectSize(mScreenRect);
                SpenNoteDoc tmpSpenNoteDoc = new SpenNoteDoc(getActivity(), notePath, mScreenRect.width(), SpenNoteDoc.MODE_WRITABLE, true);
                mSpenNoteDoc.close();
                mSpenNoteDoc = tmpSpenNoteDoc;
                mSpenPageDoc = mSpenNoteDoc.getPage(0);
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                mSpenSurfaceView.update();
                currentPage = 0;
                totalPage = mSpenNoteDoc.getPageCount();
                fabnxt.setVisibility(View.VISIBLE);
                fabprv.setVisibility(View.VISIBLE);
                String info = "Page " + (currentPage + 1) + " of " + totalPage;
                txtPageIdx.setVisibility(View.VISIBLE);
                txtPageIdx.setText(info);
                mSpenPageDoc.setHistoryListener(historyListener);
            } catch (IOException ex) {
                Log.e("~Debug [LoadException1]", ex.getMessage() + "");
            } catch (SpenUnsupportedTypeException ex2) {
                Log.e("~Debug [LoadException2]", ex2.getMessage() + "");
            } catch (SpenUnsupportedVersionException ex3) {
                Log.e("~Debug [LoadException3]", ex3.getMessage() + "");
            }

            fabnxt.setOnClickListener(v -> {
                currentPage = currentPage + 1;
                showPage(currentPage);
            });

            fabprv.setOnClickListener(v -> {
                currentPage = currentPage - 1;
                showPage(currentPage);
            });
        }
    }

    private void showPage(int index) {
        if (index == -1) {
            currentPage = totalPage - 1;
            mSpenPageDoc = mSpenNoteDoc.getPage(currentPage);
            mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
            mSpenSurfaceView.update();
        } else {
            try {
                mSpenPageDoc = mSpenNoteDoc.getPage(index);
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                mSpenSurfaceView.update();
            } catch (Exception ex) {
                if (ex.getMessage().equals("E_OUT_OF_RANGE")) {
                    currentPage = 0;
                    mSpenPageDoc = mSpenNoteDoc.getPage(currentPage);
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                    mSpenSurfaceView.update();
                }
            }
        }
        String info = "Page " + (currentPage + 1) + " of " + totalPage;
        txtPageIdx.setText(info);
    }

    private final SpenPageDoc.HistoryListener historyListener = new SpenPageDoc.HistoryListener() {
        @Override
        public void onCommit(SpenPageDoc spenPageDoc) {

        }

        @Override
        public void onUndoable(SpenPageDoc spenPageDoc, boolean b) {
            Log.e("[X-DEBUG]", "Undoable: " + b);
        }

        @Override
        public void onRedoable(SpenPageDoc spenPageDoc, boolean b) {
            Log.e("[X-DEBUG]", "Redoable: " + b);
        }
    };

    private final SpenColorPickerListener mColorPickerListener = new SpenColorPickerListener() {
        @Override
        public void onChanged(int color, int x, int y) {
            // Set the color from the Color Picker to the setting view.
            if (mPenSettingView != null) {
                Log.e("[X-DEBUG]", "mPenSettingView isn't null");
                SpenSettingPenInfo penInfo = mPenSettingView.getInfo();
                penInfo.color = color;
                mPenSettingView.setInfo(penInfo);
                mPenSettingView.savePreferences();
            } else {
                Log.e("[X-DEBUG]", "mPenSettingView is null");
            }
        }
    };

    private SpenTouchListener onPreTouchSurfaceViewListener = new SpenTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (mPenSettingView.isShown()) {
                        mPenSettingView.setVisibility(SpenSurfaceView.GONE);
                    }

                    if (mEraserSettingView.isShown()) {
                        mEraserSettingView.setVisibility(SpenSurfaceView.GONE);

                        penEnable = false;

                        canvasMenu.findItem(R.id.action_eraser_tools).getIcon().setTint(Color.WHITE);
                    }

                    if (!penEnable && !eraserEnable) {
                        Toast.makeText(getContext(), "Pen dinonaktifkan. Nyalakan pen mode terlebih dahulu.", Toast.LENGTH_LONG).show();
                    } else {
                        saveState = false;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
            }
            return false;
        }
    };

    private void initSpen() {
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();


        try {
            spenPackage.initialize(getContext());
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (Exception e1) {
            Toast.makeText(getContext(), "Tidak dapat menginisialisasi Spen",
                    Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
        }

        if (!isSpenFeatureEnabled) {
            Toast.makeText(getContext(), "Spen Tidak Ditemukan!", Toast.LENGTH_SHORT).show();
        }

        //init View
        FrameLayout spenViewContainer = rootView.findViewById(R.id.spenViewContainer2);
        RelativeLayout surface = rootView.findViewById(R.id.spenViewLayout);

        mPenSettingView = new SpenSettingPenLayout(getContext(), "", surface);
        mEraserSettingView = new SpenSettingRemoverLayout(getContext(), "", surface);

        spenViewContainer.addView(mPenSettingView);
        mSpenSurfaceView = new SpenSurfaceView(getContext());
        mSpenSurfaceView.setToolTipEnabled(true);
        surface.addView(mSpenSurfaceView);

        if (mSpenSurfaceView == null) {
            Toast.makeText(getContext(), "Cannot create new SpenView.",
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        mPenSettingView.setCanvasView(mSpenSurfaceView);
        mEraserSettingView.setCanvasView(mSpenSurfaceView);

        // Get the dimension of the device screen.
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Rect mScreenRect = new Rect();
        display.getRectSize(mScreenRect);
        // Create SpenNoteDoc
        mSpenNoteDoc = null;
        try {
            mSpenNoteDoc = new SpenNoteDoc(getContext(), mScreenRect.width(), mScreenRect.height());
        } catch (IOException e) {
            Toast.makeText(getContext(), "Cannot create new NoteDoc.",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            getActivity().finish();
        } catch (Exception e) {
            e.printStackTrace();
            getActivity().finish();
        }

        SpenPageDoc page = mSpenNoteDoc.appendPage();
        mSpenSurfaceView.setPageDoc(page, false);

        // Initialize Pen settings
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        mSpenSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);
        mPenSettingView.loadPreferences();
        mSpenSurfaceView.setColorPickerListener(mColorPickerListener);
        mSpenSurfaceView.setPreTouchListener(onPreTouchSurfaceViewListener);

        SpenSettingRemoverInfo removerInfo = new SpenSettingRemoverInfo();
        removerInfo.size = 1;
        removerInfo.type = SpenSettingRemoverInfo.CUTTER_TYPE_CUT;
        mSpenSurfaceView.setRemoverSettingInfo(removerInfo);
        mEraserSettingView.setInfo(removerInfo);

        mToolType = SpenSettingViewInterface.TOOL_SPEN;

        mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_NONE);
    }

    private void hideBookmark() {
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

    private void showBookmark() {
        backState |= BOOKMARK_STATE;
        bookmarkToggle.getIcon().setTint(getResources().getColor(R.color.colorPrimaryDark));
        bookmarkLayout.setVisibility(View.VISIBLE);
        // Start the animation
        bookmarkLayout.animate().translationY(0).setListener(null);

    }

    private void alertExit(final String fileName) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                .setTitle("Konfirmasi Keluar")
                .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                .setMessage("Pekerjaan anda sudah disimpan, namun BRM masih belum di-Checkout. Yakin ingin keluar ?")
                .setPositiveButton("Simpan", (dialogInterface, i) -> {

                    File filePath = new File(getContext().getFilesDir() + "/archives/");
                    if (!filePath.exists()) {
                        if (!filePath.mkdirs()) {
                            Toast.makeText(getContext(), "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (!fileName.equals("")) {

                    } else {
                        Toast.makeText(getContext(), "Invalid filename !!!", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Keluar", (dialogInterface, i) -> {
                    purgeAllFiles(ARCHIVE_FILE);
                    getActivity().finish();
                });
        dialog.create();
        dialog.show();
    }

    private static void purgeAllFiles(File rootParentDir) {
        if (rootParentDir.exists()) {
            if (rootParentDir.isDirectory()) {
                String[] children = rootParentDir.list();

                if (Objects.requireNonNull(children).length > 0) {
                    for (String child : children) {
                        File f = new File(rootParentDir, child);

                        if (f.isDirectory()) {
                            purgeAllFiles(f);
                        } else {
                            if (f.delete()) {
                                Log.d("D-Log", "File on " + f.getAbsolutePath() + " has been purged");
                            } else {
                                Log.d("D-Log", "Failed to purge file on " + f.getAbsolutePath());
                            }
                        }
                    }
                }
            }

            if (rootParentDir.delete()) {
                Log.d("D-Log", "File on " + rootParentDir.getAbsolutePath() + " has been purged");
            } else {
                Log.d("D-Log", "Failed to purge file on " + rootParentDir.getAbsolutePath());
            }
        } else {
            Log.d("D-Log", "Failed to purge file on " + rootParentDir.getAbsolutePath() + ". File not found.");
        }
    }

    public void saveBRM() {

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Checkout BRM")
                .setIcon(getResources().getDrawable(R.drawable.ic_check_primary))
                .setMessage("Apakah anda yakin ingin Menyimpan BRM ini?\n\nPeringatan: Aksi ini tidak dapat dibatalkan.")
                .setPositiveButton("Simpan", (dialogInterface, i) -> {
//                        uploadFile(kode_file, true, false, false, null);
                    new savePage().execute();

                })
                .setNegativeButton("Tidak", null)
                .create();

        dialog.show();
    }

    //save
    private  class savePage extends AsyncTask<String, Boolean, Boolean> {


        Logger log = new Logger();



        @Override
        protected void onPreExecute() {
            BaseActivity.Baseprogress.showProgressDialog(getContext(), "Initiating Save ...");
        }

        @Override
        protected Boolean doInBackground(String... args) {
            log.x("{savePage} doInBackground");
            String saveFilePath = ARCHIVE_PATH +  nobrm+"_" +idpoli+ ".spd";
            boolean rtrn = false;

            try {
                // Save NoteDoc
                mSpenNoteDoc.save(saveFilePath, false);


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

            if (args) {
                new uploadCoding().execute();
                Toast.makeText(getContext(), "Berhasil Menyimpan Berkas", Toast.LENGTH_SHORT).show();
            }
        }

    }

    //TODO: NEW FTP FUNCTION
    private final class uploadCoding extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            BaseActivity.Baseprogress.setMessage("Initiating Upload ...");
        }

        @Override
        protected String doInBackground(String... strings) {
            String saveFilePath = ARCHIVE_PATH  + nobrm+"_" +idpoli+ ".spd";

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

                File asset = new File(saveFilePath);
                BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(asset));
                Log.e("X-DEBUG", "saveFilePath" +saveFilePath);
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
                Log.e("X-DEBUG", "OPERATION SUCCESS");
            }

            if(error){
                return "failed";
            }else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s!=null){
                setStatusCoded();
            }else{
                BaseActivity.Baseprogress.hideProgressDialog();
            }
        }

        private void setStatusCoded() {
            APIClient.setDMRtoOnCoding(Integer.parseInt(servid), userid, 7, null).enqueue(new Callback<ApiStatus>() {
                @Override
                public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                    if(response.isSuccessful()) {
                        Log.e("[X-DEBUG]", "Post data submitted to the API.");

                        if(Objects.requireNonNull(response.body()).getStatuscode() == 200) {
                            doneState = true;
                            Toast.makeText(getContext(), "Dokumen berhasil dikoding", Toast.LENGTH_LONG).show();
                            Log.e("[X-DEBUG]", "[-] coding brm result: "+response.body().getMessage());
                        }else{
                            Toast.makeText(getContext(), "Dokumen gagal dikoding. ResponseCode:"+ApiError.parseError(response).getStatuscode()+". ResponseMessage:"+ApiError.parseError(response).getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("[X-DEBUG]", "[-] coding brm failed. Message: "+response.raw().toString());
                        }
                    }else{
                        Log.e("[X-DEBUG]", "[-] coding brm isn't successful. ErrorCode:"+response.code()+" ErrorMessage:"+response.message());
                    }

                    BaseActivity.Baseprogress.hideProgressDialog();
                }

                @Override
                public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                    Log.e("[X-DEBUG]", "Unable to submit post to the API. Error: "+t.getMessage());
                    Log.e("[X-DEBUG]", "[-] coding brm onFailure thrown. Error: "+t.getLocalizedMessage());
                    Toast.makeText(getContext(), "Gagal koding dokumen. Silahkan coba lagi", Toast.LENGTH_LONG).show();

                    BaseActivity.Baseprogress.hideProgressDialog();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            noteFile = new File(getActivity().getFilesDir() + "/archives/", noteFilename);
            if (!noteFile.exists()) {
                //TODO: UBAH KE FTP
//                downloader(noteFilename);
                downloadSpd();
                Log.d("X-LOG", "onStart: DOWNLOADER");
            } else {
                notePath = noteFile.getPath();

                loadNoteFile(notePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        try {
            mSpenNoteDoc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        canvasMenu = menu;
        Log.d("LOG_D", "onCreateOptionsMenu: " + canvasMenu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_spen: {
                if (penEnable) {
                    mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_NONE);
                    penEnable = false;
                    item.getIcon().setTint(Color.WHITE);

                    if (mPenSettingView.isShown()) {
                        mPenSettingView.setVisibility(View.GONE);
                    }
                } else {
                    item.getIcon().setTint(getResources().getColor(R.color.colorPrimaryDark));
                    penEnable = true;
                    mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_STROKE);

                    if (mPenSettingView.isShown()) {
                        mPenSettingView.setVisibility(View.GONE);
                    } else {
                        mPenSettingView.setVisibility(View.VISIBLE);
                    }

                    if (eraserEnable) {
                        eraserEnable = false;
                        canvasMenu.findItem(R.id.action_eraser_tools).getIcon().setTint(Color.WHITE);
                    }
                }

                break;
            }
            case R.id.action_eraser_tools: {
                if (mPenSettingView.isShown()) {
                    mPenSettingView.setVisibility(View.GONE);
                }

                if (eraserEnable) {
                    mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_NONE);
                    eraserEnable = false;
                    item.getIcon().setTint(Color.WHITE);
                } else {
                    mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE_REMOVER);
                    eraserEnable = true;
                    item.getIcon().setTint(getResources().getColor(R.color.colorPrimaryDark));

                    if (penEnable) {
                        penEnable = false;
                        canvasMenu.findItem(R.id.action_spen).getIcon().setTint(Color.WHITE);
                    }
                }

                break;
            }
            case R.id.action_bookmark: {
                bookmarkToggle = item;
                if ((backState & BOOKMARK_STATE) != 0) {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_bookmark_list));
                    hideBookmark();
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_bookmark_fill_white));
                    showBookmark();
                    BaseActivity.Baselog.d("onClick: SHOW BOOKMARK");
                }
                break;
            }
            case R.id.action_exit: {
                Log.e("[X-DEBUG]", "saveState value: " + saveState);
                Log.e("[X-DEBUG]", "undoable value: " + isUndoable);
                Log.e("[X-DEBUG]", "redoable value: " + isRedoable);

                if (mSpenPageDoc.isUndoable() || saveState) {
                    if (!doneState) {
                        alertExit(noteFilename);
                    } else {
                        purgeAllFiles(ARCHIVE_FILE);
                        getActivity().finish();
                    }
                } else {
                    purgeAllFiles(ARCHIVE_FILE);
                    getActivity().finish();
                }
                break;
            }

            case R.id.action_save: {
                saveBRM();
                break;
            }
            case R.id.action_undo: {
                if (mSpenPageDoc.isUndoable()) {
                    SpenPageDoc.HistoryUpdateInfo[] userData = mSpenPageDoc.undo();
                    mSpenSurfaceView.updateUndo(userData);
                }
                break;
            }

            case R.id.action_redo: {
                if (mSpenPageDoc.isRedoable()) {
                    SpenPageDoc.HistoryUpdateInfo[] userData = mSpenPageDoc.redo();
                    mSpenSurfaceView.updateRedo(userData);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
