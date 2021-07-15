package com.sabin.digitalrm.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.DetailVisitor;
import com.sabin.digitalrm.models.DetailVisitor;
import com.sabin.digitalrm.utils.Timestamp;

import java.util.List;

public abstract class BrmAktifAdapter extends RecyclerView.Adapter<BrmAktifAdapter.VisitorHolder> {
    private List<DetailVisitor> mDatasets;
    private Context mContext;
    private int queryType;
    private LinearLayout layoutStatus;


    public class VisitorHolder extends RecyclerView.ViewHolder{
        TextView poliTujuan, statusBRM, noBrm, bukaPada, ambilPada, selesaiPada, namaDokter;
        TextView namaPasien, noVisit, terdaftar, tempatLahir, tanggalLahir;
        TextView penjamin, noHp, alamat;
        private VisitorHolder(View itemView) {
            super(itemView);

            poliTujuan = itemView.findViewById(R.id.poli_tujuan);
            statusBRM = itemView.findViewById(R.id.status_brm);
            noBrm = itemView.findViewById(R.id.no_brm);
            bukaPada = itemView.findViewById(R.id.dibuka_pada);
            ambilPada = itemView.findViewById(R.id.ditangani_pada);
            selesaiPada = itemView.findViewById(R.id.selesai_pada);
            namaDokter = itemView.findViewById(R.id.nama_dokter);

            namaPasien = itemView.findViewById(R.id.nama_pasien);
            noVisit = itemView.findViewById(R.id.no_visit);
            terdaftar = itemView.findViewById(R.id.terdaftar);
            tempatLahir = itemView.findViewById(R.id.tempat_lahir);
            tanggalLahir = itemView.findViewById(R.id.tanggal_lahir);

            penjamin = itemView.findViewById(R.id.nama_penjamin);
            noHp = itemView.findViewById(R.id.no_hp);
            alamat = itemView.findViewById(R.id.alamat);
            layoutStatus = itemView.findViewById(R.id.brm_status);
        }

        public void bindInfo(DetailVisitor brm){
            if(brm.getStatus() == DetailVisitor.VISITOR_DMR_ANALITYC){
                layoutStatus.setVisibility(View.VISIBLE);
                switch (brm.getIsDmrOK()){
                    case DetailVisitor.DMR_NOT_CHECKED:{
                        statusBRM.setText("Belum Diperiksa");
                        break;
                    }
                    case DetailVisitor.DMR_CHECKED_AND_INCOMPLETE:{
                        statusBRM.setText("Tidak Lengkap");
                        break;
                    }
                    case DetailVisitor.DMR_REVIEWED_BY_DOCTOR:{
                        statusBRM.setText("Sudah Dilengkapi");
                        break;
                    }
                    default:{
                        statusBRM.setText("Lengkap");
                    }
                }
            }else if(queryType == DetailVisitor.VISITOR_DMR_ALL){
                layoutStatus.setVisibility(View.VISIBLE);
                statusBRM.setText(DetailVisitor.BRM_STATUS_NAME[brm.getStatus()]);
            }
            noBrm.setText(" : " + brm.getInfoPasien().getNoBrm());
            bukaPada.setText(" : " + (brm.getCreatedAt()));
            ambilPada.setText(" : " + (brm.getTakenAt()));
            selesaiPada.setText(" : " + (brm.getSubmitAt()));

            if(brm.getInfoDokter() != null)
                namaDokter.setText(" : " + brm.getInfoDokter().getNama());

            namaPasien.setText(" : " + brm.getInfoPasien().getNamaPasien());
            noVisit.setText(" : " + brm.getIdVisit());
            terdaftar.setText(" : " + brm.getInfoPasien().getCreatedAt());
            tempatLahir.setText(" : " + brm.getInfoPasien().getTempatLahir());
            tanggalLahir.setText(" : " + brm.getInfoPasien().getTanggalLahir());
            penjamin.setText(" : " + brm.getNamaPenjamin());
            noHp.setText(" : " + brm.getHpPenjamin());
            alamat.setText(brm.getAlamatPenjamin());
        }
    }

    public BrmAktifAdapter(Context context, List<DetailVisitor> _mDataset, int queryType){
        mDatasets = _mDataset;
        mContext = context;
        this.queryType = queryType;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mDatasets.size();
    }


    @NonNull
    @Override
    public VisitorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new VisitorHolder(inflater.inflate(R.layout.cardview_brm_aktif, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull VisitorHolder holder, int position) {
        onBindViewHolder(holder, position, mDatasets.get(position));
    }

    protected abstract void onBindViewHolder(@NonNull VisitorHolder holder, int position, @NonNull DetailVisitor model);
}
