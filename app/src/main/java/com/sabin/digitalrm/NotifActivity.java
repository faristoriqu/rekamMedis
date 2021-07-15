package com.sabin.digitalrm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.sabin.digitalrm.adapters.NotifAdapter;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.InfoBRMV2;
import com.sabin.digitalrm.utils.APIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sabin.digitalrm.BaseActivity.toastErr;

public class NotifActivity extends AppCompatActivity {
    private Context mContext;
    private APIService APIClient;
    private SharedPreferences pref;
    private List<InfoBRMV2> notifList;
    private NotifAdapter notifAdapter;
    SwipeRefreshLayout pullToRefresh;
    RecyclerView.LayoutManager lym;
    RecyclerView rvNotif;
    RelativeLayout emptyLayout;
    private int uid, pid;
    private View rView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);
        setSupportActionBar(findViewById(R.id.notif_toolbar));

        mContext = this;

        ActionBar ab = getSupportActionBar();
        Objects.requireNonNull(ab).setTitle("Digital MR - Notifikasi");
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        rvNotif = findViewById(R.id.rvNotif);
        emptyLayout = findViewById(R.id.emptyLayout);

        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void init(){
        pref = getSharedPreferences("SESSION_", 0);
        APIClient = APIUtils.getAPIService(mContext);

        uid = Integer.valueOf(pref.getString("uid", "-1"));
        pid = pref.getInt("id_poli", -1);

        RecyclerView recyclerView = findViewById(R.id.rvNotif);
        rView = recyclerView.getRootView();
        pullToRefresh = findViewById(R.id.pullToRefresh);
        notifList = new ArrayList<>();

        pullToRefresh.setColorSchemeResources(R.color.colorPrimary);
        pullToRefresh.setDistanceToTriggerSync(150);
        pullToRefresh.setOnRefreshListener(this::getNotif);

        getNotif();

        lym = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(lym);

        notifAdapter = new NotifAdapter(notifList) {
            @Override
            protected void onBindViewHolder(@NonNull NotifHolder holder, int pos, @NonNull InfoBRMV2 model) {
                holder.itemView.setOnClickListener(view -> onNotifSelected(model));
                holder.bindInfo(mContext, model);
            }
        };

        recyclerView.setAdapter(notifAdapter);
    }

    private void onNotifSelected(InfoBRMV2 notif){
        Intent intent = new Intent(mContext, DoctorMainActivity.class);
        intent.putExtra("mode", DoctorMainActivity.FIX_MODE);
        intent.putExtra("serv", notif.getId());
        intent.putExtra("id", String.valueOf(notif.getId_unit()));
        intent.putExtra("uid", String.valueOf(uid));
        intent.putExtra("no_brm", notif.getNo_brm());
        intent.putExtra("nama_poli", notif.getUnit_name());
        intent.putExtra("id_berkas", notif.getBerkas());
        startActivityForResult(intent, 101);
    }

    private void getNotif(){
        pullToRefresh.setRefreshing(true);
        Call<List<InfoBRMV2>> notifResponseCall = APIClient.getNotifV2(uid, pid, 1);

        notifResponseCall.enqueue(new Callback<List<InfoBRMV2>>() {
            @Override
            public void onResponse(@NonNull Call<List<InfoBRMV2>> call, @NonNull Response<List<InfoBRMV2>> response) {
                BaseActivity.Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    if(Objects.requireNonNull(response.body()).size() > 0) {
                        Log.e("[X-DEBUG]", "Notification Found");
                        updateNotifList(Objects.requireNonNull(response.body()));
                    }else {
                        Log.e("[X-DEBUG]", "Empty Notification");
                        Snackbar.make(rView, "Tidak ada notifikasi ditemukan", Snackbar.LENGTH_LONG).show();
                        notifList.clear();
                        notifAdapter.notifyDataSetChanged();
                        rvNotif.setVisibility(View.GONE);
                        emptyLayout.setVisibility(View.VISIBLE);
                    }
                }else {
                    BaseActivity.Baselog.d("Response error " + response.message());
                    toastErr(mContext,  response.message());
                }

                pullToRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<List<InfoBRMV2>> call, @NonNull Throwable t) {
                BaseActivity.Baselog.d("Failure " + t.getLocalizedMessage());
                toastErr(mContext, t.getLocalizedMessage());

                pullToRefresh.setRefreshing(false);
            }
        });
    }

    private void updateNotifList(List<InfoBRMV2> notif) {
        notifList.clear();
        notifList.addAll(notif);
        notifAdapter.notifyDataSetChanged();

        rvNotif.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            getNotif();
        }
    }
}
