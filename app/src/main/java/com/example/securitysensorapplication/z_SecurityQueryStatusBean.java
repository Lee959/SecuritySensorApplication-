package com.example.securitysensorapplication;

public class z_SecurityQueryStatusBean {
    private String status;  // "on" for armed, "off" for disarmed
    private String ieee;    // Device physical address
    private int ep;         // Device endpoint

    public z_SecurityQueryStatusBean() {
        this.status = "off"; // Default to disarmed
    }

    public z_SecurityQueryStatusBean(String status, String ieee, int ep) {
        this.status = status;
        this.ieee = ieee;
        this.ep = ep;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public boolean isArmed() {
        return "on".equals(status);
    }
}