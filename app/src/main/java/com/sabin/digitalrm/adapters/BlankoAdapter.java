package com.sabin.digitalrm.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.InfoPoli;
import com.sabin.digitalrm.utils.Timestamp;

import java.util.List;

public abstract class BlankoAdapter extends RecyclerView.Adapter<BlankoAdapter.BRMHodler> {
    List<InfoPoli> mDataset;

    public BlankoAdapter(List <InfoPoli> dataset) {
        mDataset = dataset;
    }

    public class BRMHodler extends RecyclerView.ViewHolder{
        private TextView idPoli;
        private TextView namPoli;
        private TextView tanggal;
        private TextView modBy;
        private TextView chkSum;

        public BRMHodler(View itemView) {
            super(itemView);

            idPoli = itemView.findViewById(R.id.id_poli);
            namPoli = itemView.findViewById(R.id.poli);
            tanggal = itemView.findViewById(R.id.tanggal);
            modBy = itemView.findViewById(R.id.mod_by);
            chkSum = itemView.findViewById(R.id.chk_sum);

        }

        @SuppressLint("SetTextI18n")
        public void bindInfo(InfoPoli poli){
            idPoli.setText(poli.getPoliCode());
            namPoli.setText(poli.getNamaPoli());
            tanggal.setText("Tanggal: " + Timestamp.getDateTime(poli.getTemplateUpdate()));

            if(poli.getModBy() != null)
                modBy.setText("Oleh: " + poli.getModBy());

            if(poli.getChkSum() != null && poli.getChkSum().length() > 10)
                chkSum.setText("Checksum: " + poli.getChkSum().substring(0, 10) + "...");
        }
    }

    @NonNull
    @Override
    public BRMHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //TODO: MOVE TO PARENT, NOT HERE
        return new BRMHodler(inflater.inflate(R.layout.cardview_blanko, parent, false));
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

    protected abstract void onBindViewHolder(@NonNull BRMHodler hodler, int position, @NonNull InfoPoli model);
}
