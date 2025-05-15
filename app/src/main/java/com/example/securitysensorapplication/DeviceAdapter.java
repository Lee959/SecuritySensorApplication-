package com.example.securitysensorapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying security devices in a RecyclerView
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<EPListBean> devices;
    private Context context;
    private OnDeviceClickListener listener;

    // Interface for click events
    public interface OnDeviceClickListener {
        void onDeviceClick(EPListBean device);
        void onStatusButtonClick(EPListBean device);
    }

    public DeviceAdapter(Context context, List<EPListBean> devices, OnDeviceClickListener listener) {
        this.context = context;
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        EPListBean device = devices.get(position);

        holder.deviceName.setText(device.getName());
        holder.deviceType.setText(device.getDeviceTypeName());

        // Set status indicator based on device link status
        if (device.isLinkStatus()) {
            holder.deviceStatus.setText("在线");
            holder.deviceStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.deviceStatus.setText("离线");
            holder.deviceStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        // Set icon based on device type using standard Android icons instead of custom drawables
        switch (device.getDeviceType()) {
            case DeviceTypeCode.MOTION_SENSOR_ZONE:
                holder.deviceIcon.setImageResource(R.drawable.motion_sensor_24px);
                break;
            case DeviceTypeCode.SMOKE_SENSOR_ZONE:
                holder.deviceIcon.setImageResource(R.drawable.detector_smoke_24px);
                break;
            case DeviceTypeCode.WARN_SENSOR:
                holder.deviceIcon.setImageResource(R.drawable.light_sound_sensor_24px);
                break;
            default:
                holder.deviceIcon.setImageResource(R.drawable.device_unknown_24px);
                break;
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeviceClick(device);
            }
        });

        holder.statusButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStatusButtonClick(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices == null ? 0 : devices.size();
    }

    public void updateDevices(List<EPListBean> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    public void updateDeviceStatus(EPListBean device, boolean isDetected) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getId().equals(device.getId())) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        ImageView deviceIcon;
        TextView deviceName;
        TextView deviceType;
        TextView deviceStatus;
        View statusButton;

        DeviceViewHolder(View itemView) {
            super(itemView);
            deviceIcon = itemView.findViewById(R.id.device_icon);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceType = itemView.findViewById(R.id.device_type);
            deviceStatus = itemView.findViewById(R.id.device_status);
            statusButton = itemView.findViewById(R.id.status_button);
        }
    }
}