package com.sabin.digitalrm.utils;

import android.content.Context;
import android.content.Intent;

public class ShareUtils {
    public static void shareText(Context context, String data){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, data);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }
}
