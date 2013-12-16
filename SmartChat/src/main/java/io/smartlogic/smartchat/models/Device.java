package io.smartlogic.smartchat.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("device")
public class Device {
    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("device_type")
    private String deviceType = "android";

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }
}
