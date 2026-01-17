package com.example.user.client;

public class ExistsResponse {
    
    private boolean exists;

    public ExistsResponse() {}

    public ExistsResponse(boolean exists) {
        this.exists = exists;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
