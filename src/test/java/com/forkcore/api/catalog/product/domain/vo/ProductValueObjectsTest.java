package com.forkcore.api.catalog.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductValueObjectsTest {

	@Test
	void shouldNotValidateWhenConstructingValueObjectsDirectly() {
		assertThat(new ProductName("   ").value()).isEqualTo("   ");
		assertThat(new ProductDescription(null).value()).isNull();
		assertThat(new ProductPrice(new BigDecimal("-1.00")).value()).isEqualByComparingTo("-1.00");
		assertThat(new ProductStatus("archived").value()).isEqualTo("archived");
	}

	@Test
	void shouldValidateAndNormalizeWhenCreatingValueObjectsFromFactoryMethods() {
		assertThat(ProductName.from("  Burger  ").value().value()).isEqualTo("Burger");
		assertThat(ProductDescription.from(null)).isNull();
		assertThat(ProductDescription.from("  Classic burger  ").value()).isEqualTo("Classic burger");
		assertThat(ProductPrice.from(new BigDecimal("12.50")).value().value()).isEqualByComparingTo("12.50");
		assertThat(ProductStatus.from("  INACTIVE  ").value().value()).isEqualTo("inactive");
		assertThat(ProductStatus.from("archived").isFailure()).isTrue();
	}
}
