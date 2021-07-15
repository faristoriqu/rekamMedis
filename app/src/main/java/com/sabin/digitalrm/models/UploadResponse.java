package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UploadResponse {
    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;


    public UploadResponse(){

    }

    public UploadResponse(int statuscode, String message){
        this.statuscode = statuscode;
        this.message = message;
    }

    public void setStatuscode(int statuscode) {
        this.statuscode = statuscode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatuscode() {
        return statuscode;
    }

    public String getMessage() {
        return message;
    }
}
