package com.hacom.orderprocessing.infrastructure.grpc;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcServerRunner {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerRunner.class);

    private final Server server;

    public GrpcServerRunner(OrderServiceGrpcImpl orderServiceGrpcImpl, GrpcProperties props) {
        int port = props.getPort();
        this.server = NettyServerBuilder.forPort(port)
                .addService(orderServiceGrpcImpl)
                .build();

        try {
            server.start();
            log.info("gRPC server started on port {}", port);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start gRPC server", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                GrpcServerRunner.this.stop();
            } catch (InterruptedException ignored) {}
        }));
    }

    //@PreDestroy
    public void stop() throws InterruptedException {
        if (server != null) {
            log.info("Shutting down gRPC server...");
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}