package com.sabin.digitalrm.models;

import java.util.Map;

public class Bookmark extends Object {
    public String title;
    public String desc;
    public String time;
    public Integer page;

    public Bookmark(){

    }

    public Bookmark(int _page, String _title, String _desc, String _time){
        title = _title;
        desc = _desc;
        time = _time;
        page = _page;
    }

//    public Bookmark(Map<String, Object> bookmark){
//        title = bookmark.get("TITLE").toString();
//        desc = bookmark.get("DESC").toString();
//        timestamp = bookmark.get("TS").toString();
//        pageNo = Integer.valueOf(bookmark.get("NO").toString());
//    }
}
