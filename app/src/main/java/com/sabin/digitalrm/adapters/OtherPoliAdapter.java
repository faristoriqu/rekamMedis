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

import java.util.List;

public abstract class OtherPoliAdapter extends RecyclerView.Adapter<OtherPoliAdapter.BRMHoldeer> {
    List<DMRPatient> mDataset;
    String namapas;
    int idpoli;

    public OtherPoliAdapter(List<DMRPatient> dataset, String pasien, int idpoli) {
        mDataset = dataset;
        namapas = pasien;
        idpoli = idpoli;
    }

    public class BRMHoldeer extends RecyclerView.ViewHolder{
        private TextView poli;
        private TextView namaPasien;
        private TextView noBRM;
        private TextView lastEdit;
        private ImageView icon;

        public BRMHoldeer(View itemView) {
            super(itemView);

            poli = itemView.findViewById(R.id.nama_poli);
            namaPasien = itemView.findViewById(R.id.nm_pasien);
            noBRM = itemView.findViewById(R.id.no_brm);
            lastEdit = itemView.findViewById(R.id.last_edited);
            icon = itemView.findViewById(R.id.imgPoliIcon);
        }

        public void bindInfo(Context ctx, DMRPatient poli) {
            int drawabl;
            String[] poliname = poli.getName().split("/");
            String unitname = "";

            if (poliname.length > 1){
                unitname = poliname[1];
            }

            this.poli.setText(unitname);
            this.noBRM.setText(poli.getNoRM());
            this.lastEdit.setText(poli.getLastEdit());

            if(namapas!=null){
                namaPasien.setText(namapas);
            }

//            String[] w = poli.getUnit_name().split(" ");
//            String[] w2 = w[1].split("-");
//
//            if(w2.length > 1){
//                w[1] = w2[0];
//            }
//
//            w[1] = w[1].toLowerCase();

//            try {
//                drawabl = ctx.getResources().getIdentifier("ic_" + w[1], "drawable", ctx.getPackageName());
//            }catch (Exception ex){
//                drawabl = R.drawable.ic_brm;
//            }

            drawabl = R.drawable.ic_brm;
            icon.setBackgroundResource(drawabl);
        }
    }

    @NonNull
    @Override
    public OtherPoliAdapter.BRMHoldeer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new OtherPoliAdapter.BRMHoldeer(inflater.inflate(R.layout.card_other_poli, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OtherPoliAdapter.BRMHoldeer holder, int position) {
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

    protected abstract void onBindViewHolder(@NonNull OtherPoliAdapter.BRMHoldeer hodler, int position, @NonNull DMRPatient model);
}
