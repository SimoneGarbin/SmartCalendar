package com.example.andrea.smartcalendar.calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.content.ContentValues;

import com.example.andrea.smartcalendar.utilites.GeocoderClass;
import com.example.andrea.smartcalendar.R;
import com.example.andrea.smartcalendar.utilites.DistanceDuration;
import com.example.andrea.smartcalendar.utilites.UrlGetter;
import com.example.andrea.smartcalendar.alarms.AlarmSender;
import com.example.andrea.smartcalendar.eventsdb.DBManager;
import com.example.andrea.smartcalendar.eventsdb.Event;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by simone on 13/12/17.
 */

// modify a selected event
public class ModifyEvent extends Activity {

    Event event= null;
    Date date= null;
    Calendar cal = Calendar.getInstance();
    TextView eventName;
    TextView eventPosition;
    Button initialDateButton;
    Button finalDateButton;
    Button initialHourButton;
    Button finalHourButton;
    Spinner transportSpinner;
    String receivedId;
    Spinner notificationType;
    TextView notificationTime;
    private String notificationEnabled = "0";
    private String positionNotificationEnabled = "0";
    AlarmSender alarm;
    int id;
    String name;
    Date datetime;
    private Context context;
    private Location lastKnownLoc;
    private Integer selectedTime = 0;
    private DBManager manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_event);

        Intent eventIntent = getIntent();
        receivedId = eventIntent.getExtras().getString("id");
        Bundle b = eventIntent.getBundleExtra("Location");
        lastKnownLoc = (Location) b.getParcelable("Location");

        manager = new DBManager(getApplicationContext());
        manager.open();

        event = manager.getIdEvent(receivedId);


        eventName = (TextView) findViewById(R.id.modify_event_name);
        eventName.setText(event.getName());
        eventPosition = (TextView) findViewById(R.id.modify_event_position);
        eventPosition.setText(event.getPosition());

        initialDateButton = (Button) findViewById(R.id.modify_event_initial_date);
        finalDateButton = (Button) findViewById(R.id.modify_event_final_date);
        initialDateButton.setText(event.getInitial_date());
        finalDateButton.setText(event.getFinal_date());

        //set hours buttons
        initialHourButton = (Button)findViewById(R.id.modify_event_initial_hour);
        finalHourButton = (Button)findViewById(R.id.modify_event_final_hour);
        initialHourButton.setText(event.getInitial_hour());
        finalHourButton.setText(event.getFinal_hour());

        //set dropdown spinner
        String[] transports = new String[] {getString(R.string.car), getString(R.string.walk)};
        transportSpinner = (Spinner) findViewById(R.id.modify_spinner_transports);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, transports);
        transportSpinner.setAdapter(adapter);
        transportSpinner.setSelection(adapter.getPosition(event.getTransport()));


        notificationType = (Spinner) findViewById(R.id.modify_notification_type);
        String[] notificationTypes = new String[] {getString(R.string.minutes), getString(R.string.hours), getString(R.string.days), getString(R.string.weeks)};
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, notificationTypes);
        notificationType.setAdapter(typesAdapter);
        notificationType.setEnabled(false);
        notificationType.setSelection(typesAdapter.getPosition(event.getType_notification()));


        final Switch enableNotification = (Switch) findViewById(R.id.modify_activate_notification);
        final Switch enablePositionNotification = (Switch) findViewById(R.id.modify_activate_position_notification);
         notificationTime = (EditText) findViewById(R.id.modify_notification_time);

        if (event.getNotification().equals("0")) {
            enableNotification.setChecked(false);
            notificationEnabled = "0";
        }
        else if (event.getNotification().equals("1")){
            enableNotification.setChecked(true);
            enablePositionNotification.setEnabled(true);
            notificationTime.setEnabled(true);
            notificationType.setEnabled(true);
            notificationTime.setText(event.getTime_notification());
            notificationEnabled = "1";
            if (event.getPosition_notification().equals("1")) {
                enablePositionNotification.setChecked(true);
                positionNotificationEnabled = "1";
            }
        }

        enableNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (enableNotification.isChecked()) {
                    enablePositionNotification.setEnabled(true);
                    notificationTime.setEnabled(true);
                    notificationType.setEnabled(true);
                    notificationEnabled = "1";

                }
                else {
                    enablePositionNotification.setEnabled(false);
                    notificationTime.setEnabled(false);
                    notificationType.setEnabled(false);
                    notificationEnabled = "0";

                }
            }
        });

        enablePositionNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (enablePositionNotification.isChecked()) {
                    if (lastKnownLoc == null) {
                        enablePositionNotification.setChecked(false);
                        positionNotificationEnabled = "0";
                        AlertDialog.Builder builder;
                        builder = new AlertDialog.Builder(ModifyEvent.this);
                        builder.setTitle(getResources().getString(R.string.no_gps))
                                .setMessage(getResources().getString(R.string.no_gps_message))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                    else
                        positionNotificationEnabled = "1";
                }
                else {
                    positionNotificationEnabled = "0";
                }
            }

        });

        //datetime = Calendar.getInstance().getTime();

        SimpleDateFormat sdfDatetime = new SimpleDateFormat("dd/MM/yyyy kk:mm");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("kk:mm");
        int hour =0;
        int minute = 0;
        String time = initialHourButton.getText().toString();
        try {
            hour = sdfTime.parse(time).getHours();
            minute = sdfTime.parse(time).getMinutes();
            System.out.println("OREEEE " + hour);
            System.out.println("MINUTIIII " + minute);
        }
        catch (ParseException e){
            e.printStackTrace();
        }

        String selectedTime = String.format("%02d", hour) + ":" + String.format("%02d", minute);
        String datetimeTemp;

        datetimeTemp = initialDateButton.getText() + " " + selectedTime;
        System.out.println("DATAAAA  " + datetimeTemp);
            try {
                datetime = sdfDatetime.parse(datetimeTemp);
            } catch(ParseException e) {
                e.printStackTrace();
            }

        manager.close();
    }

    //////////////////////////////////////
    //datepicker function
    //////////////////////////////////////
    private void datePicker(final Button selectedButton) {
        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                updateDate(dayOfMonth, monthOfYear + 1, year, selectedButton);
            }
        };

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try {
            date = sdf.parse(event.getInitial_date());
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cal = toCalendar(date);

        new DatePickerDialog(ModifyEvent.this, mDateListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    public static Calendar toCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    private void updateDate(int day, int month, int year, Button selectedButton) {
        String date = String.format("%02d", day) + "/" + String.format("%02d", month) + "/" + year;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        //if initial date, set final date equal to initial date
        if(selectedButton.getId() == R.id.modify_event_initial_date){
            Button finalDateButton = (Button) findViewById(R.id.modify_event_final_date);
            selectedButton.setText(date);
            finalDateButton.setText(date);

            //case of final date
        } else {
            Button initialDateButton = (Button) findViewById(R.id.modify_event_initial_date);

            try {
                Date finalDate = sdf.parse(date);
                Date initialDate = sdf.parse((String) initialDateButton.getText());

                if(finalDate.before(initialDate)){
                    Toast warningDateToast = Toast.makeText(getApplicationContext(), getString(R.string.error_date), Toast.LENGTH_LONG);
                    warningDateToast.show();
                } else {
                    selectedButton.setText(date);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    //change date functions
    public void setInitialDate(View view) {
        Button initialDateButton = (Button) findViewById(R.id.modify_event_initial_date);
        datePicker(initialDateButton);
    }

    public void setFinaleDate(View view) {
        Button finalDateButton = (Button) findViewById(R.id.modify_event_final_date);
        datePicker(finalDateButton);
    }

    //timepicker function
    private void timePicker(final Button selectedButton) {
        TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                updateTime(hourOfDay, minute, selectedButton);
            }
        };

        new TimePickerDialog(ModifyEvent.this, mTimeListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), true).show();
    }


    private void updateTime(int hour, int minute, Button selectedButton) {
        //date buttons
        Button initialDateButton = (Button) findViewById(R.id.modify_event_initial_date);
        Button finalDateButton = (Button) findViewById(R.id.modify_event_final_date);

        Calendar mCalendar = Calendar.getInstance();

        //date formats
        SimpleDateFormat sdfDatetime = new SimpleDateFormat("dd/MM/yyyy kk:mm");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("kk:mm");

        String selectedTime = String.format("%02d", hour) + ":" + String.format("%02d", minute);
        String datetimeTemp;
        Date finalDatetime, initialDatetime;

        //selected the initial hour
        if(selectedButton.getId() == R.id.modify_event_initial_hour){
            datetimeTemp = initialDateButton.getText() + " " + selectedTime;
            Button finalHourButton = (Button) findViewById(R.id.modify_event_final_hour);

            try {
                //increment of one hour the selected time
                datetime = sdfDatetime.parse(datetimeTemp);
                mCalendar.setTime(datetime);
                mCalendar.add(Calendar.HOUR, 1);

                //get the updated datetime
                finalDatetime = mCalendar.getTime();
                String finalDate = sdf.format(finalDatetime);
                String finalHour = sdfTime.format(finalDatetime);

                //set date and time
                finalDateButton.setText(finalDate);
                finalHourButton.setText(finalHour);

                selectedButton.setText(selectedTime);

            } catch(ParseException e) {
                e.printStackTrace();
            }

            //selected the final hour
        } else {
            Button initialHourButton = (Button) findViewById(R.id.modify_event_initial_hour);
            datetimeTemp = finalDateButton.getText() + " " + selectedTime;

            try {
                //set initial and final datetime
                finalDatetime = sdfDatetime.parse(datetimeTemp);
                initialDatetime = sdfDatetime.parse((String) finalDateButton.getText() + " " + (String) initialHourButton.getText());

                //check if selected final datetime is before initial datetime
                if(finalDatetime.before(initialDatetime)){
                    Toast warningDateToast = Toast.makeText(getApplicationContext(), getString(R.string.error_hour), Toast.LENGTH_LONG);
                    warningDateToast.show();
                } else {
                    selectedButton.setText(selectedTime);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    //change time functions
    public void setInitialHour(View view) {
        Button setInitialHour = (Button) findViewById(R.id.modify_event_initial_hour);
        timePicker(setInitialHour);
    }

    public void setFinalHour(View view) {
        Button setFinalHour = (Button) findViewById(R.id.modify_event_final_hour);
        timePicker(setFinalHour);
    }


    public void modifyEvent(View view) {

        manager = new DBManager(getApplicationContext());
        manager.open();
        SQLiteDatabase database;
        Long datetimeint = (datetime.getTime());
        String datetimestring = datetimeint.toString();
        database = manager.getDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", eventName.getText().toString());
        contentValues.put("position", eventPosition.getText().toString());
        contentValues.put("initial_date", initialDateButton.getText().toString());
        contentValues.put("final_date", finalDateButton.getText().toString());
        contentValues.put("initial_hour", initialHourButton.getText().toString());
        contentValues.put("final_hour", finalHourButton.getText().toString());
        contentValues.put("transport", transportSpinner.getSelectedItem().toString());
        contentValues.put("notification", notificationEnabled);
        contentValues.put("position_notification", positionNotificationEnabled);
        contentValues.put("time_notification", notificationTime.getText().toString());
        contentValues.put("type_notification", notificationType.getSelectedItem().toString());
        contentValues.put("milliseconds_time", datetimestring);

        database.update("Events", contentValues, "_id" + " = ?", new String[]{receivedId});

        if (notificationEnabled.equals("1")) {
            String eventInitialHour = initialHourButton.getText().toString();
            String eventFinalHour = finalHourButton.getText().toString();
            final String notificationText = eventInitialHour + " - " + eventFinalHour;
            alarm = new AlarmSender(getApplicationContext());
            id = Integer.parseInt(receivedId);

            name = eventName.getText().toString();
            if (!notificationTime.getText().toString().equals("")) {
                selectedTime = Integer.parseInt(notificationTime.getText().toString());
            }

            if (positionNotificationEnabled.equals("1")) {

                setPositionNotification(notificationText);

            } else {
                setNormalNotification(notificationText);

            }
        }
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.event_modified), Toast.LENGTH_SHORT).show();
        database.close();
        manager.close();
        finish();
    }

    private void setPositionNotification (final String notificationText) {
        double latitude = 0;
        double longitude = 0;
        List<Address> address = GeocoderClass.getAddress(eventPosition.getText().toString(), this);
        if (address != null) {
            latitude = address.get(0).getLatitude();
            longitude = address.get(0).getLongitude();
        }


        LatLng origin = new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
        LatLng latlng = new LatLng(latitude, longitude);

        String transportation;
        if (transportSpinner.getSelectedItem().toString().equals(getResources().getString(R.string.walk))) {
            transportation = "walking";
        } else
            transportation = "driving";
        String url = UrlGetter.getDirectionsUrl(origin, latlng, transportation);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        int notificationTimes = Integer.parseInt(notificationTime.getText().toString());
                        notificationTimes = notificationTimes / 60 + selectedTime;
                        alarm.createAlarm(datetime, 0, id, name, notificationTimes, notificationText);
                        break;
                    default:
                        break;
                }
            }
        };
        DistanceDuration distance = new DistanceDuration();
        distance.DistanceDurationView(notificationTime, this, url, handler, true);
    }

    private void setNormalNotification (String notificationText) {
        int typeOfNotification = notificationType.getSelectedItemPosition();
        alarm.createAlarm(datetime, typeOfNotification, id, name, selectedTime, notificationText);
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
        manager.close();
        super.onDestroy();
    }
}

