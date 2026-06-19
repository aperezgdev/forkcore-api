package com.forkcore.api.orders.domain.vo;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;

public record OrderTotal(BigDecimal value) {

	public static Result<OrderTotal> from(BigDecimal value) {
		if (value == null) {
			return Result.failure(new ValidationError("total", "total is required"));
		}

		if (value.signum() < 0) {
			return Result.failure(new ValidationError("total", "total must be greater than or equal to zero"));
		}

		return Result.success(new OrderTotal(value));
	}
}
