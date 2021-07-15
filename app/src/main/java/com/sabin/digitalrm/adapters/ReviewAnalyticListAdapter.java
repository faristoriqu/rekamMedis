package com.sabin.digitalrm.adapters;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.AnalyticNote;

import java.util.List;


public abstract class ReviewAnalyticListAdapter extends ArrayAdapter<AnalyticNote> {
    private List<AnalyticNote> mDataset;
    private Context mContext;
    int resource;

    public ReviewAnalyticListAdapter(@NonNull Context context, int resource, @NonNull List<AnalyticNote> objects) {
        super(context, resource, objects);
        mDataset = objects;
        mContext = context;
        this.resource = resource;
        Log.d("X-DEBUG", "BRMListAdapter on Review");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //we need to get the view of the xml for our list item
        //And for this we need a layoutinflater
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_review_analytic_notes, null, true);

        }

        Log.d("X-DEBUG", "getView: ");

        TextView tvNote = convertView.findViewById(R.id.note);
        TextView tvPage = convertView.findViewById(R.id.page);
        ImageView btnStatus = convertView.findViewById(R.id.btn_status);
        ImageView btnAccept = convertView.findViewById(R.id.btn_accept);
        ImageView btnReject = convertView.findViewById(R.id.btn_reject);

        btnAccept.setOnClickListener(view -> onAccept(position));
        btnReject.setOnClickListener(view -> onReject(position));
        btnStatus.setOnClickListener(view -> onStatusClick(position));

        //getting the hero of the specified position
        AnalyticNote note = mDataset.get(position);

        //adding values to the list item
        switch (note.getStatus()){
            case AnalyticNote.RNOTE_ACCEPTED_BY_DOCTOR:
                btnStatus.setColorFilter(mContext.getResources().getColor(R.color.colorPrimary));
                break;
            case AnalyticNote.RNOTE_REJECTED_BY_DOCTOR:
                btnStatus.setColorFilter(mContext.getResources().getColor(R.color.colorAlert));
                break;
        }
        tvNote.setText(note.getNote());
        tvPage.setText(String.valueOf(note.getPage() + 1));

        return convertView;
    }

    protected abstract void onAccept(int pos);
    protected abstract void onReject(int pos);
    protected abstract void onStatusClick(int pos);
}
