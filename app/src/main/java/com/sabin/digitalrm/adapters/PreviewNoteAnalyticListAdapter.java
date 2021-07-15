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


public class PreviewNoteAnalyticListAdapter extends ArrayAdapter<AnalyticNote> {
    private List<AnalyticNote> mDataset;
    private Context mContext;
    int resource;

    public PreviewNoteAnalyticListAdapter(@NonNull Context context, int resource, @NonNull List<AnalyticNote> objects) {
        super(context, resource, objects);
        mDataset = objects;
        mContext = context;
        this.resource = resource;
        Log.d("X-DEBUG", "BRMListAdapter preview");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //we need to get the view of the xml for our list item
        //And for this we need a layoutinflater
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_preview_analytic_notes, null, true);

        }

        Log.d("X-DEBUG", "getView: ");

        TextView tvNote = convertView.findViewById(R.id.note);
        TextView page = convertView.findViewById(R.id.page);

        //getting the hero of the specified position
        AnalyticNote note = mDataset.get(position);

        //adding values to the list item
        tvNote.setText(note.getNote());
//        page.setText(String.valueOf(note.getPage() + 1));

        return convertView;
    }
}
