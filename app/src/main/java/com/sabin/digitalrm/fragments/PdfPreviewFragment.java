package com.sabin.digitalrm.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class PdfPreviewFragment extends Fragment implements View.OnClickListener{
    private static String STATE_CURRENT_PAGE_INDEX = "current_page_index";
    private static String id, no_brm, id_poli;
    private static int idBerkas;

    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;

    private ImageView mImageView;
    private FloatingActionButton navPrev, navNext;
    private Context mContext;
    private APIService APIClient;

    private File PdfFile;
    private String PdfPath, fname, filename;

    private int scaleFactor = 2;
    private int currentPage;

    MenuItem rootMenu, lastMenu;

    //private String path;
    private int mPageIndex;

    public PdfPreviewFragment() {
        // Required empty public constructor
    }

    public static PdfPreviewFragment newInstance(String uid, String brm, String poli, int idBerkas) {
        PdfPreviewFragment fragment = new PdfPreviewFragment();
        Bundle args = new Bundle();
        args.putString("uid", uid);
        args.putString("brm", brm);
        args.putString("poli", poli);
        args.putInt("id_berkas", idBerkas);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        id = requireArguments().getString("uid");
        no_brm = getArguments().getString("brm");
        id_poli = getArguments().getString("poli");
        idBerkas = getArguments().getInt("id_berkas");
        fname = idBerkas+"_"+no_brm+".pdf";
        mContext = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pdf_preview, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.tbarpdfprev);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle("BRM Lama - "+no_brm);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageView = view.findViewById(R.id.pdf_image);
        navPrev = view.findViewById(R.id.fab_prev);
        navNext = view.findViewById(R.id.fab_next);

        navPrev.setOnClickListener(this);
        navNext.setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_pdf_preview, menu);
        MenuItem submenu = menu.findItem(R.id.action_ratio);
        inflater.inflate(R.menu.submenu_aspect_ratio, submenu.getSubMenu());
        rootMenu = menu.findItem(R.id.action_ratio);
        lastMenu = menu.findItem(R.id.action_normal);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_normal:{
                scaleFactor = 2;
                rootMenu.setTitle("Normal Quality");
                refreshPage();
                return true;
            }

            case R.id.action_medium:{
                scaleFactor = 4;
                rootMenu.setTitle("Medium Quality");
                refreshPage();
                return true;
            }
            
            case R.id.action_high:{
                scaleFactor = 6;
                rootMenu.setTitle("High Quality");
                refreshPage();
                return true;
            }

        }

        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            File file = new File(mContext.getFilesDir()+"/archives/", fname);
            if(!file.exists()) {
                downloadPDF();
            }else{
                PdfFile = file;
                PdfPath = PdfFile.getPath();

                openRenderer(mContext);
                showPage(mPageIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        try {
            closeRenderer();
//            purgeFile(PdfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_next:{
                showPage(mCurrentPage.getIndex() + 1);
                break;
            }

            case R.id.fab_prev:{
                showPage(mCurrentPage.getIndex() - 1);
                break;
            }
        }
    }

    private void purgeFile(File file){
        try{
            file.delete();
            Log.e("~Debug", "File Purged");
        }catch (Exception ex){
            Log.e("~Debug", "Failed to purge the file. "+ex.getMessage());
        }
    }

    private void initRetrofit(){
        BaseActivity.Baselog.d("Initretrofit");
        APIClient = APIUtils.getAPIService(mContext);
    }

    public void downloadPDF(){
        Log.e("[X-DEBUG]", "id_berkas to download: "+idBerkas);
        BaseActivity.Baseprogress.showProgressDialog(mContext, "Initiating Download ...");

        initRetrofit();

        Call<ResponseBody> callDownload = APIClient.downloadOldBRMPDF(id, idBerkas);
        callDownload.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    ResponseBody responseBody = response.body();
                    if(responseBody.contentLength()!=-1){
                        //TODO: UBAH KE FTP
                        new downloadPDFAsync().execute(responseBody);
                    }else{
                        String statuscode  = null;
                        String message = null;

                        try {
                            JSONArray jar = new JSONArray(responseBody);
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
                        getActivity().finish();
                    }
                }else{
                    Toast.makeText(mContext, response.message()+": "+ ApiError.parseError(response).getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("~Debug",response.message());
                }
                Log.e("~Debug","downloadPDF() - onResponse Triggered");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.e("~Debug",t.getLocalizedMessage());
                Log.e("~Debug","downloadPDF() - onFailed Triggered");
            }
        });
    }

    public class downloadPDFAsync extends AsyncTask<ResponseBody, Integer, String> {

        @Override
        protected String doInBackground(ResponseBody... responseBodies) {
            Log.e("~Debug","downloadPDFAsync() - doInBackground Triggered");
            int count;
            byte data[] = new byte[1024 * 4];

            ResponseBody body = responseBodies[0];

            long fileSize = body.contentLength();
            BaseActivity.Baselog.d("Download start: " + fileSize);

            InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
            File outputDir = new File(mContext.getFilesDir() + "/archives/");
            File outputFile = new File(mContext.getFilesDir()+"/archives/", fname);

            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                    cancel(true);
                }
            }

            Log.e("~Debug","downloadPDFAsync() - File size: "+fileSize);
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
                        BaseActivity.Baselog.d("Progress " + progress);
                        publishProgress(progress);
                        timeCount++;
                    }

                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                bis.close();
            }catch (Exception e){
                BaseActivity.Baselog.d("Error " + e.getLocalizedMessage());
            }

            PdfFile = outputFile;
            return outputFile.getPath();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            BaseActivity.Baseprogress.setMessage("Download Progress: " + values[0] + "%");
        }

        @Override
        protected void onPostExecute(String path) {

            Log.e("~Debug","downloadPDFAsync() - onPostExecute Triggered");
            BaseActivity.Baselog.d("Path " + path);


            try {
                PdfPath = path;
                openRenderer(mContext);
                BaseActivity.Baseprogress.hideProgressDialog();
            }catch (Exception e){
                BaseActivity.Baselog.d("Exception " + e.getMessage());
                BaseActivity.Baseprogress.hideProgressDialog();
                Toast.makeText(mContext, "Terjadi kesalahan. File dokumen rusak", Toast.LENGTH_LONG).show();
                requireActivity().finish();
            }
        }
    }

    private void openRenderer(Context context) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        mFileDescriptor = ParcelFileDescriptor.open(PdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (mFileDescriptor != null) {
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        }

        showPage(mPageIndex);
    }

    private void closeRenderer() throws IOException {
        if (mCurrentPage != null) {
            mCurrentPage.close();
        }
        if(mPdfRenderer != null) {
            mPdfRenderer.close();
        }
        if(mFileDescriptor != null) {
            mFileDescriptor.close();
        }
    }

    private void showPage(int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            Toast.makeText(mContext, "Halaman Terahir", Toast.LENGTH_SHORT).show();
            return;
        }else if(index < 0){
            Toast.makeText(mContext, "Halaman Pertama", Toast.LENGTH_SHORT).show();
            return;
        }
        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        mCurrentPage = mPdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        Log.d("X-LOG", "showPage: " + mCurrentPage.getWidth());
        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth() * scaleFactor, mCurrentPage.getHeight() * scaleFactor,
                Bitmap.Config.ARGB_8888);
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // We are ready to show the Bitmap to user.
        mImageView.setImageBitmap(bitmap);
        updateUi();
    }

    private void refreshPage(){
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth() * scaleFactor, mCurrentPage.getHeight() * scaleFactor,
                Bitmap.Config.ARGB_8888);
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // We are ready to show the Bitmap to user.
        mImageView.setImageBitmap(bitmap);
        updateUi();
    }

    /**
     * Updates the state of 2 control buttons in response to the current page index.
     */
    private void updateUi() {
        int index = mCurrentPage.getIndex();
        int pageCount = mPdfRenderer.getPageCount();
        navPrev.setEnabled(0 != index);
        navNext.setEnabled(index + 1 < pageCount);
        requireActivity().setTitle("Arsip BRM " + (index + 1) + "/" + pageCount);
    }


}
