package com.sabin.digitalrm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by xkill on 09/11/18.
 */

public class DetailBRMAktif {
    public static final int BRM_ALL_CODE = 0;
    public static final int BRM_OPEN_CODE = 1;  // Aktif
    public static final int BRM_ACTIVE_CODE = 2; // Sedang ditangani
    public static final int BRM_CODING_CODE = 3; // selesai digangani
    public static final int BRM_CODING_FIN = 4; // selesai coding
    private final String NA_RET = "N/A";

    @SerializedName("no_brm")
    @Expose
    private String noBrm;

    @SerializedName("id_poli")
    @Expose
    private Integer idPoli;

    @SerializedName("status")
    @Expose
    private Integer status;

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

    @SerializedName("info_pasien")
    @Expose
    private InfoPasien infoPasien;

    @SerializedName("info_dokter")
    @Expose
    private InfoDokter infoDokter;

    @SerializedName("info_kunjungan")
    @Expose
    private InfoVisitor infoVisitor;

    public DetailBRMAktif() {
    }

    public String getNoBrm() {
        return noBrm;
    }

    public Integer getIdPoli() {
        return idPoli;
    }

    public Integer getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getTakenAt() {
        return takenAt == null ? NA_RET : takenAt;
    }

    public String getSubmitAt() {
        return submitAt == null ? NA_RET : submitAt;
    }

    public String getUid() {
        return uid;
    }

    public InfoPasien getInfoPasien() {
        return infoPasien;
    }

    public InfoDokter getInfoDokter() {
        return infoDokter;
    }

    public InfoVisitor getInfoVisitor() {
        return infoVisitor;
    }
}
