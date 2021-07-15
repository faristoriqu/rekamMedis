package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Patient {

    @SerializedName("norm")
    @Expose
    private int no_rm;

    @SerializedName("nama_pasien")
    @Expose
    private String nama_pasien;

    @SerializedName("tempat_lahir")
    @Expose
    private String tempat_lahir;

    @SerializedName("tanggal_lahir")
    @Expose
    private String tanggal_lahir;

    @SerializedName("jenis_kelamin")
    @Expose
    private String jenis_kelamin;

    @SerializedName("alamat")
    @Expose
    private String alamat;

    @SerializedName("agama")
    @Expose
    private String agama;

    @SerializedName("hp_pasien")
    @Expose
    private String hp_pasien;

    @SerializedName("status_perkawinan")
    @Expose
    private String status_perkawinan;

    @SerializedName("pendidikan")
    @Expose
    private String pendidikan;

    @SerializedName("pekerjaan")
    @Expose
    private String pekerjaan;

    @SerializedName("created_at")
    @Expose
    private String created_at;

    public Patient(){}

    public int getNo_rm() {
        return no_rm;
    }

    public String getNama_pasien() {
        return nama_pasien;
    }

    public String getJenis_kelamin() {
        return jenis_kelamin;
    }

    public String getTanggal_lahir() {
        return tanggal_lahir;
    }

    public String getTempat_lahir() {
        return tempat_lahir;
    }

    public String getAlamat() {
        return alamat;
    }

    public String getHp_pasien() {
        return hp_pasien;
    }

    public String getPekerjaan() {
        return pekerjaan;
    }

    public String getPendidikan() {
        return pendidikan;
    }

    public String getStatus_perkawinan() {
        return status_perkawinan;
    }

    public String getAgama() {
        return agama;
    }

    public String getCreated_at() {
        return created_at;
    }
}
