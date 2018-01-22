package com.example.andrea.smartcalendar.calendar;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.os.Handler;
import android.widget.Toast;

import java.util.List;

import com.example.andrea.smartcalendar.utilites.GeocoderClass;
import com.example.andrea.smartcalendar.R;
import com.example.andrea.smartcalendar.utilites.DistanceDuration;
import com.example.andrea.smartcalendar.utilites.UrlGetter;
import com.example.andrea.smartcalendar.alarms.AlarmSender;
import com.example.andrea.smartcalendar.eventsdb.DBManager;
import com.example.andrea.smartcalendar.eventsdb.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by simone on 11/12/17.
 */

public class ViewEvent extends AppCompatActivity implements OnMapReadyCallback {


    String receivedIdString ="";
    private Event event;
    private GoogleMap mMap = null;
    private Context context;
    private Location lastKnownLoc;
    private boolean reload = false;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_event);
        Intent eventIntent = getIntent();
        Long receivedId = eventIntent.getExtras().getLong("id");
        receivedIdString = receivedId.toString();
        Bundle b = eventIntent.getBundleExtra("Location");
        lastKnownLoc = (Location) b.getParcelable("Location");
        TextView title = (TextView) findViewById(R.id.lbl_title);
        MapsInitializer.initialize(getApplicationContext());
        DBManager manager = new DBManager(getApplicationContext());
        manager.open();
        event = manager.getIdEvent(receivedIdString);
        title.setText(event.getName());

        TextView eventInitalDate = (TextView) findViewById(R.id.textInitial_Date);
        eventInitalDate.setText(event.getInitial_date());
        TextView eventFinalDate = (TextView) findViewById(R.id.textFinal_Date);
        eventFinalDate.setText(event.getFinal_date());
        TextView eventInitialHour = (TextView) findViewById(R.id.textInitial_Hour);
        eventInitialHour.setText(event.getInitial_hour());
        TextView eventFinalHour = (TextView) findViewById(R.id.textFinal_Hour);
        eventFinalHour.setText(event.getFinal_hour());
        TextView eventPosition = (TextView) findViewById(R.id.textPosition);
        eventPosition.setText(event.getPosition());
        TextView eventNotification = (TextView) findViewById(R.id.textNotification);
        if (event.getNotification().equals("0"))
            eventNotification.setText(R.string.no_notification);
        else {
            eventNotification.setText(event.getTime_notification());
            TextView eventTypeNotification = (TextView) findViewById(R.id.textTypeNotification);
            eventTypeNotification.setText(event.getType_notification());
        }

        if(event.getPosition_notification().equals("0")) {
            ImageView positonDeactivated = (ImageView) findViewById(R.id.imagePositionDeactivated);
            positonDeactivated.setVisibility(View.VISIBLE);
        }
        else{
            ImageView positonActivated = (ImageView) findViewById(R.id.imagePositionActivated);
            positonActivated.setVisibility(View.VISIBLE);
        }


        double latitude = 0;
        double longitude = 0;

        List<Address> address = null;
        if (!event.getPosition().equals(""))
            address = GeocoderClass.getAddress(event.getPosition(), this);

        if (address != null){
            latitude = address.get(0).getLatitude();
            longitude = address.get(0).getLongitude();
        }

        if (mMap == null) {

            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }

        placeMarker(event.getName(), latitude, longitude);
        manager.close();
    }


    public void placeMarker (String title, double lat, double lon) {
        LatLng latlng = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_GREEN)).title(title));
        // Zoom in
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,
                17));

        if (lastKnownLoc == null) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.no_gps))
                    .setMessage(getResources().getString(R.string.no_gps_message))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {
            LatLng origin = new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
            String transportation;
            if (event.getTransport().equals(getResources().getString(R.string.walk))){
                transportation = "walking";
            }
            else
                transportation = "driving";

            String url = UrlGetter.getDirectionsUrl(origin, latlng, transportation);
            TextView textDuration = (TextView) findViewById(R.id.duration);
            Handler handler = new Handler();
            DistanceDuration distance = new DistanceDuration();
            distance.DistanceDurationView(textDuration, this, url, handler, false);

        }
    }


    public void modifyCalendarEvent(View view) {
        Intent newEventActivity = new Intent(ViewEvent.this, ModifyEvent.class);
        newEventActivity.putExtra("id", receivedIdString);
        Bundle b = new Bundle();
        b.putParcelable("Location", lastKnownLoc);
        newEventActivity.putExtra("Location", b);
        startActivity(newEventActivity);
    }

    public void deleteCalendarEvent (View view) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(ViewEvent.this);
        builder.setTitle(getResources().getString(R.string.delete_entry))
                .setMessage(getResources().getString(R.string.delete_entry_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DBManager manager = new DBManager(getApplicationContext());
                        manager.open();
                        SQLiteDatabase database;
                        database = manager.getDatabase();
                        database.delete("Events","_id" + " = ?",new String[]{receivedIdString});
                        int id = Integer.parseInt(receivedIdString);
                        AlarmSender alarm = new AlarmSender(getApplicationContext());
                        alarm.deleteAlarm(id);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.event_deleted), Toast.LENGTH_SHORT).show();
                        manager.close();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap ) {
        mMap = googleMap;
    }

    @Override
    protected void onPause(){
        super.onPause();

    }
    @Override
    protected void onResume(){
        super.onResume();
        if(reload) {
            recreate();
        }
        reload = true;
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

    }
}
