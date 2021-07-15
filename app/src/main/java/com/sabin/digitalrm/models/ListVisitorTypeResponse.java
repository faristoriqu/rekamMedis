package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by xkill on 25/10/18.
 */

public class ListVisitorTypeResponse {
    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("result")
    @Expose
    private List<VisitorType> visitorTypeList;

    public ListVisitorTypeResponse() {
    }

    public String getMessage() {
        return message;
    }

    public int getStatuscode() {
        return statuscode;
    }

    public List<VisitorType> getVisitorTypeList() {
        return visitorTypeList;
    }
}
