package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by xkill on 30/10/18.
 */

public class InfoPasien {
    @SerializedName("norm")
    @Expose
    private String noBrm;

    @SerializedName("nama_pasien")
    @Expose
    private String namaPasien;

    @SerializedName("tempat_lahir")
    @Expose
    private String tempatLahir;

    @SerializedName("tanggal_lahir")
    @Expose
    private String tanggalLahir;

    @SerializedName("alamat")
    @Expose
    private String alamat;

    @SerializedName("jenis_kelamin")
    @Expose
    private String jenisKelamin;

    @SerializedName("agama")
    @Expose
    private String agama;

    @SerializedName("hp_pasien")
    @Expose
    private String hpPasien;

    @SerializedName("status_perkawinan")
    @Expose
    private String statusKawin;

    @SerializedName("pendidikan")
    @Expose
    private String pendidikan;

    @SerializedName("pekerjaan")
    @Expose
    private String pekerjaan;

    @SerializedName("suku_bangsa_bahasa")
    @Expose
    private String sukuBB;

    @SerializedName("data_alergi")
    @Expose
    private String dataAlergi;

    @SerializedName("brm_lama")
    @Expose
    private String brm_lama;

    @SerializedName("created_at")
    @Expose
    private String createdAt;

    @SerializedName("asal_layanan")
    @Expose
    private String asl;

    @SerializedName("dmrs")
    @Expose
    private List<DMRPatient> dmrPatientList;

    public InfoPasien() {
    }

    public String getNamaPasien() {
        return namaPasien;
    }

    public String getNoBrm() {
        return noBrm;
    }

    public String getAlamat() {
        return alamat;
    }

    public String getDataAlergi() {
        return dataAlergi;
    }

    public String getBrm_lama() {
        return brm_lama;
    }

    public String getTanggalLahir() {
        return tanggalLahir;
    }

    public String getTempatLahir() {
        if(tempatLahir == null)
            return "N/A";
        return tempatLahir;
    }

    public String getCreatedAt() {
        if(createdAt == null)
            return "N/A";
        return createdAt;
    }

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public String getAgama() {
        return agama;
    }

    public String getHpPasien() {
        return hpPasien;
    }

    public String getStatusKawin() {
        return statusKawin;
    }

    public String getPendidikan() {
        return pendidikan;
    }

    public String getPekerjaan() {
        return pekerjaan;
    }

    public String getSukuBB() {
        return sukuBB;
    }

    public String getAsl() {
        return asl;
    }

    public List<DMRPatient> getDmrPatientList() {
        return dmrPatientList;
    }
}
