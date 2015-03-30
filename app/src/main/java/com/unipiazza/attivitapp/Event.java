package com.unipiazza.attivitapp;

/**
 * Created by monossido on 30/03/15.
 */
public class Event {

    private int id;
    private String title;

    public Event(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }
}
