package com.example.andrea.smartcalendar.utilites;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.content.Context;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import com.example.andrea.smartcalendar.R;
import com.example.andrea.smartcalendar.alarms.AlarmSender;
import com.example.andrea.smartcalendar.eventsdb.Event;
import com.example.andrea.smartcalendar.utilites.CheckConnection;
import com.example.andrea.smartcalendar.utilites.DirectionsJSONParser;
import com.example.andrea.smartcalendar.utilites.DirectionsLines;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by simone on 30/12/17.
 */

public class DistanceDuration {

    private String distance = "";
    private String duration = "";
    private String durationvalue = "";
    private TextView textDuration;
    private Handler handler;
    private Boolean position;
    int id;
    private Event event;
    private Context context;
    private int call = 0;
    private GoogleMap mMap = null;
    private HashMap<Integer, String> durationMap = new HashMap<Integer, String>();
    private int counter = 0;

    public void DistanceDurationView(TextView textDuration, Context context, String url, Handler handler, Boolean position) {
        this.textDuration = textDuration;
        this.handler = handler;
        this.position = position;
        this. context = context;
        this.call = 1;
        if (CheckConnection.isNetworkAvailable(context)) {
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        }
        else{
            NoInternetDialog();
        }
    }

    public void DistanceDurationText(Event event, Context context, String url, int id) {
        this.id = id;
        this.event = event;
        this.context = context;
        this.call = 2;
        if (CheckConnection.isNetworkAvailable(context)) {
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        }
        else{
            NoInternetDialog();
        }
    }

    public void DistanceDurationLines (String url, GoogleMap mMap, Handler handler){
        this.mMap = mMap;
        this.call = 3;
        this.handler = handler;
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {


        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (call == 1){
                ParserTaskView parserTask = new ParserTaskView();
                parserTask.execute(result);

            }
            else if (call == 2) {
                ParserTaskText parserTask = new ParserTaskText();
                parserTask.execute(result);
            }
            else if (call == 3) {
                ParserTaskLines parserTask = new ParserTaskLines();
                parserTask.execute(result);
            }

            // Invokes the thread for parsing the JSON data
        }


        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            } catch (Exception e) {
                Log.d("Excep while down url", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    // return duration of travel and distance
    private class ParserTaskView extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    } else if (j == 2) { // Get duration from the list
                        durationvalue = (String) point.get("durationValue");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
               String text;
                if (!position)
                    text = distance + " " + duration;
                else {
                    text = durationvalue;
                }
                textDuration.setText(text);
                handler.sendEmptyMessage(0);

            }
        }
    }

    // set alarm based on duration of travel
    private class ParserTaskText extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    } else if (j == 2) { // Get duration from the list
                        durationvalue = (String) point.get("durationValue");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                long millisecondsTime = Long.parseLong(event.getMilliseconds_time());
                int notificationTimes = Integer.parseInt(durationvalue);
                long timeNotification = 0;
                if (!event.getTime_notification().equals("")) {
                    timeNotification = Long.parseLong(event.getTime_notification());
                    timeNotification = TimeUnit.MINUTES.toMillis(timeNotification);
                }
                long when = millisecondsTime - notificationTimes * 1000 - timeNotification;
                AlarmSender alarm = new AlarmSender(context);
                String eventInitialHour = event.getInitial_hour();
                String eventFinalHour = event.getFinal_hour();
                final String notificationText = eventInitialHour + " - " + eventFinalHour;
                alarm.setOneTimeAlarm(id, when, event.getName(), notificationText);
            }
        }
    }

    //create lines of routes in map
    private class ParserTaskLines extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsLines parser = new DirectionsLines();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        String duration = (String) point.get("duration");
                        durationMap.put(counter, duration);
                        counter ++;
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
            int what = 1;
            Message msg = handler.obtainMessage(what);
            Bundle b = new Bundle();
            b.putSerializable("DurationMap", durationMap);
            msg.setData(b);
            handler.sendMessage(msg);
        }

    }

    private void NoInternetDialog () {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.no_connection))
                .setMessage(context.getResources().getString(R.string.no_connection_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
