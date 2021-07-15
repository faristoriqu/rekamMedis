package com.sabin.digitalrm.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.DMRPatient;
import com.sabin.digitalrm.models.UnitForBRMDetail;

import java.util.List;

public abstract class PrmPoliListAdapter extends RecyclerView.Adapter<PrmPoliListAdapter.BRMHodler> {
    List<DMRPatient> mDataset;

    public PrmPoliListAdapter(List <DMRPatient> dataset) {
        mDataset = dataset;
    }

    public class BRMHodler extends RecyclerView.ViewHolder{
        private TextView poli;
        private TextView namaPasien;
        private TextView noBRM;

        public BRMHodler(View itemView) {
            super(itemView);

            poli = itemView.findViewById(R.id.poli);
            namaPasien = itemView.findViewById(R.id.nama_pasiens);
            noBRM = itemView.findViewById(R.id.no_brm);
        }

        public void bindInfo(DMRPatient poli){
            this.poli.setText(String.valueOf(poli.getName()));
            noBRM.setText("Modifikasi: "+poli.getLastEdit());
            namaPasien.setText(poli.getFilename());
        }
    }

    @NonNull
    @Override
    public BRMHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new BRMHodler(inflater.inflate(R.layout.cardview_brm, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BRMHodler holder, int position) {
        onBindViewHolder(holder, position, mDataset.get(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    protected abstract void onBindViewHolder(@NonNull BRMHodler hodler, int position, @NonNull DMRPatient model);
}
