package com.example.securitysensorapplication;

import java.util.List;

public class DeviceListBean {
    private List<EPListBean> devices;

    public DeviceListBean(List<EPListBean> devices) {
        this.devices = devices;
    }

    public List<EPListBean> getDevices() {
        return devices;
    }

    public void setDevices(List<EPListBean> devices) {
        this.devices = devices;
    }
}