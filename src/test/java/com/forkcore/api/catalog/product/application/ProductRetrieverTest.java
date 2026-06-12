package com.forkcore.api.catalog.product.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.ValidationError;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProductRetrieverTest {

	@Test
	void shouldReturnAllProductsWhenStatusIsNotProvided() {
		var repository = new InMemoryRepository(
			Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value(),
			Product.create("Salad", "Green salad", new BigDecimal("9.00"), "inactive").value()
		);

		var retriever = new ProductRetriever(repository);

		assertThat(retriever.run(null).value()).hasSize(2);
	}

	@Test
	void shouldReturnProductsFilteredByStatus() {
		var repository = new InMemoryRepository(
			Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value(),
			Product.create("Salad", "Green salad", new BigDecimal("9.00"), "inactive").value()
		);

		var retriever = new ProductRetriever(repository);

		assertThat(retriever.run("inactive").value()).extracting(Product::name).containsExactly("Salad");
	}

	@Test
	void shouldRejectInvalidStatusFilter() {
		var retriever = new ProductRetriever(new InMemoryRepository());
		var result = retriever.run("archived");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("status", "product status is invalid"));
	}

	private record InMemoryRepository(List<Product> products) implements ProductRepository {

		private InMemoryRepository(Product... products) {
			this(List.of(products));
		}

		@Override
		public Product save(Product product) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Product> findAll() {
			return products;
		}

		@Override
		public List<Product> findByStatus(String status) {
			return products.stream().filter(product -> product.status().equals(status)).toList();
		}

		@Override
		public Optional<Product> findById(Id id) {
			return products.stream().filter(product -> product.id().equals(id)).findFirst();
		}
	}
}
