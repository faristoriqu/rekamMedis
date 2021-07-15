package com.sabin.digitalrm.dialogs.fragments;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.DmkBerkasExportListAdapter;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.DMKBerkas;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.StringUtilities;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sabin.digitalrm.fragments.BaseFragment.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class DmkInDMRListFragment extends Fragment {
    DmkBerkasExportListAdapter dmkVisitListAdapter;

    APIService APIClient;
    Context mContext;

    private Integer idDMR;

    List<DMKBerkas> dmkList;
    List<String> inDmkList;
    List<String> codeLIst;

    public DmkInDMRListFragment() {
        // Required empty public constructor
    }

    public static DmkInDMRListFragment newInstance(int idDMR){
        DmkInDMRListFragment fragment = new DmkInDMRListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("EX1", idDMR);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        APIClient = ApiServiceGenerator.createService(requireActivity().getApplicationContext(), APIService.class);
        dmkList = new ArrayList<>();
        inDmkList = new ArrayList<>();
        codeLIst = new ArrayList<>();
        if(getArguments() != null){
            idDMR = getArguments().getInt("EX1");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dmk_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListView(view);
    }

    private void initListView(View view){
        ListView listView = view.findViewById(R.id.list_view_item);

        dmkVisitListAdapter = new DmkBerkasExportListAdapter(mContext, R.layout.listview_blanko_visit, dmkList){
            @Override
            public void onDmkCheck(boolean checked, DMKBerkas dmk) {
                String pages = dmk.getPage() + "-" + (dmk.getPage() + dmk.getTotalPage() - 1);
                String code = dmk.getCode();
                if(!checked){
                    inDmkList.remove(pages);
                    codeLIst.remove(code);
                    Log.d(TAG, "onDmkCheck: remove " + dmk.getId());
                }else{
                    Log.d(TAG, "onDmkCheck: add " + dmk.getId());
                    inDmkList.add(pages);
                    codeLIst.add(code);
                }
            }
        };
        listView.setAdapter(dmkVisitListAdapter);
        fetchDMK();
    }

    public List<String> getInDmkList() {
        return inDmkList;
    }

    public String getCode(){
        return StringUtilities.join(", ", codeLIst);
    }

    private void fetchDMK(){
        Call<List<DMKBerkas>> call = APIClient.getDMKsInDMR(PetugasMainActivity.UID, idDMR);
        call.enqueue(new Callback<List<DMKBerkas>>() {
            @Override
            public void onResponse(Call<List<DMKBerkas>> call, Response<List<DMKBerkas>> response) {
                if (response.isSuccessful()){
                    dmkList.clear();
                    dmkList.addAll(response.body());
                    dmkVisitListAdapter.notifyDataSetChanged();
                }else {
                    Log.d(TAG, "onResponse: " + ApiError.parseError(response).getMessage());
                }
            }

            @Override
            public void onFailure(Call<List<DMKBerkas>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

}
