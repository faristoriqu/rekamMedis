package com.sabin.digitalrm.utils;

import android.util.Log;
import java.util.ArrayList;

public class Logger {
    public void x(final String msg){
        Log.e("[X-DEBUG]", msg);
    }
    
    public void printr(final ArrayList<String> obj){
        int i = 0;
        for (String msg : obj) {
            Log.e("[X-DEBUG]", "ARRAY "+i+" :"+msg);
            i++;
        }
    }
}
