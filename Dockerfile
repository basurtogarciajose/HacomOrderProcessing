FROM gradle:8.7-jdk17 AS builder

WORKDIR /app

COPY . .

RUN ./gradlew clean build -x test

FROM eclipse-temurin:17

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENV mongodbUri="mongodb://127.0.0.1:27017"
ENV mongodbDatabase="exampleDb"
ENV apiPort=9898
ENV grpcPort=6565
ENV smpp_host="127.0.0.1"
ENV smpp_port=2775
ENV smpp_systemId="smppclient1"
ENV smpp_password="password"
ENV smpp_source="OrderSvc"
ENV smpp_enabled=false

EXPOSE 9898
EXPOSE 6565

ENTRYPOINT ["java", "-jar", "app.jar"]