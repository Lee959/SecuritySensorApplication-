package com.example.securitysensorapplication;

/**
 * Bean class for motion sensor data
 */
public class MotionSensorBean {
    private String ieee;        // Device physical address
    private int ep;             // Device endpoint
    private boolean detected;   // Motion detection status
    private long timestamp;     // Timestamp of the last detection

    public MotionSensorBean() {
        this.detected = false;
        this.timestamp = System.currentTimeMillis();
    }

    public MotionSensorBean(String ieee, int ep, boolean detected) {
        this.ieee = ieee;
        this.ep = ep;
        this.detected = detected;
        this.timestamp = System.currentTimeMillis();
    }

    public String getIeee() {
        return ieee;
    }

    public void setIeee(String ieee) {
        this.ieee = ieee;
    }

    public int getEp() {
        return ep;
    }

    public void setEp(int ep) {
        this.ep = ep;
    }

    public boolean isDetected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
        if (detected) {
            this.timestamp = System.currentTimeMillis();
        }
    }

    public long getTimestamp() {
        return timestamp;
    }
}
