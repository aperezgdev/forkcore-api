package com.forkcore.api.catalog.product.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.catalog.product.application.ProductCreator;
import com.forkcore.api.catalog.product.application.ProductRetriever;
import com.forkcore.api.catalog.product.application.ProductUpdater;
import com.forkcore.api.catalog.product.domain.Product;
import com.forkcore.api.catalog.product.domain.ProductRepository;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.HttpStatus;

class ProductControllerTest {

	@Test
	void shouldCreateProduct() {
		var repository = new InMemoryRepository();
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);
		var request = new CreateProductRequest("Burger", "Classic burger", new BigDecimal("12.50"), null);

		var response = controller.create(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().getLocation()).isNotNull();
		assertThat(response.getHeaders().getLocation().getPath()).matches("/products/.+");
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().name()).isEqualTo("Burger");
		assertThat(response.getBody().description()).isEqualTo("Classic burger");
		assertThat(response.getBody().status()).isEqualTo("active");
	}

	@Test
	void shouldReturnBadRequestWhenPriceIsInvalid() {
		var repository = new InMemoryRepository();
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);

		try {
			controller.create(new CreateProductRequest("Burger", "Classic burger", new BigDecimal("-1"), null));
		} catch (ProductErrorHandler.InvalidProductException exception) {
			var problem = new ProductErrorHandler().handleInvalidProductException(exception);
			assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
			assertThat(problem.getTitle()).isEqualTo("Invalid product");
			assertThat(problem.getDetail()).isEqualTo("validation errors occurred");
			assertThat(problem.getProperties()).containsKey("errors");
		}
	}

	@Test
	void shouldReturnAllProductsWhenNoStatusFilterIsProvided() {
		var repository = new InMemoryRepository();
		repository.save(Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value());
		repository.save(Product.create("Salad", "Green salad", new BigDecimal("9.00"), "inactive").value());
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);

		var response = controller.get(null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody()).extracting(ProductResponse::name).containsExactly("Burger", "Salad");
	}

	@Test
	void shouldReturnProductsFilteredByStatus() {
		var repository = new InMemoryRepository();
		repository.save(Product.create("Burger", "Classic burger", new BigDecimal("12.50"), "active").value());
		repository.save(Product.create("Salad", "Green salad", new BigDecimal("9.00"), "inactive").value());
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);

		var response = controller.get("inactive");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody()).extracting(ProductResponse::name).containsExactly("Salad");
	}

	@Test
	void shouldReturnEmptyListWhenNoProductsExist() {
		var repository = new InMemoryRepository();
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);

		var response = controller.get(null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEmpty();
	}

	@Test
	void shouldReturnBadRequestWhenStatusFilterIsInvalid() {
		var repository = new InMemoryRepository();
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);

		try {
			controller.get("archived");
		} catch (ProductErrorHandler.InvalidProductException exception) {
			var problem = new ProductErrorHandler().handleInvalidProductException(exception);
			assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
			assertThat(problem.getTitle()).isEqualTo("Invalid product");
			assertThat(problem.getDetail()).isEqualTo("product status is invalid");
		}
	}

	@Test
	void shouldReturnBadRequestWhenMultipleValidationErrorsExist() {
		var repository = new InMemoryRepository();
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);

		try {
			controller.create(new CreateProductRequest("   ", "Classic burger", new BigDecimal("-1"), null));
		} catch (ProductErrorHandler.InvalidProductException exception) {
			var problem = new ProductErrorHandler().handleInvalidProductException(exception);
			assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
			assertThat(problem.getTitle()).isEqualTo("Invalid product");
			assertThat(problem.getDetail()).isEqualTo("validation errors occurred");
			assertThat(problem.getProperties()).containsKey("errors");
			assertThat(exception.error()).isInstanceOf(CompositeValidationError.class);
		}
	}

	@Test
	void shouldUpdateProductPartially() {
		var repository = new InMemoryRepository();
		var saved = Product.fromPrimitives(
			"11111111-1111-1111-1111-111111111111",
			"Burger",
			"Classic burger",
			new BigDecimal("12.50"),
			"active"
		);
		repository.save(saved);
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);
		var request = new UpdateProductRequest(
			JsonNullable.of("Burger deluxe"),
			JsonNullable.undefined(),
			JsonNullable.undefined(),
			JsonNullable.undefined()
		);

		var response = controller.update("11111111-1111-1111-1111-111111111111", request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().id()).isEqualTo("11111111-1111-1111-1111-111111111111");
		assertThat(response.getBody().name()).isEqualTo("Burger deluxe");
		assertThat(response.getBody().description()).isEqualTo("Classic burger");
		assertThat(response.getBody().price()).isEqualByComparingTo("12.50");
		assertThat(response.getBody().status()).isEqualTo("active");
	}

	@Test
	void shouldUpdateDescriptionToNullWhenExplicitlySent() {
		var repository = new InMemoryRepository();
		repository.save(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);
		var request = new UpdateProductRequest(
			JsonNullable.undefined(),
			JsonNullable.of(null),
			JsonNullable.undefined(),
			JsonNullable.undefined()
		);

		var response = controller.update("11111111-1111-1111-1111-111111111111", request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().description()).isNull();
	}

	@Test
	void shouldReturnNotFoundWhenProductDoesNotExist() {
		var repository = new InMemoryRepository();
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);
		var request = new UpdateProductRequest(
			JsonNullable.of("Burger deluxe"),
			JsonNullable.undefined(),
			JsonNullable.undefined(),
			JsonNullable.undefined()
		);

		try {
			controller.update("99999999-9999-9999-9999-999999999999", request);
		} catch (ProductErrorHandler.ProductNotFoundException exception) {
			var problem = new ProductErrorHandler().handleProductNotFoundException(exception);
			assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
			assertThat(problem.getTitle()).isEqualTo("Product not found");
			assertThat(problem.getDetail()).contains("99999999-9999-9999-9999-999999999999");
		}
	}

	@Test
	void shouldReturnBadRequestWhenPartialUpdateNameIsInvalid() {
		var repository = new InMemoryRepository();
		repository.save(
			Product.fromPrimitives(
				"11111111-1111-1111-1111-111111111111",
				"Burger",
				"Classic burger",
				new BigDecimal("12.50"),
				"active"
			)
		);
		var controller = new ProductController(
			new ProductCreator(repository),
			new ProductRetriever(repository),
			new ProductUpdater(repository)
		);
		var request = new UpdateProductRequest(
			JsonNullable.of("   "),
			JsonNullable.undefined(),
			JsonNullable.undefined(),
			JsonNullable.undefined()
		);

		try {
			controller.update("11111111-1111-1111-1111-111111111111", request);
		} catch (ProductErrorHandler.InvalidProductException exception) {
			var problem = new ProductErrorHandler().handleInvalidProductException(exception);
			assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
			assertThat(problem.getTitle()).isEqualTo("Invalid product");
		}
	}

	private static final class InMemoryRepository implements ProductRepository {

		private final List<Product> products = new ArrayList<>();

		@Override
		public Product save(Product product) {
			products.add(product);
			return product;
		}

		@Override
		public List<Product> findAll() {
			return products.stream().toList();
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
