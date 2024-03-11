package com.safe4.DeviceAPI.mapper;

import com.safe4.DeviceAPI.model.Device;
import com.safe4.DeviceAPI.model.dto.DeviceDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

  /** Maps {@link Device} to {@link DeviceDto}. */
  DeviceDto toDeviceDto(Device device);
}
