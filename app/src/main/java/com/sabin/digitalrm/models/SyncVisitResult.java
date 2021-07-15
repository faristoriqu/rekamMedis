package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SyncVisitResult {
    @SerializedName("visit_update")
    @Expose
    private Integer visitCount;

    @SerializedName("poli_update")
    @Expose
    private Integer poliCount;

    @SerializedName("kunjungan")
    @Expose
    private List<VisitorType> visitorTypeList;

    public SyncVisitResult() {

    }

    public List<VisitorType> getVisitorTypeList() {
        return visitorTypeList;
    }

    public Integer getVisitCount() {
        return visitCount;
    }

    public Integer getPoliCount() {
        return poliCount;
    }
}
