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
import com.sabin.digitalrm.adapters.NoteAnalyticListAdapter;
import com.sabin.digitalrm.dialogs.DialogAddNoteAnalytic;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.AnalyticNote;
import com.sabin.digitalrm.models.ApiStatus;
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
public class AddNoteAnalyticFragment extends Fragment {
    private final String TAG = "X-LOG";
    private Context mContext;
    private NoteAnalyticListAdapter noteAdapter;
    private List<AnalyticNote> analyticNoteList;
    private LinearLayout noteLayout;

    private int indexNote, idDMR, page;
    private String uid;
    private APIService APIClient;

    Button btnPositive, btnNegative;
    EditText etNote;
    ProgressBar progressBar;

    public AddNoteAnalyticFragment() {
        // Required empty public constructor
    }

    public static AddNoteAnalyticFragment newInstance(int idDMR, String uid){
        AddNoteAnalyticFragment fragment = new AddNoteAnalyticFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogAddNoteAnalytic.ARG_ID_DMR, idDMR);
        bundle.putString(DialogAddNoteAnalytic.ARG_UID, uid);
//        bundle.putInt(DialogAddNoteAnalytic.ARG_PAGE, page);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        if(getArguments() != null){
            idDMR = getArguments().getInt(DialogAddNoteAnalytic.ARG_ID_DMR);
            uid = getArguments().getString(DialogAddNoteAnalytic.ARG_UID);
        }

        APIClient = ApiServiceGenerator.createService(requireActivity().getApplicationContext(), APIService.class);

        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_note_analytic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noteLayout = view.findViewById(R.id.add_layout);

        btnPositive = view.findViewById(R.id.btn_positive);
        btnNegative = view.findViewById(R.id.btn_negative);
        etNote = view.findViewById(R.id.note);
        progressBar = view.findViewById(R.id.progress);

        btnNegative.setOnClickListener(view1 -> {
            noteLayout.setVisibility(View.GONE);
            btnNegative.setVisibility(View.GONE);
            btnPositive.setText("Add");
        });

        btnPositive.setOnClickListener(view1 -> {
            if(btnPositive.getText().toString().equals("Add")) {
                noteLayout.setVisibility(View.VISIBLE);
                btnNegative.setVisibility(View.VISIBLE);
                btnPositive.setText("Save");
                etNote.setText("");
                etNote.setEnabled(true);
                etNote.requestFocus();
            }else if(btnPositive.getText().toString().equals("Update")){
                progressBar.setVisibility(View.GONE);
                String note = etNote.getText().toString();
                Call<ApiStatus> call = APIClient.updateNoteRNoteInDMR(uid, analyticNoteList.get(indexNote).getId(), note, null);
                Log.d(TAG, "onViewCreated: " + call.request().toString());
                call.enqueue(new Callback<ApiStatus>() {
                    @Override
                    public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                        progressBar.setVisibility(View.GONE);
                        if(response.isSuccessful()){
                            noteLayout.setVisibility(View.GONE);
                            btnNegative.setVisibility(View.GONE);
                            btnPositive.setText("Add");

                            analyticNoteList.get(indexNote).setNote(etNote.getText().toString());
                            noteAdapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "Catatan Berhasil Diupdate", Toast.LENGTH_SHORT).show();
                        }else{
                            String msg = ApiError.parseError(response).getMessage();
                            Log.d(TAG, "onResponse: " + msg);
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiStatus> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                        Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else if(btnPositive.getText().toString().equals("Delete")){
                progressBar.setVisibility(View.VISIBLE);
                Call<ApiStatus> call = APIClient.deleteRNoteInDMR(uid, analyticNoteList.get(indexNote).getId());
                Log.d(TAG, "onViewCreated: " + call.request().toString());
                call.enqueue(new Callback<ApiStatus>() {
                    @Override
                    public void onResponse(Call<ApiStatus> call, Response<ApiStatus> response) {
                        progressBar.setVisibility(View.GONE);
                        if(response.isSuccessful()){
                            noteLayout.setVisibility(View.GONE);
                            btnNegative.setVisibility(View.GONE);
                            btnPositive.setText("Add");
                            analyticNoteList.remove(indexNote);
                            noteAdapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "Catatan Berhasil Dihapus", Toast.LENGTH_SHORT).show();
                        }else{
                            String msg = ApiError.parseError(response).getMessage();
                            Log.d(TAG, "onResponse: " + msg);
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiStatus> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                        Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                progressBar.setVisibility(View.VISIBLE);
                String note = etNote.getText().toString();
                Call<AnalyticNote> call = APIClient.addRNoteInDMR(uid, idDMR, page, note);
                call.enqueue(new Callback<AnalyticNote>() {
                    @Override
                    public void onResponse(Call<AnalyticNote> call, Response<AnalyticNote> response) {
                        progressBar.setVisibility(View.GONE);
                        if(response.isSuccessful()){
                            Toast.makeText(getContext(), "Catatan Berhasil Ditambahkan", Toast.LENGTH_SHORT).show();
//                            analyticNoteList.add(response.body());
//                            noteAdapter.notifyDataSetChanged();
                            fetchRNotesInPage();
                            noteLayout.setVisibility(View.GONE);
                            btnNegative.setVisibility(View.GONE);
                            btnPositive.setText("Add");
//                            noteAdapter.notifyDataSetChanged();
                        }else {
                            String msg = ApiError.parseError(response).getMessage();
                            Log.d(TAG, "onResponse: " + msg);
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AnalyticNote> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                        Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        initListView(view);
        fetchRNotesInPage();
    }

    private void initListView(View view){
        analyticNoteList = new ArrayList<>();
        ListView listView = view.findViewById(R.id.list_view_item);
        noteAdapter = new NoteAnalyticListAdapter(mContext, R.layout.listview_blanko_visit, analyticNoteList){
            @Override
            protected void onNoteDelete(int pos) {
                indexNote = pos;
                noteLayout.setVisibility(View.VISIBLE);
                btnNegative.setVisibility(View.VISIBLE);
                btnPositive.setText("Delete");
                etNote.setText(analyticNoteList.get(pos).getNote());
                etNote.setEnabled(false);
            }

            @Override
            protected void onNoteEdit(int pos) {
                indexNote = pos;
                noteLayout.setVisibility(View.VISIBLE);
                btnNegative.setVisibility(View.VISIBLE);
                btnPositive.setText("Update");
                etNote.setText(analyticNoteList.get(pos).getNote());
                etNote.requestFocus();
                etNote.setEnabled(true);
            }
        };
        listView.setAdapter(noteAdapter);
    }

    private void fetchRNotesInPage(){
        if(progressBar != null)
            progressBar.setVisibility(View.VISIBLE);

        

        Call<List<AnalyticNote>> call = APIClient.getRNotesInDMR(uid, idDMR, null, AnalyticNote.RNOTE_ISSUED);
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
