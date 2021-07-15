package com.sabin.digitalrm.utils;

import java.util.List;

public class StringUtilities {
    public static String join(String delimiter, List<String> arr){
        String strUndmk = "";
        for (String item : arr){
            strUndmk += item + ',';
        }

        int end = strUndmk.length() > 0 ? strUndmk.length() - 1 : 0;
        return strUndmk.substring(0, end);
    }
}
