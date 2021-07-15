package com.sabin.digitalrm;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.sabin.digitalrm.adapters.OldBRMAdapter;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.OldBRM;
import com.sabin.digitalrm.utils.APIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OldBRMActivity extends AppCompatActivity {
    private Context mContext;
    private APIService APIClient;
    private String uid, pxName;
    private int norm, unit;
    private RecyclerView.LayoutManager lym;
    private OldBRMAdapter obrmAdapter;
    private List<OldBRM> obrmList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_brm);
        Toolbar tb = findViewById(R.id.tbar);
        setSupportActionBar(tb);

        ActionBar ab = getSupportActionBar();
        Objects.requireNonNull(ab).setTitle("List BRM Lama");
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        mContext = this;

        Bundle bnd = getIntent().getExtras();
        uid = Objects.requireNonNull(bnd).getString("uid");
        norm = Integer.valueOf(bnd.getString("norm"));
        pxName = bnd.getString("px_name");
        unit = bnd.getInt("poli");

        obrmList = new ArrayList<>();

        initListDataset();
    }

    private void initListDataset(){
        if(APIClient == null) {
            APIClient = APIUtils.getAPIService(mContext);
        }

        BaseActivity.Baseprogress.showProgressDialog(mContext,"Mengambil daftar unit ...");
        Call<List<OldBRM>> listPoliResponseCall = APIClient.getAllOBRM(
                uid, norm
        );

        listPoliResponseCall.enqueue(new Callback<List<OldBRM>>() {
            @Override
            public void onResponse(Call<List<OldBRM>> call, Response<List<OldBRM>> response) {
                BaseActivity.Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    List<OldBRM> resp = response.body();

                    Log.e("[X-DEBUG]", "response: "+response.raw());

                    if(resp.size() > 0) {
                        initOldBRM();

                        updateOldBRMList(resp);
                    }else {
                        BaseActivity.Baselog.d("No record");
                        Toast.makeText(mContext, "List BRM Lama Kosong", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Log.e("[X-DEBUG]", "ON RESPONSE FAILED");
                    Toast.makeText(mContext, response.message(), Toast.LENGTH_LONG).show();
                    BaseActivity.Baselog.d(response.message());
                }
            }

            @Override
            public void onFailure(Call<List<OldBRM>> call, Throwable t) {
                BaseActivity.Baseprogress.hideProgressDialog();
                Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initOldBRM(){
        RecyclerView recyclerView = findViewById(R.id.rvOldBRM);
        lym = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(lym);
        obrmAdapter = new OldBRMAdapter(obrmList, pxName, unit) {
            @Override
            protected void onBindViewHolder(@NonNull BRMHoldeer hodler, int position, @NonNull final OldBRM model) {
                hodler.itemView.setOnClickListener(view -> onOBRMSelected(model));
                hodler.bindInfo(mContext, model);
            }
        };

        recyclerView.setAdapter(obrmAdapter);
    }

    private void onOBRMSelected(OldBRM model) {
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("mode", PreviewActivity.PDF_MODE);
        intent.putExtra("uid", uid);
        intent.putExtra("brm", norm);
        intent.putExtra("poli", unit);
        intent.putExtra("namapoli", "");
        intent.putExtra("idBerkas", model.getId());
        startActivity(intent);
    }

    private void updateOldBRMList(List<OldBRM> dataset){
        obrmList.clear();
        obrmList.addAll(dataset);
        obrmAdapter.notifyDataSetChanged();
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
}
