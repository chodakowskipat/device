package com.safe4.DeviceAPI.model.dto;

import jakarta.annotation.Nullable;
import java.time.Instant;

public record DeviceDto(
    String id, String manufacturer, @Nullable String name, Instant lastEditTimestamp) {}
