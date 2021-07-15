package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by xkill on 09/11/18.
 */

public class InfoDokter {
    private final String NA_RET = "N/A";
    @SerializedName("user_id")
    @Expose
    private String id;

    @SerializedName("user_name")
    @Expose
    private String userName;

    @SerializedName("person_name")
    @Expose
    private String personName;

    public InfoDokter() {
    }

    public String getId() {
        return id;
    }

    public String getNama() {
        return personName == null ? NA_RET : personName;
    }
}
