package com.example.andrea.smartcalendar.utilites;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
/**
 * Created by simone on 16/01/18.
 */

// return address coordinates
public class GeocoderClass {
    public static List<Address> getAddress (String position, Context context) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = null;

        if (!Geocoder.isPresent())
            Log.d("GEOCODER", "NO GEOCODER");
        else {
            try {
                addresses = geocoder.getFromLocationName(position, 1);
            } catch (IOException e) {
                Log.d("GEOCODER", "IO Exception", e);
            }
        }
        return addresses;
    }
}
