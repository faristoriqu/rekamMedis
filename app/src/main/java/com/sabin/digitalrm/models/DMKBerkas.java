package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DMKBerkas {
    @SerializedName("id")
    private Integer id;

    @Expose
    @SerializedName("id_berkas")
    private Integer idBerkas;

    @Expose
    @SerializedName("id_dmk")
    private Integer idDmk;

    @Expose
    @SerializedName("page")
    private Integer page;

    @Expose
    @SerializedName("total_page")
    private Integer totalPage;

    @SerializedName("code")
    private String code;

    @SerializedName("name")
    private String name;

    @SerializedName("version")
    private String version;

    @SerializedName("version_name")
    private String versionName;



    public DMKBerkas() {

    }

    public DMKBerkas(int idBerkas, int idDmk, int page, int totalPage){
        this.idBerkas = idBerkas;
        this.idDmk = idDmk;
        this.page = page;
        this.totalPage = totalPage;
    }

    public Integer getId() {
        return id;
    }

    public Integer getIdBerkas() {
        return idBerkas;
    }

    public Integer getIdDmk() {
        return idDmk;
    }

    public Integer getPage() {
        return page;
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

    public String getVersion() {
        return version;
    }

    public String getVersionName() {
        return versionName;
    }
}
