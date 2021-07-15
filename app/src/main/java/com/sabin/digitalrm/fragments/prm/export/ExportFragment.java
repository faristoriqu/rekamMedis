package com.sabin.digitalrm.fragments.prm.export;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.BRMListAdapter;
import com.sabin.digitalrm.adapters.PrmPoliListAdapter;
import com.sabin.digitalrm.dialogs.DialogExportDMK;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.helpers.DefaultDialog;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.DMRExport;
import com.sabin.digitalrm.models.DMRPatient;
import com.sabin.digitalrm.models.InfoPasien;
import com.sabin.digitalrm.prm.ExportActivity;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.ClipboardUtils;
import com.sabin.digitalrm.utils.ShareUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExportFragment extends BaseFragment implements SearchView.OnQueryTextListener {
    private RelativeLayout layoutBrmEmpty;
    private FrameLayout layoutPoliEmpty;
    private TextView filterBRM, totalBRM;

    private BRMListAdapter brmListAdapter;
    private PrmPoliListAdapter poliListAdapter;
    private ListView brmListView;
    private MenuItem searchMenuItem;
    private SearchView searchView;
    private ProgressBar progressBar;

    private Context mContext;
    private APIService APIClient;

    private List<InfoPasien> infoBRMList;
    private List<DMRPatient> infoPoliList;
    private String query;

    private final String QUERY_ALL_BRM = "all";
    private final String QUERY_ACTIVE_BRM = "aktif";

    private String selectedPXName;

    public ExportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getContext();
        APIClient = APIUtils.getAPIService(mContext);
        infoBRMList = new ArrayList<>();
        infoPoliList = new ArrayList<>();
        query = QUERY_ACTIVE_BRM;

        //fetchBRMList();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_export, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutBrmEmpty = view.findViewById(R.id.brm_list_empty);
        layoutPoliEmpty = view.findViewById(R.id.poli_list_empty);
        filterBRM = view.findViewById(R.id.txt_filter);
        totalBRM = view.findViewById(R.id.total_brm);
        progressBar = view.findViewById(R.id.progress);

        initListBRM(view);
        initDetailBRM(view);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.petugas_fragment_export_menu, menu);

        SearchManager searchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        searchView.setSearchableInfo(Objects.requireNonNull(searchManager).
                getSearchableInfo(Objects.requireNonNull(getActivity()).getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Cari BRM...");
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_refresh:{
                fetchBRMList();
                break;
            }

            case R.id.action_brm_aktif:{
                query = QUERY_ACTIVE_BRM;
                filterBRM.setText("BRM Aktfi");
                fetchBRMList();
                break;
            }

            case R.id.action_brm_all:{
                query = QUERY_ALL_BRM;
                filterBRM.setText("Semua BRM");
                fetchBRMList();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Log.d(TAG, "onQueryTextSubmit: " + query);

        filterBRM.setText("Hasil Pencarian");
        Baseprogress.showProgressDialog(mContext, "Mencari BRM...");
        Call <List<InfoPasien>> call = APIClient.getPatients(PetugasMainActivity.UID, query);
        fetchBRM(call);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(TAG, "onQueryTextChange: " + newText);
        return false;
    }

    private void initListBRM(View view){
        brmListAdapter = new BRMListAdapter(mContext, R.layout.listview_brm, infoBRMList);

        brmListView = view.findViewById(R.id.lvItems);
        brmListView.setAdapter(brmListAdapter);
        brmListView.setOnItemClickListener((parent, view1, position, id) -> onBRMSelected(Objects.requireNonNull(brmListAdapter.getItem(position))));
    }

    private void initDetailBRM(View view){
        RecyclerView recyclerView = view.findViewById(R.id.brm_card_view);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        poliListAdapter = new PrmPoliListAdapter(infoPoliList) {
            @Override
            protected void onBindViewHolder(@NonNull BRMHodler hodler, int position, @NonNull final DMRPatient model) {

                hodler.itemView.setOnClickListener(view1 -> {
                    showExportOption(model);
                });
                hodler.bindInfo(model);
            }
        };

        recyclerView.setAdapter(poliListAdapter);
    }


    private void onBRMSelected(InfoPasien data){
        fetchPoliList(data.getNoBrm());
        selectedPXName = data.getNamaPasien();
    }

    private void updateBRMList(List<InfoPasien> dataset){
        infoBRMList.clear();
        infoBRMList.addAll(dataset);
        brmListAdapter.notifyDataSetChanged();
    }

    private void showExportOption(DMRPatient data){
        String [] options = {"Export...", "Export DMK(s)"};
        new AlertDialog.Builder(mContext)
                .setTitle("Opsi Export")
                .setItems(options, ((dialog, which) -> {
                    switch (which){
                        case 0:{
                            Intent intent = new Intent(mContext, ExportActivity.class);
                            if(selectedPXName==null){
                                selectedPXName = "no name";
                            }

                            intent.putExtra(ExportActivity.EXTRA_ID_DMR, data.getId());
                            intent.putExtra(ExportActivity.EXTRA_NORM, data.getNoRM());
                            intent.putExtra(ExportActivity.EXTRA_ID_UNIT, data.getUnitId());
                            intent.putExtra(ExportActivity.EXTRA_PXNAME, selectedPXName);

                            startActivity(intent);
                            break;
                        }

                        case 1:{
                            DialogExportDMK dialogExportDMK = DialogExportDMK.newInstance(data.getId());
                            dialogExportDMK.setOnFinishListerner(export ->  {
                                    Log.d(TAG, "onFinish: " + export.getFileName());
                                    exportLink(export);
                            });
                            dialogExportDMK.show(getChildFragmentManager(), null);

                            break;
                        }
                    }
                } ))
                .create()
                .show();
    }

    private void exportLink(DMRExport export){
        LayoutInflater inflater = getLayoutInflater();
        View container = inflater.inflate(R.layout.layout_export_link_action, null);

        String url = ApiServiceGenerator.getBaseUrl(requireActivity().getApplicationContext()) + "download/exports/" + export.getId() + "?fkey=" + export.getFileKey();

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

    private void updatePoliList(List<DMRPatient> dataset){
        infoPoliList.clear();
        infoPoliList.addAll(dataset);
        poliListAdapter.notifyDataSetChanged();
    }

    private void fetchPoliList(final String brm){
        progressBar.setVisibility(View.VISIBLE);
        Log.e("~Debug", "CURRENT BRM: "+brm);
        Call<InfoPasien> listPoliResponseCall = APIClient.getDMRsPatient(
                PetugasMainActivity.UID,
                brm,
                "export"
        );

        listPoliResponseCall.enqueue(new Callback<InfoPasien>() {
            @Override
            public void onResponse(Call<InfoPasien> call, Response<InfoPasien> response) {
                progressBar.setVisibility(View.GONE);
                if(response.isSuccessful()){
                    Integer size = response.body().getDmrPatientList().size();
                    if(size > 0){
                        layoutPoliEmpty.setVisibility(View.GONE);
                        updatePoliList(response.body().getDmrPatientList());
                    }else {
                        toastInfo(mContext, response.body().getNoBrm() + " tidak memiliki DMR");
                        layoutPoliEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<InfoPasien> call, Throwable t) {

            }
        });
    }

    private void fetchBRMList(){
        Baseprogress.showProgressDialog(mContext, "Mengambil daftar BRM...");
        Call<List<InfoPasien>> listBRMResponseCall = APIClient.getPatients(PetugasMainActivity.UID, query);
        fetchBRM(listBRMResponseCall);
    }

    private void fetchBRM(Call<List<InfoPasien>> call){
        call.enqueue(new Callback<List<InfoPasien>>() {
            @Override
            public void onResponse(Call<List<InfoPasien>> call, Response<List<InfoPasien>> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    Integer size = Objects.requireNonNull(response.body()).size();
                    if(size > 0) {
                        updateBRMList(Objects.requireNonNull(response.body()));
                        layoutBrmEmpty.setVisibility(View.GONE);
                        if(size == 50)
                            totalBRM.setText(size + "+");
                        else
                            totalBRM.setText(size.toString());
                    }else {
                        layoutPoliEmpty.setVisibility(View.VISIBLE);
                        layoutBrmEmpty.setVisibility(View.VISIBLE);
                        Baselog.d("No record");
                        toastInfo(mContext, "Daftar BRM Kosong");
                        totalBRM.setText("0");
                    }
                }else {
                    String msg = ApiError.parseError(response).getMessage();
                    Log.d(TAG, "onResponse: " + msg);
                    toastErr(mContext, msg);
                }
            }

            @Override
            public void onFailure(Call<List<InfoPasien>> call, Throwable t) {
                Baseprogress.hideProgressDialog();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                toastErr(mContext, t.getLocalizedMessage());
            }
        });
    }
}
