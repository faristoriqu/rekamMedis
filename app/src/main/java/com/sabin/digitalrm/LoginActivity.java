package com.sabin.digitalrm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.annotation.NonNull;import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sabin.digitalrm.helpers.ApiServiceGenerator;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.LoginResponse;
import com.sabin.digitalrm.models.Units;
import com.sabin.digitalrm.utils.APIUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.sabin.digitalrm.prm.*;

import org.json.JSONObject;

import static com.sabin.digitalrm.utils.StringHash.MD5;
import static com.samsung.android.sdk.pen.util.SpenEngineUtil.SDK_VERSION;

public class LoginActivity extends AppCompatActivity {
    private View dialogView;
    EditText uname, pwd, settingIp;
    String valueIP;
    Button btnLogin;
    ProgressDialog progressDialog;
    private static APIService mAPIService;
    SharedPreferences pref, prefSettings;
    SharedPreferences.Editor editor, editorPrefIP;
    TextView txtVer;
    AlertDialog.Builder dialog;
    LayoutInflater inflater;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setSupportActionBar(findViewById(R.id.my_toolbar));

        txtVer = findViewById(R.id.txtVer);
        txtVer.setText("Client App Version "+getAppInfo().get("version").toString()+" "+getAppInfo().get("release_status").toString());

        pref = getSharedPreferences(ApiServiceGenerator.sessionPrefsName, 0);
        prefSettings = getSharedPreferences(ApiServiceGenerator.settingPrefsName, 0);

        if(!hasWriteStoragePermission()){
            requestWriteStoragePermission();
        }

        if(!hasReadStoragePermission()){
            requestReadStoragePermission();
        }

        valueIP = prefSettings.getString("api_host", ApiServiceGenerator.defaultAPIHost);

