package com.hacom.orderprocessing.infrastructure.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "grpc")
public class GrpcProperties {
    private int port = 6565;

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
}