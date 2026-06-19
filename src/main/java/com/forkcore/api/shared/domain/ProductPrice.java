package com.forkcore.api.shared.domain;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;

public record ProductPrice(Id productId, BigDecimal unitPrice) {

	public static Result<ProductPrice> from(BigDecimal value) {
		if (value == null) {
			return Result.failure(new ValidationError("price", "product price is required"));
		}

		if (value.signum() < 0) {
			return Result.failure(
				new ValidationError("price", "product price must be greater than or equal to zero")
			);
		}

		return Result.success(new ProductPrice(Id.create(), value));
	}

	public static Result<ProductPrice> from(Id productId, BigDecimal value) {
		if (value == null) {
			return Result.failure(new ValidationError("price", "product price is required"));
		}

		if (value.signum() < 0) {
			return Result.failure(
				new ValidationError("price", "product price must be greater than or equal to zero")
			);
		}

		return Result.success(new ProductPrice(productId, value));
	}
}
