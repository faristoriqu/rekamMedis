package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by xkill on 23/10/18.
 */

public class ListVisitorsResponse {
    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("result")
    @Expose
    private List<DetailVisitor> visitorList;

    public ListVisitorsResponse() {
    }

    public int getStatuscode() {
        return statuscode;
    }

    public List<DetailVisitor> getVisitorList() {
        return visitorList;
    }

    public String getMessage() {
        return message;
    }
}
