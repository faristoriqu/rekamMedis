package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UnitVersionResponse {
    @SerializedName("units")
    @Expose
    private List<UnitsBlanko> units;

    @SerializedName("versions")
    @Expose
    private List<VersionsBlanko> versions;

    public List<UnitsBlanko> getUnits() {
        return units;
    }

    public List<VersionsBlanko> getVersions() {
        return versions;
    }
}
