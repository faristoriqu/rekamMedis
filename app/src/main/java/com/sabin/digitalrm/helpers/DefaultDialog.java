package com.sabin.digitalrm.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.sabin.digitalrm.R;

public class DefaultDialog {
    private Button negativeBtn, positiveBtn, normalBtn;
    private TextView title, subTitle;
    private ImageView icon;
    private View container;
    private RelativeLayout nav;
    private LayoutInflater inflater;

    private ProgressBar progressBar;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    public DefaultDialog(Context context) {
        builder = new AlertDialog.Builder(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_layout_1, null);
        icon = view.findViewById(R.id.dialog_icon);
        title = view.findViewById(R.id.dialog_title);
        subTitle = view.findViewById(R.id.dialog_subtitle);
        container = view.findViewById(R.id.dialog_message);
        nav = view.findViewById(R.id.nav_footer);
        negativeBtn = view.findViewById(R.id.btn_negative);
        positiveBtn = view.findViewById(R.id.btn_positive);
        normalBtn = view.findViewById(R.id.btn_normal);
        progressBar = view.findViewById(R.id.progress);

        builder.setCancelable(false);
        builder.setView(view);
    }

    public void setView(View view){
        ViewGroup parent = (ViewGroup) container.getParent();
        int index = parent.indexOfChild(container);
        parent.removeView(container);
//        container = inflater.inflate(optionId, parent, false);
        parent.addView(view, index);
    }

    public void setMessage(String msg){
        ((TextView) container).setText(msg);
    }

    public void setTitle(String title){
        this.title.setText(title);
    }

    public void setIcon(Drawable icon){
        this.icon.setImageDrawable(icon);
    }

    public void setPositiveButton(String text, View.OnClickListener listener){
        nav.setVisibility(View.VISIBLE);
        positiveBtn.setVisibility(View.VISIBLE);
        positiveBtn.setText(text);
        if (listener != null)
            positiveBtn.setOnClickListener(listener);
        else
            positiveBtn.setOnClickListener(view -> dismiss());
    }

    public void setNegativeButton(String text, View.OnClickListener listener){
        nav.setVisibility(View.VISIBLE);
        negativeBtn.setVisibility(View.VISIBLE);
        negativeBtn.setText(text);
        if (listener != null)
            negativeBtn.setOnClickListener(listener);
        else
            negativeBtn.setOnClickListener(view -> dismiss());
    }

    public void setNormalButton(String text, View.OnClickListener listener){
        nav.setVisibility(View.VISIBLE);
        normalBtn.setVisibility(View.VISIBLE);
        normalBtn.setText(text);
        if (listener != null)
            normalBtn.setOnClickListener(listener);
        else
            normalBtn.setOnClickListener(view -> dismiss());
    }

    public void setProgressBar(int visibility){
        progressBar.setVisibility(visibility);
    }

    public void dismiss(){
        dialog.dismiss();
    }

    public void show(){
        dialog = builder.create();
        dialog.show();
    }
}
