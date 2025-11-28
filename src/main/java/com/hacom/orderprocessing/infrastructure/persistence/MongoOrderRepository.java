package com.hacom.orderprocessing.infrastructure.persistence;

import com.hacom.orderprocessing.domain.model.Order;
import com.hacom.orderprocessing.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class MongoOrderRepository implements OrderRepository {

    private final ReactiveMongoTemplate mongo;

    @Override
    public Mono<Order> save(Order order) {
        return mongo.save(order);
    }

    @Override
    public Mono<Order> findByOrderId(String orderId) {
        return mongo.findOne(Query.query(Criteria.where("orderId").is(orderId)), Order.class);
    }

    @Override
    public Flux<Order> findByRange(Instant start, Instant end) {
        Query q = new Query();
        q.addCriteria(Criteria.where("ts").gte(start).lte(end));
        return mongo.find(q, Order.class);
    }
}