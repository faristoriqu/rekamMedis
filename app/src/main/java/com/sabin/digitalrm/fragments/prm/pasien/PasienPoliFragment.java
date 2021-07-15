package com.sabin.digitalrm.fragments.prm.pasien;


import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.VisitorAdapter;
import com.sabin.digitalrm.dialogs.DialogPrepareDMR;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.DetailBlanko;
import com.sabin.digitalrm.models.DetailVisitor;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.prm.PoliBaruActivity;
import com.sabin.digitalrm.utils.ApiError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;

public class PasienPoliFragment extends BaseFragment {
    private static final String ARG_PATH = "param1";
    private static final String ARG_ISALL = "param2";

    private Integer path;
    private boolean isAll = false;
    private boolean isCreated = false, isEmpety = true;


    private List<DetailVisitor> visitorList;
    private APIService APIClient;

    private Context context;
    private RecyclerView visitorRecycler;
    private VisitorAdapter visitorAdapter;
    private FloatingActionButton fab;
    private RelativeLayout layoutKosong;

    private OnListEmpetyListener mListListener;

    public interface OnListEmpetyListener{
        void onListEmpety(int categoryID);
    }

    public PasienPoliFragment() {
        // Required empty public constructor
    }

    public void setOnListEmpetyListener(OnListEmpetyListener listener){
        mListListener = listener;
    }

