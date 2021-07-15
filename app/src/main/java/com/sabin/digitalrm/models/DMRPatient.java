package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DMRPatient {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("norm")
    @Expose
    private String noRM;

    @SerializedName("id_unit_cat")
    @Expose
    private int unitCat;

    @SerializedName("id_unit")
    @Expose
    private int unitId;

    @SerializedName("filename")
    @Expose
    private String filename;

    @SerializedName("last_edited")
    @Expose
    private String lastEdit;

    @SerializedName("created_at")
    @Expose
    private String createdAt;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("dmks")
    @Expose
    private List<DMKBerkas> dmkBerkasList;

    public DMRPatient() {

    }

    public int getId() {
        return id;
    }

    public String getNoRM() {
        return noRM;
    }

    public int getUnitCat() {
        return unitCat;
    }

    public int getUnitId() {
        return unitId;
    }

    public String getFilename() {
        return filename;
    }

    public String getLastEdit() {
        return lastEdit;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public List<DMKBerkas> getDmkBerkasList() {
        return dmkBerkasList;
    }
}
