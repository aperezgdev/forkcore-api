package com.forkcore.api.shared.domain.error;

import java.util.Objects;

public record ValidationError(String field, String message) implements DomainError {

	public ValidationError {
		Objects.requireNonNull(field, "validation error field is required");
		Objects.requireNonNull(message, "validation error message is required");

		if (field.isBlank()) {
			throw new IllegalArgumentException("validation error field is required");
		}

		if (message.isBlank()) {
			throw new IllegalArgumentException("validation error message is required");
		}
	}
}
