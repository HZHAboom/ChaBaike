package com.hzh.chabaike.utils;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by my on 2016/11/14.
 */

public class InternetUtils {
    public static boolean isConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo==null){
            return false;
        }
        switch (activeNetworkInfo.getType()){
            case ConnectivityManager.TYPE_WIFI:
                return true;
            case ConnectivityManager.TYPE_MOBILE:
                return true;
        }
        return false;
    }
}
