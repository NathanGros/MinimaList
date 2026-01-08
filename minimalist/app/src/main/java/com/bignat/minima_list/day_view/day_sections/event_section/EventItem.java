package com.bignat.minima_list.day_view.day_sections.event_section;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class EventItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public Date startDate;
    public Date endDate;
    public boolean isAllDay;
    public boolean optional;
    public boolean repeat;
    public int nbDaysRepeat;

    public EventItem(String title, Date startDate, Date endDate) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.optional = false;
        this.repeat = false;
        this.nbDaysRepeat = 1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public int getNbDaysRepeat() {
        return nbDaysRepeat;
    }

    public void setNbDaysRepeat(int nbDaysRepeat) {
        this.nbDaysRepeat = nbDaysRepeat;
    }
}
