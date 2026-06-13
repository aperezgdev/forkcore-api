package com.forkcore.api.catalog.product.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.FieldUpdate;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.error.ValidationError;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProductUpdaterTest {

	@Test
	void shouldUpdateExistingProductWithPartialFields() {
		var repository = new InMemoryRepository(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var updater = new ProductUpdater(repository);

		var result = updater.run(
			"11111111-1111-1111-1111-111111111111",
			FieldUpdate.of("Burger deluxe"),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().id().asString()).isEqualTo("11111111-1111-1111-1111-111111111111");
		assertThat(result.value().name()).isEqualTo("Burger deluxe");
		assertThat(result.value().description()).isEqualTo("Classic burger");
		assertThat(result.value().price()).isEqualByComparingTo("12.50");
		assertThat(result.value().status()).isEqualTo("active");
	}

	@Test
	void shouldPreserveExistingFieldsWhenTheyAreAbsentInTheUpdate() {
		var repository = new InMemoryRepository(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var updater = new ProductUpdater(repository);

		var result = updater.run(
			"11111111-1111-1111-1111-111111111111",
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.of("inactive")
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().name()).isEqualTo("Burger");
		assertThat(result.value().description()).isEqualTo("Classic burger");
		assertThat(result.value().price()).isEqualByComparingTo("12.50");
		assertThat(result.value().status()).isEqualTo("inactive");
	}

	@Test
	void shouldUpdateDescriptionToNullWhenExplicitlyRequested() {
		var repository = new InMemoryRepository(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var updater = new ProductUpdater(repository);

		var result = updater.run(
			"11111111-1111-1111-1111-111111111111",
			FieldUpdate.absent(),
			FieldUpdate.of((String) null),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().description()).isNull();
	}

	@Test
	void shouldUpdateDescriptionToEmptyStringWhenExplicitlyRequested() {
		var repository = new InMemoryRepository(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var updater = new ProductUpdater(repository);

		var result = updater.run(
			"11111111-1111-1111-1111-111111111111",
			FieldUpdate.absent(),
			FieldUpdate.of(""),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().description()).isEqualTo("");
	}

	@Test
	void shouldPersistTheUpdatedProduct() {
		var repository = new InMemoryRepository(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var updater = new ProductUpdater(repository);

		updater.run(
			"11111111-1111-1111-1111-111111111111",
			FieldUpdate.of("Burger deluxe"),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		var persisted = repository.findById(Id.fromStringOrThrow("11111111-1111-1111-1111-111111111111"));
		assertThat(persisted).isPresent();
		assertThat(persisted.get().name()).isEqualTo("Burger deluxe");
	}

	@Test
	void shouldReturnNotFoundErrorWhenProductDoesNotExist() {
		var repository = new InMemoryRepository();
		var updater = new ProductUpdater(repository);

		var result = updater.run(
			"99999999-9999-9999-9999-999999999999",
			FieldUpdate.of("Burger deluxe"),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(NotFoundError.class);
		assertThat(result.error().message()).contains("99999999-9999-9999-9999-999999999999");
	}

	@Test
	void shouldRejectUpdateWithInvalidName() {
		var repository = new InMemoryRepository(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var updater = new ProductUpdater(repository);

		var result = updater.run(
			"11111111-1111-1111-1111-111111111111",
			FieldUpdate.of("   "),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(
			new CompositeValidationError(List.of(new ValidationError("name", "product name is required")))
		);
	}

	@Test
	void shouldRejectUpdateWithInvalidPrice() {
		var repository = new InMemoryRepository(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var updater = new ProductUpdater(repository);

		var result = updater.run(
			"11111111-1111-1111-1111-111111111111",
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.of(new BigDecimal("-1.00")),
			FieldUpdate.absent()
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(
			new CompositeValidationError(
				List.of(
					new ValidationError(
						"price",
						"product price must be greater than or equal to zero"
					)
				)
			)
		);
	}

	@Test
	void shouldRejectUpdateWithInvalidStatus() {
		var repository = new InMemoryRepository(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var updater = new ProductUpdater(repository);

		var result = updater.run(
			"11111111-1111-1111-1111-111111111111",
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.of("archived")
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(
			new CompositeValidationError(List.of(new ValidationError("status", "product status is invalid")))
		);
	}

	@Test
	void shouldPropagateValidationErrorWhenIdIsMalformed() {
		var repository = new InMemoryRepository();
		var updater = new ProductUpdater(repository);

		var result = updater.run(
			"not-a-uuid",
			FieldUpdate.of("Burger deluxe"),
			FieldUpdate.absent(),
			FieldUpdate.absent(),
			FieldUpdate.absent()
		);

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
