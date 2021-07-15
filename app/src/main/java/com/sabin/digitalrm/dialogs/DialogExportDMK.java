package com.sabin.digitalrm.dialogs;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sabin.digitalrm.adapters.DialogAdapter;
import com.sabin.digitalrm.dialogs.fragments.DmkInDMRListFragment;
import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.DMRExport;
import com.sabin.digitalrm.prm.PetugasMainActivity;
import com.sabin.digitalrm.utils.ApiError;
import com.sabin.digitalrm.utils.StringUtilities;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogExportDMK extends DialogAdapter {
    private final String TAG = "X-LOG";

    private DmkInDMRListFragment blankoFragment;
    private APIService apiClient;
    private Context context;

    private int idDMR;

    public interface OnFinisListener{
        void onFinish(DMRExport export);
    }

    private OnFinisListener mListener;

    public DialogExportDMK() {

    }

    public void setOnFinishListerner(OnFinisListener listerner){
        mListener = listerner;
    }

    public static DialogExportDMK newInstance(int idDMR){
        Bundle args = new Bundle();
        args.putInt("EX1", idDMR);
        DialogExportDMK dialog = new DialogExportDMK();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idDMR = getArguments().getInt("EX1");
        }
        context = getContext();
        apiClient = ApiServiceGenerator.createService(requireActivity().getApplicationContext(), APIService.class);
    }

    @NonNull
    @Override
    public Fragment onCreateDialogContainer(FragmentManager fragmentManager) {
        blankoFragment = DmkInDMRListFragment.newInstance(idDMR);

        return blankoFragment;
    }

    @Override
    public void onDialogCreated(DialogFragment dialog) {
        setTitle("Export DMR");
        setSubTitle("Pilih DMK yang akan di export!");
        setPositiveButton("Export", view -> {
            List<String> unDMK = blankoFragment.getInDmkList();
            if(unDMK.size() == 0){
                Toast.makeText(context, "Tidak ada DMK yang dipilih!", Toast.LENGTH_SHORT).show();
            }else{
                doExport(StringUtilities.join(", ", unDMK));
            }
        });

        setNormalButton("Batal", view ->
            dialog.dismiss()
        );
    }

    public void doExport(String page){
        setProgressBar(View.VISIBLE);
        Call<DMRExport> call = apiClient.exportDMR(PetugasMainActivity.UID, idDMR, page, ("DMK " + blankoFragment.getCode() + " export, halaman: " + page) );
        Log.d(TAG, "doExport: " + page);
        call.enqueue(new Callback<DMRExport>() {
            @Override
            public void onResponse(Call<DMRExport> call, Response<DMRExport> response) {
                setProgressBar(View.GONE);
                if(response.isSuccessful()){
                    DMRExport export = response.body();
                    onDialogFinish(export);
                }else{
                    String msg = ApiError.parseError(response).getMessage();
                    Log.d(TAG, "onResponse: " + msg);
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DMRExport> call, Throwable t) {
                setProgressBar(View.GONE);
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onDialogFinish(DMRExport export){
        if(mListener != null){
            mListener.onFinish(export);
        }
        dismiss();
    }
}
