package com.forkcore.api.orders.infrastructure.in.web;

import com.forkcore.api.orders.domain.Order;
import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
	String id,
	String status,
	List<OrderLineResponse> lines,
	String tableId,
	String notes,
	BigDecimal total
) {

	public static OrderResponse from(Order order) {
		return new OrderResponse(
			order.id().asString(),
			order.status().name(),
			order.lines().stream().map(OrderLineResponse::from).toList(),
			order.tableId() != null ? order.tableId().asString() : null,
			order.notes(),
			order.total()
		);
	}
}
