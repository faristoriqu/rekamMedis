package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BookmarkResponse {
    @SerializedName("statuscode")
    @Expose
    private int statuscode;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("data")
    @Expose
    private List<DMKBookmark> bookmarks;

    public int getStatuscode() {
        return statuscode;
    }

    public String getMessage() {
        return message;
    }

    public List<DMKBookmark> getBookmarks() {
        return bookmarks;
    }
}
