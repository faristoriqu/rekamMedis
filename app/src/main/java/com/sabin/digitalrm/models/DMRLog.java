package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by abudawud on 13/06/19.
 */

public class DMRLog {
    public static final int
        ACTION_EXPORT_ID        = 1,
        ACTION_DISTRIBUTE_ID    = 2,
        ACTION_HANDLING_ID      = 3,
        ACTION_HANDLED_ID       = 4,
        ACTION_INCOMPLETE_ID    = 5,
        ACTION_COMPLETED_ID     = 6,
        ACTION_CODING_ID        = 7,
        ACTION_CLOSED_ID        = 8;

    public DMRLog() {

    }

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("description")
    @Expose
    private String desc;

    @SerializedName("datetime")
    @Expose
    private String dateTime;

    @SerializedName("action")
    @Expose
    private String action;

    @SerializedName("person_name")
    @Expose
    private String personName;

    public int getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getAction() {
        return action;
    }

    public String getPersonName() {
        return personName;
    }
}
