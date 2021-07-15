package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FieldList {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("field")
    @Expose
    private String field;

    @SerializedName("field_alias")
    @Expose
    private String fieldAlias;

    public FieldList(){}

    public int getId() {
        return id;
    }

    public String getField() {
        return field;
    }

    public String getFieldAlias() {
        return fieldAlias;
    }
}
