package com.nice.avishkar;

/**
 * POJO representing resource information
 */
public class ResourceInfo {
    private String resourceId;
    private String location;
    private String availability;
    private double capacity;
    
    public ResourceInfo() {}
    
    public ResourceInfo(String resourceId, String location, String availability, double capacity) {
        this.resourceId = resourceId;
        this.location = location;
        this.availability = availability;
        this.capacity = capacity;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getAvailability() {
        return availability;
    }
    
    public void setAvailability(String availability) {
        this.availability = availability;
    }
    
    public double getCapacity() {
        return capacity;
    }
    
    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }
    
    @Override
    public String toString() {
        return String.format("ResourceInfo{resourceId='%s', location='%s', availability='%s', capacity=%.2f}", 
                           resourceId, location, availability, capacity);
    }
}