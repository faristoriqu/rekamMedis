package com.sabin.digitalrm;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.sabin.digitalrm.fragments.PdfPreviewFragment;
import com.sabin.digitalrm.fragments.PoliSurfaceFragment;

public class PreviewActivity extends AppCompatActivity {
    public static final int SPD_MODE = 1;
    public static final int PDF_MODE = 2;
    private int mode;
    private boolean isCoding = false;
    private MenuItem bookmarkToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int mode;
        String uid, brm, poli, namapoli;
        Integer idBerkas;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Bundle bundle = getIntent().getExtras();
        mode = bundle.getInt("mode");
        uid = bundle.getString("uid");
        brm = bundle.getString("brm");
        poli = bundle.getString("poli");
        namapoli = bundle.getString("namapoli");
        idBerkas = bundle.getInt("idBerkas");
        isCoding = bundle.getBoolean("isCoding", false);
        Log.d("X-LOG", "onCreate: " + brm);
        previewDoc(mode, uid, brm, poli, namapoli, idBerkas, isCoding);
    }


    private void previewDoc(int mode, String uid, String brm, String poli, String namapoli, int idBerkas, boolean isCoding){
        this.mode = mode;
        switch (mode){
            case PDF_MODE:{
                Log.d("X-LOG", "Here the mode");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, PdfPreviewFragment.newInstance(uid, brm, poli, idBerkas))
                        .commit();

                break;
            }
            case SPD_MODE:{
                Log.d("X-LOG", "Here the mode");
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, PoliSurfaceFragment.newInstance(true, uid, brm, poli, namapoli, idBerkas, isCoding))
                        .commit();

                break;
            }
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if(this.mode == SPD_MODE) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_coding, menu);
            bookmarkToggle = menu.findItem(R.id.action_bookmark);
            MenuItem subMenu = menu.findItem(R.id.action_more);
            MenuItem pen = menu.findItem(R.id.action_spen);

            pen.getIcon().setTint(Color.WHITE);

            getMenuInflater().inflate(R.menu.submenu_coding, subMenu.getSubMenu());
        }
        return true;
    }
}
