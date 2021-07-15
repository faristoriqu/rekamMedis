package com.sabin.digitalrm.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Units implements Parcelable {
    @SerializedName("id_poli")
    @Expose
    private int id_poli;

    @SerializedName("nama_poli")
    @Expose
    private String nama_poli;

    public Units(){

    }

    public Units(int id_poli, String nama_poli){
        this.id_poli = id_poli;
        this.nama_poli = nama_poli;
    }

    public Units(Parcel par){
        id_poli = par.readInt();
        nama_poli = par.readString();
    }

    public void setId_poli(int id_poli) {
        this.id_poli = id_poli;
    }


    public int getId_poli() {
        return id_poli;
    }

    public void setNama_poli(String nama_poli) {
        this.nama_poli = nama_poli;
    }

    public String getNama_poli() {
        return nama_poli;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id_poli);
        parcel.writeString(nama_poli);
    }

    public static final Parcelable.Creator<Units> CREATOR = new Parcelable.Creator<Units>(){

        @Override
        public Units createFromParcel(Parcel parcel) {
            return new Units(parcel);
        }

        @Override
        public Units[] newArray(int i) {
            return new Units[i];
        }
    };
}
