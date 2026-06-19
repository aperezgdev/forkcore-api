package com.forkcore.api.orders.domain;

import com.forkcore.api.orders.domain.vo.OrderLineQuantity;
import com.forkcore.api.orders.domain.vo.OrderLineUnitPrice;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;

public final class OrderLine {

	private final Id id;
	private final Id productId;
	private final OrderLineQuantity quantity;
	private final OrderLineUnitPrice unitPrice;

	private OrderLine(Id id, Id productId, OrderLineQuantity quantity, OrderLineUnitPrice unitPrice) {
		this.id = id;
		this.productId = productId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public static Result<OrderLine> create(String productId, Integer quantity, BigDecimal unitPrice) {
		var productIdResult = Id.from(productId);
		var quantityResult = OrderLineQuantity.from(quantity);
		var unitPriceResult = OrderLineUnitPrice.from(unitPrice);
		var errors = Stream.of(productIdResult, quantityResult, unitPriceResult)
			.filter(Result::isFailure)
			.map(result -> (ValidationError) result.error())
			.toArray(ValidationError[]::new);

		if (errors.length > 0) {
			return Result.failure(CompositeValidationError.from(errors));
		}

		return Result.success(new OrderLine(Id.create(), productIdResult.value(), quantityResult.value(), unitPriceResult.value()));
	}

	public static OrderLine fromPrimitives(String id, String productId, int quantity, BigDecimal unitPrice) {
		return new OrderLine(
			new Id(UUID.fromString(id)),
			new Id(UUID.fromString(productId)),
			new OrderLineQuantity(quantity),
			new OrderLineUnitPrice(unitPrice)
		);
	}

	public Id id() {
		return id;
	}

	public Id productId() {
		return productId;
	}

	public OrderLineQuantity quantity() {
		return quantity;
	}

	public BigDecimal unitPrice() {
		return unitPrice.value();
	}
}
