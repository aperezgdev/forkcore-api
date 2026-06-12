package com.forkcore.api.catalog.product.domain.vo;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;

public record ProductName(String value) {

	public static Result<ProductName> from(String value) {
		if (value == null || value.isBlank()) {
			return Result.failure(new ValidationError("name", "product name is required"));
		}

		return Result.success(new ProductName(value.trim()));
	}
}
