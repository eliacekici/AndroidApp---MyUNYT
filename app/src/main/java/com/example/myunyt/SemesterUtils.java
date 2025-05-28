package com.example.myunyt;

import java.util.Calendar;

public class SemesterUtils {

    public static int currentSemesterId() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        if (month >= Calendar.OCTOBER || month <= Calendar.JANUARY) return 1;
        if (month >= Calendar.FEBRUARY && month <= Calendar.JUNE)   return 2;
        return 0;
    }

    public static boolean isEditWindowOpen() {
        return currentSemesterId() != 0;
    }
}
