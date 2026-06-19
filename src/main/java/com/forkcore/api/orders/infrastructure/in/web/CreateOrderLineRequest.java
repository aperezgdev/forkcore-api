package com.forkcore.api.orders.infrastructure.in.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateOrderLineRequest(
	String productId,
	Integer quantity
) {
}
