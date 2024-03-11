package com.safe4.DeviceAPI;

import com.safe4.DeviceAPI.service.NatsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class DeviceApiApplication {

  public static void main(String[] args) throws Exception {
    var context = SpringApplication.run(DeviceApiApplication.class, args);

    context.getBean(NatsService.class).subscribe();
  }
}
