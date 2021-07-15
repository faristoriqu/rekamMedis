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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.AnalyticNote;
import com.sabin.digitalrm.models.DetailBlanko;

import java.util.List;


public abstract class NoteAnalyticListAdapter extends ArrayAdapter<AnalyticNote> {
    private List<AnalyticNote> mDataset;
    private Context mContext;
    int resource;

    public NoteAnalyticListAdapter(@NonNull Context context, int resource, @NonNull List<AnalyticNote> objects) {
        super(context, resource, objects);
        mDataset = objects;
        mContext = context;
        this.resource = resource;
        Log.d("X-DEBUG", "BRMListAdapter: add ");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //we need to get the view of the xml for our list item
        //And for this we need a layoutinflater
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_analytic_notes, null, true);

        }

        Log.d("X-DEBUG", "getView: ");

        TextView tvNote = convertView.findViewById(R.id.note);
        ImageView btnEdit = convertView.findViewById(R.id.btn_edit);
        ImageView btnDel = convertView.findViewById(R.id.btn_delete);

        btnDel.setOnClickListener(view -> onNoteDelete(position));
        btnEdit.setOnClickListener(view -> onNoteEdit(position));

        //getting the hero of the specified position
        AnalyticNote note = mDataset.get(position);

        //adding values to the list item
        tvNote.setText(note.getNote());

        return convertView;
    }

    protected abstract void onNoteDelete(int pos);
    protected abstract void onNoteEdit(int pos);
}
