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
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.DetailBlanko;
import com.sabin.digitalrm.models.InfoBRM;

import java.util.List;


public class BlankoVisitListAdapter extends ArrayAdapter<DetailBlanko> {
    private List<DetailBlanko> mDataset;
    private Context mContext;
    int resource;

    public BlankoVisitListAdapter(@NonNull Context context, int resource, @NonNull List<DetailBlanko> objects) {
        super(context, resource, objects);
        mDataset = objects;
        mContext = context;
        this.resource = resource;
        Log.d("X-DEBUG", "BRMListAdapter: ");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //we need to get the view of the xml for our list item
        //And for this we need a layoutinflater
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_blanko_visit, null, true);

        }

        Log.d("X-DEBUG", "getView: ");

        TextView title = convertView.findViewById(R.id.title);
        TextView lastUpdate = convertView.findViewById(R.id.last_update);

        //getting the hero of the specified position
        DetailBlanko blanko = mDataset.get(position);

        //adding values to the list item
        title.setText(blanko.getName());
        lastUpdate.setText("Terahir diupdate: " + blanko.getUpdated());

        return convertView;
    }
}
