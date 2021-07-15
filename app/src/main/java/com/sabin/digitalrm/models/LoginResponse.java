package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginResponse {
    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("result")
    @Expose
    private boolean result;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("akses")
    @Expose
    private int akses;

    @SerializedName("unit")
    @Expose
    private List<Units> unit;


    public LoginResponse(){

    }

    public LoginResponse(int statuscode, String message, boolean result, String id, int akses, List<Units> unit){
        this.statuscode = statuscode;
        this.message = message;
        this.result = result;
        this.id = id;
        this.akses = akses;
        this.unit = unit;
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

    public void setResult(boolean result) {
        this.result = result;
    }

    public boolean getResult(){
        return result;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAkses(int akses) {
        this.akses = akses;
    }

    public int getAkses() {
        return akses;
    }

    public void setUnit(List<Units> unit) {
        this.unit = unit;
    }

    public List<Units> getUnit() {
        return unit;
    }
}
