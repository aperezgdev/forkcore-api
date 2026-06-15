package com.forkcore.api.shared.domain.error;

import java.util.Objects;

public record ConflictError(String field, String message) implements DomainError {

	public ConflictError {
		Objects.requireNonNull(field, "conflict error field is required");
		Objects.requireNonNull(message, "conflict error message is required");

		if (field.isBlank()) {
			throw new IllegalArgumentException("conflict error field is required");
		}

		if (message.isBlank()) {
			throw new IllegalArgumentException("conflict error message is required");
		}
	}
}
