package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DMK {
    public static final int STATUS_DMK_NEW = 1;
    public static final int STATUS_DMK_GENERATED = 2;
    public static final int STATUS_DMK_COORD = 3;
    public static final int STATUS_DMK_ACTIVE = 4;

    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("id_dmk_version")
    @Expose
    private Integer idDmkVersion;

    @SerializedName("code")
    @Expose
    private String code;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("total_page")
    @Expose
    private Integer totalPage;

    @SerializedName("filename")
    @Expose
    private String filename;

    @SerializedName("status")
    @Expose
    private Integer status;

    public DMK() {

    }

    public DMK(int idDmk, String dmkCode, String dmkName, int dmkPage, int dmkStatus){
        id = idDmk;
        code = dmkCode;
        name = dmkName;
        totalPage = dmkPage;
        status = dmkStatus;
    }

    public Integer getId() {
        return id;
    }

    public Integer getIdDmkVersion() {
        return idDmkVersion;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public String getFilename() {
        return filename;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public void setName(String name) {
        this.name = name;
    }
}
