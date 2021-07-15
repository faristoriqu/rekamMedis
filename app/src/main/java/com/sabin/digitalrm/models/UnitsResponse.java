package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UnitsResponse {

    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String msg;

    @SerializedName("units")
    @Expose
    private List<Units> units;

    public UnitsResponse(){

    }

    public UnitsResponse(int statuscode, String msg, List<Units> units){
        this.statuscode = statuscode;
        this.msg = msg;
        this.units = units;
    }

    public void setStatuscode(int statuscode) {
        this.statuscode = statuscode;
    }

    public int getStatuscode() {
        return statuscode;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setUnits(List<Units> units) {
        this.units = units;
    }

    public List<Units> getUnits() {
        return units;
    }
}
