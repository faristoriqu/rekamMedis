package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by xkill on 21/11/18.
 */

public class InfoPoliResponse {
    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("result")
    @Expose
    private InfoPoli infoPoli;

    public int getStatuscode() {
        return statuscode;
    }

    public String getMessage() {
        return message;
    }

    public InfoPoli getInfoPoli() {
        return infoPoli;
    }
}
