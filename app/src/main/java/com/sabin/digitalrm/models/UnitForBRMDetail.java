package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UnitForBRMDetail {
    @SerializedName("unit_id")
    @Expose
    private int unit_id;

    @SerializedName("unit_code")
    @Expose
    private String unit_code;

    @SerializedName("unit_name")
    @Expose
    private String unit_name;

    @SerializedName("unit_nickname")
    @Expose
    private String unit_nickname;

    @SerializedName("nama_file")
    @Expose
    private String nama_file;

    @SerializedName("no_brm")
    @Expose
    private String no_brm;

    @SerializedName("last_edited")
    @Expose
    private String last_edited;

    public UnitForBRMDetail() {

    }

    public int getUnit_id() {
        return unit_id;
    }

    public String getUnit_code() {
        return unit_code;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public String getUnit_nickname() {
        return unit_nickname;
    }

    public String getNama_file() {
        return nama_file;
    }

    public String getNo_brm() {
        return no_brm;
    }

    public String getLast_edited() {
        return last_edited;
    }
}