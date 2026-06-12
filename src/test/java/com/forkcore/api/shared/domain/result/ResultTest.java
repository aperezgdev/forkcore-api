package com.forkcore.api.shared.domain.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.forkcore.api.shared.domain.error.ValidationError;
import org.junit.jupiter.api.Test;

class ResultTest {

	@Test
	void shouldCreateSuccessfulResult() {
		var result = Result.success("Burger");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.isFailure()).isFalse();
		assertThat(result.value()).isEqualTo("Burger");
	}

	@Test
	void shouldCreateFailedResult() {
		var error = new ValidationError("name", "product name is required");
		var result = Result.failure(error);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.isSuccess()).isFalse();
		assertThat(result.error()).isSameAs(error);
	}

	@Test
	void shouldMapSuccessfulResult() {
		var result = Result.success("Burger");

		var mappedResult = result.map(String::length);

		assertThat(mappedResult.isSuccess()).isTrue();
		assertThat(mappedResult.value()).isEqualTo(6);
	}

	@Test
	void shouldKeepErrorWhenMappingFailedResult() {
		var error = new ValidationError("name", "product name is required");
		var result = Result.<String>failure(error);

		var mappedResult = result.map(String::length);

		assertThat(mappedResult.isFailure()).isTrue();
		assertThat(mappedResult.error()).isSameAs(error);
	}

	@Test
	void shouldFlatMapSuccessfulResult() {
		var result = Result.success("Burger");

		var mappedResult = result.flatMap(value -> Result.success(value.length()));

		assertThat(mappedResult.isSuccess()).isTrue();
		assertThat(mappedResult.value()).isEqualTo(6);
	}

	@Test
	void shouldKeepErrorWhenFlatMappingFailedResult() {
		var error = new ValidationError("name", "product name is required");
		var result = Result.<String>failure(error);

		var mappedResult = result.flatMap(value -> Result.success(value.length()));

		assertThat(mappedResult.isFailure()).isTrue();
		assertThat(mappedResult.error()).isSameAs(error);
	}

	@Test
	void shouldRejectReadingValueFromFailedResult() {
		var result = Result.failure(new ValidationError("name", "product name is required"));

		assertThatThrownBy(result::value)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("result does not contain a value");
	}

	@Test
	void shouldRejectReadingErrorFromSuccessfulResult() {
		var result = Result.success("Burger");

		assertThatThrownBy(result::error)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("result does not contain an error");
	}
}
