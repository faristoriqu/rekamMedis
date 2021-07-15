package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;

import java.util.List;

public class DetailBlankoResponse {

    @Expose
    private List<DetailBlanko> detail;

    public List<DetailBlanko> getDetail() {
        return detail;
    }
}
