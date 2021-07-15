package com.sabin.digitalrm.prm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.helpers.FileDownloader;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiResponse;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.FieldList;
import com.sabin.digitalrm.models.FieldNameListResponse;
import com.sabin.digitalrm.models.GenTextSetting;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.StringWithTag;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingViewInterface;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectTextBox;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.document.textspan.SpenFontSizeSpan;
import com.samsung.android.sdk.pen.document.textspan.SpenLineSpacingParagraph;
import com.samsung.android.sdk.pen.document.textspan.SpenTextParagraphBase;
import com.samsung.android.sdk.pen.document.textspan.SpenTextSpanBase;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.engine.SpenView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FieldSelectorActivity extends BaseActivity {
    public static final String DMK_POS = "POS";
    private File SpdFile;
    private Context ctx;
    private APIService APIClient;
    private PopupWindow popupWindow;
    private RelativeLayout.LayoutParams lp;
    private View popupView;
    private ImageView iv;
    private static List<StringWithTag> fieldLists;
    private int PG_NEXT = 2;
    private int PG_PREV = 1;
    Rect mScreenRect;
    SpenNoteDoc mSpenNoteDoc;
    SpenPageDoc mSpenPageDoc;
    SpenView mSpenSurfaceView;
    SpenObjectTextBox textObj;
    SharedPreferences pref;
    RelativeLayout canv;
    StringWithTag s;
    Object tag;
    String SpdPath, uid, filename, namapoli;
    int id, spinnerSelectedId, txtStyle, currentPage, totalPage;
    float txtSize;
    boolean selecting;
    boolean bold, italic, underline;
    List<GenTextSetting> allSettings;

    private int dmkPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_selector);

        Toolbar tb = findViewById(R.id.tbarfldsel);

        setSupportActionBar(tb);
        ctx = FieldSelectorActivity.this;
        pref = getSharedPreferences("SESSION_", 0);
        Bundle bundle = getIntent().getExtras();

        canv = findViewById(R.id.canvas);

        id = bundle.getInt("id");
        filename = bundle.getString("filename");
        uid = pref.getString("uid", null);
        namapoli = bundle.getString("namapoli");
        dmkPos = bundle.getInt(DMK_POS);

        getSupportActionBar().setTitle("Tambah Koordinat Field  -  DMK "+namapoli);

        fieldLists = new ArrayList<>();

        Log.e("[X-DEBUG]", "UID:"+uid+" PoliID:"+id+" Filename:"+filename);

        initSpen();
        initSpenView();
        download();

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.dialog_set_field_coordinate, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void initSpen(){
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        }catch (Exception e1) {
            Toast.makeText(ctx, "Tidak dapat menginisialisasi Spen",
                    Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
//            finish();
        }

        if(!isSpenFeatureEnabled){
            Toast.makeText(ctx, "Spen Tidak Ditemukan!", Toast.LENGTH_SHORT).show();
//            finish();
        }
    }

    private void initSpenView(){
        // Create Spen View
        RelativeLayout spenViewLayout = findViewById(R.id.canvas);


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

        // Initialize Pen settings
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        mSpenSurfaceView.setPenSettingInfo(penInfo);

        // DISABLE SPEN
        mSpenSurfaceView.setToolTypeAction(SpenSettingViewInterface.TOOL_SPEN, SpenSettingViewInterface.ACTION_NONE);
    }

    private void download(){
        APIClient = APIUtils.getAPIService(ctx);

        Log.d("X-LOG", "download: fname " + filename);

        Call<ResponseBody> callDownload = APIClient.downloadDMK(id, PetugasMainActivity.UID);
        FileDownloader downloader = new FileDownloader(ctx, callDownload, filename);
        downloader.setBaseDir("tmp");
        downloader.setOnFileDownloadListener(new FileDownloader.FileDownloadListener() {
            @Override
            public void onStart(String msg) {
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
                    SpdPath = filePath;
                    openDoc(SpdPath);
                    BaseActivity.Baseprogress.hideProgressDialog();
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
            allSettings = new ArrayList<>();
            SpenNoteDoc tmpSpenNoteDoc = new SpenNoteDoc(ctx, FilePath, mScreenRect.width(),
                    SpenNoteDoc.MODE_WRITABLE, true);
            mSpenNoteDoc.close();
            mSpenNoteDoc = tmpSpenNoteDoc;
            mSpenPageDoc = mSpenNoteDoc.getPage(0);
            mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
            mSpenSurfaceView.setZoomable(false);
            mSpenSurfaceView.setPreTouchListener(onPreTouchSurfaceViewListener);
            mSpenSurfaceView.update();
            currentPage = 0;
            totalPage = mSpenNoteDoc.getPageCount();
        }catch (Exception ex){
            Log.e("[X-EXCEPTION]", "Error: "+ex.getMessage());
        }
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

    private SpenTouchListener onPreTouchSurfaceViewListener = (view, event) -> {
        // TODO Auto-generated method stub
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if(!popupWindow.isShowing()) {
                    PointF canvasPos = getCanvasPoint(event);
                    float x = canvasPos.x;
                    float y = canvasPos.y;

                    if (fieldLists.isEmpty()) {
                        getFieldNameList(view, x, y);
                    } else {
                        popupDialog(view, x, y);
                    }
                }

                break;
            }
            case MotionEvent.ACTION_MOVE:
        }
        return false;
    };

    private void getFieldNameList(View v, float x, float y){
        BaseActivity.Baseprogress.showProgressDialog(ctx, "Getting Field List ...");

        APIClient = APIUtils.getAPIService(ctx);

        Call<List<FieldList>> call = APIClient.getGenTextList(PetugasMainActivity.UID);
        call.enqueue(new Callback<List<FieldList>>() {
            @Override
            public void onResponse(Call<List<FieldList>> call, Response<List<FieldList>> response) {
                BaseActivity.Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    fieldLists.clear();
                    fieldLists.add(new StringWithTag("-- Pilih Nama Field --", "0"));
                    for(FieldList item : response.body()){
                        fieldLists.add(new StringWithTag(item.getFieldAlias(), item.getId()));
                    }

                    popupDialog(v, x, y);
                }else {
                    String msg = ApiError.parseError(response).getMessage();
                    Log.d(TAG, "onResponse: " + msg);
                    toastErr(ctx, msg);
                }
            }

            @Override
            public void onFailure(Call<List<FieldList>> call, Throwable t) {
                BaseActivity.Baseprogress.hideProgressDialog();
                toastErr(ctx, t.getLocalizedMessage());
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

/*        callDownload.enqueue(new Callback<FieldNameListResponse>() {
            @Override
            public void onResponse(Call<FieldNameListResponse> call, Response<FieldNameListResponse> response) {
                BaseActivity.Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    FieldNameListResponse responseBody = response.body();
                    if(responseBody.getStatuscode() == 200){
                        for (int i = 0; i < responseBody.getData().size(); i++){
                            if(i==0){
                                fieldLists.clear();
                                fieldLists.add(new StringWithTag("-- Pilih Nama Field --", "0"));
                            }

                            fieldLists.add(new StringWithTag(responseBody.getData().get(i).getField_name(), String.valueOf(responseBody.getData().get(i).getId())));
                        }

                        popupDialog(v, x, y);
                    }else{
                        Toast.makeText(ctx, "Terjadi kesalahan: "+responseBody.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("~Debug", "Response: "+responseBody.getMessage());
                    }
                }else{
                    Toast.makeText(ctx, response.message(), Toast.LENGTH_LONG).show();
                    Log.e("~Debug", "not successful message: "+response.message());
                }
                Log.e("~Debug","getFieldNameList() - onResponse Triggered");
            }

            @Override
            public void onFailure(Call<FieldNameListResponse> call, Throwable t) {
                BaseActivity.Baseprogress.hideProgressDialog();
                String msg = t.getLocalizedMessage();
                Log.e("~Debug", msg);
                Log.e("~Debug","getFieldNameList() - onFailed Triggered");
                String[] hh = msg.split(" ");
                if(hh[0].equals("Unexpected") && hh[1].equals("status") && hh[2].equals("line:")){
                    getFieldNameList(v, x, y);
                }
            }
        });
        */
    }

    private void exitDlg(){
        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle("Yakin Ingin Keluar")
                .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                .setMessage("Semua pekerjaan anda akan hilang jika belum disimpan. Tetap ingin keluar ?")
                .setPositiveButton("Simpan", (dialogInterface, i) -> {
                    generateJsonFieldData();
                })
                .setNeutralButton("Batal", null)
                .setNegativeButton("Keluar", (dialogInterface, i) -> finish())
                .create();

        dialog.show();
    }

    private void popupDialog(View v, float x, float y){
        if(popupWindow==null || !popupWindow.isShowing()) {
            selecting = false;
            bold = false;
            italic = false;
            underline = false;
            txtStyle = SpenObjectTextBox.HINT_TEXT_STYLE_NONE;
            txtSize = 20;

//            addIndicatorImage(v.getRootView(), x, y);

            ImageButton togBold = popupView.findViewById(R.id.togBold);
            ImageButton togItalic = popupView.findViewById(R.id.togItalic);
            ImageButton togUnderline = popupView.findViewById(R.id.togUnderline);
            Button btnDismiss = popupView.findViewById(R.id.btnDlgNeg);
            Button btnSave = popupView.findViewById(R.id.btnDlgPos);

            togBold.setBackgroundColor(Color.parseColor("#DBDBDB"));
            togBold.setImageResource(R.drawable.ic_bold);
            togItalic.setBackgroundColor(Color.parseColor("#DBDBDB"));
            togItalic.setImageResource(R.drawable.ic_italic);
            togUnderline.setBackgroundColor(Color.parseColor("#DBDBDB"));
            togUnderline.setImageResource(R.drawable.ic_underline);

            Spinner popupSpinner = popupView.findViewById(R.id.spField);
            NumberPicker npTextSz = popupView.findViewById(R.id.npTextSz);

            npTextSz.setMinValue(8);
            npTextSz.setMaxValue(72);
            npTextSz.setValue((int)txtSize);

            npTextSz.setOnValueChangedListener((numberPicker, i, i1) -> textObj.setFontSize((float)i1));

            ArrayAdapter<StringWithTag> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, fieldLists);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            popupSpinner.setAdapter(adapter);

            togBold.setOnClickListener(v12 -> activateBold(togBold));

            togItalic.setOnClickListener(v13 -> activateItalic(togItalic));

            togUnderline.setOnClickListener(v14 ->  activateUnderline(togUnderline));

            generateText("", x, y, txtSize, txtStyle);

            popupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    spinnerSelectedId = position;
                    s = (StringWithTag) parent.getItemAtPosition(position);
                    tag = s.tag;
                    String item = s.string;

                    if(position!=0){
                        selecting = true;

                        textObj.setText(item);
                    }else{
                        selecting = false;
                        textObj.setText("");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            btnSave.setOnClickListener(v1 -> {
                if(selecting) {
                    float x1, x2, y1, y2;
                    String coord;
                    int page, fnsz, fnst;

                    x1 = textObj.getDrawnRect().centerX()-(textObj.getDrawnRect().width()/2);
                    y1 = textObj.getDrawnRect().centerY()-(textObj.getDrawnRect().height()/2);
                    x2 = x + textObj.getDrawnRect().width();
                    y2 = y + textObj.getDrawnRect().height();

                    coord = x1 + "," + y1 + "," + x2 + "," + y2;

                    page = mSpenNoteDoc.getLastEditedPageIndex();
                    fnsz = (int) textObj.getFontSize();
                    fnst = textObj.getTextStyle();

                    GenTextSetting textSetting = new GenTextSetting(id, Integer.valueOf(tag.toString()), page, fnsz, fnst, coord);
                    allSettings.add(textSetting);

                    popupWindow.dismiss();
                }else{
                    Toast.makeText(ctx, "Pilih nama field terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            });

            btnDismiss.setOnClickListener(v1 -> {
                ((ViewGroup) v.getRootView()).removeView(iv);

                popupWindow.dismiss();
            });

            popupWindow.setHeight(400);
            popupWindow.setWidth(600);
            popupWindow.showAsDropDown(v, (int)x, (int)y-30);

            popupView.setOnTouchListener(new View.OnTouchListener() {
                int orgX, orgY;
                int offsetX, offsetY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            orgX = (int) event.getX();
                            orgY = (int) event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            offsetX = (int) event.getRawX() - orgX;
                            offsetY = (int) event.getRawY() - orgY;
                            popupWindow.update(offsetX, offsetY, -1, -1, true);
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private void generateJsonFieldData(){
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Type type = new TypeToken<List<GenTextSetting>>(){}.getType();

        String json = gson.toJson(allSettings, type);
        Log.d(TAG, "generateJsonFieldData: " + json);
        saveAllFieldAsync(json);
    }

    private void activateBold(ImageButton v){
        if(bold){
            bold = false;

            v.setBackgroundColor(Color.parseColor("#DBDBDB"));
            v.setImageResource(R.drawable.ic_bold);
        }else{
            bold = true;

            v.setBackgroundResource(R.color.colorPrimary);
            v.setImageResource(R.drawable.ic_bold_white);
        }

        textObj.setTextStyle(SpenObjectTextBox.HINT_TEXT_STYLE_NONE);

        txtStyle = SpenObjectTextBox.HINT_TEXT_STYLE_BOLD;

        if(italic){
            txtStyle = txtStyle|SpenObjectTextBox.HINT_TEXT_STYLE_ITALIC;
        }

        if(underline){
            txtStyle = txtStyle|SpenObjectTextBox.HINT_TEXT_STYLE_UNDERLINE;
        }

        textObj.setTextStyle(txtStyle);
    }

    private void activateItalic(ImageButton v){
        if(italic){
            italic = false;

            v.setBackgroundColor(Color.parseColor("#DBDBDB"));
            v.setImageResource(R.drawable.ic_italic);
        }else{
            italic = true;

            v.setBackgroundResource(R.color.colorPrimary);
            v.setImageResource(R.drawable.ic_italic_white);
        }

        textObj.setTextStyle(SpenObjectTextBox.HINT_TEXT_STYLE_NONE);

        txtStyle = SpenObjectTextBox.HINT_TEXT_STYLE_ITALIC;

        if(bold){
            txtStyle = txtStyle|SpenObjectTextBox.HINT_TEXT_STYLE_BOLD;
        }

        if(underline){
            txtStyle = txtStyle|SpenObjectTextBox.HINT_TEXT_STYLE_UNDERLINE;
        }

        textObj.setTextStyle(txtStyle);
    }

    private void activateUnderline(ImageButton v){
        if(underline){
            underline = false;

            v.setBackgroundColor(Color.parseColor("#DBDBDB"));
            v.setImageResource(R.drawable.ic_underline);
        }else{
            underline = true;

            v.setBackgroundResource(R.color.colorPrimary);
            v.setImageResource(R.drawable.ic_underline_white);
        }

        textObj.setTextStyle(SpenObjectTextBox.HINT_TEXT_STYLE_NONE);

        txtStyle = SpenObjectTextBox.HINT_TEXT_STYLE_UNDERLINE;

        if(italic){
            txtStyle = txtStyle|SpenObjectTextBox.HINT_TEXT_STYLE_ITALIC;
        }

        if(bold){
            txtStyle = txtStyle|SpenObjectTextBox.HINT_TEXT_STYLE_BOLD;
        }

        textObj.setTextStyle(txtStyle);
    }

    private void saveAllFieldAsync(String json){
        //TODO: Call func to save field data to the api
        BaseActivity.Baseprogress.showProgressDialog(ctx, "Saving Field Data ...");

        APIClient = APIUtils.getAPIService(ctx);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), json);
        Call<ApiStatus> call = APIClient.addCoordsInDMK(id, PetugasMainActivity.UID, requestBody);

        call.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    toastInfo(ctx, "Data berhasil disimpan");
                    Intent intent = new Intent();

                    intent.putExtra(DMK_POS, dmkPos);
                    setResult(RESULT_OK, intent);
                    finish();
                }else{
                    String msg = ApiError.parseError(response).getMessage();
                    Log.d(TAG, "onResponse: " + msg);
                    toastErr(ctx, msg);
                }
            }

            @Override
            public void onFailure(Call<ApiStatus> call, Throwable t) {
                Baseprogress.hideProgressDialog();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                toastErr(ctx, t.getLocalizedMessage());
            }
        });
    }

    private void addIndicatorImage(View v, float x, float y){
        lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        iv = new ImageView(getApplicationContext());
        lp.setMargins((int)x, (int)y, 0, 0);
        iv.setLayoutParams(lp);
        iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_default_pin));
        ((ViewGroup) v).addView(iv);
    }

    private void generateText(String txt, float x, float y, float fntsz, int txtstl){
        textObj = new SpenObjectTextBox();
        float textBoxHeight = getTextBoxDefaultHeight(textObj);
        if ((y + textBoxHeight) > mSpenPageDoc.getHeight()) {
            y = mSpenPageDoc.getHeight() - textBoxHeight;
        }
        RectF rect = new RectF(x, y, x + 350, y + textBoxHeight);
        textObj.setRect(rect, true);
        textObj.setText(txt);
        textObj.setFontSize(fntsz);
        textObj.setTextStyle(txtstl);
        textObj.setSelectable(false);
        textObj.setRotatable(false);

        mSpenPageDoc.appendObject(textObj);
        mSpenPageDoc.selectObject(textObj);
        mSpenSurfaceView.update();
    }

    private float getTextBoxDefaultHeight(SpenObjectTextBox textBox) {
        if (textBox == null) {
            return 0;
        }

        float height = 0, lineSpacing = 0, lineSpacePercent = 1.3f;
        float margin = textBox.getTopMargin() + textBox.getBottomMargin();

        ArrayList<SpenTextParagraphBase> pInfo = textBox.getTextParagraph();
        if (pInfo != null) {
            for (SpenTextParagraphBase info : pInfo) {
                if (info instanceof SpenLineSpacingParagraph) {
                    if (((SpenLineSpacingParagraph) info).getLineSpacingType() ==
                            SpenLineSpacingParagraph.TYPE_PERCENT) {
                        lineSpacePercent = ((SpenLineSpacingParagraph) info).getLineSpacing();
                    } else if (((SpenLineSpacingParagraph) info).getLineSpacingType() ==
                            SpenLineSpacingParagraph.TYPE_PIXEL) {
                        lineSpacing = ((SpenLineSpacingParagraph) info).getLineSpacing();
                    }
                }
            }
        }

        if (lineSpacing != 0){
            height = lineSpacing + margin;
        } else {
            float fontSize = mSpenPageDoc.getWidth()/20;
            ArrayList<SpenTextSpanBase> sInfo =
                    textBox.findTextSpan(textBox.getCursorPosition(), textBox.getCursorPosition());
            if (sInfo != null) {
                for (SpenTextSpanBase info : sInfo) {
                    if (info instanceof SpenFontSizeSpan) {
                        fontSize = ((SpenFontSizeSpan) info).getSize();
                        break;
                    }
                }
            }
            height = fontSize * lineSpacePercent;
        }

        return height;
    }

    private PointF getCanvasPoint(MotionEvent event) {
        float panX = mSpenSurfaceView.getPan().x;
        float panY = mSpenSurfaceView.getPan().y;
        float zoom = mSpenSurfaceView.getZoomRatio();
        return new PointF(event.getX() / zoom + panX, event.getY() / zoom + panY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_field_selector, menu);

        return true;
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

            case R.id.save:{
                generateJsonFieldData();
                
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
            Log.e( "[X-DEBUG]", "onBackPressed trigerred!");

        exitDlg();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("~Debug", "onDestroy triggered");

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

        try{
            if(SpdFile.exists()){
                if(SpdFile.delete()){
                    Log.e("[X-DEBUG]", "Recent file has been purged!");
                }else{
                    Log.e("[X-DEBUG]", "Cannot purge recent file!");
                }
            }
        }catch (Exception ex){
            Log.e("[X-DEBUG]", "Error: "+ex.getMessage());
        }
    }
}
