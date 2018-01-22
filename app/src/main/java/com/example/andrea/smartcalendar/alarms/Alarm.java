package com.example.andrea.smartcalendar.alarms;

/**
 * Created by simone on 08/01/18.
 */

public class Alarm {
    private int id;
    private String name;
    private String when;
    private String text;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getWhen() {
        return when;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
