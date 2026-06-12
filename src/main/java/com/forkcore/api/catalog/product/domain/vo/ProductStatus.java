package com.forkcore.api.catalog.product.domain.vo;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.util.Set;

public record ProductStatus(String value) {

	private static final String ACTIVE = "active";
	private static final Set<String> ALLOWED_VALUES = Set.of(ACTIVE, "inactive");

	public static Result<ProductStatus> from(String value) {
		var normalizedValue = value;
		if (normalizedValue == null || normalizedValue.isBlank()) {
			normalizedValue = ACTIVE;
		} else {
			normalizedValue = normalizedValue.trim().toLowerCase();
		}

		if (!ALLOWED_VALUES.contains(normalizedValue)) {
			return Result.failure(new ValidationError("status", "product status is invalid"));
		}

		return Result.success(new ProductStatus(normalizedValue));
	}
}
