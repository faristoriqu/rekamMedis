package com.sabin.digitalrm.prm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import android.os.Bundle;import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.sabin.digitalrm.BaseActivity;
import com.sabin.digitalrm.LoginActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.fragments.prm.blanko.BlankoV3Fragment;
import com.sabin.digitalrm.fragments.prm.brm.HistoryFragment;
import com.sabin.digitalrm.fragments.prm.export.ExportFragment;
import com.sabin.digitalrm.fragments.prm.brm.DaftarBrmAktifFragment;
import com.sabin.digitalrm.fragments.prm.pasien.ContainerPasienFragment;
import com.sabin.digitalrm.helpers.Updater;
import com.sabin.digitalrm.services.UpdateCheckerService;

import java.util.Objects;

public class PetugasMainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String EXTRA_NOBRM = "EX_NOBRM";
    public static final String EXTRA_IDPOLI = "EX_IDPOLI";
    public static final String EXTRA_NAME = "EX_PXNAME";
    private final int NAV_KUNJUNGAN = 1;
    private final int NAV_OTHER = 2;
    private BroadcastReceiver receiver;

    public static String UID;

    private int navPos = 1;

    DrawerLayout drawer;
    AppBarLayout appBarLayout;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Context mContext;

    FragmentManager fm;
    ContainerPasienFragment pasienFragment = null;
    DaftarBrmAktifFragment brmAktifFragment = null;
    ExportFragment exportFragment = null;
    BlankoV3Fragment blankoFragment = null;
    HistoryFragment historyFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petugas_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        mContext = this;

        Intent in = new Intent(mContext, UpdateCheckerService.class);
        startService(in);

        appBarLayout = findViewById(R.id.appbarLayout);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_daftar_kunjungan);

        pasienFragment = new ContainerPasienFragment();

        fm = getSupportFragmentManager();
        FragmentTransaction transaction;
        transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, pasienFragment);
        transaction.commit();

        this.setTitle(mContext.getResources().getString(R.string.daftar_kunjungan));

        pref = getSharedPreferences("SESSION_", 0);
        UID = pref.getString("uid", "ANON");

        Log.d(TAG, "onCreate: " + UID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_refresh && navPos == NAV_KUNJUNGAN){
            Log.d(TAG, "onOptionsItemSelected: PRENT REFERSH");
            FragmentTransaction transaction = fm.beginTransaction();
            pasienFragment = new ContainerPasienFragment();
            transaction.replace(R.id.fragment_container, pasienFragment);
            transaction.commit();
            return true;
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_daftar_kunjungan:{
                navPos = NAV_KUNJUNGAN;

                    FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, pasienFragment);
                transaction.commit();
                this.setTitle(getString(R.string.daftar_kunjungan));
                break;
            }

            case R.id.nav_daftar_brm_aktif:{
                navPos = NAV_OTHER;

                if(brmAktifFragment == null){
                    brmAktifFragment = new DaftarBrmAktifFragment();
                }

                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, brmAktifFragment);
                transaction.commit();
                this.setTitle(getString(R.string.daftar_brm_aktif));
                break;
            }

            case R.id.nav_export_brm:{
                navPos = NAV_OTHER;

                if(exportFragment == null){
                    exportFragment = new ExportFragment();
                }

                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, exportFragment);
                transaction.commit();
                this.setTitle(mContext.getResources().getString(R.string.export_brm));
                break;
            }

            case R.id.nav_riwayat_brm:{
                if(historyFragment == null){
                    historyFragment = new HistoryFragment();
                }

                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, historyFragment);
                transaction.commit();
                this.setTitle(getString(R.string.riwayat_brm));
                break;
            }

            case R.id.nav_daftar_blanko:{
                navPos = NAV_OTHER;

                if(blankoFragment == null){
                    blankoFragment = new BlankoV3Fragment();
                }

                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, blankoFragment);
                transaction.commit();
                this.setTitle(getString(R.string.daftar_blanko));
                break;
            }

            case R.id.nav_logout:{
                new AlertDialog.Builder(mContext)
                        .setTitle("Konfirmasi Logout")
                        .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                        .setMessage("Yakin anda ingin keluar ?")
                        .setPositiveButton("YA", (dialog, which) -> doLogout())
                        .setNegativeButton("Tidak", null)
                        .create().show();
                break;
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    private void doLogout(){
        editor = pref.edit();
        editor.putString("token", null);
        editor.putBoolean("is_loggedin", false);
        editor.putString("uid", null);
        editor.putInt("akses", 0);
        editor.putInt("id_poli", 0);
        editor.putString("nama_poli", null);
        editor.apply();

        Intent i = new Intent(PetugasMainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);

        Intent in = new Intent(mContext, UpdateCheckerService.class);
        stopService(in);
    }
}
