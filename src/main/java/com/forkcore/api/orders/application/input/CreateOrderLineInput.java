package com.forkcore.api.orders.application.input;

public record CreateOrderLineInput(String productId, Integer quantity) {
}
