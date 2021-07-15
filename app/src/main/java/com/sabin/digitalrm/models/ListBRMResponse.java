package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListBRMResponse {
    @SerializedName("code")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("result")
    @Expose
    private List<InfoBRM> brmList;

    public ListBRMResponse(){

    }

    public List<InfoBRM> getBrmList() {
        return brmList;
    }

    public int getStatuscode() {
        return statuscode;
    }

    public String getMessage() {
        return message;
    }
}
