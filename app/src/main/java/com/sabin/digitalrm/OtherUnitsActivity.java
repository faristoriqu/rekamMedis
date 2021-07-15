package com.sabin.digitalrm;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.sabin.digitalrm.adapters.OtherPoliAdapter;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.InfoPasien;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.models.DMRPatient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtherUnitsActivity extends AppCompatActivity {
private Context mContext;
RecyclerView.LayoutManager lym;
OtherPoliAdapter poliAdapter;
List<DMRPatient> poliList;
String uid, brm;
int idpoli, idBerkas;
APIService APIClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_poli);
        Toolbar tb = findViewById(R.id.tbar);
        setSupportActionBar(tb);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("List Berkas Unit Lain");
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        mContext = this;
        Bundle bundle = getIntent().getExtras();
        uid = bundle.getString("uid");
        brm = bundle.getString("brm");
        idpoli = Integer.parseInt(bundle.getString("poli"));
        idBerkas = bundle.getInt("idBerkas");

        poliList = new ArrayList<>();

        initRetrofit();
        prepareDataset();
    }

    private void initRetrofit(){
        BaseActivity.Baselog.d("Initretrofit");
        APIClient = APIUtils.getAPIService(mContext);
    }

    private void initOtherPoli(String nama){
        RecyclerView recyclerView = findViewById(R.id.rvOtherPoli);
        lym = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(lym);
        poliAdapter = new OtherPoliAdapter(poliList, nama, idpoli) {
            @Override
            protected void onBindViewHolder(@NonNull BRMHoldeer hodler, int position, @NonNull final DMRPatient model) {
                hodler.itemView.setOnClickListener(view -> onPoliSelected(model));
                hodler.bindInfo(mContext, model);
            }
        };

        recyclerView.setAdapter(poliAdapter);
    }

    public void onPoliSelected(DMRPatient infoPoli) {
        String[] poliname = infoPoli.getName().split("/");
        String unitname = "";

        if (poliname.length > 1){
            unitname = poliname[1];
        }

        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("mode", PreviewActivity.SPD_MODE);
        intent.putExtra("uid", uid);
        intent.putExtra("brm", brm);
        intent.putExtra("poli", infoPoli.getUnitId());
        intent.putExtra("namapoli", unitname);
        intent.putExtra("idBerkas", infoPoli.getId());
        startActivity(intent);
    }

    private void prepareDataset(){
        fetchPoliList();
    }

    private void fetchPoliList(){
        BaseActivity.Baseprogress.showProgressDialog(mContext,"Mengambil daftar unit ...");
        Call<InfoPasien> listPoliResponseCall = APIClient.getDMRsPatient(
                uid, brm, null
        );

        listPoliResponseCall.enqueue(new Callback<InfoPasien>() {
            @Override
            public void onResponse(Call<InfoPasien> call, Response<InfoPasien> response) {
                BaseActivity.Baseprogress.hideProgressDialog();
                if(response.isSuccessful()){
                    InfoPasien resp = response.body();
                    List<DMRPatient> data = resp.getDmrPatientList();

                    if(data.size() > 0) {
                        String sNama = resp.getNamaPasien();

                        initOtherPoli(sNama);

                        updatePoliList(data);
                    }else {
                        BaseActivity.Baselog.d("No record");
                        Toast.makeText(mContext, "List UnitForBRMDetail Kosong", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Log.e("[X-DEBUG]", "ON RESPONSE FAILED");
                    Toast.makeText(mContext, response.message(), Toast.LENGTH_LONG).show();
                    BaseActivity.Baselog.d(response.message());
                }
            }

            @Override
            public void onFailure(Call<InfoPasien> call, Throwable t) {
                BaseActivity.Baseprogress.hideProgressDialog();
                Toast.makeText(mContext, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updatePoliList(List<DMRPatient> dataset){
        poliList.clear();
        poliList.addAll(dataset);
        poliAdapter.notifyDataSetChanged();
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
