package com.sabin.digitalrm.models;

import com.google.gson.annotations.SerializedName;

public class RejectedNote {

    @SerializedName("id_note")
    private int id_note;

    @SerializedName("id_berkas")
    private int id_berkas;

    @SerializedName("page")
    private int page;

    @SerializedName("note")
    private String note;

    @SerializedName("status")
    private int status;

    @SerializedName("reply_note")
    private String reply_note;

    public RejectedNote() {}

    public int getId_note() {
        return id_note;
    }

    public int getId_berkas() {
        return id_berkas;
    }

    public int getPage() {
        return page;
    }

    public String getNote() {
        return note;
    }

    public int getStatus() {
        return status;
    }

    public String getReply_note() {
        return reply_note;
    }
}
