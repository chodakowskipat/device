package com.safe4.DeviceAPI.controller;

import com.safe4.DeviceAPI.mapper.DeviceMapper;
import com.safe4.DeviceAPI.model.dto.DeviceDto;
import com.safe4.DeviceAPI.service.DeviceService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/devices")
public class DeviceController {

  private final DeviceService deviceService;
  private final DeviceMapper deviceMapper;

  @GetMapping
  List<DeviceDto> getDevices(@RequestParam(required = false, value = "manufacturer") String manufacturer) {
    return Optional.ofNullable(manufacturer)
            .map(deviceService::getDevicesByManufacturer)
            .orElseGet(deviceService::getDevices)
            .stream()
            .map(deviceMapper::toDeviceDto)
            .toList();
  }

  @GetMapping("/{id}")
  DeviceDto getDevice(@PathVariable("id") String deviceId) {
    UUID id =
        tryParseUUID(deviceId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, String.format("Invalid UUID: %s", deviceId)));

    return deviceService
        .getDeviceById(id)
        .map(deviceMapper::toDeviceDto)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  private static Optional<UUID> tryParseUUID(String id) {
    try {
      return Optional.of(UUID.fromString(id));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
