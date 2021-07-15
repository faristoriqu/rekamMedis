package com.sabin.digitalrm.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.DMRPatient;
import com.sabin.digitalrm.models.OldBRM;

import java.util.List;

public abstract class OldBRMAdapter extends RecyclerView.Adapter<OldBRMAdapter.BRMHoldeer> {
    List<OldBRM> mDataset;
    String namapas;
    int idpoli;

    public OldBRMAdapter(List<OldBRM> dataset, String pasien, int idpoli) {
        mDataset = dataset;
        namapas = pasien;
        idpoli = idpoli;
    }

    public class BRMHoldeer extends RecyclerView.ViewHolder{
        private TextView poli, namaPasien, noBRM, lastEdit, tvLastChangedTitle, tvLastChangedDivider;
        private ImageView icon;

        public BRMHoldeer(View itemView) {
            super(itemView);

            poli = itemView.findViewById(R.id.nama_poli);
            namaPasien = itemView.findViewById(R.id.nm_pasien);
            noBRM = itemView.findViewById(R.id.no_brm);
            lastEdit = itemView.findViewById(R.id.last_edited);
            icon = itemView.findViewById(R.id.imgPoliIcon);
            tvLastChangedTitle = itemView.findViewById(R.id.tvLastChangedTitle);
            tvLastChangedDivider = itemView.findViewById(R.id.tvLastChangedDivider);
        }

        public void bindInfo(Context ctx, OldBRM obrm) {
            int drawabl;

            this.poli.setText(obrm.getName());
            this.noBRM.setText(obrm.getNorm());
            this.lastEdit.setVisibility(View.GONE);
            this.tvLastChangedTitle.setVisibility(View.GONE);
            this.tvLastChangedDivider.setVisibility(View.GONE);

            if(namapas!=null){
                namaPasien.setText(namapas);
            }

            drawabl = R.drawable.ic_brm;
            icon.setBackgroundResource(drawabl);
        }
    }

    @NonNull
    @Override
    public OldBRMAdapter.BRMHoldeer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new OldBRMAdapter.BRMHoldeer(inflater.inflate(R.layout.card_other_poli, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OldBRMAdapter.BRMHoldeer holder, int position) {
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

    protected abstract void onBindViewHolder(@NonNull OldBRMAdapter.BRMHoldeer hodler, int position, @NonNull OldBRM model);
}
