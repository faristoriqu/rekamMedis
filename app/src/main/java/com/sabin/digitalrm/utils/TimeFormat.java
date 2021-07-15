package com.sabin.digitalrm.utils;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeFormat {
    private static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1) );
    private static final List<String> timesString = Arrays.asList("tahun","bulan","hari","jam","menit","detik");

    public static String timeAgoFormat(String datetime) {
        Date curentDate = Calendar.getInstance(Locale.US).getTime();
        long dateNow = datetime2long(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", curentDate).toString());
        long dateInput = datetime2long(datetime);
        long duration = dateNow - dateInput;
        StringBuilder res = new StringBuilder();

        for(int i=0;i< TimeFormat.times.size(); i++) {
            Long current = TimeFormat.times.get(i);
            long temp = duration/current;
            if(temp>0) {
                res.append(temp).append(" ").append(TimeFormat.timesString.get(i)).append(" yang lalu");
                break;
            }
        }

        Log.e("[X-DEBUG]", "time now: "+dateNow+"; time input: "+dateInput+"; duration: "+duration+"; time ago: "+res.toString()+";");

        if("".equals(res.toString()))
            return "Baru saja";
        else if("1 hari yang lalu".equals(res.toString()))
            return "Kemarin";
        else
            return res.toString();
    }

    private static long datetime2long(String datetime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date mDate = sdf.parse(datetime);
            Log.e("[X-DEBUG]", "Date: "+datetime+" converted to long: "+mDate.getTime());
            return mDate.getTime();
        } catch (ParseException e) {
            Log.e("[X-DEBUG]", "Date to long return 0. Exception: "+e.getMessage());
            return 0;
        }
    }

    public static String dateTimeFormat(String datetimeInput, String formatOutput){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        SimpleDateFormat odf = new SimpleDateFormat(formatOutput, Locale.US);

        try {
            String mDate = odf.format(sdf.parse(datetimeInput));
            return mDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "N/A";
    }

    public static String dateTimeFormat(String datetimeInput){
        return dateTimeFormat(datetimeInput, "dd/MM/yyyy HH:mm");
    }
}
