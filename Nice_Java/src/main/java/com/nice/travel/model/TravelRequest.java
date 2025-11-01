package com.nice.travel.model;

public class TravelRequest {
    private String requestId;
    private String source;
    private String destination;
    private String criteria;

    public TravelRequest(String requestId, String source, String destination, String criteria) {
        this.requestId = requestId;
        this.source = source;
        this.destination = destination;
        this.criteria = criteria;
    }

    public String getRequestId() { return requestId; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getCriteria() { return criteria; }
}