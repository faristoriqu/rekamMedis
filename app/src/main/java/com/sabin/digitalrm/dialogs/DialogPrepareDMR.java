package com.sabin.digitalrm.dialogs;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.widget.Button;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.DialogAdapter;
import com.sabin.digitalrm.dialogs.fragments.BlankoListFragment;
import com.sabin.digitalrm.dialogs.fragments.DmkListFragment;
import com.sabin.digitalrm.models.DetailBlanko;

import java.util.List;

public class DialogPrepareDMR extends DialogAdapter implements BlankoListFragment.OnBlankoVisitClickListener {
    private final String TAG = "X-LOG";

    private FragmentManager fm;
    private Button btnPositive, btnNegative, btnNormal;

    private BlankoListFragment blankoFragment;
    private DmkListFragment dmkListFragment;

    private DetailBlanko blanko;
    private int wizardState = 0;
    private int idCat;

    public interface OnFinisListener{
        void onFinish(DetailBlanko blanko, String undmk);
    }

    private OnFinisListener mListener;

    public DialogPrepareDMR() {

    }

    public void setOnFinishListerner(OnFinisListener listerner){
        mListener = listerner;
    }

    public static DialogPrepareDMR newInstance(int idCat){
        Bundle args = new Bundle();
        args.putInt(BlankoListFragment.ARG_IDCAT, idCat);
        DialogPrepareDMR dialog = new DialogPrepareDMR();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idCat = getArguments().getInt(BlankoListFragment.ARG_IDCAT);
        }
    }

    @NonNull
    @Override
    public Fragment onCreateDialogContainer(FragmentManager fragmentManager) {
        fm = fragmentManager;
        blankoFragment = BlankoListFragment.newInstance(idCat);

        blankoFragment.setOnBlankoVisitClickListener(this);
        return blankoFragment;
    }

    @Override
    public void onDialogCreated(DialogFragment dialog) {
        setTitle("Susun DMR Pasien");
        setSubTitle("Pilih blanko yang akan digunakan!");
        setPositiveButton("Lanjut", view -> {
            switch (wizardState){
                case 0:{
                    dmkListFragment = DmkListFragment.newInstance(blanko.getId());

                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.dialog_container, dmkListFragment);
                    transaction.commit();
                    wizardState = 1;
                    btnPositive.setText("Selesai");
                    setSubTitle("Sesuaikan DMK pada blanko ini!");
                    btnNegative.setEnabled(true);
                    break;
                }

                case 1:{
                    List<Integer> unDMK = dmkListFragment.getUnDMKlistID();
                    String strUndmk = "";
                    for (Integer item : unDMK){
                        strUndmk += item.toString() + ',';
                    }

                    int end = strUndmk.length() > 0 ? strUndmk.length() - 1 : 0;

                    onDialogFinish(strUndmk.substring(0, end));

                    dialog.dismiss();
                    break;
                }
            }
        });

        setNegativeButton("Kembali", view -> {
            switch (wizardState){
                case 1:{
                    btnPositive.setText("Lanjut");
                    setSubTitle("Pilih blanko yang akan digunakan!");
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.dialog_container, blankoFragment);
                    transaction.commit();
                    btnNegative.setEnabled(false);
                    wizardState = 0;
                    break;
                }
            }
        });

        setNormalButton("Cancel", view ->
            dialog.dismiss()
        );

        btnPositive = getPositiveButton();
        btnNormal = getNormalButton();
        btnNegative = getNegativeButton();

        btnPositive.setEnabled(false);
        btnNegative.setEnabled(false);
    }

    @Override
    public void onBlankoClick(DetailBlanko blanko) {
        btnPositive.setEnabled(true);
        this.blanko = blanko;
        Log.d(TAG, "onListClick: " + blanko.getName());
    }

    public void onDialogFinish(String unDMK){
        if(mListener != null){
            mListener.onFinish(blanko, unDMK);
        }
    }
}
