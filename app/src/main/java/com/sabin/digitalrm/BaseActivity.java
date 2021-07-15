package com.sabin.digitalrm;

import android.app.ProgressDialog;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {
    public static final String TAG = "X-LOG";
    private static final boolean DEBUG_MODE = true;

    public static void toastErr(Context context, String msg){
        Toast.makeText(context, "Terjadi kesalahan, " + msg, Toast.LENGTH_SHORT).show();
    }

    public static void toastInfo(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static class Baseprogress{
        private static ProgressDialog mProgressDialog;

        public static void showProgressDialog(Context context, String msg) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setCancelable(false);
            }

            mProgressDialog.setMessage(msg);
            mProgressDialog.show();
        }

        public static void setMessage(String msg){
            mProgressDialog.setMessage(msg);
        }

        public static void hideProgressDialog() {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }

        public static boolean isDialogShowing(){
            return mProgressDialog.isShowing();
        }
    }

    public static class Baselog{
        public static void d(String msg){
            if(DEBUG_MODE)
                Log.d(TAG, msg);
        }
    }
}
