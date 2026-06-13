package com.forkcore.api.shared.domain;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

public record Id(UUID value) {

	public static Id create() {
		return new Id(UuidCreator.getTimeOrderedEpoch());
	}

	@Deprecated(forRemoval = true)
	public static Id fromStringOrThrow(String value) {
		return new Id(UUID.fromString(value));
	}

	public static Result<Id> from(String value) {
		if (value == null || value.isBlank()) {
			return Result.failure(new ValidationError("id", "must be a valid UUID"));
		}
		try {
			return Result.success(new Id(UUID.fromString(value)));
		} catch (IllegalArgumentException exception) {
			return Result.failure(new ValidationError("id", "must be a valid UUID"));
		}
	}

	public String asString() {
		return value.toString();
	}
}
