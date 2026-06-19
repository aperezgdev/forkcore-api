package com.forkcore.api.orders.domain;

import java.math.BigDecimal;

public record OrderLineInput(String productId, Integer quantity, BigDecimal unitPrice) {
}
