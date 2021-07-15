package com.sabin.digitalrm.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xkill on 09/11/18.
 */

public final class Timestamp {
    public static String now(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy HH:mm");

        return dateFormat.format(date);
    }

    public static String getDateTime(Integer ts){
        if(ts == null)
            return "N/A";

        Date date = new Date(ts * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy HH:mm");

        return dateFormat.format(date);
    }

    public static String getDate(Integer ts, String format){
        if(ts == null)
            return "N/A";

        Date date = new Date(ts * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        return dateFormat.format(date);
    }

    public static Integer getTimeStamp(String strDate){
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyy HH:mm");

        Date date;
        try {
            date = formatter.parse(strDate);
            return (int)(date.getTime() / 1000);
        }catch (ParseException e){
            return 0;
        }
    }
}
