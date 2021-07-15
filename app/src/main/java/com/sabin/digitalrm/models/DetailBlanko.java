package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sysdata.widget.accordion.Item;

import java.util.List;

public class DetailBlanko extends Item{

    private String mName;
    private int mID;
    private List<DMKBlanko> mDMKs;

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("id_unit_cat")
    @Expose
    private int id_unit_c;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("status")
    @Expose
    private int status;

    @SerializedName("checksum")
    @Expose
    private String checksum;

    @SerializedName("last_update")
    @Expose
    private String updated;

    @SerializedName("dmr_name")
    @Expose
    private String dmr;

    @SerializedName("dmks")
    @Expose
    private List<DMKBlanko> dmks;


    public DetailBlanko() {
    }


    public int getId() {
        return id;
    }

    public int getId_unit_c() {
        return id_unit_c;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getUpdated() {
        return updated;
    }

    public String getDmr() {
        return dmr;
    }

    public List<DMKBlanko> getDmks() {
        return dmks;
    }

    public static DetailBlanko create(int idBl, String name, List<DMKBlanko> dmk) {
        return new DetailBlanko(idBl, name, dmk);
    }

    private DetailBlanko(int id, String name, List<DMKBlanko> dmk) {
        mID = id;
        mName = name;
        mDMKs = dmk;
    }

    public int getIdBl() {
        return mID;
    }

    public String getLName() {
        return mName;
    }

    public List<DMKBlanko> getLDMK() {
        return mDMKs;
    }

    @Override
    public int getUniqueId() {
        return hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DetailBlanko that = (DetailBlanko) o;

        return mID!=0 && mName.equals(that.mName) && (mDMKs != null ? mDMKs.equals(that.mDMKs) : that.mDMKs == null);
    }

    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result = 31 * mID + result + (mDMKs != null ? mDMKs.hashCode() : 0);
        return result;
    }

}
