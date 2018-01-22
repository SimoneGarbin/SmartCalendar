package com.example.andrea.smartcalendar.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Message;
import android.content.DialogInterface;

import com.example.andrea.smartcalendar.utilites.GeocoderClass;
import com.example.andrea.smartcalendar.R;
import com.example.andrea.smartcalendar.utilites.DistanceDuration;
import com.example.andrea.smartcalendar.utilites.UrlGetter;
import com.example.andrea.smartcalendar.alarms.AlarmSender;
import com.example.andrea.smartcalendar.eventsdb.DBManager;
import com.example.andrea.smartcalendar.eventsdb.Event;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Simone on 19/05/17.
 */

public class CreateEvent extends Activity{
    private Calendar calendarTemp = Calendar.getInstance();
    Event last;
    Date datetime;
    private String notificationEnabled = "0";
    private String positionNotificationEnabled = "0";
    Spinner notificationType;
    DBManager manager;
    int id;
    String name;
    AlarmSender alarm;
    EditText notificationTime;
    Context context;
    Location lastKnownLoc;
    private Integer selectedTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);

        Intent calendarIntent = getIntent();
        String receivedDate = calendarIntent.getExtras().getString("selected_date");
        Bundle b = calendarIntent.getBundleExtra("Location");
        lastKnownLoc = (Location) b.getParcelable("Location");

        //set date buttons
        Button initialDateButton = (Button) findViewById(R.id.event_initial_date);
        Button finalDateButton = (Button) findViewById(R.id.event_final_date);
        initialDateButton.setText(receivedDate);
        finalDateButton.setText(receivedDate);

        //get current hour
        int hour = calendarTemp.get(Calendar.HOUR_OF_DAY);
        int minutes = calendarTemp.get(Calendar.MINUTE);

        //set hours buttons
        Button initialHourButton = (Button)findViewById(R.id.event_initial_hour);
        Button finalHourButton = (Button)findViewById(R.id.event_final_hour);
        String initialHour = String.valueOf(hour) + ":" + minutes;
        String finalHour = String.valueOf(hour + 1) + ":" + minutes;
        initialHourButton.setText(initialHour);
        finalHourButton.setText(finalHour);

        //set transport spinner
        String[] transports = new String[] {getString(R.string.car), getString(R.string.walk)};
        Spinner dropdownSpinner = (Spinner) findViewById(R.id.spinner_transports);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, transports);
        dropdownSpinner.setAdapter(adapter);

        //set notification type spinner
        notificationType = (Spinner) findViewById(R.id.notification_type);
        String[] notificationTypes = new String[] {getString(R.string.minutes), getString(R.string.hours), getString(R.string.days), getString(R.string.weeks)};
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, notificationTypes);
        notificationType.setAdapter(typesAdapter);
        notificationType.setEnabled(false);

        final Switch enableNotification = (Switch) findViewById(R.id.activate_notification);
        final Switch enablePositionNotification = (Switch) findViewById(R.id.activate_position_notification);
        final EditText notificationTime = (EditText) findViewById(R.id.notification_time);

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
                        builder = new AlertDialog.Builder(CreateEvent.this);
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
                        positionNotificationEnabled = "1";
                        notificationType.setEnabled(false);
                    }
                }
                else
                    positionNotificationEnabled = "0";
            }

        });

        datetime = Calendar.getInstance().getTime();


    }

    //datepicker function
    private void datePicker(final Button selectedButton) {
        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                updateDate(dayOfMonth, monthOfYear + 1, year, selectedButton);
            }
        };

        new DatePickerDialog(CreateEvent.this, mDateListener,
                calendarTemp.get(Calendar.YEAR),
                calendarTemp.get(Calendar.MONTH),
                calendarTemp.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDate(int day, int month, int year, Button selectedButton) {
        String date = String.format("%02d", day) + "/" + String.format("%02d", month) + "/" + year;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        //if initial date, set final date equal to initial date
        if(selectedButton.getId() == R.id.event_initial_date){
            Button finalDateButton = (Button) findViewById(R.id.event_final_date);
            selectedButton.setText(date);
            finalDateButton.setText(date);

        //case of final date
        } else {
            Button initialDateButton = (Button) findViewById(R.id.event_initial_date);

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
        Button initialDateButton = (Button) findViewById(R.id.event_initial_date);
        datePicker(initialDateButton);
    }

    public void setFinaleDate(View view) {
        Button finalDateButton = (Button) findViewById(R.id.event_final_date);
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

        new TimePickerDialog(CreateEvent.this, mTimeListener,
                calendarTemp.get(Calendar.HOUR_OF_DAY),
                calendarTemp.get(Calendar.MINUTE), true).show();
    }

    private void updateTime(int hour, int minute, Button selectedButton) {
        //date buttons
        Button initialDateButton = (Button) findViewById(R.id.event_initial_date);
        Button finalDateButton = (Button) findViewById(R.id.event_final_date);

        Calendar mCalendar = Calendar.getInstance();

        //date formats
        SimpleDateFormat sdfDatetime = new SimpleDateFormat("dd/MM/yyyy kk:mm");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("kk:mm");

        String selectedTime = String.format("%02d", hour) + ":" + String.format("%02d", minute);
        String datetimeTemp;
        Date finalDatetime, initialDatetime;

        datetimeTemp = initialDateButton.getText() + " " + selectedTime;


        //selected the initial hour
        if(selectedButton.getId() == R.id.event_initial_hour){
            datetimeTemp = initialDateButton.getText() + " " + selectedTime;
            Button finalHourButton = (Button) findViewById(R.id.event_final_hour);

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
            Button initialHourButton = (Button) findViewById(R.id.event_initial_hour);
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
        Button setInitialHour = (Button) findViewById(R.id.event_initial_hour);
        timePicker(setInitialHour);
    }

    public void setFinalHour(View view) {
        Button setFinalHour = (Button) findViewById(R.id.event_final_hour);
        timePicker(setFinalHour);
    }

    //save the event on db
    public void saveEvent(View view) {
        EditText eventName = (EditText) findViewById(R.id.event_name);
        EditText eventPosition = (EditText) findViewById(R.id.event_position);
        Button initialDate = (Button) findViewById(R.id.event_initial_date);
        Button finalDate = (Button) findViewById(R.id.event_final_date);
        Button initialHour = (Button) findViewById(R.id.event_initial_hour);
        Button finalHour = (Button) findViewById(R.id.event_final_hour);
        Spinner transportSpinner = (Spinner) findViewById(R.id.spinner_transports);
        notificationTime = (EditText) findViewById(R.id.notification_time);
        Spinner notificationType = (Spinner) findViewById(R.id.notification_type);
        Long datetimeint = (datetime.getTime());
        String datetimestring = datetimeint.toString();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", eventName.getText().toString());
        map.put("position", eventPosition.getText().toString());
        map.put("initial_date", initialDate.getText().toString());
        map.put("final_date", finalDate.getText().toString());
        map.put("initial_hour", initialHour.getText().toString());
        map.put("final_hour", finalHour.getText().toString());
        map.put("transport", transportSpinner.getSelectedItem().toString());
        map.put("notification", notificationEnabled);
        map.put("position_notification", positionNotificationEnabled);
        map.put("time_notification", notificationTime.getText().toString());
        map.put("type_notification", notificationType.getSelectedItem().toString());
        map.put("milliseconds_time",datetimestring);

        manager = new DBManager(getApplicationContext());
        manager.open();
        manager.saveValues(map);


        if (notificationEnabled.equals("1")) {
            alarm = new AlarmSender(getApplicationContext());
            last = manager.getLastInsertEvent();
            String eventInitialHour = last.getInitial_hour();
            String eventFinalHour = last.getFinal_hour();
            final String notificationText = eventInitialHour + " - " + eventFinalHour;
            id = (int) last.getId();
            name = last.getName();

            if (positionNotificationEnabled.equals("1")) {
                setPositionNotification(notificationText);

                }
            else {
                setNormalNotification(notificationText);
            }
        }
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.event_saved), Toast.LENGTH_SHORT).show();
        manager.close();
        this.finish();
    }


    private void setPositionNotification (final String notificationText) { //set the notification based on the actual position
        double latitude = 0;
        double longitude = 0;
        String eventPosition = last.getPosition();

        List<Address> address = GeocoderClass.getAddress(eventPosition, this);
        if (address != null){
            latitude = address.get(0).getLatitude();
            longitude = address.get(0).getLongitude();
        }

        LatLng latlng = new LatLng(latitude, longitude);

        LatLng origin = new LatLng(lastKnownLoc.getLatitude(),lastKnownLoc.getLongitude());

        String transportation;
        if (last.getTransport().equals(getResources().getString(R.string.walk))){
            transportation = "walking";
        }
        else
            transportation = "driving";
        String url = UrlGetter.getDirectionsUrl(origin, latlng, transportation);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        int notificationTimes = Integer.parseInt(notificationTime.getText().toString());
                        notificationTimes = notificationTimes/60 + selectedTime;
                        alarm.createAlarm(datetime, 0, id, name, notificationTimes, notificationText);
                        break;
                }
            }
        };

        if (!notificationTime.getText().toString().equals(""))
            selectedTime = Integer.parseInt(notificationTime.getText().toString());

        DistanceDuration distance = new DistanceDuration();
        distance.DistanceDurationView(notificationTime, this, url, handler, true);
    }


    private void setNormalNotification (String notificationText) { //set the notification based on the selected time
        int typeOfNotification = notificationType.getSelectedItemPosition();
        int notificationTimes = Integer.parseInt(last.getTime_notification());
        alarm.createAlarm(datetime, typeOfNotification, id, name, notificationTimes, notificationText);
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
