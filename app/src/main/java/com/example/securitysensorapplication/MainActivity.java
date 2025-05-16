package com.example.securitysensorapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitysensorapplication.owon.sdk.util.Constants;
import com.example.securitysensorapplication.owon.sdk.util.DeviceMessagesManager;
import com.example.securitysensorapplication.owon.sdk.util.SocketMessageListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SocketMessageListener, DeviceAdapter.OnDeviceClickListener {
    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private List<EPListBean> deviceList = new ArrayList<>();

    private TextView securityStatusText;
    private Button armButton;
    private Button disarmButton;
    private TextView eventLogText;

    private boolean isSecurityArmed = false;
    private StringBuilder eventLog = new StringBuilder();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private DeviceMessagesManager deviceManager;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.device_list);
        securityStatusText = findViewById(R.id.security_status);
        armButton = findViewById(R.id.arm_button);
        disarmButton = findViewById(R.id.disarm_button);
        eventLogText = findViewById(R.id.event_log);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(this, deviceList, this);
        recyclerView.setAdapter(adapter);

        // Initialize DeviceMessagesManager
        deviceManager = DeviceMessagesManager.getInstance();
        deviceManager.registerMessageListener(this);

        // Set up button listeners Default Value Set to 225
        armButton.setOnClickListener(v -> {
            deviceManager.SecurityDeployment(255);
            addEventLog("安全系统已布防");
        });

        disarmButton.setOnClickListener(v -> {
            deviceManager.SecurityDisarming(255);
            addEventLog("安全系统已撤防");
        });

        login();
    }

    private void login() {
        // Simulate login with default credentials
        deviceManager.LoginSocket(this, "fbadmin", "fbadmin");

        // Get the list of devices after a short delay
        handler.postDelayed(() -> {
            deviceManager.GetEpList();
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceManager.unregisterMessageListener(this);
    }

    @Override
    public void getMessage(int commandID, Object bean) {
        Log.d(TAG, "Received message with command ID: " + commandID);

        runOnUiThread(() -> {
            switch (commandID) {
                case 100: // Login successful
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                    break;

                case Constants.ZigBeeGetEPList:
                    handleDeviceList((DeviceListBean) bean);
                    break;

                case Constants.UpdateSwitchgear:
                    if (bean instanceof z_SecurityQueryStatusBean) {
                        handleSecurityStatus((z_SecurityQueryStatusBean) bean);
                    }
                    break;

                case Constants.MotionSensorUpdate:
                    if (bean instanceof MotionSensorBean) {
                        handleMotionSensorUpdate((MotionSensorBean) bean);
                    }
                    break;

                case Constants.MotionSensor:
                    if (bean instanceof MotionSensorBean) {
                        handleMotionDetection((MotionSensorBean) bean);
                    }
                    break;

                case Constants.WarningSensor:
                    if (bean instanceof SmokeDetectorBean) {
                        handleSmokeDetection((SmokeDetectorBean) bean);
                    }
                    break;
            }
        });
    }

    private void handleDeviceList(DeviceListBean deviceListBean) {
        deviceList.clear();

        List<EPListBean> allDevices = deviceListBean.getDevices();
        for (EPListBean device : allDevices) {
            if (device.getDeviceType() == DeviceTypeCode.MOTION_SENSOR_ZONE ||
                    device.getDeviceType() == DeviceTypeCode.SMOKE_SENSOR_ZONE ||
                    device.getDeviceType() == DeviceTypeCode.WARN_SENSOR) {
                deviceList.add(device);
            }
        }

        adapter.updateDevices(deviceList);
        addEventLog("设备列表已更新. 发现 " + deviceList.size() + " 个安防设备.");

        // Query status for all devices
        for (EPListBean device : deviceList) {
            deviceManager.getDeviceState(device, 0);
        }
    }

    private void handleSecurityStatus(z_SecurityQueryStatusBean statusBean) {
        isSecurityArmed = "on".equals(statusBean.getStatus());

        if (isSecurityArmed) {
            securityStatusText.setText("已布防");
            securityStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            armButton.setEnabled(false);
            disarmButton.setEnabled(true);
        } else {
            securityStatusText.setText("已撤防");
            securityStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            armButton.setEnabled(true);
            disarmButton.setEnabled(false);
        }
    }

    private void handleMotionSensorUpdate(MotionSensorBean motionSensor) {
        for (EPListBean device : deviceList) {
            if (device.getId().equals(motionSensor.getIeee()) &&
                    device.getDeviceType() == DeviceTypeCode.MOTION_SENSOR_ZONE) {

                // Update the UI
                adapter.updateDeviceStatus(device, motionSensor.isDetected());

                if (motionSensor.isDetected()) {
                    addEventLog(device.getName() + " 检测到移动");
                }

                break;
            }
        }
    }

    private void handleMotionDetection(MotionSensorBean motionSensor) {
        if (motionSensor.isDetected()) {
            for (EPListBean device : deviceList) {
                if (device.getId().equals(motionSensor.getIeee()) &&
                        device.getDeviceType() == DeviceTypeCode.MOTION_SENSOR_ZONE) {

                    // Update the UI
                    adapter.updateDeviceStatus(device, true);

                    // Log the event
                    String eventMessage = device.getName() + " 检测到移动!";
                    addEventLog(eventMessage);

                    if (isSecurityArmed) {
                        showAlertDialog("移动警报", eventMessage);
                    }

                    // Reset after a delay for demo purposes
                    handler.postDelayed(() -> {
                        deviceManager.resetMotionDetection(device.getId());
                    }, 10000);

                    break;
                }
            }
        }
    }

    private void handleSmokeDetection(SmokeDetectorBean smokeDetector) {
        if (smokeDetector.isDetected()) {
            for (EPListBean device : deviceList) {
                if (device.getId().equals(smokeDetector.getIeee()) &&
                        device.getDeviceType() == DeviceTypeCode.SMOKE_SENSOR_ZONE) {

                    // Update the UI
                    adapter.updateDeviceStatus(device, true);

                    // Log the event
                    String eventMessage = device.getName() + " 检测到烟雾! 危险!";
                    addEventLog(eventMessage);

                    showAlertDialog("烟雾警报", eventMessage);

                    handler.postDelayed(() -> {
                        deviceManager.resetSmokeDetection(device.getId());
                    }, 10000);

                    break;
                }
            }
        }
    }

    private void addEventLog(String message) {
        String timestamp = dateFormat.format(new Date());
        String logEntry = timestamp + " - " + message + "\n";

        eventLog.insert(0, logEntry);

        if (eventLog.length() > 1000) {
            eventLog.setLength(1000);
        }

        eventLogText.setText(eventLog.toString());
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确认", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onDeviceClick(EPListBean device) {
        String details = "设备名称: " + device.getName() + "\n" +
                "设备类型: " + device.getDeviceTypeName() + "\n" +
                "设备ID: " + device.getId() + "\n" +
                "状态: " + (device.isLinkStatus() ? "在线" : "离线");

        new AlertDialog.Builder(this)
                .setTitle("设备详情")
                .setMessage(details)
                .setPositiveButton("确定", null)
                .show();
    }


    // Simulate a detection event for demo purposes
    @Override
    public void onStatusButtonClick(EPListBean device) {
        if (device.getDeviceType() == DeviceTypeCode.MOTION_SENSOR_ZONE) {
            deviceManager.simulateMotionDetected(device.getId());
            Toast.makeText(this, "模拟移动检测", Toast.LENGTH_SHORT).show();
        } else if (device.getDeviceType() == DeviceTypeCode.SMOKE_SENSOR_ZONE) {
            deviceManager.simulateSmokeDetected(device.getId());
            Toast.makeText(this, "模拟烟雾检测", Toast.LENGTH_SHORT).show();
        }
    }
}