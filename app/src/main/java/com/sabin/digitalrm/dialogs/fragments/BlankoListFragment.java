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
import com.sabin.digitalrm.adapters.BlankoVisitListAdapter;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.DetailBlanko;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.ApiError;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankoListFragment extends Fragment{
    public static final String ARG_IDCAT = "ARG1";

    private final String TAG = "X-LOG";

    private Context mContext;
    private APIService APIClient;
    private boolean isCreated;

    private List<DetailBlanko> blankoList;
    private BlankoVisitListAdapter blankoVisitListAdapter;

    private int idCat;

    public interface OnBlankoVisitClickListener {
        void onBlankoClick(DetailBlanko blanko);
    }

    private OnBlankoVisitClickListener mListener;

    public void setOnBlankoVisitClickListener(OnBlankoVisitClickListener listener){
        mListener = listener;
    }

    public BlankoListFragment() {
        // Required empty public constructor
    }

    public static BlankoListFragment newInstance(int idCat){
        Bundle args = new Bundle();

        args.putInt(ARG_IDCAT, idCat);
        BlankoListFragment fragment = new BlankoListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            idCat = getArguments().getInt(ARG_IDCAT);
        }

        mContext = getContext();
        APIClient = ApiServiceGenerator.createService(requireActivity().getApplicationContext(), APIService.class);
        blankoList = new ArrayList<>();
    }
        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blanko_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListView(view);
        fetchBlanko();
    }

    private void initListView(View view){
        ListView listView = view.findViewById(R.id.list_view_item);
        blankoVisitListAdapter = new BlankoVisitListAdapter(mContext, R.layout.listview_blanko_visit, blankoList);
        listView.setAdapter(blankoVisitListAdapter);
        listView.setOnItemClickListener((adapterView, view1, i, l) -> onBlankoClick(blankoList.get(i)));
    }

    private void onBlankoClick(DetailBlanko blanko){
        if(mListener != null){
            mListener.onBlankoClick(blanko);
        }
    }

    private void fetchBlanko(){
        Call<List<DetailBlanko>> call = APIClient.getBlankoDetailV2(idCat, PetugasMainActivity.UID);
        call.enqueue(new Callback<List<DetailBlanko>>() {
            @Override
            public void onResponse(Call<List<DetailBlanko>> call, Response<List<DetailBlanko>> response) {
                if(response.isSuccessful()){
                    blankoList.clear();
                    blankoList.addAll(response.body());
                    blankoVisitListAdapter.notifyDataSetChanged();
                }else {
                    Log.d(TAG, "onResponse: " + ApiError.parseError(response).getMessage());
                }
            }

            @Override
            public void onFailure(Call<List<DetailBlanko>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }
}
