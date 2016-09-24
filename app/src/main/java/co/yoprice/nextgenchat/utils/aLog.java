package co.yoprice.nextgenchat.utils;

import android.util.Log;

import java.util.Date;

import co.yoprice.nextgenchat.data.Constants;

/**
 * Created by cj on 4/30/16.
 */
public class aLog {
    public static void w (String TAG, String msg){
        if(Constants.LOGGING) {
            if (TAG != null && msg != null)
                Log.w(TAG, msg);
        }
    }

    public static void e(String TAG, String msg){
        if(Constants.LOGGING) {
            if (TAG != null && msg != null)
                Log.e(TAG, msg);
        }
    }

    public static void i(String TAG, String msg){
        if(Constants.LOGGING) {
            if (TAG != null && msg != null)
                Log.i(TAG, msg);
        }
    }


    public static void wtf(String TAG, String msg){
        if(Constants.LOGGING) {
            if (TAG != null && msg != null)
                Log.wtf(TAG, msg);
        }
    }

    public static void v(String TAG, String msg){
        if(Constants.LOGGING) {
            if (TAG != null && msg != null)
                Log.v(TAG, msg);
        }
    }
}
