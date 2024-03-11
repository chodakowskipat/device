package com.safe4.DeviceAPI.service;

import com.safe4.DeviceAPI.model.Device;
import com.safe4.DeviceAPI.repository.DeviceRepository;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

  private final DeviceRepository deviceRepository;

  /** Returns a {@link List} of all devices. */
  public List<Device> getDevices() {
    return deviceRepository.findAll();
  }

  /** Returns a {@link Device} with given {@code ID}. */
  public Optional<Device> getDeviceById(UUID id) {
    return deviceRepository.findDeviceById(id);
  }

  /** Returns a {@link List} of all devices with given {@code manufacturer}. */
  public List<Device> getDevicesByManufacturer(String manufacturer) {
    return deviceRepository.findDeviceByManufacturer(manufacturer);
  }

  /**
   * Persists a new device in the database.
   *
   * <p>If the incoming edit has an {@code updateDateTime} before the one already saved, no changes
   * are made.
   */
  @Transactional
  public Device upsertDevice(
      UUID id, String manufacturer, @Nullable String name, Instant requestTimestamp) {
    Device deviceToSave =
        deviceRepository
            .findDeviceById(id)
            .filter(device -> device.getLastEditTimestamp().isAfter(requestTimestamp))
            .orElseGet(
                () ->
                    Device.builder()
                        .id(id)
                        .manufacturer(manufacturer)
                        .name(name)
                        .lastEditTimestamp(requestTimestamp)
                        .build());

    deviceRepository.save(deviceToSave);

    log.atTrace().log("Successfully added device: {}", deviceToSave);
    return deviceToSave;
  }

  /**
   * Removes a device with given {@code id} from the database (noop if device with {@code id} does
   * not exist).
   */
  @Transactional
  public void removeDevice(UUID id, Instant requestTimestamp) {
    deviceRepository
        .findDeviceById(id)
        .filter(device -> device.getLastEditTimestamp().isBefore(requestTimestamp))
        .map(Device::getId)
        .ifPresent(deviceRepository::deleteById);

    log.atTrace().log("Successfully removed device with ID={} (if existed)", id);
  }
}
