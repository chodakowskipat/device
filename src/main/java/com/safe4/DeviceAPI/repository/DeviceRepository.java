package com.safe4.DeviceAPI.repository;

import com.safe4.DeviceAPI.model.Device;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

  /** Returns device with given {@code ID}. */
  Optional<Device> findDeviceById(UUID id);

  /** Returns a {@link java.util.List} of all devices */
  List<Device> findDeviceByManufacturer(String manufacturer);
}
