package com.sabin.digitalrm.models;

import android.graphics.RectF;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xkill on 27/11/18.
 */

public class GenTextSetting {
    @SerializedName("id")
    private Integer id;

    @SerializedName("page")
    @Expose
    private Integer page;

    @SerializedName("font_size")
    @Expose
    private Integer fontSize;

    @SerializedName("font_style")
    @Expose
    private Integer fontStyle;

    @SerializedName("coord")
    @Expose
    private String coord;

    @Expose
    @SerializedName("id_dmk")
    private Integer idDmk;

    @Expose
    @SerializedName("id_field")
    private Integer idField;

    @SerializedName("field")
    private String field;

    public GenTextSetting() {

    }

    public GenTextSetting(int idDmk, int idField, int page, int fontSize, int fontStyle, String coord) {
        this.idDmk = idDmk;
        this.idField = idField;
        this.page = page;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
        this.coord = coord;
    }

    public Integer getId() {
        return id;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public Integer getFontStyle() {
        return fontStyle;
    }

    public String getCoord() {
        return coord;
    }

    public RectF getCoordPos(){
        String pos [] = coord.split(",");

        float x1 = Float.valueOf(pos[0]);
        float y1 = Float.valueOf(pos[1]);
        float x2 = Float.valueOf(pos[2]);
        float y2 = Float.valueOf(pos[3]);

        return new RectF(x1, y1, x2, y2);
    }

    public String getField() {
        return field;
    }
}
