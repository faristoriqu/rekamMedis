package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by xkill on 25/10/18.
 */

public class VisitorType {
    public static final int KUNJUNGAN_OFFSET = 254; //BARU: 255, POLI BARU = 254
    public static final int KUNJUNGAN_BARU = 1;
    public static final int KUNJUNGAN_POLI_BARU = 0;
    public static final int KUNJUNGAN_LAMA = 2;

    @SerializedName("id_poli")
    @Expose
    private int idPoli;

    @SerializedName("nama_poli")
    @Expose
    private String namaPoli;

    @SerializedName("last_update")
    @Expose
    private String lastUpdate;

    public VisitorType() {
    }


    public VisitorType (int idPoli, String namaPoli){
        this.idPoli = idPoli;
        this.namaPoli = namaPoli;
    }

    public String getNamaPoli() {
        return namaPoli;
    }

    public int getIdPoli() {
        return idPoli;
    }

    @Override
    public String toString() {
        return namaPoli;
    }
}
