package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by abudawud on 11/06/19.
 */

public class EHOSUnit {
    @SerializedName("unit_id")
    @Expose
    private int unitID;

    @SerializedName("unit_type")
    @Expose
    private int catID;

    @SerializedName("unit_name")
    @Expose
    private String unitName;

    public EHOSUnit(int unitID, String unitName) {
        this.unitID = unitID;
        this.unitName = unitName;
    }

    public int getUnitID() {
        return unitID;
    }

    public int getCatID() {
        return catID;
    }

    public String getUnitName() {
        return unitName;
    }

    public String toString(){
        return unitName;
    }
}
