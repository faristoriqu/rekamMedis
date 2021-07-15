package com.sabin.digitalrm.fragments.prm.blanko;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.DmkAdapter;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.helpers.DefaultDialog;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.DMK;
import com.sabin.digitalrm.prm.DmkGeneratorActivity;
import com.sabin.digitalrm.prm.FieldSelectorActivity;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class DMKDetailFragment extends BaseFragment {
    private final int REQUEST_DMK_GENERATOR = 1;
    private final int REQUEST_DMK_FIELD_GENERATOR = 2;

    private APIService apiService;
    private Context mContext;

    private DmkAdapter dmkAdapter;
    private List<DMK> dmkList;

    private Integer dmkVersion = 0;

    private TextView tvDmks;

    public DMKDetailFragment() {
        // Required empty public constructor
    }

    public static DMKDetailFragment newInstance(Integer dmkVersion) {
        DMKDetailFragment fragment = new DMKDetailFragment();
        Bundle args = new Bundle();
        args.putInt("VERSION", dmkVersion);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);
        if (getArguments() != null) {
            dmkVersion = getArguments().getInt("VERSION");
        }

        mContext = getContext();
        apiService = APIUtils.getAPIService(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dmk_detail, container, false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDmks = view.findViewById(R.id.dmks);
        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(view1 -> {
            LayoutInflater inflater = getLayoutInflater();
            View container = inflater.inflate(R.layout.layout_new_dmk, null);
            final EditText etCode = container.findViewById(R.id.dmk_code);
            final EditText etName = container.findViewById(R.id.dmk_name);

            ProgressBar progressBar = container.findViewById(R.id.progress);

            DefaultDialog dialog = new DefaultDialog(mContext);
            dialog.setView(container);
            dialog.setTitle("Tambah DMK Baru");
            dialog.setIcon(mContext.getDrawable(R.drawable.ic_add));
            dialog.setPositiveButton("TAMBAH", view2 -> {
                progressBar.setVisibility(View.VISIBLE);
                String dmkCode = etCode.getText().toString();
                String dmkName = etName.getText().toString();
                Call<DMK> call = apiService.addDmk(dmkVersion, PetugasMainActivity.UID, dmkCode, dmkName, 0);
                call.enqueue(new Callback<DMK>() {
                    @Override
                    public void onResponse(Call<DMK> call, Response<DMK> response) {
                        if(response.isSuccessful()){
                            dialog.dismiss();
                            toastInfo(mContext, "DMK Berhasil ditambahkan");
                            dmkList.add(response.body());
                            dmkAdapter.notifyItemInserted(dmkList.indexOf(response.body()));
                        }else{
                            progressBar.setVisibility(View.GONE);
                            toastErr(mContext, ApiError.parseError(response).getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<DMK> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        toastErr(mContext, t.getLocalizedMessage());
                    }
                });
            });
            dialog.setNormalButton("Batal", null);
            dialog.show();
        });
        initRecView(view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_DMK_GENERATOR:{
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    int index = bundle.getInt(DmkGeneratorActivity.POS_DMK);
                    int pages = bundle.getInt(DmkGeneratorActivity.TOTAL_PAGE);
                    dmkList.get(index).setStatus(DMK.STATUS_DMK_GENERATED);
                    dmkList.get(index).setTotalPage(pages);

                    Log.d(TAG, "onActivityResult: " + dmkList.get(index).getStatus());
                    dmkAdapter.notifyItemChanged(index);
                }else{
                    toastErr(mContext, "DMK Generator dibatalkan");
                }

                break;
            }

            case REQUEST_DMK_FIELD_GENERATOR:{
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    int index = bundle.getInt(FieldSelectorActivity.DMK_POS);
                    dmkList.get(index).setStatus(DMK.STATUS_DMK_COORD);
                    dmkAdapter.notifyItemChanged(index);
                }else{
                    toastErr(mContext, "DMK Field Generator dibatalkan");
                }
                break;
            }
        }
    }

    private void initRecView(View view){
        dmkList = new ArrayList<>();

        RecyclerView recyclerView = view.findViewById(R.id.recView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        dmkAdapter = new DmkAdapter(dmkList) {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            protected void onBindViewHolder(@NonNull DmkHolder holder, int position, @NonNull final DMK model) {
                ImageView ivEdit, ivDelete;
                ivEdit = holder.itemView.findViewById(R.id.edit);
                ivDelete = holder.itemView.findViewById(R.id.delete);

                ivEdit.setOnClickListener(view1 -> {
                    LayoutInflater inflater = getLayoutInflater();
                    View container = inflater.inflate(R.layout.layout_new_dmk, null);
                    final EditText etCode = container.findViewById(R.id.dmk_code);
                    final EditText etName = container.findViewById(R.id.dmk_name);

                    etCode.setText(model.getCode());
                    etCode.setEnabled(false);
                    etName.setText(model.getName());
                    etName.requestFocus();

                    ProgressBar progressBar = container.findViewById(R.id.progress);

                    DefaultDialog dialog = new DefaultDialog(mContext);
                    dialog.setView(container);
                    dialog.setTitle("Edit DMK");
                    dialog.setIcon(mContext.getDrawable(R.drawable.ic_mode_edit_tint));
                    dialog.setPositiveButton("Simpan", view2 -> {
                        progressBar.setVisibility(View.VISIBLE);
                        String name = etName.getText().toString();
                        Call<ApiStatus> call = apiService.updateDMKName(model.getId(), PetugasMainActivity.UID, name);
                        call.enqueue(new Callback<ApiStatus>() {
                            @Override
                            public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                                if(response.isSuccessful()){
                                    int dataIndex = dmkList.indexOf(model);
                                    toastInfo(mContext, "Data berhasil disimpan");
                                    dmkList.get(dataIndex).setName(name);
                                    dmkAdapter.notifyItemChanged(dataIndex);
                                    dialog.dismiss();
                                }else{
                                    progressBar.setVisibility(View.GONE);
                                    toastErr(mContext, ApiError.parseError(response).getMessage());
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiStatus> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                toastErr(mContext, t.getLocalizedMessage());
                            }
                        });
                    });
                    dialog.setNormalButton("Batal", null);
                    dialog.show();
                });
                ivDelete.setOnClickListener(view1 -> {
                    DefaultDialog dialog = new DefaultDialog(mContext);
                    dialog.setTitle("Hapus DMK");
                    dialog.setIcon(mContext.getDrawable(R.drawable.ic_delete));
                    dialog.setMessage("Apakah anda yakin akan menghapus DMK " + model.getCode() + " ?");
                    dialog.setPositiveButton("Hapus", view2 -> {
                        Call <ApiStatus> call = apiService.deleteDMK(model.getId(), PetugasMainActivity.UID);
                        call.enqueue(new Callback<ApiStatus>() {
                            @Override
                            public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                                if(response.isSuccessful()){
                                    toastInfo(mContext, "DMK Berhasil Dihapus");
                                    dmkAdapter.notifyItemRemoved(dmkList.indexOf(model));
                                    dmkList.remove(model);
                                }else{
                                    String msg = ApiError.parseError(response).getMessage();
                                    Log.d(TAG, "onResponse: " + msg);
                                    toastErr(mContext, msg);
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiStatus> call, Throwable t) {
                                toastErr(mContext, t.getLocalizedMessage());
                                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                            }
                        });
                        dialog.dismiss();
                    });
                    dialog.setNormalButton("Batal", null);
                    dialog.show();

                });

                holder.itemView.setOnClickListener(view1 -> {
                    switch (model.getStatus()){
                        case DMK.STATUS_DMK_NEW:
                            dmkMenuLvl1(model);
                            break;
                        case DMK.STATUS_DMK_GENERATED:
                            dmkMenuLvl2(model);
                            break;
                        case DMK.STATUS_DMK_COORD:
                            dmkMenuLvl3(model);
                            break;
                    }
                });
                holder.bindInfo(model);
            }
        };

        recyclerView.setAdapter(dmkAdapter);
        recyclerView.setLayoutManager(manager);
        fetchDMKs(dmkVersion);
    }

    private void dmkMenuLvl1(DMK dmk){
        // new blanko with no spd yet
        String options[] = {"Render File DMK"};

        new AlertDialog.Builder(mContext).setTitle("Pilih Opsi")
                .setItems(options, (dialogInterface, i) -> {
                    switch (i){
                        case 0:{
                            genDMK(dmk);
                            break;
                        }
                    }
                })
                .show();
    }

    private void dmkMenuLvl2(DMK dmk){
        // blanko generated with no coordinate
        String options[] = {"Perbarui File DMK", "Create Coordinate"};

        new AlertDialog.Builder(mContext).setTitle("Pilih Opsi")
                .setItems(options, (dialogInterface, i) -> {
                    switch (i){
                        case 0:{
                            new AlertDialog.Builder(mContext)
                                    .setTitle("DMK Akan Diperbarui")
                                    .setMessage("DMK ini telah telah tersedia sebelumnya, Jika anda melanjutkan maka akan akan diganti dan semua data dmk sebelumnya akan dihapus")
                                    .setPositiveButton("Lanjutkan", (dialog, which) -> genDMK(dmk))
                                    .setNegativeButton("Batal", null)
                                    .setCancelable(false)
                                    .create()
                                    .show();
                            break;
                        }

                        case 1:{
                            genCoordinate(dmk);
                        }
                    }
                })
                .show();
    }

    private void dmkMenuLvl3(DMK dmk){
        // blanko with coordinate
        String [] options= new String[]{"Perbarui File DMK", "Update Coordinate",""};

        if(dmk.getStatus() == DMK.STATUS_DMK_COORD)
            options[2] = "Activate DMK";
        else if(dmk.getStatus() == DMK.STATUS_DMK_ACTIVE)
            options[2] = "Deactivate DMK";

        new AlertDialog.Builder(mContext).setTitle("Pilih Opsi")
                .setItems(options, (dialogInterface, i) -> {
                    switch (i){
                        case 0:{
                            new AlertDialog.Builder(mContext)
                                    .setTitle("DMK Akan Diperbarui")
                                    .setMessage("DMK ini telah telah tersedia sebelumnya, Jika anda melanjutkan maka akan akan diganti dan semua data dmk sebelumnya akan dihapus")
                                    .setPositiveButton("Lanjutkan", (dialog, which) -> genDMK(dmk))
                                    .setNegativeButton("Batal", null)
                                    .setCancelable(false)
                                    .create()
                                    .show();
                            break;
                        }

                        case 1:{
                            new AlertDialog.Builder(mContext)
                                    .setTitle("Update Coordinate")
                                    .setMessage("Update coordinate pada DMK ?")
                                    .setPositiveButton("Lanjutkan", (dialog, which) -> genCoordinate(dmk))
                                    .setNegativeButton("Batal", null)
                                    .setCancelable(false)
                                    .create()
                                    .show();
                            break;
                        }

                        case 2:{
                            if(dmk.getStatus() == DMK.STATUS_DMK_ACTIVE)
                                setDMKStatus(dmk, DMK.STATUS_DMK_COORD);
                            else
                                setDMKStatus(dmk, DMK.STATUS_DMK_ACTIVE);

                            break;
                        }
                    }
                })
                .show();
    }

    private void genDMK(DMK dmk){
        Intent intent = new Intent(mContext, DmkGeneratorActivity.class);
        intent.putExtra(DmkGeneratorActivity.STATUS_DMK, dmk.getStatus());
        intent.putExtra(DmkGeneratorActivity.ID_DMK, dmk.getId());
        intent.putExtra(DmkGeneratorActivity.CODE_DMK, dmk.getCode());
        intent.putExtra(DmkGeneratorActivity.POS_DMK, dmkList.indexOf(dmk));
        startActivityForResult(intent, REQUEST_DMK_GENERATOR);
    }

    private void genCoordinate(DMK dmk){
        Intent i = new Intent(mContext, FieldSelectorActivity.class);
        i.putExtra("id", dmk.getId());
        i.putExtra("namapoli", dmk.getCode());
        i.putExtra("filename", dmk.getFilename().split("/")[1]);
        i.putExtra(FieldSelectorActivity.DMK_POS, dmkList.indexOf(dmk));
        startActivityForResult(i, REQUEST_DMK_FIELD_GENERATOR);
    }

    private void updateDmkData(List<DMK> dmks){
        tvDmks.setText(dmks.size() + " DMK(s)");
        dmkList.clear();
        dmkList.addAll(dmks);
        dmkAdapter.notifyDataSetChanged();
    }

    private void setDMKStatus(DMK dmk, int status){

    }

    public void fetchDMKs(int version){
        dmkVersion = version;
        Log.e("[X-DEBUG]", "fetchDMKs [v: "+version+" ; uid: "+PetugasMainActivity.UID);
        Call<List<DMK>> call = apiService.getDmks(version, PetugasMainActivity.UID);
        call.enqueue(new Callback<List<DMK>>() {
            @Override
            public void onResponse(Call<List<DMK>> call, @NonNull Response<List<DMK>> response) {
                if(response.isSuccessful()){
                    Log.e(TAG, "onResponse DMKS: Successful");
                    updateDmkData(response.body());
                }else{
                    Log.e(TAG, "onResponse DMKS: " + ApiError.parseError(response).getMessage());
                }
            }

            @Override
            public void onFailure(Call<List<DMK>> call, Throwable t) {

            }
        });
    }

}
