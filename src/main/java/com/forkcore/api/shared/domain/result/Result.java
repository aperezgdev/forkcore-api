package com.forkcore.api.shared.domain.result;

import com.forkcore.api.shared.domain.error.DomainError;
import java.util.Objects;
import java.util.function.Function;

public final class Result<T> {

	private final T value;
	private final DomainError error;

	private Result(T value, DomainError error) {
		this.value = value;
		this.error = error;
	}

	public static Result<Void> success() {
		return new Result<>(null, null);
	}

	public static <T> Result<T> success(T value) {
		return new Result<>(value, null);
	}

	public static <T> Result<T> failure(DomainError error) {
		return new Result<>(null, Objects.requireNonNull(error, "result error is required"));
	}

	public boolean isSuccess() {
		return error == null;
	}

	public boolean isFailure() {
		return error != null;
	}

	public T value() {
		if (isFailure()) {
			throw new IllegalStateException("result does not contain a value");
		}

		return value;
	}

	public DomainError error() {
		if (isSuccess()) {
			throw new IllegalStateException("result does not contain an error");
		}

		return error;
	}

	public <R> Result<R> map(Function<? super T, ? extends R> mapper) {
		Objects.requireNonNull(mapper, "result mapper is required");

		if (isFailure()) {
			return Result.failure(error);
		}

		return Result.success(mapper.apply(value));
	}

	public <R> Result<R> flatMap(Function<? super T, Result<R>> mapper) {
		Objects.requireNonNull(mapper, "result mapper is required");

		if (isFailure()) {
			return Result.failure(error);
		}

		return Objects.requireNonNull(mapper.apply(value), "result mapper cannot return null");
	}
}
