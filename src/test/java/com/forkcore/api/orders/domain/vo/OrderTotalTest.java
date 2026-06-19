package com.forkcore.api.orders.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.shared.domain.error.ValidationError;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderTotalTest {

	@Test
	void shouldAcceptPositiveTotal() {
		var result = OrderTotal.from(new BigDecimal("100.00"));
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualByComparingTo("100.00");
	}

	@Test
	void shouldAcceptZeroTotal() {
		var result = OrderTotal.from(BigDecimal.ZERO);
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().value()).isEqualByComparingTo("0");
	}

	@Test
	void shouldRejectNull() {
		var result = OrderTotal.from(null);
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("total", "total is required"));
	}

	@Test
	void shouldRejectNegative() {
		var result = OrderTotal.from(new BigDecimal("-1.00"));
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("total", "total must be greater than or equal to zero"));
	}
}
