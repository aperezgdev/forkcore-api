package com.forkcore.api.orders.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.shared.domain.error.ValidationError;
import org.junit.jupiter.api.Test;

class OrderLineQuantityTest {

	@Test
	void shouldAcceptPositiveQuantity() {
		var result = OrderLineQuantity.from(5);
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo(5);
	}

	@Test
	void shouldAcceptMinimumQuantity() {
		var result = OrderLineQuantity.from(1);
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualTo(1);
	}

	@Test
	void shouldRejectNull() {
		var result = OrderLineQuantity.from(null);
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("quantity", "quantity is required"));
	}

	@Test
	void shouldRejectZero() {
		var result = OrderLineQuantity.from(0);
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("quantity", "quantity must be greater than or equal to 1"));
	}

	@Test
	void shouldRejectNegative() {
		var result = OrderLineQuantity.from(-1);
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("quantity", "quantity must be greater than or equal to 1"));
	}
}
