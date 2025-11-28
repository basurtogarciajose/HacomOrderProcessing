package com.hacom.orderprocessing.infrastructure.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.hacom.orderprocessing.infrastructure.actor.OrderProcessorActor;
import com.hacom.orderprocessing.infrastructure.grpc.OrderServiceGrpcImpl;
import com.hacom.orderprocessing.infrastructure.persistence.MongoOrderRepository;
import com.hacom.orderprocessing.infrastructure.smpp.SmsSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class AkkaConfig {

    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create("order-processing-system");
    }

    @Bean
    public ActorRef orderProcessorActor(ActorSystem actorSystem, MongoOrderRepository orderRepository, SmsSender smsSender) {
        return actorSystem.actorOf(OrderProcessorActor.props(orderRepository, smsSender),"orderProcessorActor");
    }
    
    @Bean
    public OrderServiceGrpcImpl orderServiceGrpcImpl(ActorRef orderProcessorActor, MeterRegistry meterRegistry) {
        return new OrderServiceGrpcImpl(orderProcessorActor, meterRegistry);
    }
}