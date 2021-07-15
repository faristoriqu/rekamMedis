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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.PreviewNoteAnalyticListAdapter;
import com.sabin.digitalrm.dialogs.DialogAddNoteAnalytic;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.AnalyticNote;
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
public class PreviewNoteAnalyticFragment extends Fragment {
    private final String TAG = "X-LOG";
    private Context mContext;
    private PreviewNoteAnalyticListAdapter noteAdapter;
    private List<AnalyticNote> analyticNoteList;
    private LinearLayout noteLayout;

    private int indexNote, idDMR;
    private APIService APIClient;

    Button btnPositive, btnNegative;
    EditText etNote;
    ProgressBar progressBar;

    public PreviewNoteAnalyticFragment() {
        // Required empty public constructor
    }

    public static PreviewNoteAnalyticFragment newInstance(int idDMR){
        PreviewNoteAnalyticFragment fragment = new PreviewNoteAnalyticFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogAddNoteAnalytic.ARG_ID_DMR, idDMR);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        if(getArguments() != null){
            idDMR = getArguments().getInt(DialogAddNoteAnalytic.ARG_ID_DMR);
        }

        APIClient = ApiServiceGenerator.createService(requireActivity().getApplicationContext(), APIService.class);

        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview_note_analytic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noteLayout = view.findViewById(R.id.add_layout);

        progressBar = view.findViewById(R.id.progress);
        initListView(view);
        fetchRNotes();
    }

    private void initListView(View view){
        analyticNoteList = new ArrayList<>();
        ListView listView = view.findViewById(R.id.list_view_item);
        noteAdapter = new PreviewNoteAnalyticListAdapter(mContext, R.layout.listview_preview_analytic_notes, analyticNoteList);

        listView.setAdapter(noteAdapter);
    }

    private void fetchRNotes(){
        if(progressBar != null)
            progressBar.setVisibility(View.VISIBLE);

        Call<List<AnalyticNote>> call = APIClient.getRNotesInDmrByStatusRange(PetugasMainActivity.UID, idDMR, null, AnalyticNote.RNOTE_ACCEPTED_BY_DOCTOR);
        call.enqueue(new Callback<List<AnalyticNote>>() {
            @Override
            public void onResponse(Call<List<AnalyticNote>> call, Response<List<AnalyticNote>> response) {
                progressBar.setVisibility(View.GONE);
                if(response.isSuccessful()) {
                    analyticNoteList.clear();
                    analyticNoteList.addAll(response.body());
                    noteAdapter.notifyDataSetChanged();
                }else{
                    String msg = ApiError.parseError(response).getMessage();
                    Log.d(TAG, "onResponse: " + msg);
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnalyticNote>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
