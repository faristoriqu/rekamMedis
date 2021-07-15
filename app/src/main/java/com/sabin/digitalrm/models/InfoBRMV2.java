package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InfoBRMV2 {

    @SerializedName("srv_id")
    @Expose
    private int id;

    @SerializedName("norm")
    @Expose
    private String no_brm;

    @SerializedName("id_unit")
    @Expose
    private int id_unit;

    @SerializedName("unit_name")
    @Expose
    private String unit_name;

    @SerializedName("status")
    @Expose
    private Integer status;

    @SerializedName("active_at")
    @Expose
    private String activeAt;

    @SerializedName("nama_penjamin")
    @Expose
    private String penjamin;

    @SerializedName("keterangan")
    @Expose
    private String ket;

    @SerializedName("handled_by")
    @Expose
    private int handler;

    @SerializedName("id_berkas")
    @Expose
    private int berkas;

    @SerializedName("rejected_at")
    @Expose
    private String rejected_at;

    @SerializedName("is_dmr_ok")
    @Expose
    private int is_dmr_ok;

    @SerializedName("patient")
    @Expose
    private Patient patient;

    @SerializedName("reject_count")
    @Expose
    private int rcount;

    public InfoBRMV2(){}

    public int getId(){return id; }

    public String getCreateAt() {
        return activeAt;
    }

    public String getNo_brm() {
        return no_brm;
    }

    public int getId_unit() {
        return id_unit;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public Integer getStatus() {
        return status;
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

    public String getRejected_at() {
        return rejected_at;
    }

    public Patient getPatient() {
        return patient;
    }

    public int getRcount() {
        return rcount;
    }

    public int getIs_dmr_ok() {
        return is_dmr_ok;
    }
}