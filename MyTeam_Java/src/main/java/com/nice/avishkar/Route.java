package com.nice.avishkar;

/**
 * POJO representing a travel route
 */
public class Route {
    private String origin;
    private String destination;
    private double distance;
    private double travelTime;
    
    public Route() {}
    
    public Route(String origin, String destination, double distance, double travelTime) {
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.travelTime = travelTime;
    }
    
    public String getOrigin() {
        return origin;
    }
    
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public double getTravelTime() {
        return travelTime;
    }
    
    public void setTravelTime(double travelTime) {
        this.travelTime = travelTime;
    }
    
    @Override
    public String toString() {
        return String.format("Route{origin='%s', destination='%s', distance=%.2f, travelTime=%.2f}", 
                           origin, destination, distance, travelTime);
    }
}