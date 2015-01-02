package com.devsmart;


public class StringUtils {

    public static boolean isEmptyString(String str) {
        return !(str != null && str.trim().length() > 0);
    }


}
