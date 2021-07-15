package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by xkill on 30/10/18.
 */

public class InfoPoli {
    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("poli_code")
    @Expose
    private String poliCode;

    @SerializedName("nama_poli")
    @Expose
    private String namaPoli;

    @SerializedName("template_filename")
    @Expose
    private String templateName;

    @SerializedName("status")
    @Expose
    private Integer status;

    @SerializedName("template_update")
    @Expose
    private Integer templateUpdate;

    @SerializedName("template_checksum")
    @Expose
    private String chkSum;

    @SerializedName("nama")
    @Expose
    private String modBy;

    public InfoPoli() {
    }

    public Integer getId() {
        return id;
    }

    public String getNamaPoli() {
        return namaPoli;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getChkSum() {
        return chkSum;
    }

    public Integer getTemplateUpdate() {
        return templateUpdate;
    }

    public String getModBy() {
        return modBy;
    }

    public Integer getStatus() {
        return status;
    }

    public String getPoliCode() {
        return poliCode;
    }
}
