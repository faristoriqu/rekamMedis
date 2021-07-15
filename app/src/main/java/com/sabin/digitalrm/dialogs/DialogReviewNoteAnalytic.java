package com.sabin.digitalrm.dialogs;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.DialogAdapter;
import com.sabin.digitalrm.dialogs.fragments.ReviewNoteAnalyticFragment;

public class DialogReviewNoteAnalytic extends DialogAdapter {
    public static final String ARG_ID_DMR = "ARG1";

    private int idDMR, page;
    public DialogReviewNoteAnalytic() {
    }


    public static DialogReviewNoteAnalytic newInstance(int idDMR){
        DialogReviewNoteAnalytic fragment = new DialogReviewNoteAnalytic();
        Bundle args = new Bundle();
        args.putInt(ARG_ID_DMR, idDMR);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        if(getArguments() != null){
            idDMR = getArguments().getInt(ARG_ID_DMR);
         }
    }

    @NonNull
    @Override
    public Fragment onCreateDialogContainer(FragmentManager fragmentManager) {

        return ReviewNoteAnalyticFragment.newInstance(idDMR);
    }

    @Override
    public void onDialogCreated(DialogFragment dialog) {
        setTitle("Review Catatan");
        setSubTitle("Daftar catatan yang telah direview oleh dokter");
        setIcon(getContext().getDrawable(R.drawable.ic_content_paste));
        setCancelable(false);
        setNormalButton("OK", view -> dialog.dismiss());
    }
}
