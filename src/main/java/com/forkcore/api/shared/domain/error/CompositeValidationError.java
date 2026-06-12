package com.forkcore.api.shared.domain.error;

import java.util.List;
import java.util.Objects;

public record CompositeValidationError(List<ValidationError> errors) implements DomainError {

	private static final String MESSAGE = "validation errors occurred";

	public static DomainError from(ValidationError... errors) {
		Objects.requireNonNull(errors, "validation errors are required");

		return new CompositeValidationError(List.of(errors));
	}

	public CompositeValidationError {
		Objects.requireNonNull(errors, "validation errors are required");
		errors = List.copyOf(errors);

		if (errors.isEmpty()) {
			throw new IllegalArgumentException("validation errors are required");
		}
	}

	@Override
	public String message() {
		return MESSAGE;
	}
}
