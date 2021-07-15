package com.sabin.digitalrm.interfaces;

import com.sabin.digitalrm.models.AnalyticNote;
import com.sabin.digitalrm.models.ApiResponse;
import com.sabin.digitalrm.models.ApiStatus;
import com.sabin.digitalrm.models.BookmarkResponse;
import com.sabin.digitalrm.models.DMK;
import com.sabin.digitalrm.models.DMKBerkas;
import com.sabin.digitalrm.models.DMKBlanko;
import com.sabin.digitalrm.models.DMRExport;
import com.sabin.digitalrm.models.DMRLog;
import com.sabin.digitalrm.models.DMRPatient;
import com.sabin.digitalrm.models.DetailBlanko;
import com.sabin.digitalrm.models.DetailVisitor;
import com.sabin.digitalrm.models.EHOSUnit;
import com.sabin.digitalrm.models.FieldList;
import com.sabin.digitalrm.models.GenTextBlanko;
import com.sabin.digitalrm.models.GenTextSetting;
import com.sabin.digitalrm.models.InfoBRMV2;
import com.sabin.digitalrm.models.InfoPasien;
import com.sabin.digitalrm.models.InfoPoliResponse;
import com.sabin.digitalrm.models.ListBRMAktifResponse;
import com.sabin.digitalrm.models.ListBRMResponse;
import com.sabin.digitalrm.models.ListInfoPoliResponse;
import com.sabin.digitalrm.models.BRMDetailResponse;
import com.sabin.digitalrm.models.ListVisitorTypeResponse;
import com.sabin.digitalrm.models.ListVisitorsResponse;
import com.sabin.digitalrm.models.LoginResponse;
import com.sabin.digitalrm.models.NotifResponse;
import com.sabin.digitalrm.models.OldBRM;
import com.sabin.digitalrm.models.ProgressRequestBody;
import com.sabin.digitalrm.models.RejectedNote;
import com.sabin.digitalrm.models.UnitCategory;
import com.sabin.digitalrm.models.UnitVersionResponse;
import com.sabin.digitalrm.models.UnitsResponse;
import com.sabin.digitalrm.models.VersionsBlanko;
import com.sabin.digitalrm.models.VisitUnit;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface APIService {

    @FormUrlEncoded
    @PATCH("patient_visits/{id}")
    Call<ApiStatus> resetStatusBRM(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer id_visit,
            @Field("status") Integer status,
            @Field("handled_by") Integer user,
            @Field("handled_at") String handleDatestamp,
            @Field("handled_finish_at") String finishDatestamp);

    @FormUrlEncoded
    @PATCH("patient_visits/{id}")
    Call<ApiStatus> setBRMRejectStatus(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer id_visit,
            @Field("is_dmr_ok") Integer status);

    @FormUrlEncoded
    @PATCH("patient_visits/{id}")
    Call<ApiStatus> setStatusBRMOnHandle(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer id_visit,
            @Field("status") Integer status,
            @Field("handled_by") Integer user,
            @Field("handled_at") String handleDatestamp);

    @GET("dmrs/{id}/rnotes")
    Call<List<RejectedNote>> getRejectedNotes(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer id_berkas);

    @FormUrlEncoded
    @PATCH("patient_visits/{id}")
    Call<ApiStatus> setStatusBRMOnFinishHandle(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer id_visit,
            @Field("status") Integer status,
            @Field("handled_finish_at") String finishDatestamp);

//     @FormUrlEncoded
//     @POST("auth.api.php")
//     Call<LoginResponse> login(
//             @Field("username") String username,
//             @Field("password") String password
//     );

//     @FormUrlEncoded
//     @POST("auth.api.php")
//     Call<UnitsResponse> getUserUnits(
//             @Field("uid") String uid
//     );
//     TODO:: dibawah ini untuk local

    @FormUrlEncoded
    @POST("auth/login")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password

    );


    @GET("auth/units/{id}")
    Call<UnitsResponse> getUserUnits(
            @Path("id") String uid
    );

    @GET("unit_visits/{id}/patients")
    Call <List<InfoBRMV2>> getListBRM(
            @Header("X-Auth-Token") String uid,
            @Path("id") int unit,
            @Query("penanganan") int penanganan
    );

//     @FormUrlEncoded
//     @POST("ep_brm_list.php?action=listBRM")
//     Call <ListBRMResponse> getListBRM(
//             @Field("uid") String uid,
//             @Query("list") String query
//     );

    @FormUrlEncoded
    @POST("ep_brm_list.php?action=update")
    Call <ApiResponse> updateListBRM(
            @Field("uid") String uid,
            @Field("no_brm") String brm,
            @Field("id_poli") String poli,
            @Field("status") String status
    );

