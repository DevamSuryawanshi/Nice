package com.nice.travel.util;

public class TimeUtil {
    
    public static int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
    
    public static int calculateDuration(String departureTime, String arrivalTime) {
        int depMinutes = timeToMinutes(departureTime);
        int arrMinutes = timeToMinutes(arrivalTime);
        return arrMinutes >= depMinutes ? arrMinutes - depMinutes : (24 * 60) - depMinutes + arrMinutes;
    }
    
    public static int calculateWaitingTime(String arrivalTime, String departureTime) {
        int arrMinutes = timeToMinutes(arrivalTime);
        int depMinutes = timeToMinutes(departureTime);
        return depMinutes >= arrMinutes ? depMinutes - arrMinutes : (24 * 60) - arrMinutes + depMinutes;
    }
}