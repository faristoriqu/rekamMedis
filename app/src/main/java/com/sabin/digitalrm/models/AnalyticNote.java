package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AnalyticNote {
    public static final int RNOTE_ISSUED = 0;
    public static final int RNOTE_ACCEPTED_BY_DOCTOR = 1;
    public static final int RNOTE_REJECTED_BY_DOCTOR = 2;
    public static final int RNOTE_CLOSED = 3;

    @SerializedName("id_note")
    private int id;

    @SerializedName("page")
    @Expose
    private Integer page;

    @SerializedName("note")
    @Expose
    private String note;

    @SerializedName("status")
    @Expose
    private Integer status;

    public AnalyticNote() {

    }

    public AnalyticNote(int page, String note) {
        this.page = page;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public Integer getPage() {
        return page;
    }

    public String getNote() {
        return note;
    }

    public Integer getStatus() {
        return status;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
