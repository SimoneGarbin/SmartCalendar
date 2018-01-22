package com.example.andrea.smartcalendar.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.example.andrea.smartcalendar.utilites.GeocoderClass;
import com.example.andrea.smartcalendar.R;
import com.example.andrea.smartcalendar.utilites.CheckConnection;
import com.example.andrea.smartcalendar.utilites.DistanceDuration;
import com.example.andrea.smartcalendar.utilites.UrlGetter;
import com.example.andrea.smartcalendar.eventsdb.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.CameraUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by simone on 22/12/17.
 */

public class ViewEventsMap extends FragmentActivity implements OnMapReadyCallback, OnMarkerClickListener {

    private GoogleMap mMap = null;
    private  LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private double latitude = 0;
    private double longitude = 0;
    private LatLng origin = null;
    private Context context;
    private HashMap<Integer, String> durationMap = new HashMap<Integer, String>();
    private ArrayList<Event> eventsList;
    int counter = 0;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_events_map);

        Bundle b = getIntent().getBundleExtra("Location");
        Location lastKnownLoc = (Location) b.getParcelable("Location");
        if (!CheckConnection.isNetworkAvailable(this)) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.no_connection))
                    .setMessage(getResources().getString(R.string.no_connection_message))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else if (lastKnownLoc == null){
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.no_gps))
                    .setMessage(getResources().getString(R.string.no_gps_message))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {
            origin = new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
            eventsList = (ArrayList<Event>) getIntent().getExtras().getSerializable("events");

            if (mMap == null) {
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapevents))
                        .getMap();
                mMap.setOnMarkerClickListener(this);

            }
            placeMarker(getResources().getString(R.string.origin), "", lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude(), 0);
            if (eventsList.size() > 0) {
                DistanceDuration distance = new DistanceDuration();

                for (int i = 0; i < eventsList.size(); i++) {

                    List<Address> address = GeocoderClass.getAddress(eventsList.get(i).getPosition(),this);
                    latitude = address.get(0).getLatitude();
                    longitude = address.get(0).getLongitude();

                    LatLng dest = new LatLng(latitude, longitude);
                    String transportation;
                    if (eventsList.get(i).getTransport().equals(getResources().getString(R.string.walk))) {
                        transportation = "walking";
                    } else
                        transportation = "driving";
                    String url = UrlGetter.getDirectionsUrl(origin, dest, transportation);

                    Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case 1:
                                    durationMap = (HashMap) msg.getData().getSerializable("DurationMap");
                            }
                        }
                    };
                    distance.DistanceDurationLines(url, mMap, handler);
                    String snippet = eventsList.get(i).getInitial_hour() + " - " + eventsList.get(i).getFinal_hour();
                    placeMarker(eventsList.get(i).getName(), snippet, latitude, longitude, 120);

                    origin = new LatLng(latitude, longitude);

                }
                LatLngBounds bounds = builder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.20);

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                mMap.animateCamera(cu);
            } else
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin,
                        17));
        }
    }

    public void placeMarker (String title, String snippet, double lat, double lon, float hue) {
        LatLng latlng= new LatLng(lat, lon);
        Marker marker =  mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(hue)).title(title).snippet(snippet));
        marker.setAlpha(1);
        builder.include(marker.getPosition());
    }


    @Override
    public void onMapReady(GoogleMap googleMap ) {
        mMap = googleMap;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        for (int i=0; i<eventsList.size(); i++ ) {
            if (marker.getTitle().equals(eventsList.get(i).getName())){
                Toast.makeText(this,
                                getResources().getString(R.string.duration) + " " + durationMap.get(i),
                        Toast.LENGTH_SHORT).show();
                break;
            }

        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}

