package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DMKBookmark {
    /*
        API Result Samples:

        "id": "9",
        "id_berkas": "39",
        "id_dmk": "9",
        "page": "1",
        "total_page": "0",
        "norm": "629786",
        "id_unit_cat": "21",
        "filename": "1.0/6_12.2.2",
        "last_edited": "2019-02-19 21:16:13",
        "edited_by": "218",
        "created_at": "2019-02-19 21:16:13",
        "name": "Assesment Bedah Darurot",
        "id_unit": "0",
        "pdf_available": "0",
        "id_dmk_version": "6",
        "code": "12.2.2",
        "status": "1"
     */

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("code")
    @Expose
    private String dmk;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("page")
    @Expose
    private int page;

    @SerializedName("total_page")
    @Expose
    private int totalPages;

    public DMKBookmark() {

    }

    public int getId() {
        return id;
    }

    public String getDmk() {
        return dmk;
    }

    public String getName() {
        return name;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDmk(String dmk) {
        this.dmk = dmk;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
