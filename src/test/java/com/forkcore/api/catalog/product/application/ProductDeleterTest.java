package com.forkcore.api.catalog.product.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.error.ValidationError;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProductDeleterTest {

	@Test
	void shouldDeleteExistingProductAndReturnSuccess() {
		var repository = new InMemoryRepository(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var deleter = new ProductDeleter(repository);

		var result = deleter.run("11111111-1111-1111-1111-111111111111");

		assertThat(result.isSuccess()).isTrue();
		assertThat(repository.findById(Id.fromStringOrThrow("11111111-1111-1111-1111-111111111111"))).isEmpty();
	}

	@Test
	void shouldReturnNotFoundErrorWhenProductDoesNotExist() {
		var repository = new InMemoryRepository();
		var deleter = new ProductDeleter(repository);

		var result = deleter.run("99999999-9999-9999-9999-999999999999");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(NotFoundError.class);
		var notFoundError = (NotFoundError) result.error();
		assertThat(notFoundError.resource()).isEqualTo("Product");
		assertThat(notFoundError.id()).isEqualTo("99999999-9999-9999-9999-999999999999");
	}

	@Test
	void shouldPropagateValidationErrorWhenIdIsMalformed() {
		var repository = new InMemoryRepository();
		var deleter = new ProductDeleter(repository);

		var result = deleter.run("not-a-uuid");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(ValidationError.class);
		assertThat(((ValidationError) result.error()).field()).isEqualTo("id");
	}

	private static final class InMemoryRepository implements ProductRepository {

		private final List<Product> products = new ArrayList<>();

		private InMemoryRepository(Product... initial) {
			products.addAll(List.of(initial));
		}

		@Override
		public Product save(Product product) {
			products.removeIf(existing -> existing.id().equals(product.id()));
			products.add(product);
			return product;
		}

		@Override
		public List<Product> findAll() {
			return List.copyOf(products);
		}

		@Override
		public List<Product> findByStatus(String status) {
			return products.stream().filter(product -> product.status().equals(status)).toList();
		}

		@Override
		public Optional<Product> findById(Id id) {
			return products.stream().filter(product -> product.id().equals(id)).findFirst();
		}

		@Override
		public void delete(Product product) {
			products.removeIf(existing -> existing.id().equals(product.id()));
		}
	}
}
