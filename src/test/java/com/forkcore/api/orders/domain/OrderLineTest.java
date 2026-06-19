package com.forkcore.api.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ValidationError;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderLineTest {

	@Test
	void shouldCreateOrderLineWithValidPrimitives() {
		var result = OrderLine.create(
			"11111111-1111-1111-1111-111111111111",
			2,
			new BigDecimal("10.00")
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().id()).isNotNull();
		assertThat(result.value().productId().asString()).isEqualTo("11111111-1111-1111-1111-111111111111");
		assertThat(result.value().quantity().value()).isEqualTo(2);
		assertThat(result.value().unitPrice()).isEqualByComparingTo("10.00");
	}

	@Test
	void shouldRejectInvalidProductId() {
		var result = OrderLine.create("not-a-uuid", 2, new BigDecimal("10.00"));

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("id"));
	}

	@Test
	void shouldRejectNullQuantity() {
		var result = OrderLine.create(
			"11111111-1111-1111-1111-111111111111",
			null,
			new BigDecimal("10.00")
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("quantity"));
	}

	@Test
	void shouldRejectInvalidQuantity() {
		var result = OrderLine.create(
			"11111111-1111-1111-1111-111111111111",
			0,
			new BigDecimal("10.00")
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("quantity"));
	}

	@Test
	void shouldRejectNegativeQuantity() {
		var result = OrderLine.create(
			"11111111-1111-1111-1111-111111111111",
			-1,
			new BigDecimal("10.00")
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("quantity"));
	}

	@Test
	void shouldRejectNullUnitPrice() {
		var result = OrderLine.create(
			"11111111-1111-1111-1111-111111111111",
			1,
			null
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("price"));
	}

	@Test
	void shouldRejectNegativeUnitPrice() {
		var result = OrderLine.create(
			"11111111-1111-1111-1111-111111111111",
			1,
			new BigDecimal("-1.00")
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("price"));
	}

	@Test
	void shouldAccumulateMultipleValidationErrors() {
		var result = OrderLine.create("not-a-uuid", 0, new BigDecimal("-1.00"));

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors()).hasSize(3);
	}

	@Test
	void shouldNotThrowOnNullProductId() {
		var result = OrderLine.create(null, 1, new BigDecimal("10.00"));

		assertThat(result.isFailure()).isTrue();
	}
}
