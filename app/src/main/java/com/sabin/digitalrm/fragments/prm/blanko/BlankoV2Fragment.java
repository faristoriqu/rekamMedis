package com.sabin.digitalrm.fragments.prm.blanko;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.BlankoV2Adapter;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.helpers.DefaultDialog;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.UnitCategory;
import com.sabin.digitalrm.models.UnitVersionResponse;
import com.sabin.digitalrm.models.UnitsBlanko;
import com.sabin.digitalrm.models.VersionsBlanko;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankoV2Fragment extends BaseFragment {
    private Context mContext;
    private APIService APIClient;
    private static APIService SAPIClient;
    private BlankoV2Adapter blankoAdapter;
    private List<UnitsBlanko> uLists;
    public static List<VersionsBlanko> vLists;
    private RecyclerView.LayoutManager lym;
    private View rootContainer;
    private FrameLayout listEmpty;

    private DMKDetailFragment dmkDetailFragment;
    private BlankoDetailFragment blankoDetailFragment;
    private FragmentManager fm;

    private boolean isDmkDetail = false, isBlankoDetail = false;
    private Logger log = new Logger();

    public BlankoV2Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);
        mContext = this.getActivity();


        uLists = new ArrayList<>();
        vLists = new ArrayList<>();

        fm = getChildFragmentManager();

        initRetrofit();
        fetchBlanko();
    }

    private void initRetrofit() {
        BaseActivity.Baselog.d("Initretrofit");
        APIClient = APIUtils.getAPIService(mContext);
    }

    private void initBlankoView(){
        RecyclerView recyclerView = rootContainer.findViewById(R.id.rvMasterBlanko);
        lym = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(lym);
        blankoAdapter = new BlankoV2Adapter(mContext, uLists, vLists, null) {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            protected void onAddBlankoUCClick() {
                LayoutInflater inflater = getLayoutInflater();
                View container = inflater.inflate(R.layout.add_blanko_cat_dialog, null);
                final Spinner spUnitCat = container.findViewById(R.id.spUnitCat);
                final EditText etName = container.findViewById(R.id.etName);
                List<UnitCategory> lstUncat = new ArrayList<>();
                List<Integer> lstIdUnCat = new ArrayList<>();
                List<String> lstNameUnCat = new ArrayList<>();
                List<String> lstSpinnerAdapter = new ArrayList<>();

                FrameLayout progressBar = container.findViewById(R.id.content_loading);

                getUnitCatList getUnCatAsync = new getUnitCatList(mContext);
                getUnCatAsync.setOnCompleteListener(s -> {
                    lstUncat.addAll(s);

                    int lstCount = lstUncat.size();

                    for(int i=0; i<lstCount; i++){
                        lstIdUnCat.add(i, lstUncat.get(i).getId());
                        lstNameUnCat.add(i, lstUncat.get(i).getName());
                    }

                    for(int i=0; i<=lstCount+1; i++){
                        if(i==0){
                            lstSpinnerAdapter.add(i, "-- Pilih Unit Category --");
                        }else if(i==1){
                            lstSpinnerAdapter.add(i, "Tambah Baru ...");
                        }else {
                            lstSpinnerAdapter.add(i, "* " + lstNameUnCat.get(i-2));
                        }
                    }

                    ArrayAdapter<String> spAdapter2 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, Objects.requireNonNull(lstSpinnerAdapter));
                    spUnitCat.setAdapter(spAdapter2);
                    spAdapter2.notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);
                });
                getUnCatAsync.execute();

                spUnitCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if(i==1){
                            etName.setVisibility(View.VISIBLE);
                        }else{
                            etName.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                DefaultDialog dialog = new DefaultDialog(mContext);
                dialog.setView(container);
                dialog.setTitle("Tambah Kategori Blanko Baru");
                dialog.setIcon(mContext.getDrawable(R.drawable.ic_add));
                dialog.setPositiveButton("TAMBAH", view2 -> {
                    String name;
                    String id = null;
                    int selectedUnCatPos = spUnitCat.getSelectedItemPosition( ) - 2;

                    if(selectedUnCatPos==1){
                        name = etName.getText().toString();
                    }else {
                        id = String.valueOf(lstIdUnCat.get(selectedUnCatPos));
                        name = lstNameUnCat.get(selectedUnCatPos);
                    }

                    if(selectedUnCatPos==0){
                        Snackbar.make(view2, "Pilih Kategori Blanko terlebih dahulu!", Snackbar.LENGTH_LONG).show();
                    }else{
                        progressBar.setVisibility(View.VISIBLE);

                        Call<ApiStatus> call = APIClient.addNewUnitCat(PetugasMainActivity.UID, id, name);
                        call.enqueue(new Callback<ApiStatus>() {
                            @Override
                            public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                                if (response.isSuccessful()) {
                                    dialog.dismiss();
                                    toastInfo(mContext, "Kategori Blanko baru berhasil ditambahkan");
                                    fetchBlanko();
                                    blankoAdapter.notifyDataSetChanged();
                                } else {
                                    toastErr(mContext, ApiError.parseError(response).getMessage());
                                }

                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(Call<ApiStatus> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                toastErr(mContext, t.getLocalizedMessage());
                            }
                        });
                    }
                });
                dialog.setNormalButton("Batal", null);
                dialog.show();
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            protected void onAddDMKVClick() {
                LayoutInflater inflater = getLayoutInflater();
                View container = inflater.inflate(R.layout.layout_new_dmk_v, null);
                final EditText etCode = container.findViewById(R.id.version_code);
                final EditText etName = container.findViewById(R.id.version_name);

                ProgressBar progressBar = container.findViewById(R.id.progress);

                DefaultDialog dialog = new DefaultDialog(mContext);
                dialog.setView(container);
                dialog.setTitle("Tambah DMK Versi Baru");
                dialog.setIcon(mContext.getDrawable(R.drawable.ic_add));
                dialog.setPositiveButton("TAMBAH", view2 -> {
                    progressBar.setVisibility(View.VISIBLE);
                    String version = etCode.getText().toString();
                    String name = etName.getText().toString();
                    Call<VersionsBlanko> call = APIClient.addDmkVersion(PetugasMainActivity.UID, false, name, version);
                    call.enqueue(new Callback<VersionsBlanko>() {
                        @Override
                        public void onResponse(Call<VersionsBlanko> call, Response<VersionsBlanko> response) {
                            if(response.isSuccessful()){
                                dialog.dismiss();
                                toastInfo(mContext, "Versi DMK Berhasil ditambahkan");
                                vLists.add(response.body());
                                fetchBlanko();
                                blankoAdapter.notifyDataSetChanged();
                            }else{
                                progressBar.setVisibility(View.GONE);
                                toastErr(mContext, ApiError.parseError(response).getMessage());
                            }
                        }

                        @Override
                        public void onFailure(Call<VersionsBlanko> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            toastErr(mContext, t.getLocalizedMessage());
                        }
                    });
                });
                dialog.setNormalButton("Batal", null);
                dialog.show();
            }

            private void showEditDMKCDialog(int ids, String nm){
                final boolean[] newCat = {true};
                LayoutInflater inflater = getLayoutInflater();
                View container = inflater.inflate(R.layout.add_blanko_cat_dialog, null);
                final Spinner spUnitCat = container.findViewById(R.id.spUnitCat);
                final EditText etName = container.findViewById(R.id.etName);
                List<UnitCategory> lstUncat = new ArrayList<>();
                List<Integer> lstIdUnCat = new ArrayList<>();
                List<String> lstNameUnCat = new ArrayList<>();
                List<String> lstSpinnerAdapter = new ArrayList<>();

                FrameLayout progressBar = container.findViewById(R.id.content_loading);

                getUnitCatList getUnCatAsync = new getUnitCatList(mContext);
                getUnCatAsync.setOnCompleteListener(s -> {

                    lstUncat.addAll(s);

                    int lstCount = lstUncat.size();

                    for(int i=0; i<lstCount; i++){
                        lstIdUnCat.add(i, lstUncat.get(i).getId());
                        lstNameUnCat.add(i, lstUncat.get(i).getName());
                    }

                    if(lstIdUnCat.contains(ids)){
                        newCat[0] = false;
                    }

                    for(int i=0; i<=lstCount+1; i++){
                        if(i==0){
                            lstSpinnerAdapter.add(i, "-- Pilih Unit Category --");
                        }else if(i==1){
                            lstSpinnerAdapter.add(i, "Tambah Baru ...");
                        }else {
                            lstSpinnerAdapter.add(i, "* " + lstNameUnCat.get(i-2));
                        }
                    }

                    ArrayAdapter<String> spAdapter2 = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, Objects.requireNonNull(lstSpinnerAdapter));
                    spUnitCat.setAdapter(spAdapter2);

                    if(newCat[0]){
                        spUnitCat.setSelection(1);
                        etName.setVisibility(View.VISIBLE);
                        etName.setText(nm);
                    }else{
                        int spSelectedPos = spAdapter2.getPosition("* " + nm);
                        spUnitCat.setSelection(spSelectedPos);
                    }

                    spAdapter2.notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);
                });
                getUnCatAsync.execute();

                spUnitCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if(i==1){
                            etName.setVisibility(View.VISIBLE);
                        }else{
                            etName.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                DefaultDialog dialog = new DefaultDialog(mContext);
                dialog.setView(container);
                dialog.setTitle("Ubah Kategori Blanko");
                dialog.setIcon(mContext.getDrawable(R.drawable.ic_add));
                dialog.setPositiveButton("UBAH", view2 -> {
                    String name;
                    String id = null;
                    int selectedUnCatPos = spUnitCat.getSelectedItemPosition( ) - 2;

                    if(selectedUnCatPos==1){
                        name = etName.getText().toString();
                    }else {
                        id = String.valueOf(lstIdUnCat.get(selectedUnCatPos));
                        name = lstNameUnCat.get(selectedUnCatPos);
                    }

                    if(selectedUnCatPos==0){
                        Snackbar.make(view2, "Pilih Kategori Blanko terlebih dahulu!", Snackbar.LENGTH_LONG).show();
                    }else{
                        progressBar.setVisibility(View.VISIBLE);

                        Call<ApiStatus> call = APIClient.addNewUnitCat(PetugasMainActivity.UID, id, name);
                        call.enqueue(new Callback<ApiStatus>() {
                            @Override
                            public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                                if (response.isSuccessful()) {
                                    dialog.dismiss();
                                    toastInfo(mContext, "Kategori Blanko baru berhasil diubah");
                                    fetchBlanko();
                                    blankoAdapter.notifyDataSetChanged();
                                } else {
                                    toastErr(mContext, ApiError.parseError(response).getMessage());
                                }

                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(Call<ApiStatus> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                toastErr(mContext, t.getLocalizedMessage());
                            }
                        });
                    }
                });
                dialog.setNormalButton("Batal", null);
                dialog.show();
            }

            private void showEditDMKVDialog(int id, String ver, String nm){
                LayoutInflater inflater = getLayoutInflater();
                View container = inflater.inflate(R.layout.layout_new_dmk_v, null);
                final EditText etCode = container.findViewById(R.id.version_code);
                final EditText etName = container.findViewById(R.id.version_name);

                etCode.setText(ver);
                etName.setText(nm);

                ProgressBar progressBar = container.findViewById(R.id.progress);

                DefaultDialog dialog = new DefaultDialog(mContext);
                dialog.setView(container);
                dialog.setTitle("Ubah Versi DMK");
                dialog.setIcon(mContext.getDrawable(R.drawable.ic_add));
                dialog.setPositiveButton("UBAH", view2 -> {
                    progressBar.setVisibility(View.VISIBLE);
                    String version = etCode.getText().toString();
                    String name = etName.getText().toString();
                    Call<VersionsBlanko> call = APIClient.addDmkVersion(PetugasMainActivity.UID, false, name, version);
                    call.enqueue(new Callback<VersionsBlanko>() {
                        @Override
                        public void onResponse(Call<VersionsBlanko> call, Response<VersionsBlanko> response) {
                            if(response.isSuccessful()){
                                dialog.dismiss();
                                toastInfo(mContext, "Versi DMK Berhasil diubah");
                                vLists.add(response.body());
                                fetchBlanko();
                                blankoAdapter.notifyDataSetChanged();
                            }else{
                                progressBar.setVisibility(View.GONE);
                                toastErr(mContext, ApiError.parseError(response).getMessage());
                            }
                        }

                        @Override
                        public void onFailure(Call<VersionsBlanko> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            toastErr(mContext, t.getLocalizedMessage());
                        }
                    });
                });
                dialog.setNormalButton("Batal", null);
                dialog.show();
            }

            private void showMoreOptions(int type, String... params){
                String [] options = {"Ubah", "Hapus"};
                new AlertDialog.Builder(mContext)
                        .setTitle("Opsi Lainnya ...")
                        .setItems(options, ((dialog, which) -> {
                            switch (which){
                                case 0:{
                                    if(type==1){
                                        int id = Integer.parseInt(params[0]);
                                        String name = params[1];

                                        showEditDMKCDialog(id, name);
                                    }else{
                                        int id = Integer.parseInt(params[0]);
                                        String version = params[1];
                                        String name = params[2];

                                        showEditDMKVDialog(id, version, name);
                                    }

                                    break;
                                }

                                case 1:{
                                    if(type==1){
                                        int id = Integer.parseInt(params[0]);
                                        String name = params[1];

                                        showDelConfirmDlg(id, null, name, 1);
                                    }else{
                                        int id = Integer.parseInt(params[0]);
                                        String version = params[1];
                                        String name = params[2];

                                        showDelConfirmDlg(id, version, name, 2);
                                    }

                                    break;
                                }
                            }
                        } ))
                        .create()
                        .show();
            }

            private void showDelConfirmDlg(int id, String ver, String name, int type){
                String title;

                if(type==1){
                    title = "Kategori Blanko";
                }else{
                    title = "Versi DMK";
                }

                DefaultDialog dialog = new DefaultDialog(mContext);
                dialog.setTitle("Hapus "+title);
                dialog.setIcon(mContext.getDrawable(R.drawable.ic_delete));
                dialog.setMessage("Apakah anda yakin akan menghapus " + title + ": " + name + " ?");
                dialog.setPositiveButton("Batal", null);
                dialog.setNegativeButton("Hapus", view2 -> {

                });
                dialog.show();
            }

            @Override
            protected void onItemLongClick(int mode, String... params){
                if(mode==1){
                    showMoreOptions(mode, params[0], params[1]);
                }else{
                    showMoreOptions(mode, params[0], params[1], params[2]);
                }
            }

            @Override
            protected void onBlankoClick(UnitsBlanko model) {
                if(!isBlankoDetail) {
                    isBlankoDetail = true;
                    isDmkDetail = false;
                    if (blankoDetailFragment == null) {
                        blankoDetailFragment = BlankoDetailFragment.newInstance(model.getId());
//                        Toast.makeText(mContext, "ID: "+model.getId(), Toast.LENGTH_SHORT).show();
                    }
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.detail_container, blankoDetailFragment);
                    transaction.commit();
                    listEmpty.setVisibility(View.GONE);
                }else{
                    if (blankoDetailFragment != null) {
                        blankoDetailFragment.fetchDetailBlanko(model.getId());
                        listEmpty.setVisibility(View.GONE);
                    }else{
                        blankoDetailFragment = BlankoDetailFragment.newInstance(model.getId());
                    }
                }
            }

            @Override
            protected void onDmkVersionClick(VersionsBlanko model) {
                if(!isDmkDetail){
                    if(dmkDetailFragment == null){
                        dmkDetailFragment = DMKDetailFragment.newInstance(model.getId());
                    }
                    isBlankoDetail = false;
                    isDmkDetail = true;
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.detail_container, dmkDetailFragment);
                    transaction.commit();
                    listEmpty.setVisibility(View.GONE);
                }else {
                    if(dmkDetailFragment != null) {
                        dmkDetailFragment.fetchDMKs(model.getId());
                        listEmpty.setVisibility(View.GONE);
                    }else{
                        dmkDetailFragment = DMKDetailFragment.newInstance(model.getId());
                        Toast.makeText(mContext, "Fragment Detail DMK is Null", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        recyclerView.setAdapter(blankoAdapter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootContainer = Objects.requireNonNull(container).getRootView();
        return inflater.inflate(R.layout.fragment_blanko_v2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listEmpty = view.findViewById(R.id.list_empty);
    }

    private void fetchBlanko(){
        uLists.clear();
        vLists.clear();

        Call<UnitVersionResponse> call = APIClient.getUnitVersion(Integer.parseInt(PetugasMainActivity.UID));

        Baseprogress.showProgressDialog(mContext, "Mengambil daftar...");
        call.enqueue(new Callback<UnitVersionResponse>() {
            @Override
            public void onResponse(@NonNull Call<UnitVersionResponse> call, @NonNull Response<UnitVersionResponse> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    if(response.body().getUnits().size() == 0 && response.body().getVersions().size() == 0) {
                        toastInfo(mContext, "Daftar blanko kosong");
                    }else{
                        uLists = Objects.requireNonNull(response.body()).getUnits();
                        vLists = Objects.requireNonNull(response.body()).getVersions();

//                        ArrayList<String> uListName = new ArrayList<>();
//                        ArrayList<String> vListName = new ArrayList<>();
//
//                        for(UnitsBlanko obj : uLists){
//                            uListName.add(obj.getName());
//                        }
//
//                        for(VersionsBlanko obj : vLists){
//                            vListName.add(obj.getName());
//                        }
//
//                        log.x("U-List Size: "+uLists.size());
//                        log.x("U-List + Header Size: "+(uLists.size()+1));
//                        log.x("V-List Size: "+vLists.size());
//                        log.x("V-List + Header Size: "+(vLists.size()+1));
//                        log.x("==========================================");
//                        log.x("All Items Size: "+(uLists.size()+vLists.size()+2));
//                        log.x("================== [ U-List Array ] =================");
//                        log.printr(uListName);
//                        log.x("================== [ V-List Array ] =================");
//                        log.printr(vListName);

                        initBlankoView();
                    }
                }else{
                    toastErr(mContext, "[isNotSuccessful] " + response.message());
                    Baselog.d("[isNotSuccessful] " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UnitVersionResponse> call, @NonNull Throwable t) {
                Baseprogress.hideProgressDialog();
                toastErr(mContext, "[onFailed] " + t.getLocalizedMessage());
                Baselog.d("[onFailed] " + t.getLocalizedMessage());
            }
        });
    }

    public static void showParentToast(Context context, String msg, boolean isLong){
        int len;

        if(isLong){
            len = Toast.LENGTH_LONG;
        }else{
            len = Toast.LENGTH_SHORT;
        }

        Toast.makeText(context, msg, len).show();
    }

    private static class getUnitCatList extends AsyncTask<String, String, List<UnitCategory>> {
        private Context ctx;

        public interface OnCompleteListener{
            void onComplete(List<UnitCategory> unitCat);
        }

        OnCompleteListener mListener;

        getUnitCatList(Context context){
            this.ctx = context;
        }

        void setOnCompleteListener(OnCompleteListener listener){
            mListener = listener;
        }

        @Override
        protected void onCancelled(List<UnitCategory> s) {
            super.onCancelled(s);

            Log.e("[X-DEBUG]", "request UnitCategory lists canceled");
        }

        @Override
        protected void onPreExecute(){
            Log.e("[X-DEBUG]", "request UnitCategory lists onPreExe");
        }

        @Override
        protected List<UnitCategory> doInBackground(String... strings) {
            if(SAPIClient == null){
                SAPIClient = APIUtils.getAPIService(ctx);
            }

            Log.e("[X-DEBUG]", "request UnitCategory lists onDoInBg");
            List<UnitCategory> uncat;
            try {
                Call<List<UnitCategory>> callDMK = SAPIClient.getEhosUnitCategories(PetugasMainActivity.UID);
                Log.d(TAG, "doInBackground: " + callDMK.request().toString());
                Response<List<UnitCategory>> response = callDMK.execute();

                if(response.isSuccessful()){
                    uncat = response.body();
                }else{
                    cancel(true);
                    Log.e("[X-DEBUG]", "request UnitCategory lists isn't successful");
                    return null;
                }
            }catch (IOException e){
                cancel(true);
                Log.e("[X-DEBUG]", "Response JSON error saat meminta data UnitCategory");
                return null;
            }

            return uncat;
        }

        @Override
        protected void onPostExecute(List<UnitCategory> s) {
            super.onPostExecute(s);
            mListener.onComplete(s);
            Log.e("[X-DEBUG]", "request UnitCategory lists onPostExe");
        }
    }
}