    public static PasienPoliFragment newInstance(int path, boolean isAll) {
        PasienPoliFragment fragment = new PasienPoliFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PATH, path);
        args.putBoolean(ARG_ISALL, isAll);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getInt(ARG_PATH);
            isAll = getArguments().getBoolean(ARG_ISALL);
        }

        visitorList = new ArrayList<>();
        context = getContext();
        initRetrofit();
        fetchVisitsByPath();
        isCreated = true;
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_pasien_poli, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        visitorRecycler = view.findViewById(R.id.recView);
        visitorRecycler.setHasFixedSize(true);

        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        fab = view.findViewById(R.id.fab_refresh);
        layoutKosong = view.findViewById(R.id.layout_kosong);

        fab.setOnClickListener(v -> {
            Baseprogress.showProgressDialog(context, "Loading...");
            fetchVisitsByPath();
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

        if(isCreated && !isEmpety)
            layoutKosong.setVisibility(View.GONE);

        Log.d(TAG, "onViewCreated: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + path);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroy: " + path);
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

    private void initRetrofit(){
        BaseActivity.Baselog.d("Initretrofit");
        APIClient = ApiServiceGenerator.createService(requireActivity().getApplicationContext(), APIService.class);
    }

    private void showVisitorOption(DetailVisitor visitor){
        switch (visitor.getStatus()){
            case DetailVisitor.VISITOR_DMR_VISIT:{
                optionVisit(visitor);

                break;
            }
        }

        switch (visitor.getStatus()){
            case DetailVisitor.VISITOR_DMR_PASIVE:{
                optionPasive(visitor);

                break;
            }

            case DetailVisitor.VISITOR_DMR_ACTIVE:{
                optionActive(visitor);
                break;
            }

        }

        /*
        String options [];
        switch (visitor.getJenisKunjungan()){
            case VisitorType.KUNJUNGAN_LAMA:{
                options = new String[1];
                options[0] = "Buka Dokumen";
                optionOldVisit(options, visitor);
                break;
            }

            case VisitorType.KUNJUNGAN_POLI_BARU:{
                if(visitor.getStatusOpen()) {
                    options = new String[2];
                    options[1] = "Buka BRM";
                    options[0] = "Rubah Blanko UnitForBRMDetail";
                }
                else {
                    options = new String[1];
                    options[0] = "Tambah Blanko UnitForBRMDetail";
                }

                optionNewPoli(options, visitor);
                break;
            }

            case VisitorType.KUNJUNGAN_BARU:{
                options = new String[1];
                options[0] = "Susun Berkas Rekam Medik";
                optionNewVisit(options, visitor);
                break;
            }
        }
        */
    }

    private void optionVisit(DetailVisitor visitor){
        if(visitor.getIsNewVisit() == 1){
            // susun berkas
            String options [] = {"Susun Berkas"};
            new AlertDialog.Builder(context).setTitle("Pilih Opsi")
                    .setItems(options, (dialogInterface, i) -> {
                        switch (i){
                            case 0:{ // New Visit with brm unready
                                DialogPrepareDMR dialogPrepareDMR = DialogPrepareDMR.newInstance(visitor.getUnitCat());

                                dialogPrepareDMR.setOnFinishListerner((dblanko, undmk) -> onDialogPrepareFinish(dblanko, undmk, visitor, visitorList.indexOf(visitor)));

                                dialogPrepareDMR.show(getChildFragmentManager(), "dialog");
                                break;
                            }
                        }
                    })
                    .show();
        }
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

    private void optionPasive(DetailVisitor visitor){
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

    private void optionActive(DetailVisitor visitor){
        toastInfo(context, "Tidak ada Menu!");
    }

    private void optionNewPoli(String options [], final DetailVisitor data){
        new AlertDialog.Builder(context).setTitle("Pilih Opsi")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:{ //FOR OPEN DOC
                                /*
                                Intent intent = new Intent(context, PoliBaruActivity.class);
                                intent.putExtra(PoliBaruActivity.EXTRA_TEMPLATE_NAME, data.getInfoPoli().getPoliCode());
                                intent.putExtra(PoliBaruActivity.EXTRA_POLI_NAME, data.getInfoPoli().getNamaPoli());
                                intent.putExtra(PoliBaruActivity.EXTRA_NO_BRM, data.getNoBrm());
                                intent.putExtra(PoliBaruActivity.EXTRA_POLI_ID, data.getInfoPoli().getId());
                                intent.putExtra(PoliBaruActivity.EXTRA_DATASET_INDEX, visitorList.indexOf(data));

                                startActivityForResult(intent, PoliBaruActivity.REQUEST_POLI_BARU);
                                */
                                break;
                            }
                            case 1:{ //FOR OPEN BRM
                                insertNewBRM(data);
                            }
                        }
                    }
                })
                .show();
    }

    private void optionNewVisit(String options [], DetailVisitor data){
        new AlertDialog.Builder(context).setTitle("Pilih Opsi")
                .setItems(options, (dialogInterface, i) -> {
                    switch (i){
                        case 0:{ //FOR OPEN DOC
                            //TODO: NEW DOC
                            break;
                        }
                    }
                })
                .show();
    }

    private void optionOldVisit(String options [], final DetailVisitor data){
        new AlertDialog.Builder(context).setTitle("Pilih Opsi")
                .setItems(options, (dialogInterface, i) -> {
                    switch (i){
                        case 0:{ //FOR OPEN DOC
                            //TODO: OPEN DOC BY API
                            Baseprogress.showProgressDialog(context, "Membuka BRM...");
                            insertNewBRM(data);
                            break;
                        }
                    }
                })
                .show();
    }

    private void activateDMR(DetailVisitor visitor, int idDMR){
        Call<ApiStatus> callAVisit = APIClient.activatePatientVisit(visitor.getSrvId(), PetugasMainActivity.UID, 3, Integer.valueOf(PetugasMainActivity.UID), idDMR);
        Baseprogress.showProgressDialog(context, "Mengaktifkan DMR " + visitor.getInfoPasien().getNoBrm());
        callAVisit.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    toastInfo(context, "Berhasil mengaktifkan DMR");
                    visitor.setStatus(3);
                    visitorAdapter.notifyItemChanged(visitorList.indexOf(visitor));
                }else{
                    toastErr(context, ApiError.parseError(response).getMessage());
                }
            }

            @Override
            public void onFailure(Call<ApiStatus> call, Throwable t) {
                Baseprogress.hideProgressDialog();
                toastErr(context, t.getLocalizedMessage());
            }
        });
    }

    private void insertNewBRM(final DetailVisitor data){
        /*
        Baseprogress.showProgressDialog(context, "Membuka Dokumen...");
        Log.d(TAG, "insertNewBRM: " + data.toJson());
        Call <ApiResponse> call = APIClient.createListBRM(PetugasMainActivity.UID, data.toJson());
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    if(Objects.requireNonNull(response.body()).getStatuscode() == 200){
                        toastInfo(context, "BRM berhasil dibuka");
                        int index = visitorList.indexOf(data);
                        visitorList.remove(index);
                        visitorAdapter.notifyItemRemoved(index);
                        if(visitorList.size() == 0){
                            isEmpety = true;
                            layoutKosong.setVisibility(View.VISIBLE);
                        }
                    }else{
                        toastInfo(context, Objects.requireNonNull(response.body()).getMessage());
                    }
                }else{
                    toastErr(context, response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Baseprogress.hideProgressDialog();
                toastErr(context, t.getLocalizedMessage());
            }
        });
        */
    }

    private void fetchVisitsByPath(){
        Call<List<DetailVisitor>> call;
        if(isAll){
            call = APIClient.getAllPatientVisits(PetugasMainActivity.UID, 0, 0, null, null, -1, -1, -1);
        }else {
            call = APIClient.getVisitsByUnit(path, PetugasMainActivity.UID);
        }

        call.enqueue(new Callback<List<DetailVisitor>>() {
            @Override
            public void onResponse(Call<List<DetailVisitor>> call, Response<List<DetailVisitor>> response) {
                if(response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + path + response.body().get(0).getTanggalSrv());
                    visitorList.clear();
                    visitorList.addAll(response.body());
                    visitorAdapter.notifyDataSetChanged();
                    if(visitorList.size() == 0){
                        layoutKosong.setVisibility(View.VISIBLE);
                        isEmpety = true;
                    }else {
                        Log.d(TAG, "onResponse: NO Visit");
                        layoutKosong.setVisibility(View.GONE);
                        isEmpety = false;
                    }
                }else{
                    Log.d(TAG, "onResponse: " + ApiError.parseError(response).getMessage());
                }
            }

            @Override
            public void onFailure(Call<List<DetailVisitor>> call, Throwable t) {

            }
        });
    }

}
