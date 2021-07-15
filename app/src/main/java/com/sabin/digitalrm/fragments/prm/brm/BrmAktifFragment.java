package com.sabin.digitalrm.fragments.prm.brm;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.PreviewActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.BrmAktifAdapter;
import com.sabin.digitalrm.dialogs.DialogPreviewNoteAnalytic;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.helpers.DefaultDialog;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.DetailVisitor;
import com.sabin.digitalrm.models.EHOSUnit;
import com.sabin.digitalrm.prm.DMRAnalyticActivity;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.Timestamp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class BrmAktifFragment extends BaseFragment {
    private static final String ARG_QUERY = "param1";
    private final String START_DATE = "2018-12-01";
    private final String START_DATE_TEXT = "01/12/2018";

    private boolean isCreated = false;
    private boolean isEmpty = true;

    private Integer queryValue;
    private Integer queryIdPoli = 0;
    private String startDate;
    private String endDate;
    private Calendar calTmpStart, calTmpEnd;
    private boolean chooseDay;

    private APIService APIClient;
    private List<DetailVisitor> brmAktifList;
    private List<EHOSUnit> spJenisList;

    private Context context;
    private RecyclerView brmAktifRecycler;
    private BrmAktifAdapter brmAktifAdapter;
    ArrayAdapter <EHOSUnit> jenisAdapter;

    private FloatingActionButton fab;
    private RelativeLayout layoutKosong;
    private ProgressBar progressBar;
    private ImageButton btnUpdateFilter;
    private Button btnStartDate, btnEndDate;

    private TextView txtSampai;

    public BrmAktifFragment() {
        // Required empty public constructor
    }

    public static BrmAktifFragment newInstance(Integer query) {
        BrmAktifFragment fragment = new BrmAktifFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            queryValue = getArguments().getInt(ARG_QUERY);
        }

        brmAktifList = new ArrayList<>();
        spJenisList = new ArrayList<>();

        context = getContext();
        final Calendar cal = Calendar.getInstance();
        calTmpStart = Calendar.getInstance();
        calTmpEnd = Calendar.getInstance();

        int ts = (int)(cal.getTime().getTime() / 1000);
        startDate = Timestamp.getDate(ts, "yyyy-MM-dd");
        endDate = Timestamp.getDate(ts, "yyyy-MM-dd");

        Log.d(TAG, "onCreate TS: " + ts);

        initRetrofit();
        fetchBrmAktifList(queryValue);
        fetchLayananFilterList();
        Baselog.d("onCreate: ");
        isCreated = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brm_aktif, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        brmAktifRecycler = view.findViewById(R.id.recView);
        Spinner spDateType = view.findViewById(R.id.spDateType);
        Spinner spJenisKunjungan = view.findViewById(R.id.spJenisLayanan);
        btnStartDate = view.findViewById(R.id.btn_startdate);
        btnEndDate = view.findViewById(R.id.btn_enddate);
        btnUpdateFilter = view.findViewById(R.id.btn_updatefilter);
        txtSampai = view.findViewById(R.id.txtSampai);

        btnStartDate.setEnabled(false);
        btnEndDate.setEnabled(false);

        int ts = (int)(calTmpStart.getTime().getTime() / 1000);
        btnStartDate.setText(Timestamp.getDate(ts, "dd/MM/yyyy"));
        btnEndDate.setText(Timestamp.getDate(ts, "dd/MM/yyyy"));

        btnEndDate.setOnClickListener((view1 -> {
            @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(context, ((datePicker, year, month, day) -> {
                Log.d(TAG, "onItemSelected: " + day + "-" + month + "-" + year);
                String mo = String.valueOf(month + 1);
                if(mo.length()<2){
                    mo = "0" + mo;
                }
                endDate = year + "-" + mo + "-" + day;
                btnEndDate.setText(day + "/" + mo + "/" + year);
                calTmpEnd.set(year, month, day);
                Log.d(TAG, "onItemSelected: " + endDate);
            }), calTmpEnd.get(Calendar.YEAR), calTmpEnd.get(Calendar.MONTH), calTmpEnd.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.show();
        }));

        btnStartDate.setOnClickListener((view1 -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, ((datePicker, year, month, day) -> {
                String mo = String.valueOf(month + 1);
                if(mo.length()<2){
                    mo = "0" + mo;
                }

                startDate = year + "-" + mo + "-" + day;

                if(chooseDay) {
                    endDate = startDate;
                }

                Log.d(TAG, "onItemSelected: " + day + "-" + month + "-" + year);

                btnStartDate.setText(day + "/" + mo + "/" + year);
                calTmpStart.set(year, month, day);
            }), calTmpStart.get(Calendar.YEAR), calTmpStart.get(Calendar.MONTH), calTmpStart.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.show();
        }));

        btnUpdateFilter.setOnClickListener((view1 -> fetchBrmAktifList(queryValue)));

        List<String> spList = new ArrayList<>();

        spList.add("Hari Ini");
        spList.add("Kemarin");
        spList.add("Pilih Tanggal");
        spList.add("Rentang Tanggal");
        spList.add("Semua Hari");

        spJenisList.add(new EHOSUnit(0, "Semua Unit"));

        ArrayAdapter <String> spAdapter = new ArrayAdapter<>(context, R.layout.spinner_date_type, spList);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDateType.setAdapter(spAdapter);

        spDateType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: POS " + i);
                switch (i){
                    case 0:{
                        btnStartDate.setVisibility(View.VISIBLE);
                        btnEndDate.setVisibility(View.GONE);
                        txtSampai.setVisibility(View.GONE);

                        final Calendar cal = Calendar.getInstance();
                        int ts = (int) (cal.getTime().getTime() / 1000);

                        String strdate = Timestamp.getDate(ts, "yyyy-MM-dd");
                        startDate = Timestamp.getDate(ts, "yyyy-MM-dd");
                        endDate = startDate;

                        Log.d(TAG, "onItemSelected: TS " + strdate + "/" + startDate);

                        btnStartDate.setEnabled(false);
                        btnEndDate.setEnabled(false);
                        break;
                    }

                    case 1:{
                        btnStartDate.setVisibility(View.VISIBLE);
                        btnEndDate.setVisibility(View.GONE);
                        txtSampai.setVisibility(View.GONE);

                        final Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, -1);
                        int ts = (int) (cal.getTime().getTime() / 1000);

                        Log.d(TAG, "onItemSelected: TS " + ts);

                        startDate = Timestamp.getDate(ts, "yyyy-MM-dd");
                        endDate = Timestamp.getDate(ts, "yyyy-MM-dd");

                        btnStartDate.setEnabled(false);
                        btnEndDate.setEnabled(false);
                        break;
                    }

                    case 2:{
                        btnStartDate.setVisibility(View.VISIBLE);
                        btnEndDate.setVisibility(View.GONE);
                        txtSampai.setVisibility(View.GONE);

                        chooseDay = true;
                        btnStartDate.setEnabled(true);
                        btnEndDate.setEnabled(false);

                        long ts = Calendar.getInstance().getTime().getTime();
                        int its = (int)(ts / 1000);

                        btnStartDate.setText(Timestamp.getDate(its, "dd/MM/yyyy"));
                        break;
                    }

                    case 3:{
                        btnStartDate.setVisibility(View.VISIBLE);
                        btnEndDate.setVisibility(View.VISIBLE);
                        txtSampai.setVisibility(View.VISIBLE);

                        chooseDay = false;
                        btnStartDate.setEnabled(true);
                        btnEndDate.setEnabled(true);
                        long ts = Calendar.getInstance().getTime().getTime();
                        int its = (int)(ts / 1000);

                        btnStartDate.setText(Timestamp.getDate(its, "dd/MM/yyyy"));
                        break;
                    }

                    case 4:{
                        btnStartDate.setVisibility(View.GONE);
                        btnEndDate.setVisibility(View.GONE);
                        txtSampai.setVisibility(View.GONE);

                        long ts = Calendar.getInstance().getTime().getTime();
                        int its = (int)(ts / 1000);

                        btnStartDate.setEnabled(false);
                        btnEndDate.setEnabled(false);
                        btnStartDate.setText(START_DATE_TEXT);
                        btnEndDate.setText(Timestamp.getDate(its, "dd/MM/yyyy"));

                        startDate = START_DATE;
                        endDate = Timestamp.getDate(its, "yyyy-MM-dd");
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        jenisAdapter = new ArrayAdapter<>(context, R.layout.spinner_date_type, spJenisList);
        jenisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spJenisKunjungan.setAdapter(jenisAdapter);

        spJenisKunjungan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                queryIdPoli = spJenisList.get(i).getUnitID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        brmAktifRecycler.setHasFixedSize(true);

        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        progressBar = view.findViewById(R.id.progress);
        fab = view.findViewById(R.id.fab_refresh);
        layoutKosong = view.findViewById(R.id.layout_kosong);

        fab.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            fetchBrmAktifList(queryValue);
        });

        brmAktifAdapter = new BrmAktifAdapter(context, brmAktifList, queryValue) {
            @Override
            protected void onBindViewHolder(@NonNull VisitorHolder holder, int position, @NonNull DetailVisitor model) {
                switch (model.getStatus()){
                    case DetailVisitor.VISITOR_DMR_ANALITYC:{
                        holder.itemView.setOnClickListener(view1 -> dlgAnalytic(model));
                        break;
                    }
                    case DetailVisitor.VISITOR_DMR_CODING:{
                        holder.itemView.setOnClickListener((view) -> dlgCoding(model));
                        break;
                    }

                }
                holder.bindInfo(model);
            }
        };

        brmAktifRecycler.setLayoutManager(manager);
        brmAktifRecycler.setAdapter(brmAktifAdapter );

        if(isCreated && !isEmpty)
            layoutKosong.setVisibility(View.GONE);
    }

    private void initRetrofit(){
        BaseActivity.Baselog.d("Initretrofit");
        APIClient = APIUtils.getAPIService(context);
    }

    private void fetchLayananFilterList(){
        if(progressBar != null)
            progressBar.setVisibility(View.VISIBLE);

        Call <List<EHOSUnit>> call = APIClient.getUnitsInDMR(PetugasMainActivity.UID);
        Log.d(TAG, "fetchLayananFilterList: " + call.request());
        call.enqueue(new Callback<List<EHOSUnit>>() {
            @Override
            @SuppressWarnings("ConstantConditions")
            public void onResponse(@NonNull Call<List<EHOSUnit>> call,@NonNull Response<List<EHOSUnit>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()){
                    if(response.body().size() > 0) {
                        spJenisList.clear();
                        spJenisList.add(new EHOSUnit(0, "Semua Unit"));
                        spJenisList.addAll(response.body());
                        jenisAdapter.notifyDataSetChanged();
                        Log.d(TAG, "onResponse: " + spJenisList.get(0).toString());
                    }
                }else{
                    String msg = ApiError.parseError(response).getMessage();
                    Log.d(TAG, "onResponse: GOT ERROR:");
                    String raw;
                    try{
                        raw = response.errorBody().string();
                    }catch (IOException e){
                        raw = e.getLocalizedMessage();
                    }
                    Log.d(TAG, "RAW: " + raw);
                    toastErr(context, msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EHOSUnit>> call,@NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onFailure: Service Fail");
                toastErr(context, t.getLocalizedMessage());
            }
        });
    }

    private void fetchBrmAktifList(Integer val){
        if(progressBar != null)
            progressBar.setVisibility(View.VISIBLE);

        Call<List<DetailVisitor>> clientAllPatientVisits = APIClient.getAllPatientVisits(PetugasMainActivity.UID, val, queryIdPoli, startDate, endDate, -1, -1, -1);
        Log.d(TAG, "fetchBrmAktifList: " + endDate + "#" + startDate);
        Log.d(TAG, "fetchBrmAktifList: " + clientAllPatientVisits.request().toString());

        clientAllPatientVisits.enqueue(new Callback<List<DetailVisitor>>() {
            @Override
            public void onResponse(Call<List<DetailVisitor>> call, Response<List<DetailVisitor>> response) {
                progressBar.setVisibility(View.GONE);
                if(response.isSuccessful()){
                    brmAktifList.clear();
                    brmAktifList.addAll(response.body());

                    if(brmAktifList.size() == 0){
                        isEmpty = true;
                        layoutKosong.setVisibility(View.VISIBLE);
                    }else {
                        layoutKosong.setVisibility(View.GONE);
                        brmAktifAdapter.notifyDataSetChanged();
                        isEmpty = false;
                    }
                }else{
                    String msg = ApiError.parseError(response).getMessage();
                    toastErr(context, msg);
                    Log.d(TAG, "onResponse: " + msg);
                }
            }

            @Override
            public void onFailure(Call<List<DetailVisitor>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                toastErr(context, t.getLocalizedMessage());
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    private void dlgAnalytic(DetailVisitor data){
        String [] options = {"Periksa BRM", "Tandai Tidak Lengkap", "Tandai Telah Lengkap", "Lihat Catatan Kelengkapan"};
        new AlertDialog.Builder(context)
                .setTitle("Opsi BRM")
                .setItems(options, ((dialog, which) -> {
                    switch (which){
                        case 0:{
                            Intent intent = new Intent(context, DMRAnalyticActivity.class);
                            intent.putExtra(DMRAnalyticActivity.EXTRA_ID_DMR, data.getIdBerkas());
                            intent.putExtra(DMRAnalyticActivity.EXTRA_NORM, data.getInfoPasien().getNoBrm());

                            startActivity(intent);
                            break;
                        }

                        case 1:{
                            rejectBRM(data);
                            break;
                        }

                        case 2:{
                            acceptBRM(data);
                            break;
                        }

                        case 3:{
                            previewRNote(data);
                            break;
                        }
                    }
                } ))
                .create()
                .show();
    }

    //TODO: REFACTOR TO VIEWER ONLY
    private void dlgCoding(DetailVisitor data){
        String [] options = {"Lihat BRM", "Tutup BRM"};
        new AlertDialog.Builder(context)
                .setTitle("Opsi BRM")
                .setItems(options, ((dialog, which) -> {
                    switch (which){
                        case 0:{
                            priviewPoli(data);
                            break;
                        }

                        case 1:{
                            closeBRM(data);
                            break;
                        }
                    }
                } ))
                .create()
                .show();
    }

    private void previewRNote(DetailVisitor data){
        DialogPreviewNoteAnalytic dialog = DialogPreviewNoteAnalytic.newInstance(data.getIdBerkas());
        dialog.show(getFragmentManager(), null);
    }

    private void acceptBRM(DetailVisitor data){
        DefaultDialog dialog = new DefaultDialog(context);
        dialog.setTitle("Tandai DMR Telah Lengkap");
        dialog.setMessage("Tandai DMR telah lengkap dan keluarkan dari kategori Pemeriksaan Kelengkapan, yakin ingin melanjutkan ?");
        dialog.setIcon(context.getDrawable(R.drawable.ic_warning));
        dialog.setNegativeButton("Batal", null);
        dialog.setPositiveButton("Lanjutkan", view -> {
            dialog.setProgressBar(View.VISIBLE);

            Call<ApiStatus> call = APIClient.setDMRtoOnCoding(data.getSrvId(), PetugasMainActivity.UID, DetailVisitor.VISITOR_DMR_CODING, DetailVisitor.DMR_OK);
            call.enqueue(new Callback<ApiStatus>() {
                @Override
                public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                    dialog.setProgressBar(View.GONE);
                    if(response.isSuccessful()){
                        int index = brmAktifList.indexOf(data);
                        brmAktifList.remove(index);
                        brmAktifAdapter.notifyItemRemoved(index);
                        toastInfo(context, "Status DMR berhasil diupdate");
                        dialog.dismiss();
                    }else{
                        String msg = ApiError.parseError(response).getMessage();
                        toastErr(context, msg);
                        Log.d(TAG, "onResponse: " + msg);
                    }
                }

                @Override
                public void onFailure(Call<ApiStatus> call, Throwable t) {
                    dialog.setProgressBar(View.GONE);
                    Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                    toastErr(context, t.getLocalizedMessage());
                }
            });

        });
        dialog.show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void rejectBRM(DetailVisitor data){
        DefaultDialog dialog = new DefaultDialog(context);
        dialog.setTitle("Tandai Tidak Lengkap");
        dialog.setMessage("Tandai BRM sebagai tidak lengkap dan kirim lagi ke Dokter yang bersangkutan, yakin ingin melanjutkan ?");
        dialog.setIcon(context.getDrawable(R.drawable.ic_warning));
        dialog.setNegativeButton("Batal", null);
        dialog.setPositiveButton("Lanjutkan", view -> {
            dialog.setProgressBar(View.VISIBLE);

            Call<ApiStatus> call = APIClient.updateDMROK(data.getSrvId(), PetugasMainActivity.UID,DetailVisitor.VISITOR_DMR_ANALITYC, DetailVisitor.DMR_CHECKED_AND_INCOMPLETE);
            call.enqueue(new Callback<ApiStatus>() {
                @Override
                public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                    dialog.setProgressBar(View.GONE);
                    if(response.isSuccessful()){
                        int index = brmAktifList.indexOf(data);
                        brmAktifList.get(index).setIsDmrOK(DetailVisitor.DMR_CHECKED_AND_INCOMPLETE);
                        brmAktifAdapter.notifyItemChanged(index);
                        toastInfo(context, "BRM berhasil ditolak");
                        dialog.dismiss();
                    }else{
                        String msg = ApiError.parseError(response).getMessage();
                        toastErr(context, msg);
                        Log.d(TAG, "onResponse: " + msg);
                    }
                }

                @Override
                public void onFailure(Call<ApiStatus> call, Throwable t) {
                    dialog.setProgressBar(View.GONE);
                    Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                    toastErr(context, t.getLocalizedMessage());
                }
            });

        });
        dialog.show();

    }

    private void closeBRM(DetailVisitor data){
        new AlertDialog.Builder(context)
                .setTitle("Tutup BRM")
                .setMessage("BRM akan ditutup, yakin ingin melanjutkan ?")
                .setPositiveButton("Lanjutkan", ((dialog, which) -> {
                    Call <ApiStatus> call = APIClient.closeDMR(data.getSrvId(), PetugasMainActivity.UID, DetailVisitor.VISITOR_DMR_CODED, DetailVisitor.DMR_OK);
                    Baseprogress.showProgressDialog(context, "Menutup BRM...");
                    call.enqueue(new Callback<ApiStatus>() {
                        @Override
                        public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                            Baseprogress.hideProgressDialog();

                            if(response.isSuccessful()){
                                    toastInfo(context, "BRM berhasil ditutup");
                                    int index = brmAktifList.indexOf(data);
                                    brmAktifList.remove(index);
                                    brmAktifAdapter.notifyItemRemoved(index);

                                    if(brmAktifList.size() == 0){
                                        isEmpty = true;
                                        layoutKosong.setVisibility(View.VISIBLE);
                                    toastInfo(context, response.body().getMessage());
                                }
                            }else{
                                String msg = ApiError.parseError(response).getMessage();
                                toastErr(context, msg);
                                Log.d(TAG, "onResponse: " + msg);
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiStatus> call, Throwable t) {
                            Baseprogress.hideProgressDialog();
                            toastErr(context, t.getLocalizedMessage());
                        }
                    });
                }))
                .setNegativeButton("Batal", null)
                .create().show();
    }

    private void priviewPoli(DetailVisitor data){
        new AlertDialog.Builder(context)
                .setTitle("Buka Untuk Coding")
                .setMessage("BRM akan didownload terlebih dahulu, mohon untuk bersabar menunggu hingga proses download selesai")
                .setPositiveButton("Lanjutkan", ((dialog, which) -> {
                    Intent intent = new Intent(context, PreviewActivity.class);
                    intent.putExtra("mode", PreviewActivity.SPD_MODE);
                    intent.putExtra("uid", PetugasMainActivity.UID);
                    intent.putExtra("brm", data.getInfoPasien().getNoBrm());
                    intent.putExtra("poli", data.getIdUnit());
                    intent.putExtra("namapoli", data.getUnitName());
                    intent.putExtra("idBerkas", data.getIdBerkas());
                    intent.putExtra("isCoding", true);
                    Log.d(TAG, "priviewPoli: " + data.getIdBerkas());
                    startActivity(intent);
                }))
                .setNegativeButton("Batal", null)
                .create().show();

    }
}
