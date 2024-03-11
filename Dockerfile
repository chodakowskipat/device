FROM gradle:8.5.0-jdk17-alpine as build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM amazoncorretto:17-alpine

EXPOSE 8080

RUN mkdir /app
RUN wget https://github.com/nats-io/natscli/releases/download/v0.1.3/nats-0.1.3-linux-amd64.zip  \
      && unzip nats-0.1.3-linux-amd64.zip \
      && mv nats-0.1.3-linux-amd64/nats /usr/local/bin/nats
COPY --from=build /home/gradle/src/build/libs/*.jar /app/DeviceAPI/server.jar
COPY devices.json /

ENTRYPOINT sleep 5 \
      && nats stream add -s $NATS_SERVER --config /devices.json \
      && java -jar /app/DeviceAPI/server.jar
