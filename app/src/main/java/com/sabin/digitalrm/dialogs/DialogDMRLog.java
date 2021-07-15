package com.sabin.digitalrm.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.DialogAdapter;
import com.sabin.digitalrm.dialogs.fragments.DMRLogFragment;

public class DialogDMRLog extends DialogAdapter {
    private final String TAG = "X-LOG";

    private FragmentManager fm;

    private DMRLogFragment mFragment;
    private Context mContext;

    private int id;
    private String subTitle;


    public DialogDMRLog() {

    }

    public static DialogDMRLog newInstance(int id, String subTitle){
        Bundle args = new Bundle();
        args.putInt(DMRLogFragment.ARG_IDCAT, id);
        args.putString(DMRLogFragment.ARG_SUBTITLE, subTitle);
        DialogDMRLog dialog = new DialogDMRLog();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt(DMRLogFragment.ARG_IDCAT);
            subTitle = getArguments().getString(DMRLogFragment.ARG_SUBTITLE);
        }

        mContext = getContext();
    }

    @NonNull
    @Override
    public Fragment onCreateDialogContainer(FragmentManager fragmentManager) {
        fm = fragmentManager;
        mFragment = DMRLogFragment.newInstance(id);
        return mFragment;
    }

    @Override
    public void onDialogCreated(DialogFragment dialog) {
        Drawable icon = mContext.getDrawable(R.drawable.ic_history);
        icon.setTint(Color.WHITE);

        setTitle("History Digital MR");
        setSubTitle("ID DMR: " + subTitle);
        setIcon(icon);
        setPositiveButton("OK", view -> {
            dialog.dismiss();
        });
    }
}
