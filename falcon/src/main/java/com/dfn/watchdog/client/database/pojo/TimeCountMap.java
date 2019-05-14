package com.dfn.watchdog.client.database.pojo;

import java.sql.Date;

/**
 * Created by dasunp on Nov, 2018
 */
public class TimeCountMap {
    private Date date;
    private int value;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
