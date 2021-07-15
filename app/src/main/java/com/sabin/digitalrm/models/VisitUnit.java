package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VisitUnit {
    @SerializedName("id_unit")
    @Expose
    private int idUnit;

    @SerializedName("unit_name")
    @Expose
    private String unitName;

    @SerializedName("total")
    @Expose
    private int total;

    public VisitUnit() {

    }

    public int getIdUnit() {
        return idUnit;
    }

    public String getUnitName() {
        return unitName;
    }

    public int getTotal() {
        return total;
    }
}
