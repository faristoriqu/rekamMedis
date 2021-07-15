package com.sabin.digitalrm.fragments.prm.pasien;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.VisitorPagerAdapter;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.VisitUnit;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.ApiError;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class DaftarPasienFragment extends BaseFragment implements PasienPoliFragment.OnListEmpetyListener{
    private VisitorPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private Context context;
    private FrameLayout layoutEmpty;

    private ProgressBar progressBar;
    private TabLayout tabLayout;

    private APIService APIClient;

    public DaftarPasienFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        context = getContext();

        initRetrofit();
        Log.d(TAG, "onCreate: DPF");
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if(childFragment instanceof PasienPoliFragment){
            PasienPoliFragment pasienPoliFragment = (PasienPoliFragment) childFragment;
            pasienPoliFragment.setOnListEmpetyListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View resFragment = inflater.inflate(R.layout.fragment_daftar_pasien, container, false);

        // Set up the ViewPager with the sections adapter.
        mPagerAdapter = new VisitorPagerAdapter(getChildFragmentManager());
        layoutEmpty = resFragment.findViewById(R.id.kunjungan_empty);
        mPagerAdapter.notifyDataSetChanged();
        mViewPager = resFragment.findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        tabLayout = resFragment.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        progressBar = resFragment.findViewById(R.id.progress);

        return resFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        fetchVisitorTypeList();
        syncVisit();
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
    public void onListEmpety(int categoryID) {
        Log.d(TAG, "onListEmpety: TAB " + categoryID);
//        mPagerAdapter.removeFragment(categoryID);
//        getFragmentManager().beginTransaction().detach(mPagerAdapter.getItem(categoryID));
//        mPagerAdapter.notifyDataSetChanged();
    }

    private void initRetrofit(){
        BaseActivity.Baselog.d("Initretrofit");
        APIClient = ApiServiceGenerator.createService(requireActivity().getApplicationContext(), APIService.class);
    }

    private void syncVisit(){
        Call<ApiStatus> call = APIClient.syncPatientVisits(PetugasMainActivity.UID);
        call.enqueue(new Callback<ApiStatus>() {
            @Override
            public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
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
            public void onFailure(Call<ApiStatus> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    private void fetchVisitUnits(){
        Call<List<VisitUnit>> call = APIClient.getVisitUnits(PetugasMainActivity.UID);
        call.enqueue(new Callback<List<VisitUnit>>() {
            @Override
            public void onResponse(Call<List<VisitUnit>> call, Response<List<VisitUnit>> response) {
                if(response.isSuccessful()){
                    if(response.body().size() > 0){
                        Log.d(TAG, "onResponse: " + response.body().get(0).getUnitName());
                        updateTabFIlter(response.body());
                        progressBar.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.VISIBLE);
                    }else {
                        Log.d(TAG, "onResponse: no visit unit");
                        progressBar.setVisibility(View.GONE);
                    }
                }else{
                    Log.d(TAG, "onResponse: " + ApiError.parseError(response).getMessage());
                }
            }

            @Override
            public void onFailure(Call<List<VisitUnit>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

//    private void fetchVisitorTypeList(){
//        Log.d(TAG, "fetchVisitorTypeList: ");
//        Call<SyncVisitResponse> listVisitorTypeResponseCall = APIClient.syncVisitnList(PetugasMainActivity.UID);
//        progressBar.setVisibility(View.VISIBLE);
//
//        listVisitorTypeResponseCall.enqueue(new Callback<SyncVisitResponse>() {
//            @Override
//            public void onResponse(Call<SyncVisitResponse> call, Response<SyncVisitResponse> response) {
//                progressBar.setVisibility(View.GONE);
//                if(response.isSuccessful()){
//                    if(response.body().getStatuscode() == 200){
//                        SyncVisitResult syncRes = response.body().getVisitResult();
//                        BaseFragment.toastInfo(context, syncRes.getVisitCount() + " visit ditambahkan, " + syncRes.getPoliCount() + " poli baru diaktifkan");
//
//                        if(syncRes.getVisitorTypeList() != null && syncRes.getVisitorTypeList().size() > 0) {
//                            updateEnv(syncRes.getVisitorTypeList());
//                            layoutEmpty.setVisibility(View.GONE);
//                            tabLayout.setVisibility(View.VISIBLE);
//                        }else{
//                            tabLayout.setVisibility(View.GONE);
//                            layoutEmpty.setVisibility(View.VISIBLE);
//                            toastInfo(context, "Tidak ada kunjungan!");
//                        }
//                        Log.d(TAG, "onResponse: SUC");
//                    }else {
//                        toastInfo(context, response.body().getMessage());
//                        Log.d(TAG, "onResponse: not 200 " + response.body().getMessage());
//                    }
//                }else{
//                    toastInfo(context, response.message());
//                    Log.d(TAG, "onResponse: FAIL" );
//                }
//            }
//
//            @Override
//            public void onFailure(Call<SyncVisitResponse> call, Throwable t) {
//                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
//                toastInfo(context, t.getLocalizedMessage());
//                progressBar.setVisibility(View.GONE);
//            }
//        });
//    }

    private void updateTabFIlter(List<VisitUnit> visitUnitList){
        PasienPoliFragment fragment;
        for(VisitUnit item : visitUnitList){
            fragment = PasienPoliFragment.newInstance(item.getIdUnit(), false);
            fragment.setOnListEmpetyListener(this);
            mPagerAdapter.addFragment(fragment, item.getUnitName());
        }

        if(visitUnitList.size() > 0){
            fragment = PasienPoliFragment.newInstance(0, true);
            fragment.setOnListEmpetyListener(this);
            mPagerAdapter.addFragment(fragment, "Semua Poli");
        }

        mPagerAdapter.notifyDataSetChanged();
    }

    /*
    private void updateEnv(List<VisitorType> _visitorTypeList){
        PasienPoliFragment fragment;
        //visitorTypeList.clear();
        //visitorTypeList.addAll(_visitorTypeList);
        //visitorFragmentList.clear();

        int tabIndex = 0;
        for(VisitorType item : _visitorTypeList){
            int idPoli = item.getIdPoli();
            Log.d(TAG, "updateEnv: " + item.getIdPoli());
            if(idPoli >= VisitorType.KUNJUNGAN_OFFSET){
                fragment = PasienPoliFragment.newInstance("kunjungan", idPoli - VisitorType.KUNJUNGAN_OFFSET, tabIndex);
                fragment.setOnListEmpetyListener(this);
                mPagerAdapter.addFragment(fragment, item.getNamaPoli());
            }else{
                fragment = PasienPoliFragment.newInstance("poli", idPoli, tabIndex);
                fragment.setOnListEmpetyListener(this);
                mPagerAdapter.addFragment(fragment, item.getNamaPoli());
            }

            tabIndex++;
        }
        fragment = PasienPoliFragment.newInstance("all", 0, tabIndex);
        fragment.setOnListEmpetyListener(this);
        mPagerAdapter.addFragment(fragment, "Semua");

        mPagerAdapter.notifyDataSetChanged();
    }
    */

}
