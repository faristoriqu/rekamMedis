package com.sabin.digitalrm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.Units;
import com.sabin.digitalrm.models.UnitsResponse;
import com.sabin.digitalrm.utils.APIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnitSelectorActivity extends AppCompatActivity {
    private Context mContext;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ArrayList<Units> arrLst;

    Spinner spUnits;
    Button btnSelect;
    View lyLoading;
    View lyContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poli_selctor);
        setSupportActionBar(findViewById(R.id.toolbar));

        Objects.requireNonNull(getSupportActionBar()).setTitle("Digital MR - Pilih Unit");

        mContext = this;

        pref = getSharedPreferences("SESSION_", 0);

        spUnits = findViewById(R.id.spUnit);
        btnSelect = findViewById(R.id.btnSelect);
        lyLoading = findViewById(R.id.content_loading);
        lyContent = findViewById(R.id.content_spinner);

        arrLst = new ArrayList<>();
        Bundle b = getIntent().getExtras();

        if(b!=null) {
            arrLst = Objects.requireNonNull(b).getParcelableArrayList("units");

            lyLoading.setVisibility(View.GONE);
            lyContent.setVisibility(View.VISIBLE);

            List<Integer> unitId = new ArrayList<>();
            List<String> units = new ArrayList<>();

            for (int i = 0; i< Objects.requireNonNull(arrLst).size(); i++) {
                unitId.add(i, arrLst.get(i).getId_poli());
                units.add(i, arrLst.get(i).getNama_poli());
            }

            ArrayAdapter<String> spAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, Objects.requireNonNull(units));
            spUnits.setAdapter(spAdapter);

            btnSelect.setOnClickListener(view -> {
                int pos = spUnits.getSelectedItemPosition();
                String selectedUnit = spUnits.getSelectedItem().toString();
                int selectedId = unitId.get(pos);

                editor = pref.edit();
                editor.putInt("id_poli", selectedId);
                editor.putString("nama_poli", selectedUnit);
                editor.apply();

                Intent i = new Intent(mContext, BrowseActivity.class);
                startActivity(i);

                finish();
            });

            if(arrLst.size()==0){
                btnSelect.setEnabled(false);
            }
        }else{
            getUnitListFromServer(pref.getString("uid", null));
        }
    }

    private void getUnitListFromServer(String uid) {
        APIService mAPIService = APIUtils.getAPIService(this);

        mAPIService.getUserUnits(uid).enqueue(new Callback<UnitsResponse>() {
            @Override
            public void onResponse(@NonNull Call<UnitsResponse> call, @NonNull Response<UnitsResponse> response) {
                if(response.isSuccessful()) {
                    Log.e("[X-DEBUG]", "Post data sent to the API.");
                    if(Objects.requireNonNull(response.body()).getStatuscode()==200){
                        arrLst = new ArrayList<>(Objects.requireNonNull(response.body()).getUnits());

                        lyLoading.setVisibility(View.GONE);
                        lyContent.setVisibility(View.VISIBLE);

                        List<Integer> unitId = new ArrayList<>();
                        List<String> units = new ArrayList<>();

                        for (int i = 0; i< Objects.requireNonNull(arrLst).size(); i++) {
                            unitId.add(i, arrLst.get(i).getId_poli());
                            units.add(i, arrLst.get(i).getNama_poli());
                        }

                        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, Objects.requireNonNull(units));
                        spUnits.setAdapter(spAdapter);

                        btnSelect.setOnClickListener(view -> {
                            int pos = spUnits.getSelectedItemPosition();
                            String selectedUnit = spUnits.getSelectedItem().toString();
                            int selectedId = unitId.get(pos);

                            editor = pref.edit();
                            editor.putInt("id_poli", selectedId);
                            editor.putString("nama_poli", selectedUnit);
                            editor.apply();

                            Intent i = new Intent(mContext, BrowseActivity.class);
                            startActivity(i);

                            finish();
                        });

                        if(arrLst.size()==0){
                            btnSelect.setEnabled(false);
                        }
                    }else{
                        Toast.makeText(mContext, "Gagal mengambil data. Otomatis logout.", Toast.LENGTH_LONG).show();
                        logout();
                    }
                }else{
                    Log.e("[X-DEBUG]", "Cannot send post data. Error code: "+response.code());
                    Toast.makeText(mContext, "Gagal mengambil data. Otomatis logout.", Toast.LENGTH_LONG).show();
                    logout();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UnitsResponse> call, @NonNull Throwable t) {
                Log.e("[X-DEBUG]", "Unable to send post to the API. Error: "+t.getMessage());
                Toast.makeText(mContext, "Gagal mengambil data. Otomatis logout.", Toast.LENGTH_LONG).show();
                logout();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.poli_seletor_menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_logout:{
                new AlertDialog.Builder(mContext)
                        .setTitle("Konfirmasi Logout")
                        .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                        .setMessage("Yakin anda ingin keluar ?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            logout();
                        })
                        .setNegativeButton("Tidak", null)
                        .create().show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout(){
        editor = pref.edit();
        editor.putBoolean("is_loggedin", false);
        editor.putString("uid", null);
        editor.putInt("poli", 0);
        editor.apply();
        Intent i = new Intent(mContext, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
