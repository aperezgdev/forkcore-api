package com.forkcore.api.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.forkcore.api.shared.domain.error.ValidationError;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdTest {

	@Test
	void shouldNotValidateWhenConstructingIdDirectly() {
		assertThat(new Id(null).value()).isNull();
	}

	@Test
	void shouldReturnResultSuccessWhenIdIsValid() {
		var uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
		var result = Id.from("11111111-1111-1111-1111-111111111111");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value()).isEqualTo(new Id(uuid));
	}

	@Test
	void shouldReturnResultFailureWhenIdIsNull() {
		var result = Id.from(null);
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("id", "must be a valid UUID"));
	}

	@Test
	void shouldReturnResultFailureWhenIdIsBlank() {
		var result = Id.from("   ");
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("id", "must be a valid UUID"));
	}

	@Test
	void shouldReturnResultFailureWhenIdIsMalformed() {
		var result = Id.from("not-a-uuid");
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("id", "must be a valid UUID"));
	}

	@Test
	void shouldThrowWhenFromStringOrThrowReceivesMalformedInput() {
		assertThatThrownBy(() -> Id.fromStringOrThrow("not-a-uuid"))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldNotThrowWhenFromStringOrThrowReceivesValidInput() {
		var uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
		assertThat(Id.fromStringOrThrow("11111111-1111-1111-1111-111111111111").value()).isEqualTo(uuid);
	}
}
