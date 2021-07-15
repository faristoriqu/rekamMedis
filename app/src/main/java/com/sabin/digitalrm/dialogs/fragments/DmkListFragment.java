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
import com.sabin.digitalrm.adapters.DmkBlankoVisitListAdapter;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.DMKBlanko;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.ApiError;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sabin.digitalrm.fragments.BaseFragment.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class DmkListFragment extends Fragment {
    public static final String EXTRA_ID_BLANKO = "EX1";
    DmkBlankoVisitListAdapter dmkVisitListAdapter;

    APIService APIClient;
    Context mContext;

    private Integer idBlanko;

    List<DMKBlanko> dmkList;
    List<Integer> unDMKlistID;

    public DmkListFragment() {
        // Required empty public constructor
    }

    public static DmkListFragment newInstance(int idBlanko){
        DmkListFragment fragment = new DmkListFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_ID_BLANKO, idBlanko);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        APIClient = ApiServiceGenerator.createService(requireActivity().getApplicationContext(), APIService.class);
        if(getArguments() != null){
            idBlanko = getArguments().getInt(EXTRA_ID_BLANKO);
        }
        dmkList = new ArrayList<>();
        unDMKlistID = new ArrayList<>();
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
        dmkVisitListAdapter = new DmkBlankoVisitListAdapter(mContext, R.layout.listview_blanko_visit, dmkList){
            @Override
            public void onDmkCheck(boolean checked, DMKBlanko dmk) {
                if(checked){
                    unDMKlistID.remove(dmk.getId());
                    Log.d(TAG, "onDmkCheck: remove " + dmk.getId());
                }else{
                    Log.d(TAG, "onDmkCheck: add " + dmk.getId());
                    unDMKlistID.add(dmk.getId());
                }
            }
        };
        listView.setAdapter(dmkVisitListAdapter);
        fetchDMK();
    }

    public List<Integer> getUnDMKlistID() {
        return unDMKlistID;
    }

    public void setIdBlanko(int id){
        idBlanko = id;
    }

    private void fetchDMK(){
        Call<List<DMKBlanko>> call = APIClient.getDMKinBlanko(idBlanko, PetugasMainActivity.UID, null);
        call.enqueue(new Callback<List<DMKBlanko>>() {
            @Override
            public void onResponse(Call<List<DMKBlanko>> call, Response<List<DMKBlanko>> response) {
                if (response.isSuccessful()){
                    dmkList.clear();
                    dmkList.addAll(response.body());
                    dmkVisitListAdapter.notifyDataSetChanged();
                }else {
                    Log.d(TAG, "onResponse: " + ApiError.parseError(response).getMessage());
                }
            }

            @Override
            public void onFailure(Call<List<DMKBlanko>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

}
