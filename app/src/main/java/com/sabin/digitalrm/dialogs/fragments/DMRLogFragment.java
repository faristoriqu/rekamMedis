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
import android.widget.Toast;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.DMRLogListAdapter;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.DMRLog;
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
public class DMRLogFragment extends BaseFragment{
    public static final String ARG_IDCAT = "ARG1";
    public static final String ARG_SUBTITLE = "ARG2";

    private final String TAG = "X-LOG";

    private Context mContext;
    private APIService APIClient;
    private boolean isCreated;

    private List<DMRLog> dmrLogList;
    private DMRLogListAdapter dmrLogListAdapter;

    private int id;

    public interface OnListClickListener {
        void onListClick(DMRLog dmrLog);
    }

    private OnListClickListener mListener;

    public void setOnListClickListener(OnListClickListener listener){
        mListener = listener;
    }

    public DMRLogFragment() {
        // Required empty public constructor
    }

    public static DMRLogFragment newInstance(int id){
        Bundle args = new Bundle();

        args.putInt(ARG_IDCAT, id);
        DMRLogFragment fragment = new DMRLogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            id = getArguments().getInt(ARG_IDCAT);
        }

        mContext = getContext();
        APIClient = ApiServiceGenerator.createService(requireActivity().getApplicationContext(), APIService.class);
        dmrLogList = new ArrayList<>();
    }
        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dmr_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListView(view);
        fetchDMRLog();
    }

    private void initListView(View view){
        ListView listView = view.findViewById(R.id.list_view_item);
        dmrLogListAdapter = new DMRLogListAdapter(mContext, R.layout.listview_dmr_log, dmrLogList);
        listView.setAdapter(dmrLogListAdapter);
        listView.setOnItemClickListener((adapterView, view1, i, l) -> onBlankoClick(dmrLogList.get(i)));
    }

    private void onBlankoClick(DMRLog dmrLog){
        if(mListener != null){
            mListener.onListClick(dmrLog);
        }
    }

    private void fetchDMRLog(){
        Call<List<DMRLog>> call = APIClient.getLogsInDMR(PetugasMainActivity.UID, id);
        Log.d(TAG, "fetchDMRLog: " + call.request());
        call.enqueue(new Callback<List<DMRLog>>() {
            @Override
            public void onResponse(Call<List<DMRLog>> call, Response<List<DMRLog>> response) {
                if(response.isSuccessful()){
                    if(response.body().size() != 0) {
                        dmrLogList.clear();
                        dmrLogList.addAll(response.body());
                        dmrLogListAdapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(mContext, "History Kosong", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    String msg = ApiError.parseError(response).getMessage();
                    Log.d(TAG, "onResponse: " + msg);
                    toastErr(mContext, msg);
                }
            }

            @Override
            public void onFailure(Call<List<DMRLog>> call, Throwable t) {
                toastErr(mContext, t.getLocalizedMessage());
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }
}
