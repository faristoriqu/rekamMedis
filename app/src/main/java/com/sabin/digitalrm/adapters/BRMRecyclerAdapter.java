package com.sabin.digitalrm.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.InfoBRMV2;
import com.sabin.digitalrm.models.Patient;

import java.util.List;


public class BRMRecyclerAdapter extends RecyclerView.Adapter<BRMRecyclerAdapter.BRMVHolder> {
    private List<InfoBRMV2> mDataset;
    private Context mContext;

    class BRMVHolder extends RecyclerView.ViewHolder {
        TextView namaPasien, noBRM, tanggal, status, penjamin, jkel, keterangan;

        BRMVHolder(View view) {
            super(view);
            namaPasien = view.findViewById(R.id.nama_pasien);
            noBRM = view.findViewById(R.id.no_brm);
            tanggal = view.findViewById(R.id.tanggal);
            status = view.findViewById(R.id.status);
            penjamin = view.findViewById(R.id.penjamin);
            jkel = view.findViewById(R.id.jkel);
            keterangan = view.findViewById(R.id.keterangan);
        }
    }

    public BRMRecyclerAdapter(Context ctx, List<InfoBRMV2> objects) {
        this.mContext = ctx;
        this.mDataset = objects;
    }

    @Override
    public @NonNull BRMVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_brm, parent, false);

        return new BRMVHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BRMVHolder holder, int position) {
        InfoBRMV2 iBRM = mDataset.get(position);
        Patient patient = iBRM.getPatient();

        String sCreatedat = iBRM.getCreateAt();
        String[] arCreateAt1 = sCreatedat.split(" ");
        String[] arCreateAt2 = arCreateAt1[0].split("-");
        String reCreateAt = arCreateAt2[2] + "/" + arCreateAt2[1] + "/" + arCreateAt2[0] + " " + arCreateAt1[1];

        holder.tanggal.setText(reCreateAt);
        holder.namaPasien.setText(patient.getNama_pasien());
        holder.penjamin.setText(iBRM.getPenjamin());
        holder.jkel.setText(patient.getJenis_kelamin());
        holder.keterangan.setText(iBRM.getKet());

        switch (iBRM.getStatus()){
            case 3:{
                holder.status.setText("Belum ditangani");
                holder.status.setTextColor(Color.RED);
                break;
            }

            case 4:{
                holder.status.setText("Sedang ditangani");
                holder.status.setTextColor(Color.BLUE);
                break;
            }

            case 5:{
                if(iBRM.getIs_dmr_ok()==1){
                    holder.status.setText("Tidak Lengkap");
                    holder.status.setTextColor(Color.BLUE);
                }else {
                    holder.status.setText("Sudah ditangani");
                    holder.status.setTextColor(Color.GREEN);
                }
                break;
            }

        }
        holder.noBRM.setText(iBRM.getNo_brm());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