        if(pref.getBoolean("is_loggedin", false)){
            if(pref.getInt("id_poli", -1) != -1) {
                if (pref.getInt("akses", 0) == 1) {
                    Intent i = new Intent(LoginActivity.this, PetugasMainActivity.class);
                    startActivity(i);
                    finish();
                } else if (pref.getInt("akses", 0) == 2) {
                    Intent i = new Intent(LoginActivity.this, BrowseActivity.class);
                    startActivity(i);
                    finish();
                }
            }else{
                if (pref.getInt("akses", 0) == 1) {
                    Intent i = new Intent(LoginActivity.this, PetugasMainActivity.class);
                    startActivity(i);
                    finish();
                } else if (pref.getInt("akses", 0) == 2) {
                    Intent i = new Intent(LoginActivity.this, UnitSelectorActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }

        uname = findViewById(R.id.username);
        pwd = findViewById(R.id.password);
        settingIp = findViewById(R.id.settingIp);
        btnLogin = findViewById(R.id.loginButton);

        pwd.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        login(v);
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });

        mAPIService = APIUtils.getAPIService(this);

        btnLogin.setOnClickListener(view -> {
            if(!isNetworkAvailable()){
                Snackbar.make(view,"Anda sedang offline. Tidak dapat login", Snackbar.LENGTH_LONG).show();
            }else {
                login(view);
            }
        });
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_setting:{


                inflater = getLayoutInflater();
                dialogView = getLayoutInflater().inflate(R.layout.activity_setting, null);

                final AlertDialog dialog = new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Setting IP DMR")
                        .setView(dialogView)
                        .setPositiveButton("Ok", null)
                        .setNegativeButton("Cancel", null)
                        .show();
                settingIp  = (EditText) dialogView.findViewById(R.id.settingIp);
                settingIp.setText(prefSettings.getString("api_host", ApiServiceGenerator.defaultAPIHost));

                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(v -> {
                    valueIP = settingIp.getText().toString();
                    if(valueIP.isEmpty()){
                        Toast.makeText(LoginActivity.this, "Setting API host tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    }else {
                        setting();
                        dialog.dismiss();
                    }

                });


//                setting();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    private void setting(){
        editorPrefIP = prefSettings.edit();
        editorPrefIP.putString("api_host", valueIP);
        editorPrefIP.apply();

//        ApiServiceGenerator apiServiceGenerator = new ApiServiceGenerator(LoginActivity.this);
//        apiServiceGenerator.callSharedPreference();
    }

    private HashMap getAppInfo(){
        HashMap<String, String> app_info = new HashMap<>();
        String jsonData;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open("application.json")));

            jsonData = reader.readLine();

            JSONObject wrapperObject = new JSONObject(jsonData);
            JSONObject appinfo = wrapperObject.getJSONObject("app_info");
            app_info.put("namespace" ,appinfo.getString("namespace"));
            app_info.put("codename", appinfo.getString("codename"));
            app_info.put("client_id", appinfo.getString("client_id"));
            app_info.put("version", appinfo.getString("version"));
            app_info.put("release_status", appinfo.getString("release_status"));
            app_info.put("release_date", appinfo.getString("release_date"));
            app_info.put("client_size" ,appinfo.getString("client_size"));

        } catch (Exception e) {
            Log.e("[X-EXCEPTION]", "Exception thrown while reading asset file. \nException message: "+e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("[X-EXCEPTION]", "Exception thrown while close the reader. \nException message: "+e.getMessage());
                }
            }
        }

        return app_info;
    }

    private void login(View view){
        String username = uname.getText().toString();
        String password = pwd.getText().toString();

        if(attemptLogin(view, username, password)){
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Masuk ...");
            progressDialog.isIndeterminate();
            progressDialog.setCancelable(false);
            progressDialog.show();
            try {
                password = MD5(password);
                mAPIService.login(username, password).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                        if(response.isSuccessful()) {
                            Log.e("~Debug", "Post data sent to the API.");
                            if(Objects.requireNonNull(response.body()).getResult()){
                                progressDialog.dismiss();
                                int akses = Objects.requireNonNull(response.body()).getAkses();
                                ArrayList<Units> unitsList = new ArrayList<>(Objects.requireNonNull(response.body()).getUnit());

                                editor = pref.edit();
                                editor.putBoolean("is_loggedin", true);
                                editor.putString("uid", Objects.requireNonNull(response.body()).getId());
                                editor.putInt("akses", akses);
                                editor.putInt("id_poli", -1);
                                editor.putString("nama_poli", null);
                                editor.putString("activeBRM", null);
                                editor.apply();

                                if(akses == 1){
                                    Intent i = new Intent(LoginActivity.this, PetugasMainActivity.class);

                                    startActivity(i);
                                }else if(akses == 2){
                                    Bundle b = new Bundle();
                                    Intent i = new Intent(LoginActivity.this, UnitSelectorActivity.class);
                                    b.putParcelableArrayList("units", unitsList);
                                    i.putExtras(b);

                                    startActivity(i);
                                }

                                Log.e("[X-DEBUG]", "Units count: "+unitsList.size());

                                finish();
                            }else{
                                progressDialog.dismiss();
                                Snackbar.make(view,"Login gagal. "+ Objects.requireNonNull(response.body()).getMessage(), Snackbar.LENGTH_LONG).show();
                                uname.findFocus();
                            }
                        }else{
                            Log.e("~Debug", "Cannot send post data. Error code: "+response.code());
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                        Log.e("~Debug", "Unable to send post to the API. Error: "+t.getMessage());
                        Snackbar.make(view,"Tidak dapat login. Silahkan periksa sambungan anda atau coba lagi beberapa saat lagi", Snackbar.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
            } catch (Exception ex) {
                Log.e("~Debug", ex.getMessage());
                Snackbar.make(view,"An error has occurred. "+ ex.getMessage(), Snackbar.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }
    }

    private boolean attemptLogin(View v, String username, String password){
        if(username.equals("") || password.equals("")){
            Snackbar.make(v,"Field Username dan Password tidak boleh kosong!", Snackbar.LENGTH_SHORT).show();
            uname.findFocus();
            return false;
        }else if(password.length() < 4){
            Snackbar.make(v,"Password kurang dari 4 karakter", Snackbar.LENGTH_SHORT).show();
            pwd.findFocus();
            return false;
        }

        return true;
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni != null && ni.isConnected();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasWriteStoragePermission() {
        return SDK_VERSION < 23 || PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestWriteStoragePermission() {
        if (SDK_VERSION < 23) {
            return;
        }
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasReadStoragePermission() {
        return SDK_VERSION < 23 || PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestReadStoragePermission() {
        if (SDK_VERSION < 23) {
            return;
        }
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
