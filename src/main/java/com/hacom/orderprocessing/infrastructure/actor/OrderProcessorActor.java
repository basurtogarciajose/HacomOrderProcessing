package com.hacom.orderprocessing.infrastructure.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.hacom.orderprocessing.domain.model.Order;
import com.hacom.orderprocessing.domain.repository.OrderRepository;
import com.hacom.orderprocessing.infrastructure.smpp.SmsSender;
import com.hacom.orderprocessing.infrastructure.grpc.CreateOrderRequest;
import com.hacom.orderprocessing.infrastructure.grpc.CreateOrderResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;

@Slf4j
public class OrderProcessorActor extends AbstractActor {

    private final OrderRepository orderRepository;
    private final SmsSender smsSender;

    public OrderProcessorActor(OrderRepository orderRepository, SmsSender smsSender) {
        this.orderRepository = orderRepository;
        this.smsSender = smsSender;
    }

    public static Props props(OrderRepository repo, SmsSender smsSender) {
        return Props.create(OrderProcessorActor.class, () -> new OrderProcessorActor(repo, smsSender));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateOrderRequest.class, this::processOrder)
                .matchAny(msg -> log.warn("Unhandled message: {}", msg))
                .build();
    }

    private void processOrder(CreateOrderRequest req) {
        log.info("Processing order {} via actor", req.getOrderId());

        Order order = Order.builder()
                .orderId(req.getOrderId())
                .customerId(req.getCustomerId())
                .customerPhoneNumber(req.getCustomerPhoneNumber())
                .items(req.getItems().getItemsList())
                .status("PROCESSED")
                .ts(OffsetDateTime.now().toInstant())
                .build();
        
        final ActorRef replyTo = getSender();

        Mono<Order> saved = orderRepository.save(order);

        saved.subscribe(
                result -> {
                    // Enviar SMS
                	try {
                	    smsSender.sendSms(
                	            result.getCustomerPhoneNumber(),
                	            "Your order " + result.getOrderId() + " has been processed"
                	    );
                	} catch (Exception e) {
                	    log.error("SMS sending failed", e);
                	}

                    // Responder gRPC
                    CreateOrderResponse resp = CreateOrderResponse.newBuilder()
                            .setOrderId(result.getOrderId())
                            .setStatus(result.getStatus())
                            .build();

                    replyTo.tell(resp, getSelf());
                },
                error -> {
                    log.error("Error processing order", error);
                    CreateOrderResponse resp = CreateOrderResponse.newBuilder()
                            .setOrderId(req.getOrderId())
                            .setStatus("ERROR")
                            .build();

                    replyTo.tell(resp, getSelf());
                }
        );
    }
}