package com.forkcore.api.orders.domain.vo;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;

public record OrderLineQuantity(int value) {

	public static Result<OrderLineQuantity> from(Integer value) {
		if (value == null) {
			return Result.failure(new ValidationError("quantity", "quantity is required"));
		}

		if (value < 1) {
			return Result.failure(new ValidationError("quantity", "quantity must be greater than or equal to 1"));
		}

		return Result.success(new OrderLineQuantity(value));
	}
}
