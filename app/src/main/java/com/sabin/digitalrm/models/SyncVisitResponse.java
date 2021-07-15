package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SyncVisitResponse {
    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("result")
    @Expose
    private SyncVisitResult visitResult;

    public SyncVisitResponse() {

    }

    public int getStatuscode() {
        return statuscode;
    }

    public String getMessage() {
        return message;
    }

    public SyncVisitResult getVisitResult() {
        return visitResult;
    }
}
