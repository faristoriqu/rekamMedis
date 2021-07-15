package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UnitCategory {
    /*
        "catunit_id": "17",
        "nama": "Loket",
        "lvl": "2",
        "parent_id": "1",
        "status": "t",
        "date_act": "2016-02-15 07:00:00",
        "kode": "0101"
    */

    public static final int RAWAT_JALAN_ID = 21;
    public static final int RAWAT_INAP_ID = 22;
    public static final int RAWAT_DARURAT_ID = 23;
    public static final int ALL_CAT_ID = 0;

    @SerializedName("catunit_id")
    @Expose
    private int id;

    @SerializedName("nama")
    @Expose
    private String name;

    @SerializedName("lvl")
    @Expose
    private int level;

    @SerializedName("parent_id")
    @Expose
    private int parent_id;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("date_act")
    @Expose
    private String date_act;

    @SerializedName("kode")
    @Expose
    private String code;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getParent_id() {
        return parent_id;
    }

    public int getLevel() {
        return level;
    }

    public String getStatus() {
        return status;
    }

    public String getDate_act() {
        return date_act;
    }

    public String getCode() {
        return code;
    }
}
