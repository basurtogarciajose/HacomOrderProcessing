package com.hacom.orderprocessing.infrastructure.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDto {
    private String orderId;
    private String customerId;
    private String customerPhoneNumber;
    private List<String> items;
    private String status;
    private OffsetDateTime ts;
}