//    @FormUrlEncoded
//    @POST("ep_brm_detail.php")
//    Call <BRMDetailResponse> getDetailBRM(
//            @Field("no_brm") String brm,
//            @Field("uid") String uid
//    );

    @GET("brms/details/{id}")
    Call <BRMDetailResponse> getDetailBRM(
            @Header("X-Auth-Token") String uid,
            @Path("id") String brm
    );

    @FormUrlEncoded
    @Streaming
    @POST("ep_poli_download.php")
    Call <ResponseBody> downloadBRM(
            @Field("uid") String uid,
            @Field("no_brm") String brm,
            @Field("poli") String poli
    );

    @Streaming
    @GET("download/dmrs/{id}")
    Call <ResponseBody> downloadBRMDokter(
            @Header("X-Auth-Token") String uid,
            @Path("id") Integer idBerkas
    );

    @FormUrlEncoded
    @Streaming
    @POST("pdf_brm_download.php")
    Call <ResponseBody> downloadPDF(
            @Field("uid") String uid,
            @Field("no_brm") String brm
    );

    @Streaming
    @GET("download/odmrs/{id}")
    Call <ResponseBody> downloadOldBRMPDF(
            @Header("X-Auth-Token") String uid,
            @Path("id") Integer id
    );

    @FormUrlEncoded
    @POST("ep_list_visitor_type.php?type=brm")
    Call <ListVisitorTypeResponse> getListVisitTypebyBRM(
            @Field("uid") String uid
    );

//    @FormUrlEncoded
//    @POST("ep_blanko_poli.php?op=list")
//    Call <ListInfoPoliResponse> getListBlanko(
//            @Field("uid") String uid
//    );

    @GET("blankos/poli")
    Call <ListInfoPoliResponse> getListBlanko(
            @Header("X-Auth-Token") String uid
    );

//    @FormUrlEncoded
//    @POST("ep_blanko_poli.php?op=create")
//    Call <InfoPoliResponse> createBlanko(
//            @Field("uid") String uid,
//            @Field("nama_poli") String poli
//    );

    @FormUrlEncoded
    @POST("blankos/poli/create")
    Call <InfoPoliResponse> createBlanko(
            @Header("X-AUth-Token") String uid,
            @Field("nama_poli") String poli
    );

    @FormUrlEncoded
    @POST("ep_brm_list.php?action=search")
    Call <ListBRMResponse> searchBRM(
            @Field("uid") String uid,
            @Query("keyword") String keyword
    );

    @GET("version.php?action=check")
    Call <ResponseBody> checkVersion();

    @FormUrlEncoded
    @Streaming
    @POST("version.php?action=update")
    Call <ResponseBody> updateClient(
            @Field("uid") String uid
    );

    @GET("notifikasi.php")
    Call <NotifResponse> getNotif(
            @Query("uid") int uid,
            @Query("pid") int pid
    );

    @GET("unit_visits/{id}/patients")
    Call <List<InfoBRMV2>> getNotifV2(
            @Header("X-Auth-Token") int uid,
            @Path("id") int unit_id,
            @Query("notif") int isNotif
    );

