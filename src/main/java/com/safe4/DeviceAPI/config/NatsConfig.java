package com.safe4.DeviceAPI.config;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.Nats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class NatsConfig {

  @Bean
  Connection connection(@Value("${nats.server.url}") String serverUrl) throws Exception {
    return Nats.connect(serverUrl);
  }

  @Bean
  JetStream jetStream(Connection connection) throws Exception {
    return connection.jetStream();
  }
}
