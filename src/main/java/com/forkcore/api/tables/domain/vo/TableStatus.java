package com.forkcore.api.tables.domain.vo;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.util.Set;

public record TableStatus(String value) {

	private static final String AVAILABLE = "available";
	private static final Set<String> ALLOWED_VALUES = Set.of(AVAILABLE, "occupied", "out_of_service");

	public static Result<TableStatus> from(String value) {
		var normalizedValue = value;
		if (normalizedValue == null || normalizedValue.isBlank()) {
			normalizedValue = AVAILABLE;
		} else {
			normalizedValue = normalizedValue.trim().toLowerCase();
		}

		if (!ALLOWED_VALUES.contains(normalizedValue)) {
			return Result.failure(new ValidationError("status", "table status is invalid"));
		}

		return Result.success(new TableStatus(normalizedValue));
	}
}
