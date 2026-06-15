package com.forkcore.api.tables.domain.vo;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;

public record TableCapacity(Integer value) {

	public static Result<TableCapacity> from(Integer value) {
		if (value == null) {
			return Result.failure(new ValidationError("capacity", "table capacity is required"));
		}

		if (value < 1) {
			return Result.failure(new ValidationError("capacity", "table capacity must be greater than or equal to one"));
		}

		return Result.success(new TableCapacity(value));
	}
}
