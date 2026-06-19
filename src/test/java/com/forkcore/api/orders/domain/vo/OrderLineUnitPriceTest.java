package com.forkcore.api.orders.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.shared.domain.error.ValidationError;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderLineUnitPriceTest {

	@Test
	void shouldAcceptPositivePrice() {
		var result = OrderLineUnitPrice.from(new BigDecimal("10.00"));
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualByComparingTo("10.00");
	}

	@Test
	void shouldAcceptZeroPrice() {
		var result = OrderLineUnitPrice.from(BigDecimal.ZERO);
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualByComparingTo("0");
	}

	@Test
	void shouldRejectNull() {
		var result = OrderLineUnitPrice.from(null);
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("price", "line unit price is required"));
	}

	@Test
	void shouldRejectNegative() {
		var result = OrderLineUnitPrice.from(new BigDecimal("-5.00"));
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("price", "line unit price must be greater than or equal to zero"));
	}
}
