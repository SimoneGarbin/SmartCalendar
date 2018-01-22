package com.example.andrea.smartcalendar.eventsdb;

import java.io.Serializable;

/**
 * Created by simone on 06/12/17.
 */

public class Event implements Serializable {
    private long id;
    private String name;
    private String position;
    private String initial_date;
    private String final_date;
    private String initial_hour;
    private String final_hour;
    private String transport;
    private String notification;
    private String position_notification;
    private String time_notification;
    private String type_notification;
    private String milliseconds_time;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }

    public void setInitial_date(String initial_date) {
        this.initial_date = initial_date;
    }

    public String getInitial_date() {
        return initial_date;
    }

    public void setFinal_date(String final_date) {
        this.final_date = final_date;
    }

    public String getFinal_date() {
        return final_date;
    }

    public void setInitial_hour(String initial_hour) {
        this.initial_hour = initial_hour;
    }

    public String getInitial_hour() {
        return initial_hour;
    }

    public void setFinal_hour(String final_hour) {this.final_hour = final_hour;}

    public String getFinal_hour() {
        return final_hour;
    }

    public void setTransport(String transport) {this.transport = transport;}

    public String getTransport() {return transport;}

    public void setNotification(String notification) {this.notification = notification;}

    public String getNotification() {return notification;}

    public void setPosition_notification(String position_notification) {this.position_notification = position_notification;}

    public String getPosition_notification() {return position_notification;}

    public void setTime_notification(String time_notification) {this.time_notification = time_notification;}

    public String getTime_notification() {return time_notification;}

    public void setType_notification(String type_notification) {this.type_notification = type_notification;}

    public String getType_notification() {return type_notification;}

    public void setMilliseconds_time(String milliseconds_time) {this.milliseconds_time = milliseconds_time;}

    public String getMilliseconds_time() {return milliseconds_time;}

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {return name + " , " + position + " " + initial_date + " " + final_date + " " + initial_hour + " " + final_hour + " " + transport;}

}
