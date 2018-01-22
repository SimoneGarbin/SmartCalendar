package com.example.andrea.smartcalendar.utilites;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by simone on 16/01/18.
 */

// get url for request directions
public class UrlGetter {
    public static String getDirectionsUrl(LatLng origin, LatLng dest, String transportation){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&mode="+transportation;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&key=AIzaSyAY3NyGCoIG9_tQL5GHw1mOtdTt1UYfQ6Y";

        return url;
    }
}
