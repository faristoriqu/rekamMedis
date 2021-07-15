package com.sabin.digitalrm.dialogs;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.DialogAdapter;
import com.sabin.digitalrm.dialogs.fragments.PreviewNoteAnalyticFragment;

public class DialogPreviewNoteAnalytic extends DialogAdapter {
    public static final String ARG_ID_DMR = "ARG1";

    private int idDMR;
    public DialogPreviewNoteAnalytic() {
    }


    public static DialogPreviewNoteAnalytic newInstance(int idDMR){
        DialogPreviewNoteAnalytic fragment = new DialogPreviewNoteAnalytic();
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

        return PreviewNoteAnalyticFragment.newInstance(idDMR);
    }

    @Override
    public void onDialogCreated(DialogFragment dialog) {
        setTitle("Catatan Kelengkapan");
        setSubTitle("ID Dokumen: " + idDMR);
        setIcon(getContext().getDrawable(R.drawable.ic_bookmark_list));
        setCancelable(false);
        setPositiveButton("OK", view -> dialog.dismiss());
    }
}
