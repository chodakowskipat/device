package com.safe4.DeviceAPI.model.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AddDeviceEvent(
    UUID id,
    Instant timestamp,
    UUID deviceId,
    String deviceManufacturer,
    @Nullable String deviceName) {}
