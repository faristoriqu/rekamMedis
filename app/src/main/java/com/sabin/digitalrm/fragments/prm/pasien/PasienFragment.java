package com.sabin.digitalrm.fragments.prm.pasien;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import androidx.fragment.app.Fragment;

import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.VisitorAdapter;
import com.sabin.digitalrm.dialogs.DialogDMRLog;
import com.sabin.digitalrm.dialogs.DialogPrepareDMR;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.DetailBlanko;
import com.sabin.digitalrm.models.DetailVisitor;
import com.sabin.digitalrm.models.EHOSUnit;
import com.sabin.digitalrm.models.VisitUnit;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.prm.PoliBaruActivity;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.Logger;
import com.sabin.digitalrm.utils.Timestamp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class PasienFragment extends BaseFragment {
    private static final String ARG_QUERY = "param1";
    private final String START_DATE = "2020-07-01";
    private final String START_DATE_TEXT = "01/07/2020";

    private boolean isCreated = false;
    private boolean isEmpty = true;

    private Integer queryValue = -1;
    private Integer queryIdPoli = 0;
    private String startDate;
    private String endDate;
    private Calendar calTmpStart, calTmpEnd;
    private boolean chooseDay;

    private APIService APIClient;
    private List<DetailVisitor> visitorList;
    private List<EHOSUnit> spJenisList;
    private List<EHOSUnit> spJenisListRI;
    private List<EHOSUnit> spJenisListRJ;
    private List<EHOSUnit> spJenisListIGD;

    private Context context;
    private VisitorAdapter visitorAdapter;
    ArrayAdapter <EHOSUnit> jenisAdapter;

    private RelativeLayout layoutKosong;
    private ProgressBar progressBar;
    private Button btnStartDate, btnEndDate;
    private TextView txtSampai;
    Spinner spDateType;
    private static Bundle args;

    private static Logger log = new Logger();

    public PasienFragment() {
        // Required empty public constructor
    }

    public static PasienFragment newInstance(Integer query) {
        PasienFragment fragment = new PasienFragment();
        args = new Bundle();
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

        setHasOptionsMenu(true);

        visitorList = new ArrayList<>();
        spJenisList = new ArrayList<>();
        spJenisListRJ = new ArrayList<>();
        spJenisListRI = new ArrayList<>();
        spJenisListIGD = new ArrayList<>();

        context = getContext();
        final Calendar cal = Calendar.getInstance();
        calTmpStart = Calendar.getInstance();
        calTmpEnd = Calendar.getInstance();

        int ts = (int)(cal.getTime().getTime() / 1000);
        startDate = Timestamp.getDate(ts, "yyyy-MM-dd");
        endDate = Timestamp.getDate(ts, "yyyy-MM-dd");

        Log.d(TAG, "onCreate TS: " + ts);

        initRetrofit();
        fetchLayananFilterList();
        Baselog.d("onCreate: ");
        syncVisit();
        isCreated = true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brm_aktif, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_base, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_refresh:{
                Baselog.d("CHILD REFRESH");
                break;
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PoliBaruActivity.REQUEST_POLI_BARU:{
                if(resultCode == RESULT_OK){
                    int index = Objects.requireNonNull(data.getExtras()).getInt(PoliBaruActivity.EXTRA_DATASET_INDEX);
                    int idDMr = Objects.requireNonNull(data.getExtras()).getInt(PoliBaruActivity.EXTRA_ID_DMR);
                    DetailVisitor visitor = visitorList.get(index);
                    activateDMR(visitor, idDMr);
                }else{
                    toastErr(context, "Penambahan blanko poli dibatalkan / terjadi kesalahan");
                }
            }
        }
    }

    private void activateDMR(DetailVisitor visitor, int idDMR){
        Call<ApiStatus> callAVisit = APIClient.activatePatientVisit(visitor.getSrvId(), PetugasMainActivity.UID, 3, Integer.valueOf(PetugasMainActivity.UID), idDMR);
        Baseprogress.showProgressDialog(context, "Mengaktifkan DMR " + visitor.getInfoPasien().getNoBrm());
        callAVisit.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(@NonNull Call<ApiStatus> call,@NonNull Response<ApiStatus> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    toastInfo(context, "Berhasil mengaktifkan DMR");
                    visitor.setStatus(3);
                    visitor.setIdBerkas(idDMR);
                    visitorAdapter.notifyItemChanged(visitorList.indexOf(visitor));
                }else{
                    toastErr(context, ApiError.parseError(response).getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiStatus> call,@NonNull Throwable t) {
                Baseprogress.hideProgressDialog();
                toastErr(context, t.getLocalizedMessage());
            }
        });
    }

    @Override
    @SuppressWarnings("Convert2Lambda")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView visitorRecycler;
        FloatingActionButton fab;
        ImageButton btnUpdateFilter;

        super.onViewCreated(view, savedInstanceState);

        assert getArguments() != null;
        visitorRecycler = view.findViewById(R.id.recView);
        spDateType = view.findViewById(R.id.spDateType);
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
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, ((datePicker, year, month, day) -> {
                Log.d(TAG, "onItemSelected: " + day + "-" + month + "-" + year);
                endDate = year + "-" + (month + 1) + "-" + day;
                btnEndDate.setText(day + "/" + (month + 1) + "/" + year);
                calTmpEnd.set(year, month, day);
                Log.d(TAG, "onItemSelected: " + endDate);
            }), calTmpEnd.get(Calendar.YEAR), calTmpEnd.get(Calendar.MONTH), calTmpEnd.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.show();
        }));

        btnStartDate.setOnClickListener((view1 -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, ((datePicker, year, month, day) -> {
                startDate = year + "-" + (month + 1) + "-" + day;
                if(chooseDay) {
                    endDate = startDate;
                }

                Log.d(TAG, "onItemSelected: " + day + "-" + month + "-" + year);

                btnStartDate.setText(day + "/" + (month + 1) + "/" + year);
                calTmpStart.set(year, month, day);
            }), calTmpStart.get(Calendar.YEAR), calTmpStart.get(Calendar.MONTH), calTmpStart.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.show();
        }));

        btnUpdateFilter.setOnClickListener((view1 -> fetchAllVisitsFilter()));

        List<String> spList = new ArrayList<>();

        spList.add("Hari Ini");
        spList.add("Kemarin");
        spList.add("Pilih Tanggal");
        spList.add("Rentang Tanggal");
        spList.add("Semua Hari");

        spJenisList.add(new EHOSUnit(0, "Semua Unit"));
        spJenisListRI.add(new EHOSUnit(0, "Semua Unit"));
        spJenisListRJ.add(new EHOSUnit(0, "Semua Unit"));
        spJenisListIGD.add(new EHOSUnit(0, "Semua Unit"));

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

                        btnStartDate.setText(Timestamp.getDate(ts, "dd/MM/yyyy"));

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

                        btnStartDate.setText(Timestamp.getDate(ts, "dd/MM/yyyy"));

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
                        setFilterAllDate();

                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        log.x("spJenisList Len: "+spJenisList.size());
        log.x("spJenisListRJ Len: "+spJenisListRJ.size());
        log.x("spJenisListRI Len: "+spJenisListRI.size());
        log.x("spJenisListIGD Len: "+spJenisListIGD.size());

        if(queryValue == DetailVisitor.SRV_CODE_RJ){
            jenisAdapter = new ArrayAdapter<>(context, R.layout.spinner_date_type, spJenisListRJ);
        }else if(queryValue == DetailVisitor.SRV_CODE_RI){
            jenisAdapter = new ArrayAdapter<>(context, R.layout.spinner_date_type, spJenisListRI);
        }else if(queryValue == DetailVisitor.SRV_CODE_IGD){
            jenisAdapter = new ArrayAdapter<>(context, R.layout.spinner_date_type, spJenisListIGD);
        }else{
            jenisAdapter = new ArrayAdapter<>(context, R.layout.spinner_date_type, spJenisList);
        }

        jenisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jenisAdapter.notifyDataSetChanged();
        spJenisKunjungan.setAdapter(jenisAdapter);

        spJenisKunjungan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(queryValue == DetailVisitor.SRV_CODE_RJ){
                    log.x("RJ Tab Selected");
                    queryIdPoli = spJenisListRJ.get(i).getUnitID();
                }else if(queryValue == DetailVisitor.SRV_CODE_RI){
                    log.x("RI Tab Selected");
                    queryIdPoli = spJenisListRI.get(i).getUnitID();
                }else if(queryValue == DetailVisitor.SRV_CODE_IGD){
                    log.x("IGD Tab Selected");
                    queryIdPoli = spJenisListIGD.get(i).getUnitID();
                }else{
                    queryIdPoli = spJenisList.get(i).getUnitID();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        visitorRecycler.setHasFixedSize(true);

        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        progressBar = view.findViewById(R.id.progress);
        fab = view.findViewById(R.id.fab_refresh);
        layoutKosong = view.findViewById(R.id.layout_kosong);

        fab.setOnClickListener(v -> {
            fetchAllVisitsFilter();
        });

        visitorAdapter = new VisitorAdapter(context, visitorList) {
            @Override
            protected void onBindViewHolder(@NonNull VisitorHolder holder, int position, @NonNull final DetailVisitor model) {
                holder.bindInfo(model);
                holder.itemView.setOnClickListener(view1 -> showVisitorOption(model));
            }
        };

        visitorRecycler.setLayoutManager(manager);
        visitorRecycler.setAdapter(visitorAdapter);

        if(isCreated && !isEmpty){
            layoutKosong.setVisibility(View.GONE);
        }

        if(queryValue==DetailVisitor.SRV_CODE_RI){
            spDateType.setSelection(4);
            setFilterAllDate();
        }

        syncVisit();
        fetchAllVisitsFilter();
    }

    private void setFilterAllDate(){
        long ts = Calendar.getInstance().getTime().getTime();
        int its = (int)(ts / 1000);

        btnStartDate.setEnabled(false);
        btnEndDate.setEnabled(false);
        btnStartDate.setText(START_DATE_TEXT);
        btnEndDate.setText(Timestamp.getDate(its, "dd/MM/yyyy"));

        startDate = START_DATE;
        endDate = Timestamp.getDate(its, "yyyy-MM-dd");

        btnStartDate.setVisibility(View.GONE);
        btnEndDate.setVisibility(View.GONE);
        txtSampai.setVisibility(View.GONE);
    }

    private void showVisitorOption(DetailVisitor visitor){
        optionVisit(visitor);
    }

    private void optionPasive(){
        //Periksa berkas
        String options [] = {"Aktifkan DMR", "Periksa DMR"};
        new AlertDialog.Builder(context).setTitle("Pilih Opsi")
                .setItems(options, (dialogInterface, i) -> {
                    switch (i){
                        case 0:{ // Activate DMR to doctor
                            //TODO: NEW DOC
                            break;
                        }

                        case 1:{ // View DMR

                            break;
                        }
                    }
                })
                .show();
    }

    private void optionVisit(DetailVisitor visitor){
        if(visitor.getIsNewVisit() == 1){
            // susun berkas
            String options [] = new String[1];

            if(visitor.getIdBerkas() != null)
                options[0] = "History DMR";
            else
                options[0] = "Susun Berkas";

            new AlertDialog.Builder(context).setTitle("Pilih Opsi")
                    .setItems(options, (dialogInterface, i) -> {
                        switch (options[i]){
                            case "Susun Berkas":{ // New Visit with brm unready
                                DialogPrepareDMR dialogPrepareDMR = DialogPrepareDMR.newInstance(visitor.getUnitCat());

                                dialogPrepareDMR.setOnFinishListerner((dblanko, undmk) -> onDialogPrepareFinish(dblanko, undmk, visitor, visitorList.indexOf(visitor)));

                                dialogPrepareDMR.show(getChildFragmentManager(), "dialog");
                                break;
                            }

                            case "History DMR":{
                                if(visitor.getIdBerkas() != null) {
                                    DialogDMRLog dialogDMRLog = DialogDMRLog.newInstance(visitor.getIdBerkas(), visitor.getIdBerkas().toString());
                                    dialogDMRLog.show(getChildFragmentManager(), "dialogDMR");
                                }else{
                                    toastInfo(context, "DMR Belum disusun!");
                                }
                                break;
                            }
                        }
                    })
                    .show();
        }
    }

    private void optionActive(){
        toastInfo(context, "Tidak ada Menu!");
    }

    private void onDialogPrepareFinish(DetailBlanko data, String undmK, DetailVisitor visitor, int index){
        Intent intent = new Intent(context, PoliBaruActivity.class);
        intent.putExtra(PoliBaruActivity.EXTRA_DATASET_INDEX, index);
        intent.putExtra(PoliBaruActivity.EXTRA_ID_BLANKO, data.getId());
        intent.putExtra(PoliBaruActivity.EXTRA_UNDMK, undmK);
        intent.putExtra(PoliBaruActivity.EXTRA_NO_BRM, visitor.getInfoPasien().getNoBrm());
        intent.putExtra(PoliBaruActivity.EXTRA_PATIENT_NAME, visitor.getInfoPasien().getNamaPasien());
        intent.putExtra(PoliBaruActivity.EXTRA_DMR_NAME, (data.getDmr() + " / "  + visitor.getUnitName()));
        intent.putExtra(PoliBaruActivity.EXTRA_UNIT_CAT, data.getId_unit_c());
        intent.putExtra(PoliBaruActivity.EXTRA_ID_SRV, visitor.getSrvId());
        intent.putExtra(PoliBaruActivity.EXTRA_ID_UNIT, visitor.getIdUnit());

        startActivityForResult(intent, PoliBaruActivity.REQUEST_POLI_BARU);
    }

    private void syncVisit(){
        Call<ApiStatus> call = APIClient.syncPatientVisits(PetugasMainActivity.UID);
        call.enqueue(new Callback<ApiStatus>() {
            @Override
            @SuppressWarnings("ConstantConditions")
            public void onResponse(@NonNull Call<ApiStatus> call,@NonNull Response<ApiStatus> response) {
                if(response.isSuccessful()){
                    toastInfo(context, response.body().getMessage());
                    fetchVisitUnits();
                }else{
                    String msg = ApiError.parseError(response).getMessage();
                    Log.d(TAG, "onResponse: " + msg);
                    toastErr(context, msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiStatus> call,@NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    // TODO: UPDATE SPINNER BASED ON THIS RESULT!!
    private void fetchVisitUnits(){
        Call<List<VisitUnit>> call = APIClient.getVisitUnits(PetugasMainActivity.UID);
        call.enqueue(new Callback<List<VisitUnit>>() {
            @Override
            @SuppressWarnings("ConstantConditions")
            public void onResponse(@NonNull Call<List<VisitUnit>> call, @NonNull Response<List<VisitUnit>> response) {
                progressBar.setVisibility(View.GONE);
                if(response.isSuccessful()){
                    if(response.body().size() > 0){
                        Log.d(TAG, "onResponse: " + response.body().get(0).getUnitName());
                    }else {
                        Log.d(TAG, "onResponse: no visit unit");
                        progressBar.setVisibility(View.GONE);
                    }
                }else{
                    Log.d(TAG, "onResponse: " + ApiError.parseError(response).getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<VisitUnit>> call,@NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    private void initRetrofit(){
        BaseActivity.Baselog.d("Initretrofit");
        APIClient = ApiServiceGenerator.createService(requireActivity().getApplicationContext(), APIService.class);
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
                        spJenisListRI.clear();
                        spJenisListRJ.clear();
                        spJenisListIGD.clear();

                        spJenisList.add(new EHOSUnit(0, "Semua Unit"));
                        spJenisListRI.add(new EHOSUnit(0, "Semua Unit"));
                        spJenisListRJ.add(new EHOSUnit(0, "Semua Unit"));
                        spJenisListIGD.add(new EHOSUnit(0, "Semua Unit"));
                        spJenisList.addAll(response.body());
                        Log.d(TAG, "onResponse: " + spJenisList.get(0).toString());

                        for(EHOSUnit unit : response.body()){
                            if(unit.getCatID() == 22 || unit.getCatID() == 65) { //RI
                                spJenisListRI.add(unit);
                            }else if(unit.getCatID() == 21){ //RJ
                                spJenisListRJ.add(unit);
                            }else if(unit.getCatID() == 23) { //IGD
                                spJenisListIGD.add(unit);
                            }
                        }

                        jenisAdapter.notifyDataSetChanged();
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

    private void fetchAllVisitsFilter(){
        progressBar.setVisibility(View.VISIBLE);

        if(queryValue==DetailVisitor.SRV_CODE_RI && spDateType.getSelectedItemPosition()==4){
            startDate = START_DATE;
        }

        Call<List<DetailVisitor>> call = APIClient.getAllPatientVisits(PetugasMainActivity.UID, 0, queryIdPoli, startDate, endDate, -1, -1, queryValue);

        call.enqueue(new Callback<List<DetailVisitor>>() {
            @Override
            @SuppressWarnings("ConstantConditions")
            public void onResponse(@NonNull Call<List<DetailVisitor>> call, @NonNull Response<List<DetailVisitor>> response) {
                progressBar.setVisibility(View.GONE);
                if(response.isSuccessful()) {
                    visitorList.clear();
                    visitorList.addAll(response.body());

                    visitorAdapter.notifyDataSetChanged();

                    if(visitorList.size() == 0){
                        layoutKosong.setVisibility(View.VISIBLE);
                    }else {
                        Log.d(TAG, "onResponse: NO Visit");
                        layoutKosong.setVisibility(View.GONE);
                    }
                }else{
                    Log.d(TAG, "onResponse: " + ApiError.parseError(response).getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DetailVisitor>> call,@NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                toastErr(context, t.getLocalizedMessage());
            }
        });
    }
}
