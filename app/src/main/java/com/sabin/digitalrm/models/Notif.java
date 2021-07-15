package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Notif {
    @SerializedName("notif_id")
    @Expose
    private int id;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("body")
    @Expose
    private String body;

    @SerializedName("type")
    @Expose
    private int type;

    @SerializedName("sender_id")
    @Expose
    private int sender;

    @SerializedName("receiver_id")
    @Expose
    private int receiver;

    @SerializedName("send_to")
    @Expose
    private int to;

    @SerializedName("receive_from")
    @Expose
    private int from;

    @SerializedName("date")
    @Expose
    private String time;

    @SerializedName("is_read")
    @Expose
    private String read;

    public Notif() {

    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public int getType() {
        return type;
    }

    public int getSender() {
        return sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public int getTo() {
        return to;
    }

    public int getFrom() {
        return from;
    }

    public String getTime(){
        return time;
    }

    public String getRead() {
        return read;
    }
}
