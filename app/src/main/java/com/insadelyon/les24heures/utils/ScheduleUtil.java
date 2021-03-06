package com.insadelyon.les24heures.utils;

import com.insadelyon.les24heures.model.Schedule;

import java.util.List;

import static com.insadelyon.les24heures.utils.ScheduleToDateTimeConverter2015.getStart;

/**
 * Created by lbillon on 5/8/2015.
 */
public class ScheduleUtil {

    public static Schedule getNextSchedule(List<Schedule> schedules) throws Exception {
        for(Schedule schedule: schedules){
            if(getStart(schedule).isAfterNow()){
                return schedule;
            }
        }
        return null;
    }
}
