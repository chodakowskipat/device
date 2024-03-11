# DeviceAPI

## Overview
The `DeivceAPI` service keeps track of devices currently present in the system. Information can be queried using the
main `/api/v1/devices` endpoint. Detailed API definition is available through the OpenAPI spec at ``.

State of the system can be updated via events sent through `NATS` subjects `device.add` and `device.remove`. Usage of
`JetStream` mode provides the at least once delivery guarantee - the messages need to be acked by a subscriber before 
they can be removed from the queue.

## Running the application
The service requires a `NATS` server running in `JetStream` mode. The stream `devices` needs to be created before the
application starts. A convenience `docker-compose.yml` configuration was prepared to make running a demo of the app
easier. Publishing messages to `NATS` subjects requires the [nats-cli](https://github.com/nats-io/natscli).

### Start the Spring Boot app and NATS server:
```shell
docker compose up
```

### Publish messages to `device.add` subject:
```shell
nats pub device.add <<EOF
{
    "id": "170dc769-6b6e-40e0-aa58-b1e515771238", 
    "device_id": "170dc769-6b6e-40e0-aa58-b1e515778123", 
    "device_manufacturer": "nokia", 
    "timestamp": "1710026318"
}
EOF
```

### Verify the device was added
```shell
curl -s localhost:8080/api/v1/devices | jq
```
```
[
  {
    "id": "170dc769-6b6e-40e0-aa58-b1e515778123",
    "manufacturer": "nokia",
    "name": null,
    "lastEditTimestamp": "2024-03-09T23:18:38Z"
  }
]
```

### Update the device information
```shell
nats pub device.add <<EOF
{
    "id": "042e10b1-3bce-4082-966b-4c713c4b5302",
    "device_id": "170dc769-6b6e-40e0-aa58-b1e515778123",
    "device_manufacturer": "motorola",
    "device_name": "mobile_phone",
    "timestamp": "1710027429"
}
EOF
```

### Verify the update
```shell
curl -s localhost:8080/api/v1/devices | jq
```
```
[
  {
    "id": "170dc769-6b6e-40e0-aa58-b1e515778123",
    "manufacturer": "motorola",
    "name": "mobile_phone",
    "lastEditTimestamp": "2024-03-09T23:37:09Z"
  }
]
```

### Publish messages to `device.remove` subject:
```shell
nats pub device.remove <<EOF
{
    "id":"170dc769-6b6e-40e0-aa58-b1e5157785b9",
    "device_id": "170dc769-6b6e-40e0-aa58-b1e515778123",
    "timestamp": "1710027450"
}
EOF
```

### Verify the device has been deleted
```shell
curl -s localhost:8080/api/v1/devices | jq
```
```shell
[]
```

## Running tests
```shell
./gradlew test
```