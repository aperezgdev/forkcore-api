package com.forkcore.api.catalog.product.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.shared.domain.Id;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InMemoryProductRepositoryTest {

	@Test
	void shouldRemoveProductWhenDeleted() {
		var repository = new InMemoryProductRepository();
		var product = Product.fromPrimitives(
			"11111111-1111-1111-1111-111111111111",
			"Burger",
			"Classic burger",
			new BigDecimal("12.50"),
			"active"
		);
		repository.save(product);

		repository.delete(product);

		assertThat(repository.findById(Id.fromStringOrThrow("11111111-1111-1111-1111-111111111111"))).isEmpty();
	}

	@Test
	void shouldNotThrowWhenDeletingProductThatDoesNotExist() {
		var repository = new InMemoryProductRepository();
		var product = Product.fromPrimitives(
			"99999999-9999-9999-9999-999999999999",
			"Burger",
			"Classic burger",
			new BigDecimal("12.50"),
			"active"
		);

		repository.delete(product);

		assertThat(repository.findAll()).isEmpty();
	}
}