//    @GET("bookmark.php")
//    Call <BookmarkResponse> getBookmark(
//            @Query("uid") String uid,
//            @Query("brm") String brm
//    );

    @GET("bookmarks/{id}")
    Call <BookmarkResponse> getBookmark(
            @Header("X-Auth-Token") String uid,
            @Path("id") String brm
    );

    @GET("gentext/s")
    Call <List<FieldList>> getGenTextList(
            @Header("X-Auth-Token") String token
    );

    @GET("gentext/blankos/{bid}/srvs/{sid}")
    Call <List<GenTextBlanko>> getGentextBlankoList(
            @Path("bid") int idBlanko,
            @Path("sid") int idSrv,
            @Header("X-Auth-Token") String token,
            @Query("undmk") String undmk
    );

    @GET("services/versions")
    Call <UnitVersionResponse> getUnitVersion(
            @Header("X-Auth-Token") int uid
    );

    @GET("dmk/versions/{id}/dmks")
    Call <List<DMK>> getDmks(
            @Path("id") Integer idDmkVersion,
            @Header("X-Auth-Token") String token
    );

    @FormUrlEncoded
    @POST("dmk/versions/{id}/dmks")
    Call <DMK> addDmk(
            @Path("id") Integer idDmkVersion,
            @Header("X-Auth-Token") String token,
            @Field("code") String dmkCode,
            @Field("name") String dmkName,
            @Field("total_page") Integer totalPage
    );

    @FormUrlEncoded
    @POST("dmk/versions")
    Call <VersionsBlanko> addDmkVersion(
            @Header("X-Auth-Token") String token,
            @Field("is_default") Boolean isDefault,
            @Field("name") String versionName,
            @Field("version") String version
    );

    @DELETE("dmk/s/{id}")
    Call<ApiStatus> deleteDMK(
            @Path("id") Integer idDMK,
            @Header("X-Auth-Token") String token
    );

    @FormUrlEncoded
    @PATCH("dmk/versions/{id}")
    Call<ApiStatus> updateDMKVer(
        @Header("X-Auth-Token") String token,
        @Path("id") Integer idDmkVersion,
        @Field("version")String version,
        @Field("name")String name
    );

    @DELETE("dmk/versions/{id}")
    Call<ApiStatus> deleteDMKVer(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer idDMKVer
    );

    @DELETE("services/{id}")
    Call <ApiStatus> deleteUnitCat(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer categoryId
    );

    @FormUrlEncoded
    @PATCH("dmk/s/{id}")
    Call <ApiStatus> updateDMKName(
            @Path("id") Integer idDmkVersion,
            @Header("X-Auth-Token") String token,
            @Field("name")String name
    );

    @POST("dmk/s/{id}/coords")
    Call <ApiStatus> addCoordsInDMK(
            @Path("id") int idDMK,
            @Header("X-Auth-Token") String token,
            @Body RequestBody json
    );

    @FormUrlEncoded
    @PATCH("upload/dmks/{id}")
    Call<ApiStatus> uploadDMK(
            @Path("id") Integer idDmk,
            @Header("X-Auth-Token") String token,
            @Header("X-Total-Pages") Integer pages
    );

