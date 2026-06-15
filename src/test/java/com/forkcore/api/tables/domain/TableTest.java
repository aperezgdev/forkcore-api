package com.forkcore.api.tables.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ValidationError;
import java.util.List;
import org.junit.jupiter.api.Test;

class TableTest {

	@Test
	void shouldCreateTableWithDefaultStatus() {
		var result = Table.create("T-01", 4, "Terraza", null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().code()).isEqualTo("T-01");
		assertThat(result.value().capacity()).isEqualTo(4);
		assertThat(result.value().location()).isEqualTo("Terraza");
		assertThat(result.value().status()).isEqualTo("available");
	}

	@Test
	void shouldCreateTableWithExplicitStatus() {
		var result = Table.create("BT-01", 2, "Salon", "occupied");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().code()).isEqualTo("BT-01");
		assertThat(result.value().capacity()).isEqualTo(2);
		assertThat(result.value().location()).isEqualTo("Salon");
		assertThat(result.value().status()).isEqualTo("occupied");
	}

	@Test
	void shouldCreateTableWithNullLocation() {
		var result = Table.create("T-03", 6, null, null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().code()).isEqualTo("T-03");
		assertThat(result.value().capacity()).isEqualTo(6);
		assertThat(result.value().location()).isNull();
		assertThat(result.value().status()).isEqualTo("available");
	}

	@Test
	void shouldCreateTableWithWhitespaceLocationTreatedAsAbsent() {
		var result = Table.create("T-04", 2, "   ", null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().code()).isEqualTo("T-04");
		assertThat(result.value().capacity()).isEqualTo(2);
		assertThat(result.value().location()).isNull();
		assertThat(result.value().status()).isEqualTo("available");
	}

	@Test
	void shouldRejectInvalidCode() {
		var result = Table.create("", 4, "Terraza", "available");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(
			new CompositeValidationError(List.of(new ValidationError("code", "table code is required")))
		);
	}

	@Test
	void shouldRejectInvalidCapacity() {
		var result = Table.create("T-01", 0, "Terraza", "available");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(
			new CompositeValidationError(
				List.of(new ValidationError("capacity", "table capacity must be greater than or equal to one"))
			)
		);
	}

	@Test
	void shouldRejectReservedStatus() {
		var result = Table.create("T-05", 2, null, "reserved");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(
			new CompositeValidationError(List.of(new ValidationError("status", "table status is invalid")))
		);
	}

	@Test
	void shouldAccumulateMultipleValidationErrors() {
		var result = Table.create("", 0, null, null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.containsExactly(
				new ValidationError("code", "table code is required"),
				new ValidationError("capacity", "table capacity must be greater than or equal to one")
			);
	}

	@Test
	void shouldIncludeId() {
		var result = Table.create("T-01", 4, "Terraza", "available");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().id()).isNotNull();
		assertThat(result.value().id().asString()).isNotEmpty();
	}
}
