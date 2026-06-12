package com.forkcore.api.shared.domain.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationErrorTest {

	@Test
	void shouldCreateValidationError() {
		var error = new ValidationError("name", "product name is required");

		assertThat(error.field()).isEqualTo("name");
		assertThat(error.message()).isEqualTo("product name is required");
	}

	@Test
	void shouldCreateCompositeValidationError() {
		var compositeError = new CompositeValidationError(
			List.of(
				new ValidationError("name", "product name is required"),
				new ValidationError("price", "product price must be greater than or equal to zero")
			)
		);

		assertThat(compositeError.message()).isEqualTo("validation errors occurred");
		assertThat(compositeError.errors()).hasSize(2);
	}

	@Test
	void shouldReturnCompositeValidationErrorWhenCreatedFromOneError() {
		var error = new ValidationError("name", "product name is required");

		assertThat(CompositeValidationError.from(error)).isEqualTo(new CompositeValidationError(List.of(error)));
	}

	@Test
	void shouldReturnCompositeValidationErrorWhenCreatedFromMultipleErrors() {
		var error = CompositeValidationError.from(
			new ValidationError("name", "product name is required"),
			new ValidationError("price", "product price must be greater than or equal to zero")
		);

		assertThat(error).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) error).errors()).hasSize(2);
	}

	@Test
	void shouldCreateCompositeValidationErrorWithSingleError() {
		var error = new CompositeValidationError(List.of(new ValidationError("name", "product name is required")));

		assertThat(error.message()).isEqualTo("validation errors occurred");
		assertThat(error.errors()).containsExactly(new ValidationError("name", "product name is required"));
	}

	@Test
	void shouldRejectCreatingValidationErrorFromEmptyErrors() {
		assertThatThrownBy(() -> CompositeValidationError.from())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("validation errors are required");
	}
}
