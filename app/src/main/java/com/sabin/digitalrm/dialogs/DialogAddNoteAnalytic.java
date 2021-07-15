package com.sabin.digitalrm.dialogs;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.DialogAdapter;
import com.sabin.digitalrm.dialogs.fragments.AddNoteAnalyticFragment;

public class DialogAddNoteAnalytic extends DialogAdapter {
    public static final String ARG_ID_DMR = "ARG1";
    public static final String ARG_PAGE = "ARG2";
    public static final String ARG_UID = "ARG3";

    private int idDMR, page;
    private String uid;
    public DialogAddNoteAnalytic() {
    }


    public static DialogAddNoteAnalytic newInstance(int idDMR, String uid){
        DialogAddNoteAnalytic fragment = new DialogAddNoteAnalytic();
        Bundle args = new Bundle();
        args.putInt(ARG_ID_DMR, idDMR);
        args.putString(ARG_UID, uid);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        if(getArguments() != null){
            idDMR = getArguments().getInt(ARG_ID_DMR);
            uid = getArguments().getString(ARG_UID);
        }
    }

    @NonNull
    @Override
    public Fragment onCreateDialogContainer(FragmentManager fragmentManager) {

        return AddNoteAnalyticFragment.newInstance(idDMR, uid);
    }

    @Override
    public void onDialogCreated(DialogFragment dialog) {
        setTitle("Tambah Catatan");
        setSubTitle("Daftar catatan pada halaman " + (page + 1));
        setIcon(getContext().getDrawable(R.drawable.ic_note_add));
        setCancelable(false);
        setNormalButton("OK", view -> dialog.dismiss());
    }
}
