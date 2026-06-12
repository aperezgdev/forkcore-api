package com.forkcore.api.shared.domain.error;

import java.util.Objects;

public record NotFoundError(String resource, String id) implements DomainError {

	public NotFoundError {
		Objects.requireNonNull(resource, "not found error resource is required");
		Objects.requireNonNull(id, "not found error id is required");

		if (resource.isBlank()) {
			throw new IllegalArgumentException("not found error resource is required");
		}

		if (id.isBlank()) {
			throw new IllegalArgumentException("not found error id is required");
		}
	}

	@Override
	public String message() {
		return resource + " not found: " + id;
	}
}
