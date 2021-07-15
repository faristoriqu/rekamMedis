package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DMRExport {
    @SerializedName("id")
    private int id;

    @SerializedName("no_brm")
    private String noBRM;

    @SerializedName("total_page")
    private Integer totalPage;

    @SerializedName("file_name")
    private String fileName;

    @SerializedName("file_key")
    private String fileKey;

    @SerializedName("export_name")
    private String exportName;



    public DMRExport() {

    }

    public int getId() {
        return id;
    }

    public String getNoBRM() {
        return noBRM;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileKey() {
        return fileKey;
    }

    public String getExportName() {
        return exportName;
    }
}
