package com.sabin.digitalrm.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.DetailVisitor;
import com.sabin.digitalrm.models.VisitorType;

import java.util.List;

public abstract class VisitorAdapter extends RecyclerView.Adapter<VisitorAdapter.VisitorHolder> {
    private List<DetailVisitor> mDatasets;
    private Context mContext;


    public class VisitorHolder extends RecyclerView.ViewHolder{
        private TextView logoPoli, noBRM, namaPoli, namaKunjungan, tanggalKunjungan, namaPenjamin;
        private TextView namaPasien, noVisit, tempatLahir, tanggalLahir, alamat, terdaftar;
        private ImageView labelNew;

        private VisitorHolder(View itemView) {
            super(itemView);
            logoPoli = itemView.findViewById(R.id.logo_poli);
            noBRM = itemView.findViewById(R.id.no_brm);
            namaPoli = itemView.findViewById(R.id.poli_tujuan);
            namaKunjungan = itemView.findViewById(R.id.jenis_kunjungan);
            tanggalKunjungan = itemView.findViewById(R.id.tanggal_kunjungan);
            namaPenjamin = itemView.findViewById(R.id.nama_penjamin);

            namaPasien = itemView.findViewById(R.id.nama_pasien);
            noVisit = itemView.findViewById(R.id.no_visit);
            tempatLahir = itemView.findViewById(R.id.tempat_lahir);
            tanggalLahir = itemView.findViewById(R.id.tanggal_lahir);
            alamat = itemView.findViewById(R.id.alamat);
            terdaftar = itemView.findViewById(R.id.terdaftar);

            labelNew = itemView.findViewById(R.id.new_label);
        }

        @SuppressLint("SetTextI18n")
        public void bindInfo(DetailVisitor visitor){
/*            if(visitor.getInfoPoli() != null){
                logoPoli.setText(visitor.getInfoPoli().getNamaPoli());
                namaPoli.setText(" : " + visitor.getInfoPoli().getNamaPoli());
            }
            */
            String bdate = visitor.getInfoPasien().getTanggalLahir();
            String[] kunjDate = visitor.getTanggalSrv().split(" ");

            String[] bdateParser = bdate.split("-");
            String[] kunjDateParser = kunjDate[0].split("-");

            bdate = bdateParser[2] + "/" + bdateParser[1] + "/" + bdateParser[0];
            String kDate = kunjDateParser[2] + "/" + kunjDateParser[1] + "/" + kunjDateParser[0] + " " + kunjDate[1];

            namaPoli.setText(" : " + visitor.getUnitName());
            noBRM.setText(" : " + visitor.getInfoPasien().getNoBrm());
//            namaKunjungan.setText(" : " + visitor.getNamaKunjungan());
            tanggalKunjungan.setText(" : " + kDate);
            namaPenjamin.setText(" : " + visitor.getNamaPenjamin());

            namaPasien.setText(" : " + visitor.getInfoPasien().getNamaPasien());
            noVisit.setText(" : " + visitor.getIdVisit());
            tempatLahir.setText(" : " + visitor.getInfoPasien().getTempatLahir());
            tanggalLahir.setText(" : " + bdate);
            alamat.setText(visitor.getInfoPasien().getAlamat());
            terdaftar.setText(" : " + visitor.getInfoPasien().getCreatedAt());

            int vStatus = visitor.getStatus();
            if(vStatus == 1 || vStatus == 2 ){
                logoPoli.setText("Waiting to distribute");
            }else{
                logoPoli.setText("RME Distributed");
            }

            if(visitor.getIsNewVisit() != null)
            switch (visitor.getIsNewVisit()){
                case 1:{
                    namaKunjungan.setText(": Baru");
                    labelNew.setColorFilter(mContext.getResources().getColor(R.color.colorPrimary));
                    break;
                }

                case 0:{
                    namaKunjungan.setText(": Lama");
                    logoPoli.setText("Lama");
                    labelNew.setColorFilter(mContext.getResources().getColor(R.color.colorGray));
                    break;
                }

                default:{
                    namaKunjungan.setText(": N/A");
                    logoPoli.setText("N/A");
                    labelNew.setColorFilter(mContext.getResources().getColor(R.color.colorGray));
                    break;
                }
            }

/*            switch (visitor.getJenisKunjungan()){
                case VisitorType.KUNJUNGAN_BARU:{
                    labelNew.setColorFilter(mContext.getResources().getColor(R.color.colorGold));
                    break;
                }

                case VisitorType.KUNJUNGAN_POLI_BARU:{
                    labelNew.setColorFilter(mContext.getResources().getColor(R.color.colorPrimary));
                    break;
                }

                default:{
                    labelNew.setColorFilter(mContext.getResources().getColor(R.color.colorGray));
                }
            }
            */
        }
    }

    protected VisitorAdapter(Context context, List<DetailVisitor> _mDataset){
        mDatasets = _mDataset;
        mContext = context;
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
        return new VisitorHolder(inflater.inflate(R.layout.cardview_visitor, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull VisitorHolder holder, int position) {
        onBindViewHolder(holder, position, mDatasets.get(position));
    }

    protected abstract void onBindViewHolder(@NonNull VisitorHolder holder, int position, @NonNull DetailVisitor model);
}
