package com.hacom.orderprocessing.infrastructure.smpp;

public interface SmsSender {
    void sendSms(String phoneNumber, String orderId);
}