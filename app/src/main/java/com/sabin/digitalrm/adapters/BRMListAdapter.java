package com.sabin.digitalrm.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.icu.text.IDNA;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.InfoBRM;
import com.sabin.digitalrm.models.InfoPasien;
import com.sabin.digitalrm.utils.Timestamp;

import java.util.List;


public class BRMListAdapter extends ArrayAdapter<InfoPasien> {
    private List<InfoPasien> mDataset;
    private Context mContext;
    int resource;

    public BRMListAdapter(@NonNull Context context, int resource, @NonNull List<InfoPasien> objects) {
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
            convertView = layoutInflater.inflate(R.layout.listview_export_brm, null, true);

        }

        Log.d("X-DEBUG", "getView: ");

        TextView namaPasien = convertView.findViewById(R.id.nama_pasien);
        TextView noBRM = convertView.findViewById(R.id.no_brm);
        TextView tanggal = convertView.findViewById(R.id.tanggal);

        //getting the hero of the specified position
        InfoPasien brm = mDataset.get(position);

        //adding values to the list item
        tanggal.setText((brm.getCreatedAt()));
        namaPasien.setText(brm.getNamaPasien());
        Log.d("X-LOG", "getView: " + brm.getNamaPasien());

        noBRM.setText(brm.getNoBrm());
        return convertView;
    }
}
