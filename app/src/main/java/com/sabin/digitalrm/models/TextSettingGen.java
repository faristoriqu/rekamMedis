package com.sabin.digitalrm.models;

import android.graphics.RectF;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by xkill on 27/11/18.
 */

public class TextSettingGen {
    @SerializedName("font_size")
    @Expose
    private Integer fontSize;

    @SerializedName("font_style")
    @Expose
    private Integer fontStyle;

    @SerializedName("position")
    @Expose
    private String position;

    public TextSettingGen() {
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public Integer getFontStyle() {
        return fontStyle;
    }

    public String getPosition() {
        return position;
    }

    public Float getPosX(){
        return Float.valueOf(position.split(",")[0]);
    }

    public Float getPosY(){
        return Float.valueOf(position.split(",")[1]);
    }

    public RectF getPos(){
        String pos [] = position.split(",");

        float x1 = Float.valueOf(pos[0]);
        float y1 = Float.valueOf(pos[1]);
        float x2 = Float.valueOf(pos[2]);
        float y2 = Float.valueOf(pos[3]);

        return new RectF(x1, y1, x2, y2);
    }
}
