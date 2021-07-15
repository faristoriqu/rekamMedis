package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VersionsBlanko {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("version")
    @Expose
    private String version;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("is_default")
    @Expose
    private String is_default;

    public int getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getIs_default() {
        return is_default;
    }
}
