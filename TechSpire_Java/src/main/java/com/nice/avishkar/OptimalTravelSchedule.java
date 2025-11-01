package com.nice.avishkar;

import java.util.List;

/**
 * POJO representing the optimal travel schedule result
 */
public class OptimalTravelSchedule {
    private List<Route> routes;
    private double totalDistance;
    private double totalTravelTime;
    private boolean isValid;
    private String errorMessage;
    
    public OptimalTravelSchedule() {}
    
    public OptimalTravelSchedule(List<Route> routes, double totalDistance, double totalTravelTime) {
        this.routes = routes;
        this.totalDistance = totalDistance;
        this.totalTravelTime = totalTravelTime;
        this.isValid = true;
    }
    
    public List<Route> getRoutes() {
        return routes;
    }
    
    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
    
    public double getTotalDistance() {
        return totalDistance;
    }
    
    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }
    
    public double getTotalTravelTime() {
        return totalTravelTime;
    }
    
    public void setTotalTravelTime(double totalTravelTime) {
        this.totalTravelTime = totalTravelTime;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public void setValid(boolean valid) {
        isValid = valid;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.isValid = false;
    }
    
    @Override
    public String toString() {
        return String.format("OptimalTravelSchedule{routes=%s, totalDistance=%.2f, totalTravelTime=%.2f, isValid=%s}", 
                           routes, totalDistance, totalTravelTime, isValid);
    }
}