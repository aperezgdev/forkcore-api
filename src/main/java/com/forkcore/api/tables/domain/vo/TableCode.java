package com.forkcore.api.tables.domain.vo;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;

public record TableCode(String value) {

	public static Result<TableCode> from(String value) {
		if (value == null || value.isBlank()) {
			return Result.failure(new ValidationError("code", "table code is required"));
		}

		var trimmed = value.trim();

		if (trimmed.length() > 16) {
			return Result.failure(new ValidationError("code", "table code must be at most 16 characters"));
		}

		if (!trimmed.matches("[A-Za-z0-9_-]+")) {
			return Result.failure(new ValidationError("code", "table code contains invalid characters"));
		}

		return Result.success(new TableCode(trimmed));
	}
}
