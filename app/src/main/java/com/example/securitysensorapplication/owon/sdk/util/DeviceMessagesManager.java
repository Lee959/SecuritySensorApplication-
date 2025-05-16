package com.example.securitysensorapplication.owon.sdk.util;

import android.util.Log;

import com.example.securitysensorapplication.DeviceListBean;
import com.example.securitysensorapplication.DeviceTypeCode;
import com.example.securitysensorapplication.EPListBean;
import com.example.securitysensorapplication.MotionSensorBean;
import com.example.securitysensorapplication.z_SecurityQueryStatusBean;
import com.example.securitysensorapplication.SmokeDetectorBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DeviceMessagesManager {
    private static final String TAG = "DeviceMessagesManager";
    private static DeviceMessagesManager instance;
    private List<SocketMessageListener> listeners;
    private Map<String, EPListBean> deviceMap;
    private z_SecurityQueryStatusBean securityStatus;
    private Map<String, MotionSensorBean> motionSensors;
    private Map<String, SmokeDetectorBean> smokeDetectors;

    private DeviceMessagesManager() {
        listeners = new ArrayList<>();
        deviceMap = new HashMap<>();
        securityStatus = new z_SecurityQueryStatusBean();
        motionSensors = new HashMap<>();
        smokeDetectors = new HashMap<>();
    }

    public static synchronized DeviceMessagesManager getInstance() {
        if (instance == null) {
            instance = new DeviceMessagesManager();
        }
        return instance;
    }

    public void registerMessageListener(SocketMessageListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterMessageListener(SocketMessageListener listener) {
        listeners.remove(listener);
    }

    // Simulate successful login
    public void LoginSocket(Object context, String account, String password) {
        Log.d(TAG, "Login attempt with: " + account + ", " + password);

        Object loginResponse = new Object();
        for (SocketMessageListener listener : listeners) {
            listener.getMessage(100, loginResponse); // 100 is success code for login
        }
    }

    /**
     * Get the list of devices
     */
    public void GetEpList() {
        Log.d(TAG, "GetEpList called");
        simulateDeviceListResponse();
    }

    /**
     * Get the state of a device
     * @param device The device to query
     * @param cache Use cache flag (0 for no cache, 1 for using cache)
     */
    public void getDeviceState(EPListBean device, int cache) {
        Log.d(TAG, "getDeviceState called for device: " + device.getName());

        if (device.getDeviceType() == DeviceTypeCode.MOTION_SENSOR_ZONE) {
            simulateMotionSensorStatus(device);
        } else if (device.getDeviceType() == DeviceTypeCode.SMOKE_SENSOR_ZONE) {
            simulateSmokeDetectorStatus(device);
        } else if (device.getDeviceType() == DeviceTypeCode.WARN_SENSOR) {
            simulateSecurityStatus(device);
        }
    }

    /**
     * Set security system to armed mode
     * @param zoneID Zone identifier, default 255
     */
    public void SecurityDeployment(int zoneID) {
        Log.d(TAG, "SecurityDeployment called for zone: " + zoneID);

        securityStatus.setStatus("on");

        // Notify listeners
        for (SocketMessageListener listener : listeners) {
            listener.getMessage(Constants.UpdateSwitchgear, securityStatus);
        }
    }

    /**
     * Set security system to disarmed mode
     * @param zoneID Zone identifier, default 255
     */
    public void SecurityDisarming(int zoneID) {
        Log.d(TAG, "SecurityDisarming called for zone: " + zoneID);

        securityStatus.setStatus("off");

        // Notify listeners
        for (SocketMessageListener listener : listeners) {
            listener.getMessage(Constants.UpdateSwitchgear, securityStatus);
        }
    }

    /* ---------- Helper methods for creating simulated responses ---------- */

    private void simulateDeviceListResponse() {
        // Create a list of simulated devices
        List<EPListBean> devices = new ArrayList<>();

        EPListBean motionSensor1 = new EPListBean("Motion Sensor - A", "ms001", DeviceTypeCode.MOTION_SENSOR_ZONE, true);
        EPListBean motionSensor2 = new EPListBean("Motion Sensor - B", "ms002", DeviceTypeCode.MOTION_SENSOR_ZONE, true);
        EPListBean smokeDetector1 = new EPListBean("Smoke Detector - A", "sd001", DeviceTypeCode.SMOKE_SENSOR_ZONE, true);
        EPListBean smokeDetector2 = new EPListBean("Smoke Detector - B", "sd002", DeviceTypeCode.SMOKE_SENSOR_ZONE, true);
        EPListBean securityAlarm = new EPListBean("Security Alarm - A", "sa001", DeviceTypeCode.WARN_SENSOR, true);

        devices.add(motionSensor1);
        devices.add(motionSensor2);
        devices.add(smokeDetector1);
        devices.add(smokeDetector2);
        devices.add(securityAlarm);

        deviceMap.clear();
        for (EPListBean device : devices) {
            deviceMap.put(device.getId(), device);

            // Initialize sensor states for demo purposes
            if (device.getDeviceType() == DeviceTypeCode.MOTION_SENSOR_ZONE) {
                motionSensors.put(device.getId(), new MotionSensorBean(device.getId(), 1, false));
            } else if (device.getDeviceType() == DeviceTypeCode.SMOKE_SENSOR_ZONE) {
                smokeDetectors.put(device.getId(), new SmokeDetectorBean(device.getId(), 1, false));
            }
        }

        DeviceListBean deviceListBean = new DeviceListBean(devices);

        for (SocketMessageListener listener : listeners) {
            listener.getMessage(Constants.ZigBeeGetEPList, deviceListBean);
        }
    }

    private void simulateMotionSensorStatus(EPListBean device) {
        MotionSensorBean motionSensor = motionSensors.get(device.getId());
        if (motionSensor == null) {
            motionSensor = new MotionSensorBean(device.getId(), 1, false);
            motionSensors.put(device.getId(), motionSensor);
        }

        for (SocketMessageListener listener : listeners) {
            listener.getMessage(Constants.MotionSensorUpdate, motionSensor);
        }
    }

    public void simulateMotionDetected(String deviceId) {
        MotionSensorBean motionSensor = motionSensors.get(deviceId);
        if (motionSensor != null) {
            motionSensor.setDetected(true);

            if (securityStatus.isArmed()) {
                for (SocketMessageListener listener : listeners) {
                    listener.getMessage(Constants.MotionSensor, motionSensor);
                }
            }
        }
    }

    private void simulateSmokeDetectorStatus(EPListBean device) {
        SmokeDetectorBean smokeDetector = smokeDetectors.get(device.getId());
        if (smokeDetector == null) {
            smokeDetector = new SmokeDetectorBean(device.getId(), 1, false);
            smokeDetectors.put(device.getId(), smokeDetector);
        }
        for (SocketMessageListener listener : listeners) {
            listener.getMessage(Constants.WarningSensor, smokeDetector);
        }
    }

    public void simulateSmokeDetected(String deviceId) {
        SmokeDetectorBean smokeDetector = smokeDetectors.get(deviceId);
        if (smokeDetector != null) {
            smokeDetector.setDetected(true);

            if (securityStatus.isArmed()) {
                for (SocketMessageListener listener : listeners) {
                    listener.getMessage(Constants.WarningSensor, smokeDetector);
                }
            }
        }
    }

    private void simulateSecurityStatus(EPListBean device) {
        // Update status with device info
        securityStatus.setIeee(device.getId());
        securityStatus.setEp(1);

        for (SocketMessageListener listener : listeners) {
            listener.getMessage(Constants.UpdateSwitchgear, securityStatus);
        }
    }

    public void resetMotionDetection(String deviceId) {
        MotionSensorBean motionSensor = motionSensors.get(deviceId);
        if (motionSensor != null) {
            motionSensor.setDetected(false);

            for (SocketMessageListener listener : listeners) {
                listener.getMessage(Constants.MotionSensorUpdate, motionSensor);
            }
        }
    }

    public void resetSmokeDetection(String deviceId) {
        SmokeDetectorBean smokeDetector = smokeDetectors.get(deviceId);
        if (smokeDetector != null) {
            smokeDetector.setDetected(false);

            for (SocketMessageListener listener : listeners) {
                listener.getMessage(Constants.WarningSensor, smokeDetector);
            }
        }
    }
}