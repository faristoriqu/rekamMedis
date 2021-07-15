package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by xkill on 19/11/18.
 */

public class ListInfoPoliResponse {
    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("result")
    @Expose
    private List<InfoPoli> infoPoliList;

    public int getStatuscode() {
        return statuscode;
    }

    public String getMessage() {
        return message;
    }

    public List<InfoPoli> getInfoPoliList() {
        return infoPoliList;
    }
}
