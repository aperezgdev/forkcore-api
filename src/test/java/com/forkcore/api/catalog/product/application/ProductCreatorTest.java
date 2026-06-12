package com.forkcore.api.catalog.product.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.Id;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProductCreatorTest {

	@Test
	void shouldPersistCreatedProduct() {
		var repository = new ProductRepository() {
			@Override
			public Product save(Product product) {
				return product;
			}

			@Override
			public List<Product> findAll() {
				return List.of();
			}

			@Override
			public List<Product> findByStatus(String status) {
				return List.of();
			}

			@Override
			public Optional<Product> findById(Id id) {
				return Optional.empty();
			}
		};

		var creator = new ProductCreator(repository);
		var result = creator.run("Burger", "Classic burger", new BigDecimal("12.50"), null);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().name()).isEqualTo("Burger");
		assertThat(result.value().description()).isEqualTo("Classic burger");
		assertThat(result.value().price()).isEqualTo(new BigDecimal("12.50"));
		assertThat(result.value().status()).isEqualTo("active");
	}
}
