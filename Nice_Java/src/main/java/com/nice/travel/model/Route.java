package com.nice.travel.model;

public class Route {
    private String source;
    private String destination;
    private String mode;
    private String departureTime;
    private String arrivalTime;
    private int cost;
    private int duration;

    public Route(String source, String destination, String mode, String departureTime, String arrivalTime, int cost, int duration) {
        this.source = source;
        this.destination = destination;
        this.mode = mode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.cost = cost;
        this.duration = duration;
    }

    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getMode() { return mode; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public int getCost() { return cost; }
    public int getDuration() { return duration; }
}