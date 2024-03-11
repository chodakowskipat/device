package com.safe4.DeviceAPI.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.safe4.DeviceAPI.model.Device;
import com.safe4.DeviceAPI.repository.DeviceRepository;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class DeviceServiceTest {

  private static final UUID DEVICE_UUID = UUID.randomUUID();
  private static final String MANUFACTURER = "test";
  private static final String DEVICE_NAME = "test-device";

  private final Clock clock = Clock.fixed(Instant.parse("2024-03-09T10:11:12Z"), ZoneId.of("UTC"));

  @MockBean private Connection connection;

  @MockBean private JetStream jetStream;

  @MockBean private NatsService natsService;

  @Autowired private DeviceRepository deviceRepository;

  @Autowired private DeviceService deviceService;

  @BeforeEach
  void setup() {
    deviceRepository.deleteAll();
  }

  @Test
  void upsertDevice_newDevice_savedCorrectly() {
    var requestTimestamp = Instant.now(clock);

    deviceService.upsertDevice(DEVICE_UUID, MANUFACTURER, DEVICE_NAME, requestTimestamp);

    var result = deviceRepository.findDeviceById(DEVICE_UUID);
    assertThat(result)
        .isNotEmpty()
        .contains(
            Device.builder()
                .id(DEVICE_UUID)
                .manufacturer(MANUFACTURER)
                .name(DEVICE_NAME)
                .lastEditTimestamp(requestTimestamp)
                .build());
  }

  @Test
  void upsertDevice_existingDevicePastDate_updateNotSaved() {
    var existingDeviceRequestTimestamp = Instant.now(clock);
    deviceService.upsertDevice(
        DEVICE_UUID, MANUFACTURER, DEVICE_NAME, existingDeviceRequestTimestamp);

    var newManufacturerName = "test2";
    var requestTimestampInPast = existingDeviceRequestTimestamp.minusSeconds(10);
    deviceService.upsertDevice(
        DEVICE_UUID, newManufacturerName, DEVICE_NAME, requestTimestampInPast);

    var result = deviceRepository.findDeviceById(DEVICE_UUID);
    assertThat(result)
        .isNotEmpty()
        .contains(
            Device.builder()
                .id(DEVICE_UUID)
                .manufacturer(MANUFACTURER)
                .name(DEVICE_NAME)
                .lastEditTimestamp(existingDeviceRequestTimestamp)
                .build());

    var deviceByManufactureName = deviceRepository.findDeviceByManufacturer("test2");
    assertThat(deviceByManufactureName).isEmpty();
  }

  @Test
  void upsertDevice_existingDeviceFutureDate_updateSaved() {
    var existingDeviceRequestTimestamp = Instant.now(clock);
    deviceService.upsertDevice(
        DEVICE_UUID, MANUFACTURER, DEVICE_NAME, existingDeviceRequestTimestamp);

    var newManufacturerName = "test2";
    var requestTimestampInFuture = existingDeviceRequestTimestamp.plusSeconds(10);
    deviceService.upsertDevice(
        DEVICE_UUID, newManufacturerName, DEVICE_NAME, requestTimestampInFuture);

    var result = deviceRepository.findDeviceById(DEVICE_UUID);
    assertThat(result)
        .isNotEmpty()
        .contains(
            Device.builder()
                .id(DEVICE_UUID)
                .manufacturer(newManufacturerName)
                .name(DEVICE_NAME)
                .lastEditTimestamp(requestTimestampInFuture)
                .build());

    var deviceByManufactureName = deviceRepository.findDeviceByManufacturer(MANUFACTURER);
    assertThat(deviceByManufactureName).isEmpty();
  }

  @Test
  void removeDevice_existingDevicePastDate_deviceRemoved() {
    var existingDeviceRequestTimestamp = Instant.now(clock).minusSeconds(10);
    deviceService.upsertDevice(
        DEVICE_UUID, MANUFACTURER, DEVICE_NAME, existingDeviceRequestTimestamp);
    var existingDevice = deviceRepository.findDeviceById(DEVICE_UUID);

    deviceService.removeDevice(DEVICE_UUID, Instant.now(clock));

    var result = deviceRepository.findDeviceById(DEVICE_UUID);
    assertThat(result).isEmpty();
    assertThat(existingDevice)
        .isNotEmpty()
        .contains(
            Device.builder()
                .id(DEVICE_UUID)
                .manufacturer(MANUFACTURER)
                .name(DEVICE_NAME)
                .lastEditTimestamp(existingDeviceRequestTimestamp)
                .build());
  }

  @Test
  void removeDevice_existingDeviceFutureDate_deviceNotRemoved() {
    var existingDeviceRequestTimestamp = Instant.now(clock).plusSeconds(10);
    deviceService.upsertDevice(
        DEVICE_UUID, MANUFACTURER, DEVICE_NAME, existingDeviceRequestTimestamp);

    deviceService.removeDevice(DEVICE_UUID, Instant.now(clock));

    var result = deviceRepository.findDeviceById(DEVICE_UUID);
    assertThat(result)
        .isNotEmpty()
        .contains(
            Device.builder()
                .id(DEVICE_UUID)
                .manufacturer(MANUFACTURER)
                .name(DEVICE_NAME)
                .lastEditTimestamp(existingDeviceRequestTimestamp)
                .build());
  }
}
