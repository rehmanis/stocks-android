package com.example.csci571andriodstocks;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class News {

    public static final String ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    final String src;
    final String img;
    final String lastUpdated;
    final String title;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public News (String src, String img, String title, String timestamp){

        String urlHttps = img.replaceFirst("(?i)^http://", "https://");

        this.src = src;
        this.img = urlHttps;
        this.title = title;

//        Date date = new Date(timestamp);
        Date currentDate = new Date();
        Instant instant = Instant.parse(timestamp);
        long diff = currentDate.getTime() - instant.toEpochMilli();
        long days = (diff / (60*60*24*1000));
        String lastAgoTxt = "days ago";

        if (days <= 0){
            days = (diff / (60*60*1000));

            lastAgoTxt = "hours ago";

            if (days <= 0){
                days = (diff / (60*1000));
                lastAgoTxt = "minutes ago";
            }
        }

        this.lastUpdated = days + lastAgoTxt;

        Log.i("TEST" , currentDate + " - " + instant + " - " + this.lastUpdated);

//        try {
////            final SimpleDateFormat df = new SimpleDateFormat(ISO_8601_24H_FULL_FORMAT);
////            DateFormat df = new SimpleDateFormat("yyyy-mm-ddThh:mm:ssZ");
//            Date date = new Date(timestamp);
//            Date currentDate = new Date();
//            Instant instant = Instant.parse(timestamp);
//            long diff = currentDate.getTime() - instant.toEpochMilli();
//            long days = (diff / (60*60*24*1000));
//            String lastAgoTxt = "days ago";
//
//            if (days <= 0){
//                days = (diff / (60*60*1000));
//
//                lastAgoTxt = "hours ago";
//
//                if (days <= 0){
//                    days = (diff / (60*1000));
//                    lastAgoTxt = "minutes ago";
//                }
//            }
//
//            this.lastUpdated = days + lastAgoTxt;
//
//            Log.e("TEST" , currentDate + " - " + instant + " - " + this.lastUpdated);
//        } catch (ParseException e) {
//            Log.e("TEST", "Exception", e);
//        }


    }
}
