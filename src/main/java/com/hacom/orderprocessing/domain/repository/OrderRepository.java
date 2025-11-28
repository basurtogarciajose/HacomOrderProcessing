package com.hacom.orderprocessing.domain.repository;

import com.hacom.orderprocessing.domain.model.Order;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface OrderRepository {
    Mono<Order> save(Order order);
    Mono<Order> findByOrderId(String orderId);
    Flux<Order> findByRange(Instant start, Instant end);
}