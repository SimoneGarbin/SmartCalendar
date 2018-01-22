package com.example.andrea.smartcalendar.utilites;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
/**
 * Created by simone on 15/01/18.
 */

// check internet connection
public class CheckConnection { //check if internet connection is available
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}