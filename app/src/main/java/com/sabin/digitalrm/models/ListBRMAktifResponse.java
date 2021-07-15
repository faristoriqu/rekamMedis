package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by xkill on 08/11/18.
 */

public class ListBRMAktifResponse {
    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("result")
    @Expose
    private List<DetailBRMAktif> brmAktifList;

    public int getStatuscode() {
        return statuscode;
    }

    public String getMessage() {
        return message;
    }

    public List<DetailBRMAktif> getBrmAktifList() {
        return brmAktifList;
    }
}
