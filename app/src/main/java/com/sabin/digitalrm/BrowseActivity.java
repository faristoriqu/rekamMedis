package com.sabin.digitalrm;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;

import com.sabin.digitalrm.adapters.BRMRecyclerAdapter;
import com.sabin.digitalrm.adapters.PoliListAdapter;
import com.sabin.digitalrm.helpers.Updater;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.BRMDetailResponse;
import com.sabin.digitalrm.models.BRMDetailResultData;
import com.sabin.digitalrm.models.InfoBRMV2;
import com.sabin.digitalrm.models.InfoPasien;
import com.sabin.digitalrm.models.UnitForBRMDetail;
import com.sabin.digitalrm.services.NotificationService;
import com.sabin.digitalrm.services.UpdateCheckerService;
import com.sabin.digitalrm.utils.APIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowseActivity extends BaseActivity{
    private List<InfoBRMV2> infoBRMList;
    private List<UnitForBRMDetail> unitList;

    private Context mContext;
    private APIService APIClient;
    private BroadcastReceiver receiver, notifSrvReceiver;

    private PoliListAdapter unitListAdapter;
    private BRMRecyclerAdapter brmListAdapter;

    private RecyclerView brmListView;
    private TextView txtBrm, txtNama, txtVisit, txtAlamat, txtAlergi, txtPermintaan, txtTgllahir, txtTmplahir, txtCreatedat, txtAsalLayanan;
    private TextView txtJenisKelamin, txtAgama, txtHpPasien, txtStatusPkn, txtPendidikan, txtPekerjaan, txtSBB;
    private Button btnAmbil, btnLanjutkan;

    private String uid, unit, nobrm, namaunit, brmaktif, pxName;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private RecyclerView.LayoutManager lym;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    private LinearLayout frmMaster;
    private LinearLayout frmDetail;
    private FrameLayout frmItemList;
    private FrameLayout frmItemDetail;

    private RelativeLayout btnSHDetail;
    private ImageView btnSHDetIcn;
    private TextView btnSHDetTxt;

    private Boolean isExpanded = false;
    private static int statusIntent;

    private static int recentSelectedPos = -1;
    View vwMaster, vwDetail, selectV;
    private static TextView textNotifItemCount;
    public static int mNotifItemCount = 0;
    private static int serv_id = 0, idBRM = 0;
    private static boolean saveState = false;
    private int activity_mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer_split_view);
        setSupportActionBar(findViewById(R.id.my_toolbar));

        mContext = this;

