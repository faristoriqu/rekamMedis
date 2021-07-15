package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BRMDetailResultData {
    @SerializedName("info_pasien")
    @Expose
    private InfoPasien info_pasien;

    @SerializedName("berkas")
    @Expose
    private List<UnitForBRMDetail> berkas;

    public BRMDetailResultData() {}

    public InfoPasien getInfo_pasien() {
        return info_pasien;
    }

    public List<UnitForBRMDetail> getBerkas() {
        return berkas;
    }
}
