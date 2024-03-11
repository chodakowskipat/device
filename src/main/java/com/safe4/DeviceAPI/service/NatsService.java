package com.safe4.DeviceAPI.service;

import com.safe4.DeviceAPI.model.event.AddDeviceEvent;
import com.safe4.DeviceAPI.model.event.RemoveDeviceEvent;
import io.nats.client.*;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.function.json.JsonMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NatsService {

  private final String natsStream;
  private final String deviceAddSubject;
  private final String deviceRemoveSubject;
  private final String deviceWildcardSubject;

  private final JsonMapper jsonMapper;
  private final Connection connection;
  private final DeviceService deviceService;

  private Subscription subscription;

  NatsService(
      JsonMapper jsonMapper,
      @Value("${nats.stream.devices}") String natsStream,
      @Value("${nats.subject.device-add}") String deviceAddSubject,
      @Value("${nats.subject.device-remove}") String deviceRemoveSubject,
      @Value("${nats.subject.device-wildcard}") String deviceWildcardSubject,
      Connection connection,
      DeviceService deviceService) {
    this.jsonMapper = jsonMapper;
    this.natsStream = natsStream;
    this.deviceAddSubject = deviceAddSubject;
    this.deviceRemoveSubject = deviceRemoveSubject;
    this.deviceWildcardSubject = deviceWildcardSubject;
    this.connection = connection;
    this.deviceService = deviceService;
  }

  /**
   * Creates a {@link io.nats.client.Subscription} and asynchronously listens to events sent to
   * {@code device-add} and {@code device-remove} subjects in {@code devices} stream. Events are
   * parsed and forwarded to {@link DeviceService}.
   */
  @Async
  public void subscribe() throws Exception {
    JetStream jetStream = connection.jetStream();
    Dispatcher dispatcher = connection.createDispatcher();
    PushSubscribeOptions pushSubscribeOptions = PushSubscribeOptions.stream(natsStream);

    subscription =
        jetStream.subscribe(
            deviceWildcardSubject, dispatcher, this::handleMessage, false, pushSubscribeOptions);
  }

  private void handleMessage(Message message) {
    if (message == null) {
      log.atWarn().log("Received null message");
      return;
    }
    if (message.isStatusMessage()) {
      log.atTrace().log("Received status message: {}", message);
      return;
    }
    if (!message.isJetStream()) {
      log.atWarn().log("Received non-JetStream message, ignoring: {}", message);
      return;
    }

    if (message.getSubject().equals(deviceAddSubject)) {
      log.atTrace().log("Processing addDevice event");
      processAddDeviceEvent(message);
    } else if (message.getSubject().equals(deviceRemoveSubject)) {
      log.atTrace().log("Processing removeDevice event");
      processRemoveDeviceEvent(message);
    } else {
      log.atWarn().log("Message from unknown subject received: {}", message.getSubject());
    }
  }

  private void processAddDeviceEvent(Message message) {
    String rawJson = new String(message.getData());
    AddDeviceEvent event;
    try {
      event = jsonMapper.fromJson(rawJson, AddDeviceEvent.class);
    } catch (Exception e) {
      log.atError().setCause(e).log("Failed to parse addDevice event: {}", rawJson);
      message.term();
      return;
    }

    try {
      deviceService.upsertDevice(
          event.deviceId(), event.deviceManufacturer(), event.deviceName(), event.timestamp());
      message.ack();
    } catch (Exception e) {
      log.atError().setCause(e).log("Failed to save device from event: {}", event);
      message.term();
    }
  }

  private void processRemoveDeviceEvent(Message message) {
    String rawJson = new String(message.getData());
    RemoveDeviceEvent event;
    try {
      event = jsonMapper.fromJson(rawJson, RemoveDeviceEvent.class);
    } catch (Exception e) {
      log.atError().setCause(e).log("Failed to parse removeDevice event: {}", rawJson);
      message.term();
      return;
    }

    try {
      deviceService.removeDevice(event.deviceId(), event.timestamp());
      message.ack();
    } catch (Exception e) {
      log.atError().setCause(e).log("Failed to remove device from event: {}", event);
      message.term();
    }
  }

  @PreDestroy
  private void closeSubscription() throws InterruptedException, ExecutionException {
    if (subscription == null) {
      return;
    }
    log.atInfo().log(
        "Closing subscription for subject {} on stream {}", deviceWildcardSubject, natsStream);
    boolean closed = subscription.drain(Duration.ofSeconds(30)).get();

    if (closed) {
      log.atInfo().log(
          "Closed subscription for subject {} on stream {}.", deviceWildcardSubject, natsStream);
    } else {
      log.atWarn()
          .log(
              "Failed to close subscription for subject {} on stream {}.",
              deviceWildcardSubject,
              natsStream);
    }
  }
}
