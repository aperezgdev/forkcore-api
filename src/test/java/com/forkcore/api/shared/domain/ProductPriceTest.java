package com.forkcore.api.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.shared.domain.error.ValidationError;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductPriceTest {

	@Test
	void shouldCreateFromBigDecimal() {
		var result = ProductPrice.from(new BigDecimal("12.50"));
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().unitPrice()).isEqualByComparingTo("12.50");
		assertThat(result.value().productId()).isNotNull();
	}

	@Test
	void shouldCreateFromIdAndBigDecimal() {
		var productId = Id.create();
		var result = ProductPrice.from(productId, new BigDecimal("10.00"));
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().productId()).isEqualTo(productId);
		assertThat(result.value().unitPrice()).isEqualByComparingTo("10.00");
	}

	@Test
	void shouldRejectNullPrice() {
		var result = ProductPrice.from(null);
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("price", "product price is required"));
	}

	@Test
	void shouldRejectNegativePrice() {
		var result = ProductPrice.from(new BigDecimal("-1.00"));
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("price", "product price must be greater than or equal to zero"));
	}

	@Test
	void shouldBeValueEquality() {
		var id = Id.create();
		var pp1 = new ProductPrice(id, new BigDecimal("10.00"));
		var pp2 = new ProductPrice(id, new BigDecimal("10.00"));

		assertThat(pp1).isEqualTo(pp2);
		assertThat(pp1.hashCode()).isEqualTo(pp2.hashCode());
	}
}
