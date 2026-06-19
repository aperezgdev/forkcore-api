package com.forkcore.api.orders.infrastructure.in.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateOrderRequest(
	List<CreateOrderLineRequest> lines,
	String tableId,
	String notes
) {
}
