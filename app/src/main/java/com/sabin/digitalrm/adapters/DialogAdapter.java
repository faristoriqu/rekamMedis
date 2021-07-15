package com.sabin.digitalrm.adapters;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sabin.digitalrm.R;

public abstract class DialogAdapter extends DialogFragment {
    Button positiveButton, negativeButton, normalButton;
    TextView title, subTitle;
    ImageView icon;
    RelativeLayout navFooter;
    DialogFragment dialog;
    ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_layout_1, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navFooter = view.findViewById(R.id.nav_footer);

        icon = view.findViewById(R.id.dialog_icon);
        title = view.findViewById(R.id.dialog_title);
        subTitle = view.findViewById(R.id.dialog_subtitle);

        positiveButton = view.findViewById(R.id.btn_positive);
        negativeButton = view.findViewById(R.id.btn_negative);
        normalButton = view.findViewById(R.id.btn_normal);
        progressBar = view.findViewById(R.id.progress);

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.dialog_container, onCreateDialogContainer(fm));
        transaction.commit();
        onDialogCreated(dialog);
    }

    public void setPositiveButton(String title, View.OnClickListener listener) {
        navFooter.setVisibility(View.VISIBLE);
        positiveButton.setVisibility(View.VISIBLE);
        positiveButton.setText(title);
        positiveButton.setOnClickListener(listener);
    }

    public void setNegativeButton(String title, View.OnClickListener listener) {
        navFooter.setVisibility(View.VISIBLE);
        negativeButton.setVisibility(View.VISIBLE);
        negativeButton.setText(title);
        negativeButton.setOnClickListener(listener);
    }

    public void setNormalButton(String title, View.OnClickListener listener) {
        navFooter.setVisibility(View.VISIBLE);
        normalButton.setVisibility(View.VISIBLE);
        normalButton.setText(title);
        normalButton.setOnClickListener(listener);
    }

    public void setProgressBar(int visibility){
        progressBar.setVisibility(visibility);
    }

    public Button getPositiveButton() {
        return positiveButton;
    }

    public Button getNormalButton() {
        return normalButton;
    }

    public Button getNegativeButton() {
        return negativeButton;
    }

    public void setSubTitle(String titile){
        this.subTitle.setVisibility(View.VISIBLE);
        this.subTitle.setText("( " + titile + " )");
    }

    public void setIcon(Drawable icon) {
        this.icon.setImageDrawable(icon);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    @NonNull
    public abstract Fragment onCreateDialogContainer(FragmentManager fragmentManager);
    public abstract void onDialogCreated(DialogFragment dialog);
}
