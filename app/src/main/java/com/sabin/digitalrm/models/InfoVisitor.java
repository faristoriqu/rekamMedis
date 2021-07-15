package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by xkill on 09/11/18.
 */

public class InfoVisitor {
    private final String NA_RET = "N/A";

    @SerializedName("no_brm")
    @Expose
    private String noBrm;

    @SerializedName("nama_penjamin")
    @Expose
    private String penjamin;

    @SerializedName("hp_penjamin")
    @Expose
    private String hpPenjamin;

    @SerializedName("alamat_penjamin")
    @Expose
    private String alamatPenjamin;


    @SerializedName("poli_tujuan")
    @Expose
    private Integer poliTujuan;

    @SerializedName("jenis_kunjungan")
    @Expose
    private Integer jenisKunjungan;

    @SerializedName("nama_kunjungan")
    @Expose
    private String namaKunjungan;

    @SerializedName("tanggal_kunjung")
    @Expose
    private String tanggalKunjung;

    public String getNoBrm() {
        return noBrm;
    }

    public String getPenjamin() {
        return penjamin == null ? NA_RET : penjamin;
    }

    public Integer getPoliTujuan() {
        return poliTujuan;
    }

    public Integer getJenisKunjungan() {
        return jenisKunjungan;
    }

    public String getNamaKunjungan() {
        return namaKunjungan;
    }

    public String getTanggalKunjung() {
        return tanggalKunjung;
    }

    public String getHpPenjamin() {
        return hpPenjamin == null ? NA_RET : hpPenjamin;
    }

    public String getAlamatPenjamin() {
        return alamatPenjamin == null ? NA_RET : alamatPenjamin;
    }
}
