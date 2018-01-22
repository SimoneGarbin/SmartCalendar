package com.example.andrea.smartcalendar.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.andrea.smartcalendar.R;
import com.example.andrea.smartcalendar.eventsdb.DBManager;
import com.example.andrea.smartcalendar.eventsdb.Event;
import com.example.andrea.smartcalendar.utilites.UpdatePosition;


public class MainActivity extends AppCompatActivity {

    private String date = "";
    private int i;
    private int selectedYear = 0;
    private int selectedMonth = 0;
    private int selectedDay = 0;
    ListView listView;
    private Location lastKnownLoc;
    private DBManager manager;
    public static final int MY_PERMISSIONS_REQUEST = 123;
    static public Context context;
    ArrayList<Event> values;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView title = (TextView) findViewById(R.id.lbl_title);
        title.setText(R.string.app_name);
        checkPermission();
        CalendarView myCalendar = (CalendarView) findViewById(R.id.calendar_id);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                mMessageReceiver, new IntentFilter("GPSLocationUpdates"));
        Calendar c = Calendar.getInstance();
        selectedYear = c.get(Calendar.YEAR);
        selectedMonth = c.get(Calendar.MONTH);
        selectedDay = c.get(Calendar.DAY_OF_MONTH);
        GetEventList(selectedYear, selectedMonth, selectedDay);

        //when the user select a different date, change the selected date to the chosen one and show it
        myCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;
                GetEventList(year, month, dayOfMonth);
            }
               // List<Event> allvalues = manager.getAllEvents();



                //System.out.println("LISTA " +  values.get(1).toString());
        });
    }

    public void addCalendarEvent(View view) {
        CalendarView myCalendar = (CalendarView) findViewById(R.id.calendar_id);

        //if the user has not selected a date, use current day
        if(date.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            date = sdf.format(new Date(myCalendar.getDate()));
        }

        Intent newEventActivity = new Intent(MainActivity.this, CreateEvent.class);
        newEventActivity.putExtra("selected_date", date);
        Bundle b = new Bundle();
        b.putParcelable("Location", lastKnownLoc);
        newEventActivity.putExtra("Location", b);
        startActivity(newEventActivity);
    }

   public void openEventsMap(View view) {
       Long time= System.currentTimeMillis();
       String timestring = time.toString();
       manager.open();
       ArrayList<Event> events = (ArrayList<Event>) manager.getEventsForMap(date, timestring);
       manager.close();
       Intent viewEventsMap = new Intent (MainActivity.this, ViewEventsMap.class);
       viewEventsMap.putExtra("events", events);
       Bundle b = new Bundle();
       b.putParcelable("Location", lastKnownLoc);
       viewEventsMap.putExtra("Location", b);
       startActivity(viewEventsMap);
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_item_one) {
            // your code
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getBundleExtra("Location");
            lastKnownLoc = (Location) b.getParcelable("Location");
        }
    };

    private void GetEventList (int year, int month, int day) {
        date = String.format("%02d", day) + "/" + String.format("%02d", month + 1) +"/" + year;
        listView = (ListView) findViewById(R.id.list);
        manager = new DBManager(getApplicationContext());
        manager.open();
        values = (ArrayList<Event>) manager.getDayEvents(date);
        if (values.size() !=0) {
            for (i = 0; i < values.size(); i++) {
                ArrayAdapter<Event> adapter = new ArrayAdapter<Event>(MainActivity.this, android.R.layout.simple_list_item_1, values);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent,View view,int position,long id){

                        Intent newEventActivity = new Intent(MainActivity.this, ViewEvent.class);
                        newEventActivity.putExtra("selected_date", date);

                        Event  itemValue    =  (Event) listView.getItemAtPosition(position);

                        newEventActivity.putExtra("id", itemValue.getId());
                        Bundle b = new Bundle();
                        b.putParcelable("Location", lastKnownLoc);
                        newEventActivity.putExtra("Location", b);
                        startActivity(newEventActivity);
                    }
                });
            }
        }

        else
            listView.setAdapter(null);
        manager.close();
    }

    @Override
    protected void onPause(){
        super.onPause();

    }
    @Override
    protected void onResume(){
        GetEventList(selectedYear, selectedMonth, selectedDay);
        super.onResume();

    }
    @Override
    protected void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();

    }

    public void checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST);
                // return false;
            }
            else {
                startService(new Intent(MainActivity.this, UpdatePosition.class));
                // return true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123 : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    startService(new Intent(MainActivity.this, UpdatePosition.class));
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();

                } else {
                    finish();
                    // permission denied
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

}