//    @PUT("upload/dmks/{id}")
//    Call<ApiStatus> uploadDMK(
//            @Path("id") Integer idDmk,
//            @Header("X-Auth-Token") String token,
//            @Header("X-File-Length") Integer length,
//            @Header("X-Total-Pages") Integer pages,
//            @Body RequestBody dmk
//    );

    @PUT("upload/dmrs/{id}")
    Call<ApiStatus> uploadDMR(
            @Path("id") Integer idDMR,
            @Header("X-Auth-Token") String token,
            @Header("X-File-Length") Integer length,
            @Body RequestBody dmr
    );

    @PUT("upload/dmrs/zip/{id}")
    Call<ApiStatus> uploadDMRZIP(
            @Path("id") Integer idDMR,
            @Header("X-Auth-Token") String token,
            @Header("X-File-Length") Integer length,
            @Query("is_zip") Integer isPDF,
            @Body RequestBody dmr
    );

    @PUT("upload/dmrs/{id}")
    Call<ApiStatus> uploadDMRPDF(
            @Path("id") Integer idDMR,
            @Header("X-Auth-Token") String token,
            @Header("X-File-Length") Integer length,
            @Query("is_pdf") Integer isPDF,
            @Body RequestBody dmr
    );

    @PATCH("dmrs/checkout/{id}")
    Call<ApiStatus> checkoutDMR(
            @Path("id") Integer idDMR, 
            @Header("X-Auth-Token") String token
    );

    @PATCH("dmrs/checkout")
    Call<ApiStatus> checkoutDMRs(
            @Header("X-Auth-Token") String token,
            @Field("json_data") String data //JsonArray
            /*
                [JSON_DATA FORMAT EXAMPLE]
                [
                        {
                                "id" : 1,
                                "is_zip" : 1, //true
                                "is_pdf" : 0 //false
                        },
                        {
                                "id" : 2,
                                "is_zip" : 0,
                                "is_pdf" : 1
                        }
                ]
            */
    );

    @FormUrlEncoded
    @POST("dmrs/")
    Call<DMRPatient> addDMR(
        @Header("X-Auth-Token") String token,
        @Field("norm") String noRM,
        @Field("name") String dmrName,
        @Field("id_unit_cat") Integer unitCat,
        @Field("id_unit") Integer idUnit
    );

    @POST("dmrs/{id}/dmks")
    Call<ApiStatus> addDMKsInDMR(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer idBrm,
            @Body RequestBody JSONdmks
    );

    @GET("dmrs/{id}/dmks")
    Call<List<DMKBerkas>> getDMKsInDMR(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer idDMR
    );

    @FormUrlEncoded
    @POST("dmrs/{id}/rnotes")
    Call<AnalyticNote> addRNoteInDMR(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer idDMR,
            @Field("page") Integer page,
            @Field("note") String note
    );

    @FormUrlEncoded
    @POST("dmrs/{id}/export")
    Call<DMRExport> exportDMR(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer idDMR,
            @Field("page") String page,
            @Field("export_name") String exportName
    );

    @FormUrlEncoded
    @PATCH("dmrs/rnotes/{id}")
    Call<ApiStatus> updateNoteRNoteInDMR(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer idRNote,
            @Field("note") String note,
            @Field("status") Integer status
    );

    @FormUrlEncoded
    @PATCH("dmrs/rnotes")
    Call<ApiStatus> updateNoteRNoteStatusWithArray(
            @Header("X-Auth-Token") String token,
            @Query("query") Boolean isQuery,
            @Field("id_notes") String idRNote,
            @Field("status") Integer status
    );

    @DELETE("dmrs/rnotes/{id}")
    Call<ApiStatus> deleteRNoteInDMR(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer idRNote
    );

    @GET("dmrs/{id}/rnotes")
    Call<List<AnalyticNote>> getRNotesInDMR(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer idDMR,
            @Query("page") Integer page,
            @Query("status") Integer status
    );

    @GET("dmrs/{id}/rnotes")
    Call<List<AnalyticNote>> getRNotesInDmrByStatusRange(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer idDMR,
            @Query("min_status") Integer minStatus,
            @Query("max_status") Integer maxStatus
    );

    @GET("patients/{norm}/dmrs")
    Call<InfoPasien> getDMRsPatient(
            @Header("X-Auth-Token") String token,
            @Path("norm") String norm,
            @Query("type") String type
    );

    @GET("patients")
    Call<List<InfoPasien>> getPatients(
            @Header("X-Auth-Token") String token,
            @Query("filter") String filter
    );

    @Streaming
    @GET("download/dmks/{id}")
    Call<ResponseBody> downloadDMK(
            @Path("id") Integer idDmk,
            @Header("X-Auth-Token") String token
    );

    @Streaming
    @GET("download/dmrs/{id}")
    Call<ResponseBody> downloadDMR(
            @Path("id") Integer idDMR,
            @Header("X-Auth-Token") String token
    );

    @Streaming
    @GET("download/dmrs/zip/{id}")
    Call<ResponseBody> downloadDMRZip(
            @Path("id") Integer idDMR,
            @Header("X-Auth-Token") String token
    );

    @Streaming
    @GET("download/blankos/{id}")
    Call<ResponseBody> downloadBlanko(
            @Path("id") Integer idBlanko,
            @Header("X-Auth-Token") String token
    );

    @PUT("upload/exports/")
    Call<ApiStatus> uploadExport(
            @Header("X-Auth-Token") String token,
            @Header("X-MD5-File") String md5,
            @Header("X-Filename") String filename,
            @Header("X-No-RM") Integer rm,
            @Header("X-Pages") Integer length,
            @Body RequestBody dmk
    );

    @GET("services/{id}/blankos/dmks")
    Call<List<DetailBlanko>>getBlankoDetail(
            @Path("id") Integer id_serv,
            @Header("X-Auth-Token") String token
    );



    @GET("services/{id}/blankos")
    Call<List<DetailBlanko>>getBlankoDetailV2(
            @Path("id") Integer id_serv,
            @Header("X-Auth-Token") String token
    );

    @GET("blankos/{id}/dmks")
    Call<List<DMKBlanko>>getDMKinBlanko(
            @Path("id") Integer idBlanko,
            @Header("X-Auth-Token") String token,
            @Query("undmk") String undmk
    );

    @GET("dmk/{id}/versions")
    Call<List<DMKBlanko>>getDMKbyVersion(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer idVer
    );

    @GET("ehos/unit_categories")
    Call<List<UnitCategory>>getEhosUnitCategories(
            @Header("X-Auth-Token") String token
    );

    @POST("patient_visits/sync")
    Call <ApiStatus> syncPatientVisits(
            @Header("X-Auth-Token") String token
    );

    @FormUrlEncoded
    @POST("services/?")
    Call <ApiStatus> addNewUnitCat(
            @Header("X-Auth-Token") String token,
            @Field("id") String id,
            @Field("name") String name
    );

    @FormUrlEncoded
    @PATCH("services/{id}")
    Call <ApiStatus> updateUnitCat(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer unitId,
            @Field("id") Integer unitIdNew,
            @Field("name") String name
    );

    @FormUrlEncoded
    @POST("blankos/?")
    Call <ApiStatus> addNewBlanko(
            @Header("X-Auth-Token") String token,
            @Field("id_unit_cat") Integer idUnitCat,
            @Field("name") String name,
            @Field("status") Integer status,
            @Field("dmr_name") String dmrName
    );

    @FormUrlEncoded
    @PATCH("blankos/{id}")
    Call <ApiStatus> editBlanko(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer blankoId,
            @Field("name") String name
    );

    @DELETE("blankos/{id}")
    Call <ApiStatus> deleteBlanko(
            @Header("X-Auth-Token") String token,
            @Path("id") Integer blankoId
    );

    @FormUrlEncoded
    @PATCH("patient_visits/{id}")
    Call <ApiStatus> activatePatientVisit(
            @Path("id") Integer srvId,
            @Header("X-Auth-Token") String token,
            @Field("status") Integer status,
            @Field("activated_by") Integer idPRM,
            @Field("id_berkas") Integer idBerkas
    );

    @FormUrlEncoded
    @PATCH("patient_visits/{id}")
    Call <ApiStatus> updateDMROK(
            @Path("id") Integer srvId,
            @Header("X-Auth-Token") String token,
            @Field("status") Integer status,
            @Field("is_dmr_ok") Integer isDmrOK
    );

    @FormUrlEncoded
    @PATCH("patient_visits/{id}")
    Call <ApiStatus> setDMRtoOnCoding(
            @Path("id") Integer srvId,
            @Header("X-Auth-Token") String token,
            @Field("status") Integer status,
            @Field("is_dmr_ok") Integer isDmrOK
    );

    @FormUrlEncoded
    @PATCH("patient_visits/{id}")
    Call <ApiStatus> closeDMR(
            @Path("id") Integer srvId,
            @Header("X-Auth-Token") String token,
            @Field("status") Integer status,
            @Field("is_dmr_ok") Integer isDmrOK
    );

    @GET("unit_visits")
    Call <List<VisitUnit>> getVisitUnits(
            @Header("X-Auth-Token") String token
    );

    @GET("patient_visits")
    Call <List<DetailVisitor>> getAllPatientVisits(
            @Header("X-Auth-Token") String token,
            @Query("status") int status,
            @Query("unit") int unit,
            @Query("s_time") String start,
            @Query("e_time") String end,
            @Query("offset") int offset,
            @Query("length") int length,
            @Query("query") int query
    );

    @GET("unit_visits/{id}/patients")
    Call <List<DetailVisitor>> getVisitsByUnit(
            @Path("id") Integer idUnit,
            @Header("X-Auth-Token") String token
    );

    @FormUrlEncoded
    @POST("blankos/{id}/dmks")
    Call<ApiStatus> setDMKinBlanko(
            @Header("X-Auth-Token") String token,
            @Path("id") int blanko_id,
            @Field("id_dmk") int dmk_id
    );

    @DELETE("blankos/{id}/dmks/{id2}")
    Call<ApiStatus> deleteBlankoDetail(
            @Header("X-Auth-Token") String token,
            @Path("id") int idBl,
            @Path("id2") int idDmk
    );

    @GET("patients/{id}/odmrs")
    Call<List<OldBRM>> getAllOBRM(
            @Header("X-Auth-Token") String token,
            @Path("id") int norm
    );

    @GET("ehos/dmr_units")
    Call<List<EHOSUnit>> getUnitsInDMR(
            @Header("X-Auth-Token") String token
    );

    @GET("ehos/unit_categories/{id}/units")
    Call<List<EHOSUnit>> getUnitsByCat(
            @Header("X-Auth-Token") String token,
            @Path("id") int catID
    );

    @GET("dmrs/{id}/logs")
    Call<List<DMRLog>> getLogsInDMR(
            @Header("X-Auth-Token") String token,
            @Path("id") int idDMR
    );

    @FormUrlEncoded
    @POST("dmrs/{id}/logs")
    Call<ApiStatus> addLogInDMR(
            @Header("X-Auth-Token") String token,
            @Path("id") int idDMR,
            @Field("id_action") int idAction,
            @Field("desc") String description
    );
}
