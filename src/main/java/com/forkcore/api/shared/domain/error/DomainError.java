package com.forkcore.api.shared.domain.error;

public sealed interface DomainError permits ValidationError, CompositeValidationError, NotFoundError, ConflictError {

	String message();
}
