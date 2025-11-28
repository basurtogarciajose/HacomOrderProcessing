package com.hacom.orderprocessing.infrastructure.rest;

import com.hacom.orderprocessing.domain.model.Order;
import com.hacom.orderprocessing.domain.repository.OrderRepository;
import com.hacom.orderprocessing.infrastructure.rest.dto.CountResponseDto;
import com.hacom.orderprocessing.infrastructure.rest.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.HashMap;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;

    @Operation(summary = "Get order status", description = "Returns the status of an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<Map<String, String>>> getOrderStatus(@PathVariable String orderId) {
        return orderRepository.findByOrderId(orderId)
                .map(order -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("status", order.getStatus());
                    return ResponseEntity.ok(body);
                })
                .switchIfEmpty(
                        Mono.just(
                                ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("status", "NOT_FOUND"))
                        )
                );
    }

    @Operation(summary = "Get count order", description = "Returns total orders")
    @GetMapping
    public Mono<CountResponseDto> countOrders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end
    ) {
        return orderRepository.findByRange(start.toInstant(), end.toInstant())
                .count()
                .map(CountResponseDto::new);
    }

    private OrderResponseDto toDto(Order o) {
        return OrderResponseDto.builder()
                .orderId(o.getOrderId())
                .customerId(o.getCustomerId())
                .customerPhoneNumber(o.getCustomerPhoneNumber())
                .items(o.getItems())
                .status(o.getStatus())
                .ts(OffsetDateTime.ofInstant(o.getTs(), ZoneOffset.UTC))
                .build();
    }
}