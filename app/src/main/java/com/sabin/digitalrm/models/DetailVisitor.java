package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xkill on 23/10/18.
 */

public class DetailVisitor {
    public static final int DMR_NOT_CHECKED = 0;
    public static final int DMR_CHECKED_AND_INCOMPLETE = 1;
    public static final int DMR_REVIEWED_BY_DOCTOR = 2;
    public static final int DMR_OK = 3;


    public static final int REJECTED_NOTE_NOT_FIXED = 0;
    public static final int REJECTED_NOTE_OK = 1;

    public static final int VISITOR_DMR_ALL = -2;
    public static final int VISITOR_DMR_VISIT = 1;
    public static final int VISITOR_DMR_PASIVE = 2;
    public static final int VISITOR_DMR_ACTIVE = 3;
    public static final int VISITOR_DMR_HANDLING = 4;
    public static final int VISITOR_DMR_ANALITYC = 5;
    public static final int VISITOR_DMR_CODING = 6;
    public static final int VISITOR_DMR_CODED = 7;

    public static final String SRV_RI = "Rawat Inap";
    public static final String SRV_RJ = "Rawat Jalan";
    public static final String SRV_IGD = "Rawat Darurat";

    public static final int SRV_CODE_RJ = 1;
    public static final int SRV_CODE_RI = 2;
    public static final int SRV_CODE_IGD = 3;

    public static final String [] BRM_STATUS_NAME  = {
            "Semua", "Waiting to distribute",
            "Waiting to distribute", "Belum Ditangani",
            "Sedang Ditangani", "Pemeriksaan Kelengkapan",
            "Coding ICD"
    };

    private boolean statusOpen;

    @SerializedName("srv_id")
    @Expose
    private Integer srvId;

    @SerializedName("visit_id")
    @Expose
    private Integer idVisit;

    @SerializedName("status")
    @Expose
    private Integer status;

    @SerializedName("nama_penjamin")
    @Expose
    private String namaPenjamin;

    @SerializedName("hp_penjamin")
    @Expose
    private String hpPenjamin;

    @SerializedName("alamat_penjamin")
    @Expose
    private String alamatPenjamin;

    @SerializedName("id_unit")
    @Expose
    private Integer idUnit;

    @SerializedName("unit_name")
    @Expose
    private String unitName;

    @SerializedName("id_berkas")
    @Expose
    private Integer idBerkas;

    @SerializedName("unit_cat")
    @Expose
    private Integer unitCat;

    @SerializedName("keterangan")
    @Expose
    private String keterangan;

    @SerializedName("asal_layanan")
    @Expose
    private String asalLayanan;

    @SerializedName("tanggal_srv")
    @Expose
    private String tanggalSrv;

    @SerializedName("is_new_visit")
    @Expose
    private Integer isNewVisit;

    @SerializedName("is_dmr_ok")
    @Expose
    private Integer isDmrOK;

    @SerializedName("active_at")
    @Expose
    private String createdAt;

    @SerializedName("handled_at")
    @Expose
    private String takenAt;

    @SerializedName("handle_finish_at")
    @Expose
    private String submitAt;

    @SerializedName("handled_by")
    @Expose
    private String uid;

    @SerializedName("srv_type")
    @Expose
    private String srv_type;


    @SerializedName("patient")
    private InfoPasien infoPasien;

    @SerializedName("dokter")
    private InfoDokter infoDokter;

    public DetailVisitor() {
    }


    public boolean isStatusOpen() {
        return statusOpen;
    }

    public Integer getSrvId() {
        return srvId;
    }

    public Integer getIdVisit() {
        return idVisit;
    }

    public Integer getStatus() {
        return status;
    }

    public String getNamaPenjamin() {
        if(namaPenjamin == null)
            return "N/A";
        return namaPenjamin;
    }

    public String getHpPenjamin() {
        if(hpPenjamin == null)
            return "N/A";
        return hpPenjamin;
    }

    public String getAlamatPenjamin() {
        if(alamatPenjamin == null)
            return "N/A";
        return alamatPenjamin;
    }

    public Integer getIdUnit() {
        return idUnit;
    }

    public Integer getUnitCat() {
        return unitCat;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public String getAsalLayanan() {
        return asalLayanan;
    }

    public String getTanggalSrv() {
        return tanggalSrv;
    }

    public String getUnitName() {
        return unitName;
    }

    public Integer getIsNewVisit() {
        return isNewVisit;
    }

    public InfoPasien getInfoPasien() {
        return infoPasien;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setIsDmrOK(Integer isDmrOK) {
        this.isDmrOK = isDmrOK;
    }

    public String getCreatedAt() {
        if(createdAt == null)
            return "N/A";
        return createdAt;
    }

    public String getTakenAt() {
        if(takenAt == null)
            return "N/A";

        return takenAt;
    }

    public String getSubmitAt() {
        if(submitAt == null)
            return "N/A";

        return submitAt;
    }

    public String getUid() {
        return uid;
    }

    public InfoDokter getInfoDokter() {
        return infoDokter;
    }

    public Integer getIsDmrOK() {
        return isDmrOK;
    }

    public Integer getIdBerkas() {
        return idBerkas;
    }

    public void setIdBerkas(Integer idBerkas) {
        this.idBerkas = idBerkas;
    }
}