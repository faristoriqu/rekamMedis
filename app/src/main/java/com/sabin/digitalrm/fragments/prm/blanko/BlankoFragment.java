package com.sabin.digitalrm.fragments.prm.blanko;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.BlankoAdapter;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.InfoPoli;
import com.sabin.digitalrm.models.InfoPoliResponse;
import com.sabin.digitalrm.models.ListInfoPoliResponse;
import com.sabin.digitalrm.prm.DmkGeneratorActivity;
import com.sabin.digitalrm.prm.FieldSelectorActivity;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.APIUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankoFragment extends BaseFragment {
    private final int REQ_GEN_BLANKO = 1;
    private Context mContext;
    private APIService APIClient;

    private BlankoAdapter blankoAdapter;
    private List<InfoPoli> infoPoliList;

    private FloatingActionButton fabAdd;

    public BlankoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);

        mContext = getContext();
        APIClient = APIUtils.getAPIService(requireActivity());
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blanko, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> createNewBlanko());
        initBlankoVIew(view);
        fetchBlanko();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQ_GEN_BLANKO:{
                if(resultCode == RESULT_OK){
                    fetchBlanko();
                }
            }
        }
    }

    private void initDataset(){
        infoPoliList = new ArrayList<>();
    }

    private void initBlankoVIew(View view){
        RecyclerView recyclerView = view.findViewById(R.id.poli_card_view);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        blankoAdapter = new BlankoAdapter(infoPoliList) {
            @Override
            protected void onBindViewHolder(@NonNull BRMHodler hodler, int position, @NonNull final InfoPoli model) {
                hodler.itemView.setOnClickListener(view1 -> dlgMenuList(model));
                hodler.bindInfo(model);
            }
        };

        recyclerView.setAdapter(blankoAdapter);
    }

    private void dlgMenuList(InfoPoli data){
        String mnList[] = {"Generate BRM Baru", "Update blanko"};
        new AlertDialog.Builder(mContext).setItems(mnList, (dialog, which) -> {
            switch (which){
                case 0:{
                    generateNewBlanko(data);
                    break;
                }
                case 1:{
                    //TODO: Reintegration
                    Intent i = new Intent(mContext, FieldSelectorActivity.class);
                    i.putExtra("id", 8);
                    i.putExtra("namapoli", "tmpfile");
                    i.putExtra("filename", "Test DMK");
                    startActivity(i);
                    break;
                }
            }
        })
        .setTitle("Pilih Opsi")
        .show();
    }

    private void generateNewBlanko(InfoPoli data){
        //FIX THIS
        if(true){
            new AlertDialog.Builder(mContext)
                    .setCancelable(false)
                    .setTitle("Blanko Telah Tersedia")
                    .setMessage("Blanko untuk poli ini telah ada, semua data blanko poli akan dihapus. Lanjutkan ?")
                    .setPositiveButton("YA", (i, j) -> blankoGenerator(data))
                    .setNegativeButton("Batal", null)
                    .show();
        }else{
            blankoGenerator(data);
        }
//        Toast.makeText(mContext, "Coming Soon. Fitur ini belum tersedia.", Toast.LENGTH_LONG).show();
    }

    private void blankoGenerator(InfoPoli data){
        Intent intent = new Intent(mContext, DmkGeneratorActivity.class);
        intent.putExtra(DmkGeneratorActivity.ID_DMK, 8);
        intent.putExtra(DmkGeneratorActivity.STATUS_DMK, 1);
        startActivityForResult(intent, REQ_GEN_BLANKO);
    }

    private void createNewBlanko(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = inflater.inflate(R.layout.dialog_new_blanko, null);
        EditText edPoli = view.findViewById(R.id.nama_poli);

        builder.setView(view);
        builder.setPositiveButton("Tambah", (dialog, which) -> {
            String poli = edPoli.getText().toString();
            createBlanko(poli);
        });

        builder.setNegativeButton("Batal", null);
        builder.setCancelable(false);
        builder.create().show();
    }

    private void createBlanko(String poli){
        Call<InfoPoliResponse> call = APIClient.createBlanko(PetugasMainActivity.UID, poli);
        Baseprogress.showProgressDialog(mContext, "Menambahkan Blanko...");

        call.enqueue(new Callback<InfoPoliResponse>() {
            @Override
            public void onResponse(Call<InfoPoliResponse> call, Response<InfoPoliResponse> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    if(response.body().getStatuscode() == 200){
                        Log.d(TAG, "onResponse: " + response.body().getInfoPoli().getTemplateName());
                        infoPoliList.add(response.body().getInfoPoli());
                        blankoAdapter.notifyItemInserted(infoPoliList.size() - 1);
                    }else{
                        toastErr(mContext, response.body().getMessage());
                    }
                }else{
                    toastErr(mContext, response.message());
                }
            }

            @Override
            public void onFailure(Call<InfoPoliResponse> call, Throwable t) {
                Baseprogress.hideProgressDialog();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                toastErr(mContext, t.getLocalizedMessage());
            }
        });
    }

    private void fetchBlanko(){
        Call<ListInfoPoliResponse> call = APIClient.getListBlanko(PetugasMainActivity.UID);

        Baseprogress.showProgressDialog(mContext, "Mengambil daftar...");
        call.enqueue(new Callback<ListInfoPoliResponse>() {
            @Override
            public void onResponse(@NonNull Call<ListInfoPoliResponse> call, @NonNull Response<ListInfoPoliResponse> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    if(response.body().getStatuscode() == 200){
                        if(response.body().getInfoPoliList().size() > 0) {
                            infoPoliList.clear();
                            infoPoliList.addAll(response.body().getInfoPoliList());
                            blankoAdapter.notifyDataSetChanged();
                        }else{
                            toastInfo(mContext, "Daftar blanko kosong");
                        }
                    }else{
                        toastErr(mContext, response.body().getMessage());
                    }
                }else{
                    toastErr(mContext, response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ListInfoPoliResponse> call, @NonNull Throwable t) {
                Baseprogress.hideProgressDialog();
                toastErr(mContext, t.getLocalizedMessage());
            }
        });
    }
}
