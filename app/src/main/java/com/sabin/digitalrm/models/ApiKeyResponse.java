package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApiKeyResponse {
    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("key")
    @Expose
    private String key;

    public ApiKeyResponse(){

    }

    public ApiKeyResponse(String key){
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public int getStatuscode() {
        return statuscode;
    }
}
