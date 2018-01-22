package com.example.andrea.smartcalendar.utilites;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;

import com.example.andrea.smartcalendar.R;
import com.example.andrea.smartcalendar.alarms.AlarmSender;
import com.example.andrea.smartcalendar.eventsdb.DBManager;
import com.example.andrea.smartcalendar.eventsdb.Event;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 04/01/18.
 */

//service that update position, set alarms based on position and send postion to main activity
public class UpdatePosition extends Service {

    private static final String TAG = "TESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 500;
    private Event event;
    Context context;
    private Integer id;
    private AlarmSender alarm;
    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        // when location change send new location to activity and set alarm based on new position
        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            DBManager manager = new DBManager(getApplicationContext());
            manager.open();
            Long time= System.currentTimeMillis();
            String timestring = time.toString();
            ArrayList<Event> values = (ArrayList<Event>) manager.getNotificationEvents(timestring);
            String transportation;
            if (values.size() !=0) {
                for (int i = 0; i < values.size(); i++) {
                    event = values.get(i);
                    if (alarm.checkExisistingAlarm((int) (long) event.getId())) {
                        double latitude = 0;
                        double longitude = 0;
                        List<Address> address = GeocoderClass.getAddress(event.getPosition(), UpdatePosition.this);
                        if (address != null) {
                            latitude = address.get(0).getLatitude();
                            longitude = address.get(0).getLongitude();
                        }
                        LatLng latlng = new LatLng(latitude, longitude);
                        LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                        if (event.getTransport().equals(getResources().getString(R.string.walk)))
                            transportation = "walking";
                        else
                            transportation = "driving";

                        String url = UrlGetter.getDirectionsUrl(origin, latlng, transportation);
                        id = (int) (long) event.getId();
                        DistanceDuration distance = new DistanceDuration();
                        distance.DistanceDurationText(event, getApplicationContext(), url, id);
                    }

                }
            }
            manager.close();
            mLastLocation.set(location);
            sendMessageToActivity(mLastLocation);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        context= getApplicationContext();
        Log.e(TAG, "onStartCommand");
        alarm = new AlarmSender(context);
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }


    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    //send position to main activity
    private void sendMessageToActivity(Location l) {
        Intent intent = new Intent("GPSLocationUpdates");
        Bundle b = new Bundle();
        b.putParcelable("Location", l);
        intent.putExtra("Location", b);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}

