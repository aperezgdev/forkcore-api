package com.forkcore.api.orders.infrastructure.in.web;

import com.forkcore.api.orders.domain.OrderLine;
import java.math.BigDecimal;

public record OrderLineResponse(
	String productId,
	int quantity,
	BigDecimal unitPrice
) {

	public static OrderLineResponse from(OrderLine line) {
		return new OrderLineResponse(
			line.productId().asString(),
			line.quantity().value(),
			line.unitPrice()
		);
	}
}
