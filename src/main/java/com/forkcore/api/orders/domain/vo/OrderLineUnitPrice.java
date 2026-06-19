package com.forkcore.api.orders.domain.vo;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;

public record OrderLineUnitPrice(BigDecimal value) {

	public static Result<OrderLineUnitPrice> from(BigDecimal value) {
		if (value == null) {
			return Result.failure(new ValidationError("price", "line unit price is required"));
		}

		if (value.signum() < 0) {
			return Result.failure(new ValidationError("price", "line unit price must be greater than or equal to zero"));
		}

		return Result.success(new OrderLineUnitPrice(value));
	}
}