//        Intent in = new Intent(mContext, UpdateCheckerService.class);
//        startService(in);
//        startNotifService();

        vwMaster = findViewById(R.id.vwActionC);
        vwDetail = findViewById(R.id.vwDetailC);

        pref = getSharedPreferences("SESSION_", 0);
        uid = pref.getString("uid", "Anonymous");
        unit = String.valueOf(pref.getInt("id_poli", -1));
        namaunit = pref.getString("nama_poli", null);
        brmaktif = pref.getString("activeBRM", null);
        pxName = pref.getString("activePx", null);
        activity_mode = pref.getInt("mode", 0);

        Log.e("[X-DEBUG]", "id_poli: "+unit+"; nama_poli: "+namaunit);

        if(brmaktif != null){
            Log.e("~Debug", "active brm: "+brmaktif);

            Intent intent = new Intent(mContext, DoctorMainActivity.class);
            intent.putExtra("mode", activity_mode);
            intent.putExtra("serv", pref.getInt("activeServ", 0));
            intent.putExtra("id", unit);
            intent.putExtra("uid", uid);
            intent.putExtra("no_brm", pref.getString("activeBRM", null));
            intent.putExtra("nama_poli", namaunit);
            intent.putExtra("id_berkas", pref.getInt("activeBerkas", 0));
            intent.putExtra("px_name", pref.getString("activePx", null));
            startActivityForResult(intent, 101);
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle("Digital MR - "+namaunit);

//        penjamin = findViewById(R.id.penjamin);
        infoBRMList = new ArrayList<>();
        unitList = new ArrayList<>();

        setSupportActionBar(findViewById(R.id.my_toolbar));

        initUI();

        initRetrofit();
        prepareDataset();

        initListBRM();
//        initDetailBRM();
        initDetail();

        //TODO: Start notifiaction service
//        if(!isServiceRunning(NotificationService.class)) {
//            startNotifService();
//        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_explorer, menu);
        MenuItem subMenu = menu.findItem(R.id.action_exit_menu);
        MenuItem notifMenu = menu.findItem(R.id.action_notif);

        getMenuInflater().inflate(R.menu.dropdown_explorer, subMenu.getSubMenu());

        View actionView = MenuItemCompat.getActionView(notifMenu);
        textNotifItemCount = actionView.findViewById(R.id.notif_badge);

        //TODO: Set notification badge
        setupBadge();

        actionView.setOnClickListener(v -> onOptionsItemSelected(notifMenu));

        return super.onPrepareOptionsMenu(menu);
    }

    public static void setupBadge() {

        if (textNotifItemCount != null) {
            if (mNotifItemCount == 0) {
                if (textNotifItemCount.getVisibility() != View.GONE) {
                    textNotifItemCount.setVisibility(View.GONE);
                }
            } else {
                textNotifItemCount.setText(String.valueOf(Math.min(mNotifItemCount, 99)));
                if (textNotifItemCount.getVisibility() != View.VISIBLE) {
                    textNotifItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_refresh:{
                Baseprogress.showProgressDialog(mContext, "Mengambil daftar BRM...");
                item.setEnabled(false);
                item.getIcon().setAlpha(100);
                fetchBRMList(item);
                resetSelectedItem();
                break;
            }

            case R.id.action_notif:{
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                Objects.requireNonNull(notificationManager).cancel(1001);
                mNotifItemCount = 0;
                setupBadge();
                Intent i = new Intent(mContext, NotifActivity.class);
                startActivity(i);

                break;
            }

            case R.id.action_back:{
                editor = pref.edit();
                editor.putInt("akses", 0);
                editor.putInt("id_poli", -1);
                editor.putString("nama_poli", null);
                editor.putString("activeBRM", null);
                editor.putInt("activeServ", 0);
                editor.putInt("activeBerkas", 0);
                editor.putString("activePx", null);
                editor.apply();

                //TODO: Stop notif service
                if(isServiceRunning(NotificationService.class)) {
                    stopNotifService();
                }

                Intent i = new Intent(BrowseActivity.this, UnitSelectorActivity.class);
                startActivity(i);
                finish();
                break;
            }

            case R.id.action_logout:{
                new AlertDialog.Builder(mContext)
                        .setTitle("Konfirmasi Logout")
                        .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                        .setMessage("Yakin anda ingin keluar ?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            editor = pref.edit();
                            editor.putBoolean("is_loggedin", false);
                            editor.putString("uid", null);
                            editor.putInt("poli", 0);
                            editor.apply();

                            //TODO: Stop notif service
                            if(isServiceRunning(NotificationService.class)) {
                                stopNotifService();
                            }

                            Intent i = new Intent(BrowseActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                        })
                        .setNegativeButton("Tidak", null)
                        .create().show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void stopNotifService(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(notifSrvReceiver);

        Intent is = new Intent(mContext, NotificationService.class);
        stopService(is);
    }

    private void startNotifService(){
        setNotifBroadcaster();

        SharedPreferences pref2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit2 = pref2.edit();

        int notifcount = pref2.getInt("notifcount", 0);

        edit2.putString("uid", uid);
        edit2.putString("pid", unit);
        edit2.putString("poliname", namaunit);
        edit2.apply();

        Intent i = new Intent(mContext, NotificationService.class);
        startService(i);

        LocalBroadcastManager.getInstance(mContext).registerReceiver((notifSrvReceiver),
                new IntentFilter(NotificationService.BROADCAST_RESULT)
        );
    }

    public void setNotifBroadcaster() {
        notifSrvReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setupBadge();
                textNotifItemCount.findFocus();
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                Log.e("[X-DEBUG]", "Activity Result: RESULT_CANCELED");
                fetchBRMList(null);
                resetSelectedItem();
            } else if (resultCode == AppCompatActivity.RESULT_OK) {
                Log.e("[X-DEBUG]", "Activity Result: RESULT_OK");
                fetchBRMList(null);
                resetSelectedItem();
                saveState = data.getBooleanExtra("isSaved", false);

                Log.e("[X-DEBUG]", "Save value from Activity Result: " + saveState);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setBroadcaster();
        LocalBroadcastManager.getInstance(mContext).registerReceiver((receiver),
                new IntentFilter(UpdateCheckerService.RESULT)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);

        Intent in = new Intent(mContext, UpdateCheckerService.class);
        stopService(in);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);

        Intent in = new Intent(mContext, UpdateCheckerService.class);
        stopService(in);

        Intent intent = new Intent(mContext, NotificationService.class);
//        stopService(intent);
    }

    private void resetSelectedItem(){
        Log.e("[X-DEBUG]", "Recent Position: "+recentSelectedPos);
        try{
            selectV = brmListView.getChildAt(recentSelectedPos);
            selectV.setSelected(false);
        }catch (Exception ex){
            BaseActivity.Baselog.d("Exception: "+ex.getMessage());
        }

        recentSelectedPos = -1;
    }

    public void setBroadcaster() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean s = intent.getBooleanExtra(UpdateCheckerService.STATUS, false);
                long sz = intent.getLongExtra(UpdateCheckerService.SIZE, 0);
                if(s){
                    if(Updater.isDialogShown()) {
                        Updater.closeDialog();
                    }

                    new Updater(mContext, sz);
                }
            }
        };
    }

    private void initUI(){
        frmMaster = findViewById(R.id.frmMaster);
        frmDetail = findViewById(R.id.frmDetail);
        frmItemList = findViewById(R.id.fragmentAction);
        frmItemDetail = findViewById(R.id.flDetailContainer);
        btnSHDetail = findViewById(R.id.btnSHDetail);
        btnSHDetIcn = findViewById(R.id.btnSHDetIcon);
        btnSHDetTxt = findViewById(R.id.btnSHDetText);

        btnSHDetail.setOnClickListener(view -> {
            if(isExpanded){
                hideDetail();
            }else{
                showDetail();
            }
        });

        showDetail();
    }

    private void showDetail() {
        isExpanded = true;

        int actionBarHeight = 0;

        TypedValue tv = new TypedValue();
        if(getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            Log.e("[X-DEBUG]", "getTheme: true;");
        }

        Display disp = getWindowManager().getDefaultDisplay();
        Point scrSize = new Point();

        try {
            disp.getRealSize(scrSize);
        }catch (NoSuchMethodError err){
            disp.getSize(scrSize);
        }

        int x = scrSize.x;
        int y = scrSize.y;

        Log.e("[X-DEBUG]", "x-axis size = "+x+"; y-axis size = "+y+";");

        if(actionBarHeight!=0){
            int ab = actionBarHeight/2;

            y = y - ab;
            Log.e("[X-DEBUG]", "actionBarHeight = "+ab+"; y-axis resized = "+y+";");
        }else{
            Log.e("[X-DEBUG]", "actionBarHeight = 0;");
        }

        frmDetail.setVisibility(View.VISIBLE);
        frmItemList.setVisibility(View.VISIBLE);
        frmItemDetail.setVisibility(View.VISIBLE);

        LinearLayout.LayoutParams lyPVSplitTop = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (y/2));
        LinearLayout.LayoutParams lyPVSplitBottom = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams lyPHSplit = new LinearLayout.LayoutParams((x/2), LinearLayout.LayoutParams.MATCH_PARENT);
        frmMaster.setLayoutParams(lyPVSplitTop);
        frmDetail.setLayoutParams(lyPVSplitBottom);
        frmItemList.setLayoutParams(lyPHSplit);
        frmItemDetail.setLayoutParams(lyPHSplit);

        btnSHDetIcn.setImageResource(R.drawable.ic_baseline_arrow_drop_down);
        btnSHDetTxt.setText("Hide Details");
    }

    private void hideDetail() {
        isExpanded = false;

        int actionBarHeight = 0;

        TypedValue tv = new TypedValue();
        if(getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            Log.e("[X-DEBUG]", "getTheme: true;");
        }

        Display disp = getWindowManager().getDefaultDisplay();
        Point scrSize = new Point();

        try {
            disp.getRealSize(scrSize);
        }catch (NoSuchMethodError err){
            disp.getSize(scrSize);
        }

        int x = scrSize.x;
        int y = scrSize.y;

        Log.e("[X-DEBUG]", "x-axis size = "+x+"; y-axis size = "+y+";");

        if(actionBarHeight!=0){
            int ab = actionBarHeight/2;

            y = y - ab;
            Log.e("[X-DEBUG]", "actionBarHeight = "+ab+"; y-axis resized = "+y+";");
        }else{
            Log.e("[X-DEBUG]", "actionBarHeight = 0;");
        }

        LinearLayout.LayoutParams lyPVSplitTop = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, y);
        frmMaster.setLayoutParams(lyPVSplitTop);
        frmDetail.setVisibility(View.GONE);
        frmItemList.setVisibility(View.GONE);
        frmItemDetail.setVisibility(View.GONE);

        btnSHDetIcn.setImageResource(R.drawable.ic_baseline_arrow_drop_up);
        btnSHDetTxt.setText("Show Details");
    }

    private void initDetail() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
        dlg.setTitle("Konfirmasi");
        dlg.setMessage("Apakah anda yakin ingin menangani pasien ini?");
        dlg.setPositiveButton("Ya", (dialog, which) -> {
            Intent intent = new Intent(mContext, DoctorMainActivity.class);
            intent.putExtra("mode", DoctorMainActivity.ACTION_MODE);
            intent.putExtra("serv", serv_id);
            intent.putExtra("id", unit);
            intent.putExtra("uid", uid);
            intent.putExtra("no_brm", nobrm);
            intent.putExtra("nama_poli", namaunit);
            intent.putExtra("px_name", pxName);
            intent.putExtra("id_berkas", idBRM);
            intent.putExtra("status", statusIntent);
            startActivityForResult(intent, 101);
        });
        dlg.setNegativeButton("Tidak", null);
        dlg.setCancelable(true);
        final AlertDialog alert = dlg.create();

        txtBrm = findViewById(R.id.txtBrm);
        txtNama = findViewById(R.id.txtNama);
        txtVisit = findViewById(R.id.txtVisit);
        txtAlamat = findViewById(R.id.txtAlamat);
        txtAlergi = findViewById(R.id.txtAlergi);
        txtPermintaan = findViewById(R.id.txtPermintaan);
        txtTmplahir = findViewById(R.id.txtTmplahir);
        txtTgllahir = findViewById(R.id.txtTglLahir);
        txtCreatedat = findViewById(R.id.txtCreatedat);
        txtAsalLayanan = findViewById(R.id.txtAsalLayanan);
        btnAmbil = findViewById(R.id.btnAmbil);
        btnLanjutkan = findViewById(R.id.btnLanjutkan);

        txtJenisKelamin = findViewById(R.id.jkel);
        txtAgama = findViewById(R.id.txtAgama);
        txtHpPasien = findViewById(R.id.txtHpPasien);
        txtStatusPkn = findViewById(R.id.txtStatusPerkawinan);
        txtPendidikan = findViewById(R.id.txtPendidikan);
        txtPekerjaan = findViewById(R.id.txtPekerjaan);
        txtSBB = findViewById(R.id.txtSBB);

        btnAmbil.setOnClickListener(v -> alert.show());
        btnLanjutkan.setOnClickListener(view -> {
            AlertDialog.Builder dlgs = new AlertDialog.Builder(mContext);
            dlgs.setTitle("Konfirmasi");
            dlgs.setMessage("Apakah anda yakin ingin melanjutkan menangani pasien ini?");
            dlgs.setPositiveButton("Ya", (dialog, which) -> {
                Intent intent = new Intent(mContext, DoctorMainActivity.class);
                intent.putExtra("mode", DoctorMainActivity.ACTION_MODE);
                intent.putExtra("serv", serv_id);
                intent.putExtra("id", unit);
                intent.putExtra("uid", uid);
                intent.putExtra("no_brm", nobrm);
                intent.putExtra("nama_poli", namaunit);
                intent.putExtra("px_name", pxName);
                intent.putExtra("id_berkas", idBRM);
                intent.putExtra("isSaved", saveState);
                intent.putExtra("statusIntent", statusIntent);
                startActivityForResult(intent, 101);
            });
            dlgs.setNegativeButton("Tidak", null);
            dlgs.setCancelable(true);
            dlgs.create();
            dlgs.show();
        });
    }

    private void initListBRM(){
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        brmListAdapter = new BRMRecyclerAdapter(mContext, infoBRMList);

        brmListView = findViewById(R.id.rvMaster);
        brmListView.setLayoutManager(mLayoutManager);
        brmListView.setItemAnimator(new DefaultItemAnimator());
        brmListView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        brmListView.setAdapter(brmListAdapter);
        brmListView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), brmListView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                InfoBRMV2 iBRM = infoBRMList.get(position);

                Log.e("[X-DEBUG]", "[recyclerViewSelector] Recent Position Variable: "+recentSelectedPos+"\nstatusSelectHolder Size: "+selectedItems.size());

                if(recentSelectedPos!=-1){
                    selectedItems.put(recentSelectedPos, false);
                    try {

                        RecyclerView.ViewHolder holder = brmListView.findViewHolderForAdapterPosition(recentSelectedPos);
                        if (null != holder) {
                            holder.itemView.setSelected(false);
                        }
                        Log.e("[X-DEBUG]", "Recycler View Item Count: "+brmListAdapter.getItemCount());
                    }catch (Exception ex){
                        Log.e("[X-DEBUG]", "onClickRecycleView Exception: "+ex);
                    }
                }

                selectedItems.put(position, true);
                view.setSelected(true);

                recentSelectedPos = position;
                onBRMSelected(iBRM);

                Log.e("[X-DEBUG]", "SERVICE ID:"+iBRM.getId()+" ID_BERKAS:"+iBRM.getBerkas());
                Log.e("[X-DEBUG]", "[recyclerViewSelector] Recent Position Select: "+recentSelectedPos+"\nstatusSelectHolder Size: "+selectedItems.size());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    private void initDetailBRM(){
        RecyclerView recyclerView = findViewById(R.id.brm_card_view);
        lym = new LinearLayoutManager(mContext);
        DividerItemDecoration itemDecor = new DividerItemDecoration(mContext, RecyclerView.HORIZONTAL);
        itemDecor.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(lym);
        recyclerView.addItemDecoration(itemDecor);
        unitListAdapter = new PoliListAdapter(unitList) {
            @Override
            protected void onBindViewHolder(@NonNull BRMHodler hodler, int position, @NonNull final UnitForBRMDetail model) {
                hodler.itemView.setOnClickListener(view -> {
                    //TODO: ON GRID LIST CLICK
                });
                hodler.bindInfo(model);
            }
        };

        recyclerView.setAdapter(unitListAdapter);
    }

    private void initRetrofit(){
        Baselog.d("Initretrofit");
        APIClient = APIUtils.getAPIService(mContext);
    }

    private void fetchBRMList(final MenuItem item){
        Call<List<InfoBRMV2>> listBRMResponseCall = APIClient.getListBRM(uid, Integer.parseInt(unit), 1);

        listBRMResponseCall.enqueue(new Callback<List<InfoBRMV2>>() {
            @Override
            public void onResponse(@NonNull Call<List<InfoBRMV2>> call, @NonNull Response<List<InfoBRMV2>> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    if(item != null) {
                        item.getIcon().setAlpha(255);
                        item.setEnabled(true);
                    }

                    if(Objects.requireNonNull(response.body()).size() > 0) {
                        updateBRMList(Objects.requireNonNull(response.body()));
                    }else {
                        Baselog.d("No record");
                        toastInfo(mContext, "Daftar BRM Kosong");
                        infoBRMList.clear();
                        brmListAdapter.notifyDataSetChanged();
                    }
                    vwMaster.setVisibility(View.GONE);
                    vwDetail.setVisibility(View.GONE);
                }else {
                    Baselog.d("Response error " + response.message());
                    toastErr(mContext,  response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<InfoBRMV2>> call, @NonNull Throwable t) {
                Baselog.d("Failure " + t.getLocalizedMessage());
                toastErr(mContext, t.getLocalizedMessage());
                Baseprogress.hideProgressDialog();
                if(item != null) {
                    item.getIcon().setAlpha(255);
                    item.setEnabled(true);
                }
            }
        });
    }

    private void fetchBRMDetails(final int id_serv, final String brm, final int status, final int handler, final int idBerkas, final int is_dmr_ok){
        Log.e("[X-DEBUG]", "ISDMR: " + is_dmr_ok);
        serv_id = id_serv;
        idBRM = idBerkas;
        Call<BRMDetailResponse> listPoliResponseCall = APIClient.getDetailBRM(
                uid,
                brm
        );

        listPoliResponseCall.enqueue(new Callback<BRMDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<BRMDetailResponse> call, @NonNull Response<BRMDetailResponse> response) {
                Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    if(Objects.requireNonNull(response.body()).getStatuscode() == 200){
                        BRMDetailResponse list = response.body();
                        BRMDetailResultData data = Objects.requireNonNull(list).getResult();
                        InfoPasien pasien = data.getInfo_pasien();
                        List<UnitForBRMDetail> unit = data.getBerkas();

                        if(unit.size() > 0) {
                            String sNama = pasien.getNamaPasien();
                            String sBrm = pasien.getNoBrm();
//                            int iVisit = pasien.getNoVisit();
                            String sAlamat = pasien.getAlamat();
                            String sAlergi = pasien.getDataAlergi();
//                            String sPermintaan = pasien.getPermintaanPasien();
                            String sTgllahir = pasien.getTanggalLahir();
                            String sTmplahir = pasien.getTempatLahir();
                            String sCreatedat = pasien.getCreatedAt();
                            String sAsalLayanan = pasien.getAsl();
                            nobrm = sBrm;
                            pxName = sNama;

                            Log.e("X-DEBUG", "CREATED AT: "+sCreatedat);

                            String reBDate;
                            String reCreateAt;

                            if(!sTgllahir.equals("N/A")) {
                                String[] arBDate = sTgllahir.split("-");

                                reBDate = arBDate[2] + "/" + arBDate[1] + "/" + arBDate[0];
                            }else{
                                reBDate = sTgllahir;
                            }

                            if(!sCreatedat.equals("N/A")) {
                                String[] arCreateAt1 = sCreatedat.split(" ");
                                String[] arCreateAt2 = arCreateAt1[0].split("-");
                                String[] arCreateAt3 = arCreateAt1[1].split("\\.");

                                reCreateAt = arCreateAt2[2] + "/" + arCreateAt2[1] + "/" + arCreateAt2[0] + " " + arCreateAt3[0];
                            }else{
                                reCreateAt = sCreatedat;
                            }

                            txtNama.setText(sNama);
                            txtBrm.setText(sBrm);
//                            txtVisit.setText(String.valueOf(iVisit));
                            txtAlergi.setText(sAlergi);
//                            txtPermintaan.setText(sPermintaan);
                            txtTmplahir.setText(sTmplahir);
                            txtCreatedat.setText(reCreateAt);

                            txtAgama.setText(pasien.getAgama());
                            txtHpPasien.setText(pasien.getHpPasien());
                            txtStatusPkn.setText(pasien.getStatusKawin());
                            txtPendidikan.setText(pasien.getPendidikan());
                            txtPekerjaan.setText(pasien.getPekerjaan());
                            txtSBB.setText(pasien.getSukuBB());

                            txtAlamat.setText(sAlamat);
                            txtTgllahir.setText(reBDate);
                            txtAsalLayanan.setText(sAsalLayanan);

                            vwDetail.setVisibility(View.VISIBLE);
                            vwMaster.setVisibility(View.VISIBLE);
                            toastInfo(mContext, "status :"+status);
                            statusIntent = status;

                            if(status==3){
                                enableTakeButton();
                            }else{
                                disableTakeButton(handler, status);
                            }

                            if(status == 5 && is_dmr_ok == 1){
                                takeButtonForFix();
                            }

                            showDetail();
                        }else {
                            Baselog.d("No record");
                            vwDetail.setVisibility(View.GONE);
                            toastInfo(mContext, "Tidak ada UnitForBRMDetail");
                        }
                    }else{
                        Baselog.d(Objects.requireNonNull(response.body()).getMessage());
                        toastErr(mContext, Objects.requireNonNull(response.body()).getMessage());
                    }
                }else {
                    Log.e("~Debug", "ON RESPONSE FAILED");
                    toastErr(mContext, response.message());
                    Baselog.d(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BRMDetailResponse> call, @NonNull Throwable t) {
                Baseprogress.hideProgressDialog();
                toastErr(mContext, t.getLocalizedMessage());
                Log.e("[X-DEBUG]", "onFailed: "+t.getLocalizedMessage());
            }
        });
    }

    private void enableTakeButton(){
        btnAmbil.setVisibility(View.VISIBLE);
        btnLanjutkan.setVisibility(View.GONE);
        btnAmbil.setEnabled(true);
        btnAmbil.setText("Tangani pasien ini");
        btnAmbil.setBackgroundResource(R.color.colorPrimary);
    }
    private void takeButtonForFix(){
        btnAmbil.setVisibility(View.GONE);
        btnLanjutkan.setVisibility(View.VISIBLE);
        btnAmbil.setEnabled(false);
    }

    private void disableTakeButton(int uid, int status){
        int sUID = 0;

        try {
            sUID = Integer.valueOf(pref.getString("uid", null));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        Log.e("[X-DEBUG]", "UID="+sUID+" Status="+status);


        if(status==4){
            if(uid!=sUID) {
                btnAmbil.setVisibility(View.VISIBLE);
                btnLanjutkan.setVisibility(View.GONE);
                btnAmbil.setEnabled(false);
                btnAmbil.setText("Pasien ini telah ditangani");
                btnAmbil.setBackgroundColor(Color.GRAY);
            }else{
                btnAmbil.setVisibility(View.GONE);
                btnLanjutkan.setVisibility(View.VISIBLE);
            }
        }else{
            btnAmbil.setVisibility(View.VISIBLE);
            btnLanjutkan.setVisibility(View.GONE);
            btnAmbil.setEnabled(false);
            btnAmbil.setText("Pasien ini telah ditangani");
            btnAmbil.setBackgroundColor(Color.GRAY);
        }
    }

    public void onBRMSelected(InfoBRMV2 infoBRM) {
        Baseprogress.showProgressDialog(mContext,"Mengambil info visit " + infoBRM.getNo_brm() + "...");
        fetchBRMDetails(infoBRM.getId(), infoBRM.getNo_brm(), infoBRM.getStatus(), infoBRM.getHandler(), infoBRM.getBerkas(), infoBRM.getIs_dmr_ok() );
    }

    private void updateBRMList(List<InfoBRMV2> dataset){
        infoBRMList.clear();
        infoBRMList.addAll(dataset);
        brmListAdapter.notifyDataSetChanged();
    }

    private void updatePoliList(List<UnitForBRMDetail> dataset){
        unitList.clear();
        unitList.addAll(dataset);
        unitListAdapter.notifyDataSetChanged();
    }

    private void prepareDataset(){
        fetchBRMList(null);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildLayoutPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildLayoutPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        public interface ClickListener {
            void onClick(View view, int position);

            void onLongClick(View view, int position);
        }
    }

}
