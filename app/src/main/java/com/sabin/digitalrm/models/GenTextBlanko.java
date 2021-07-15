package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class GenTextBlanko {
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

    @SerializedName("contents")
    @Expose
    private Map<String, String> contents;


    @SerializedName("coords")
    private Map<String, List<GenTextSetting>> textSettings;

    public GenTextBlanko() {
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

    public Map<String, List<GenTextSetting>> getTextSettings() {
        return textSettings;
    }

    public Map<String, String> getContents() {
        return contents;
    }
}
