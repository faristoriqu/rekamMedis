package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InfoBRM {

    @SerializedName("srv_id")
    @Expose
    private int id;

    @SerializedName("norm")
    @Expose
    private String no_brm;

    @SerializedName("status")
    @Expose
    private Integer status;

    @SerializedName("nama_pasien")
    @Expose
    private String namaPasien;

    @SerializedName("active_at")
    @Expose
    private String activeAt;

    @SerializedName("nama_penjamin")
    @Expose
    private String penjamin;

    @SerializedName("jenis_kelamin")
    @Expose
    private String jkel;

    @SerializedName("keterangan")
    @Expose
    private String ket;

    @SerializedName("handled_by")
    @Expose
    private int handler;

    @SerializedName("id_berkas")
    @Expose
    private int berkas;

    @SerializedName("patients")
    @Expose
    private List<Patient> patients;

    public InfoBRM(){}

    public int getId(){return id; }

    public String getCreateAt() {
        return activeAt;
    }

    public String getNamaPasien() {
        return namaPasien;
    }

    public String getNo_brm() {
        return no_brm;
    }

    public Integer getStatus() {
        return status;
    }

    public String getJkel() {
        return jkel;
    }

    public String getPenjamin() {
        return penjamin;
    }

    public String getKet() {
        return ket;
    }

    public int getHandler() {
        return handler;
    }

    public int getBerkas() {
        return berkas;
    }

    public List<Patient> getPatients() {
        return patients;
    }
}