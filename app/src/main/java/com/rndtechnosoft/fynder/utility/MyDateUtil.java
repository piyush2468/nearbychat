package com.rndtechnosoft.fynder.utility;

import android.content.Context;
import android.util.Log;

import com.rndtechnosoft.fynder.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Ravi on 11/13/2016.
 */

public class MyDateUtil {

    private final static long LIMIT_TIME_DIFF = 60000; //1 minute

    public static String getDateMessage(Context context, long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMMM yyyy");
        Date myDate = new Date(timestamp);
        Calendar now = Calendar.getInstance();
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(timestamp);
        if ((now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) &&
                ((now.get(Calendar.MONTH) == smsTime.get(Calendar.MONTH))) &&
                ((now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)))) {
            return context.getString(R.string.today);
        } else if ((now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) &&
                ((now.get(Calendar.MONTH) == smsTime.get(Calendar.MONTH))) &&
                ((now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)))){
            return context.getString(R.string.yesterday);
        }
        return simpleDateFormat.format(myDate);
    }

    public static boolean isMergeRequired(long now, long pass) {
        return now - pass < LIMIT_TIME_DIFF;
    }

    public static String getTime(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm");
        Date myDate = new Date(timestamp);
        return simpleDateFormat.format(myDate);
    }

    public static String getDateTime(Context context, long timestamp) {
        return getDateMessage(context, timestamp) + " (" + getTime(timestamp) + ")";
    }

    public static int getDiffYears(long curTimeStamp, String stringDate){
        if(stringDate == null){
            return 0;
        }

        try{
            String month = stringDate.substring(4, 6);
            String year = stringDate.substring(0, 4);
            String day = stringDate.substring(6, 8);
            int monthInt = Integer.parseInt(month);
            int yearInt = Integer.parseInt(year);
            int dayInt = Integer.parseInt(day);
            Date curDate = new Date(curTimeStamp);
            Calendar now = Calendar.getInstance();
            now.setTime(curDate);
            int diff = now.get(Calendar.YEAR) - yearInt;
            int monthNow = now.get(Calendar.MONTH);
            int dateNow = now.get(Calendar.DATE);
            Log.i("Confirmation","Now month: "+monthNow+" , target month: "+monthInt+" , now date: "+dateNow+ " , target date: "+dayInt);
            if (monthInt > monthNow ||
                    ((monthInt == monthNow) && (dayInt > dateNow))) {
                diff--;
            }
            return diff;
        }
        catch (Exception e){
            return 0;
        }
    }

    public static String getLastChatTime(Context context, long timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(timestamp);
        if ((now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) &&
                ((now.get(Calendar.MONTH) == smsTime.get(Calendar.MONTH))) &&
                ((now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)))){
            return getTime(timestamp);
        } else if ((now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1)&&
                ((now.get(Calendar.MONTH) == smsTime.get(Calendar.MONTH))) &&
                ((now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)))){
            return context.getString(R.string.text_yesterday);
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d.MM.yy");
            Date myDate = new Date(timestamp);
            return simpleDateFormat.format(myDate);
        }
    }
}
