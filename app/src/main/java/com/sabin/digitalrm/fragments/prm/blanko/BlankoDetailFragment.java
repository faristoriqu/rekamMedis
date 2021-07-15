package com.sabin.digitalrm.fragments.prm.blanko;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.BlankoDetailCollapsedViewHolder;
import com.sabin.digitalrm.adapters.BlankoDetailExpandedViewHolder;
import com.sabin.digitalrm.adapters.BlankoDetailViewHolder;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.helpers.DefaultDialog;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.DMK;
import com.sabin.digitalrm.models.DMKBlanko;
import com.sabin.digitalrm.models.DetailBlanko;
import com.sabin.digitalrm.models.DetailBlankoResponse;
import com.sabin.digitalrm.models.VersionsBlanko;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;
import com.sysdata.widget.accordion.ExpandableItemHolder;
import com.sysdata.widget.accordion.FancyAccordionView;
import com.sysdata.widget.accordion.Item;
import com.sysdata.widget.accordion.ItemAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class BlankoDetailFragment extends BaseFragment {
    private static final String KEY_EXPANDED_ID = "expandedId";
    private Context mContext;
    private static Context sContext;
    private static APIService APIClientS;

    private BlankoDetailViewHolder blakoDetailAdapter;
    private List<DetailBlanko> blankoDetailList;

    private static Integer unitID = 0;

    private TextView tvBlanko;

    private static FancyAccordionView mRecyclerView;
    private static ImageView btnAddBlanko;
    private TextView txtHeader;
    private APIService APIClient;
    private static APIService APIClient2;
    private List<VersionsBlanko> vList;
    private static View rootView;

    private static Spinner spDMK, spVersion;
    private static List<Integer> dId;

    private ItemAdapter.OnItemClickedListener mListener = (viewHolder, id) -> {
        ItemAdapter.ItemHolder itemHolder = viewHolder.getItemHolder();
        DetailBlanko item = ((DetailBlanko) itemHolder.item);

        switch (id) {
            case ItemAdapter.OnItemClickedListener.ACTION_ID_COLLAPSED_VIEW:
                break;
            case ItemAdapter.OnItemClickedListener.ACTION_ID_EXPANDED_VIEW:
                break;
            default:
                // do nothing
                break;
        }
    };

    public BlankoDetailFragment() {}

    public static BlankoDetailFragment newInstance(Integer unitID) {
        BlankoDetailFragment fragment = new BlankoDetailFragment();
        Bundle args = new Bundle();
        args.putInt("UNIT_CAT_ID", unitID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            unitID = getArguments().getInt("UNIT_CAT_ID");
            Log.e("[X_DEBUG]", "unitID not Null. Val: "+unitID);
        }else{
            Log.e("[X_DEBUG]", "unitID is Null");
        }

        mContext = getContext();
        sContext = mContext;
        APIClient = APIUtils.getAPIService(mContext);
        APIClientS = APIUtils.getAPIService(mContext);
        vList = BlankoV3Fragment.vLists;
    }

    private void initAccordionView(View v, Bundle state){
        mRecyclerView = v.findViewById(R.id.fancy_accordion_view);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        // bind the factory to create view holder for item collapsed
        mRecyclerView.setCollapsedViewHolderFactory(BlankoDetailCollapsedViewHolder.Factory.create(getActivity(), R.layout.blanko_detail_layout_collapsed), mListener);

        // bind the factory to create view holder for item expanded
        mRecyclerView.setExpandedViewHolderFactory(BlankoDetailExpandedViewHolder.Factory.create(getActivity(), R.layout.blanko_detail_layout_expanded, vList), mListener);

        // restore the expanded item from state
        if (state != null) {
            mRecyclerView.setExpandedItemId(state.getLong(KEY_EXPANDED_ID, Item.INVALID_ID));
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void deleteBlankoDialog(Context context, int blankoId, String blankoName){
        new AlertDialog.Builder(context)
                .setTitle("Konfirmasi Hapus")
                .setIcon(context.getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                .setMessage("Anda yakin ingin menghapus blanko \""+blankoName+"\"?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    APIService APIClient = APIUtils.getAPIService(context);

                    Call<ApiStatus> call = APIClient.deleteBlanko(PetugasMainActivity.UID, blankoId);

                    Baseprogress.showProgressDialog(context, "Sedang menghapus ...");
                    call.enqueue(new Callback<ApiStatus>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                            Baseprogress.hideProgressDialog();

                            if(response.isSuccessful()){
                                BlankoV2Fragment.showParentToast(context, "Blanko berhasil dihapus", true);
                                fetchDetailBlanko(unitID);
                            }else{
                                BlankoV2Fragment.showParentToast(context, "Exception: " + response.message(), true);
                                Baselog.d("[isNotSuccessfull] " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                            Baseprogress.hideProgressDialog();
                            BlankoV2Fragment.showParentToast(context, "Failure: " + t.getLocalizedMessage(), true);
                            Baselog.d("[onFailed] " + t.getLocalizedMessage());
                        }
                    });
                })
                .setNegativeButton("Tidak", null)
                .create().show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void editBlankoDialog(Activity context, int blankoId, String blankoName){
        LayoutInflater inflater = context.getLayoutInflater();
        View container = inflater.inflate(R.layout.dialog_add_blanko, null);
        final EditText etName = container.findViewById(R.id.etName);
        FrameLayout progressBar = container.findViewById(R.id.content_loading);

        etName.setText(blankoName);

        DefaultDialog dialog = new DefaultDialog(context);
        dialog.setView(container);
        dialog.setTitle("Ubah Blanko \""+blankoName+"\"");
        dialog.setIcon(context.getDrawable(R.drawable.ic_add));
        dialog.setPositiveButton("UBAH", view2 -> {
            String name = etName.getText().toString();

            if(name.equals("")){
                Snackbar.make(view2, "Isi nama Blanko terlebih dahulu!", Snackbar.LENGTH_LONG).show();
            }else{
                progressBar.setVisibility(View.VISIBLE);

                Call<ApiStatus> call = APIClient2.editBlanko(PetugasMainActivity.UID, blankoId, name);
                call.enqueue(new Callback<ApiStatus>() {
                    @Override
                    public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            toastInfo(context, "Blanko berhasil diubah");
                            fetchDetailBlanko(unitID);
                        } else {
                            toastErr(context, ApiError.parseError(response).getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiStatus> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        toastErr(context, t.getLocalizedMessage());
                    }
                });
            }
        });
        dialog.setNormalButton("Batal", null);
        dialog.show();
    }

    public static void fetchDetailBlanko(Integer id){
        if(APIClient2 == null){
            APIClient2 = APIUtils.getAPIService(sContext);
        }

        Log.e("[X-DEBUG]","id: "+id+" ; uid: "+PetugasMainActivity.UID);
        Call<List<DetailBlanko>> call = APIClient2.getBlankoDetail(id , PetugasMainActivity.UID);

        Baseprogress.showProgressDialog(sContext, "Mengambil data...");
        call.enqueue(new Callback<List<DetailBlanko>>() {
            @Override
            public void onResponse(@NonNull Call<List<DetailBlanko>> call, @NonNull Response<List<DetailBlanko>> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    loadData(Objects.requireNonNull(response.body()));
                }else{
                    toastErr(sContext, "Exception: " + response.message());
                    Baselog.d("[isNotSuccessfull] " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DetailBlanko>> call, @NonNull Throwable t) {
                Baseprogress.hideProgressDialog();
                toastErr(sContext, "Failure: " + t.getLocalizedMessage());
                Baselog.d("[onFailed] " + t.getLocalizedMessage());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_blanko_detail, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvBlanko = view.findViewById(R.id.dmks);
        btnAddBlanko = view.findViewById(R.id.btnAddBlanko);
        txtHeader = view.findViewById(R.id.txtHeader);
        txtHeader.setText("Daftar Blanko");
        initAccordionView(view, savedInstanceState);
        fetchDetailBlanko(unitID);
    }

    private static void loadData(List<DetailBlanko> model) {
        final int dataCount = model.size();
        int index = 0;

        final List<ExpandableItemHolder> itemHolders = new ArrayList<>(dataCount);
        Item itemModel;
        ExpandableItemHolder itemHolder;
        for (; index < dataCount; index++) {
            List<DMKBlanko> lstDMK = model.get(index).getDmks();
            itemModel = DetailBlanko.create(model.get(index).getId(), model.get(index).getName(), lstDMK);
            itemHolder = new ExpandableItemHolder(itemModel);
            itemHolders.add(itemHolder);

            if(lstDMK==null){
                Log.e("[X-DEBUG]", "dmks is null");
            }else{
                Log.e("[X-DEBUG]", "dmks not null. Size:"+lstDMK.size());
            }
        }

        mRecyclerView.setAdapterItems(itemHolders);
        btnAddBlanko.setOnClickListener(view -> dlgAddBlanko(sContext));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static void dlgAddBlanko(Context mContext){
        Activity ctx = (Activity) mContext;
        LayoutInflater inflater = ctx.getLayoutInflater();
        View container = inflater.inflate(R.layout.dialog_add_blanko, null);
        final EditText etName = container.findViewById(R.id.etName);
        FrameLayout progressBar = container.findViewById(R.id.content_loading);

        DefaultDialog dialog = new DefaultDialog(mContext);
        dialog.setView(container);
        dialog.setTitle("Tambah Blanko Baru");
        dialog.setIcon(mContext.getDrawable(R.drawable.ic_add));
        dialog.setPositiveButton("TAMBAH", view2 -> {
            String name = etName.getText().toString();

            if(name.equals("")){
                Snackbar.make(view2, "Isi nama Blanko Baru terlebih dahulu!", Snackbar.LENGTH_LONG).show();
            }else{
                progressBar.setVisibility(View.VISIBLE);

                Call<ApiStatus> call = APIClient2.addNewBlanko(PetugasMainActivity.UID, unitID, name, 0, "N/A");
                call.enqueue(new Callback<ApiStatus>() {
                    @Override
                    public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            toastInfo(mContext, "Blanko baru berhasil ditambahkan");
                            fetchDetailBlanko(unitID);
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

    public static void dlgAddDMK(Context msContext, View vw, List<VersionsBlanko> lstVer, int blankoID){
        LayoutInflater inflater = (LayoutInflater) msContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        final View layout = Objects.requireNonNull(inflater).inflate(R.layout.add_dmk_dialog_v2, vw.findViewById(R.id.layout_root_dmk));
        AlertDialog.Builder builderSave = new AlertDialog.Builder(msContext);
        spVersion = layout.findViewById(R.id.spinner_version);
        spDMK = layout.findViewById(R.id.spinner_dmk);
        builderSave.setView(layout);

        List<Integer> vId = new ArrayList<>();
        List<String> ver = new ArrayList<>();

        for (int i = 0; i< Objects.requireNonNull(lstVer).size()+1; i++) {
            if(i==0){
                vId.add(i, 0);
                ver.add(i, "-- Pilih Versi DMK --");
            }else {
                int ii = i-1;
                vId.add(i, lstVer.get(ii).getId());
                ver.add(i, lstVer.get(ii).getName());
            }
        }

        spVersion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("[X-DEBUG]", "DMKVerID:"+vId.get(i));
                if(vId.get(i)!=0) {
                    getDMKList getDMKAsync = new getDMKList(msContext);
                    getDMKAsync.setOnCompleteListener(s -> {
                        dId = new ArrayList<>();
                        List<String> dmk = new ArrayList<>();

                        if(s!=null) {
                            spVersion.setEnabled(true);
                            spDMK.setEnabled(true);
                            for (int i1 = 0; i1 <= Objects.requireNonNull(s).size(); i1++) {
                                if (i1 == 0) {
                                    dId.add(i1, 0);
                                    dmk.add(i1, "-- Pilih DMK --");
                                } else {
                                    dId.add(i1, s.get(i1-1).getId());
                                    dmk.add(i1, "[DMK " + s.get(i1-1).getCode() + "] " + s.get(i1-1).getName());
                                }
                            }

                            ArrayAdapter<String> spAdapter2 = new ArrayAdapter<>(msContext, android.R.layout.simple_list_item_1, Objects.requireNonNull(dmk));
                            spDMK.setAdapter(spAdapter2);
                            spAdapter2.notifyDataSetChanged();
                        }else{
                            Log.e("[X-DEBUG]", "Response Result NULL");
                        }
                    });
                    getDMKAsync.execute(String.valueOf(vId.get(i)));
                    Log.e("[X-DEBUG]", "spVersion select on vId is not 0");
                }else{
                    spDMK.setEnabled(false);
                    spDMK.setAdapter(null);
                    Log.e("[X-DEBUG]", "spVersion select on vId is 0");
//                    spDMK.notify();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(msContext, android.R.layout.simple_list_item_1, Objects.requireNonNull(ver));
        spVersion.setAdapter(spAdapter);

        builderSave.setCancelable(false);
        builderSave.setPositiveButton("Simpan", (dialog, which) -> {
            int pos = spVersion.getSelectedItemPosition();
            int selectedId = vId.get(pos);

            if(selectedId==0){
                Snackbar.make(vw, "Pilih Versi DMK terlebih dahulu!", Snackbar.LENGTH_LONG).show();
            }else if(!spVersion.isEnabled()){
                Snackbar.make(vw, "Pilih Versi DMK terlebih dahulu!", Snackbar.LENGTH_LONG).show();
            }else if(!spDMK.isEnabled()) {
                Snackbar.make(vw, "Pilih DMK terlebih dahulu!", Snackbar.LENGTH_LONG).show();
            }else{
                int pos2 = spDMK.getSelectedItemPosition();
                int selectedId2 = dId.get(pos2);

                if(selectedId2==0){
                    Snackbar.make(vw, "Pilih DMK terlebih dahulu!", Snackbar.LENGTH_LONG).show();
                }else{
                    addDMKToServer(msContext, blankoID, selectedId2);
                }
            }
        });
        builderSave.setNegativeButton("Batal", null);

        AlertDialog dlgSave = builderSave.create();
        dlgSave.show();
    }

    private static class getDMKList extends AsyncTask<String, String, List<DMKBlanko>> {
        private Context ctx;

        public interface OnCompleteListener{
            void onComplete(List<DMKBlanko> idDMR);
        }

        OnCompleteListener mListener;

        getDMKList(Context context){
            this.ctx = context;
        }
        void setOnCompleteListener(OnCompleteListener listener){
            mListener = listener;
        }

        @Override
        protected void onCancelled(List<DMKBlanko> s) {
            super.onCancelled(s);

            Log.e("[X-DEBUG]", "request dmk lists canceled");
        }

        @Override
        protected void onPreExecute(){
            spVersion.setEnabled(false);
            spDMK.setEnabled(false);
            spDMK.setAdapter(null);
//            spDMK.notify();
            Log.e("[X-DEBUG]", "request dmk lists onPreExe");
        }

        @Override
        protected List<DMKBlanko> doInBackground(String... strings) {
            Log.e("[X-DEBUG]", "request dmk lists onDoInBg");
            List<DMKBlanko> iDMR;
                try {
                    publishProgress("Menambah data DMR pasien...");
                    Call<List<DMKBlanko>> callDMK = APIClientS.getDMKbyVersion(PetugasMainActivity.UID, Integer.valueOf(strings[0]));
                    Log.d(TAG, "doInBackground: " + callDMK.request().toString());
                    Response<List<DMKBlanko>> response = callDMK.execute();

                    if(response.isSuccessful()){
                        iDMR = response.body();
                    }else{
                        cancel(true);
                        Log.e("[X-DEBUG]", "request dmk lists isn't successful");
                        return null;
                    }
                }catch (IOException e){
                    cancel(true);
                    Log.e("[X-DEBUG]", "Response JSON error saat meminta data DMK");
                    return null;
                }

            return iDMR;
        }

        @Override
        protected void onPostExecute(List<DMKBlanko> s) {
            super.onPostExecute(s);
            mListener.onComplete(s);
            Log.e("[X-DEBUG]", "request dmk lists onPostExe");
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void deleteConfirm(Context ctx, int id, int blId, String name, String blName){
        new AlertDialog.Builder(ctx)
                .setTitle("Konfirmasi Hapus")
                .setIcon(ctx.getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                .setMessage("Anda yakin ingin menghapus data \""+name+"\" dari blanko \""+blName+"\"?")
                .setPositiveButton("Ya", (dialog, which) -> delDMKFromServer(ctx, id, blId))
                .setNegativeButton("Tidak", null)
                .create().show();
    }

    private static void addDMKToServer(Context mContext, int blankoID, int dmkID){
        APIService APIClient = APIUtils.getAPIService(mContext);

        Call<ApiStatus> call = APIClient.setDMKinBlanko(PetugasMainActivity.UID, blankoID, dmkID);

        Baseprogress.showProgressDialog(mContext, "Sedang menyimpan ...");
        call.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    BlankoV2Fragment.showParentToast(mContext, "Data berhasil ditambahkan", true);
                    fetchDetailBlanko(unitID);
                }else{
                    BlankoV2Fragment.showParentToast(mContext, "Exception: " + response.message(), true);
                    Baselog.d("[isNotSuccessfull] " + response.message());
                    Baselog.d(call.request().toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                Baseprogress.hideProgressDialog();
                BlankoV2Fragment.showParentToast(mContext, "Failure: " + t.getLocalizedMessage(), true);
                Baselog.d("[onFailed] " + t.getLocalizedMessage());
            }
        });
    }

    private static void delDMKFromServer(Context mContext, int id, int blId){
        APIService APIClient = APIUtils.getAPIService(mContext);

        Call<ApiStatus> call = APIClient.deleteBlankoDetail(PetugasMainActivity.UID, blId, id);

        Baseprogress.showProgressDialog(mContext, "Sedang menghapus ...");
        call.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(@NonNull Call<ApiStatus> call, @NonNull Response<ApiStatus> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    BlankoV2Fragment.showParentToast(mContext, "Data berhasil dihapus", true);
                    fetchDetailBlanko(unitID);
                }else{
                    BlankoV2Fragment.showParentToast(mContext, "Exception: " + response.message(), true);
                    Baselog.d("[isNotSuccessfull] " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiStatus> call, @NonNull Throwable t) {
                Baseprogress.hideProgressDialog();
                BlankoV2Fragment.showParentToast(mContext, "Failure: " + t.getLocalizedMessage(), true);
                Baselog.d("[onFailed] " + t.getLocalizedMessage());
            }
        });
    }

}
