package com.hacom.orderprocessing.infrastructure.grpc;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import com.hacom.orderprocessing.infrastructure.grpc.CreateOrderRequest;
import com.hacom.orderprocessing.infrastructure.grpc.CreateOrderResponse;
import com.hacom.orderprocessing.infrastructure.grpc.OrderServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class OrderServiceGrpcImpl extends OrderServiceGrpc.OrderServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceGrpcImpl.class);

    private final ActorRef orderProcessorActor;
    private final Duration askTimeout = Duration.ofSeconds(15);
    private final Counter ordersCreatedCounter;

    public OrderServiceGrpcImpl(ActorRef orderProcessorActor, MeterRegistry meterRegistry) {
        this.orderProcessorActor = orderProcessorActor;
        
        this.ordersCreatedCounter = Counter.builder("orders")
                .description("Total number of orders created via gRPC")
                .register(meterRegistry);
    }

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<CreateOrderResponse> responseObserver) {
        log.info("gRPC InsertOrder received: {}", request.getOrderId());
        
        ordersCreatedCounter.increment();

        // Mandamos el OrderRequest (protobuf) directamente al actor
        CompletionStage<Object> future = Patterns.ask(orderProcessorActor, request, askTimeout);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Error processing order {}", request.getOrderId(), ex);
                responseObserver.onError(ex);
                return;
            }

            try {
                if (result instanceof CreateOrderResponse) {
                    // actor devolvió el OrderResponse protobuf directamente
                    responseObserver.onNext((CreateOrderResponse) result);
                } else if (result instanceof com.hacom.orderprocessing.domain.model.Order) {
                    // alternativa: actor devolvió tu entidad domain Order -> convertir
                    com.hacom.orderprocessing.domain.model.Order saved = (com.hacom.orderprocessing.domain.model.Order) result;
                    CreateOrderResponse response = CreateOrderResponse.newBuilder()
                            .setOrderId(request.getOrderId())
                            .setStatus("CREATED")
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                } else {
                    // fallback
                    CreateOrderResponse response = CreateOrderResponse.newBuilder()
                            .setOrderId(request.getOrderId())
                            .setStatus("UNKNOWN_RESPONSE_FROM_ACTOR")
                            .build();
                    responseObserver.onNext(response);
                }
            } catch (Exception e) {
                log.error("Error building response for order {}", request.getOrderId(), e);
                responseObserver.onError(e);
                return;
            }
            responseObserver.onCompleted();
        });
    }
}