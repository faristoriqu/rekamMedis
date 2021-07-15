package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DMKBlanko {
    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("id_blanko")
    @Expose
    private Integer idBlanko;

    @SerializedName("code")
    @Expose
    private String code;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("filename")
    @Expose
    private String filename;

    @SerializedName("total_page")
    @Expose
    private Integer totalPage;

    @SerializedName("version")
    @Expose
    private String version;

    @SerializedName("version_name")
    @Expose
    private String versionName;

    public DMKBlanko() {
    }

    public Integer getId() {
        return id;
    }

    public Integer getIdBlanko() {
        return idBlanko;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionName() {
        return versionName;
    }
}
