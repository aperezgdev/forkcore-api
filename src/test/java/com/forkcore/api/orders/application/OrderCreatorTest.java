package com.forkcore.api.orders.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.orders.application.input.CreateOrderLineInput;
import com.forkcore.api.orders.domain.Order;
import com.forkcore.api.orders.domain.OrderRepository;
import com.forkcore.api.orders.domain.ProductPriceProvider;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.ProductPrice;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OrderCreatorTest {

	private static final String VALID_PRODUCT_ID = "11111111-1111-1111-1111-111111111111";
	private static final String VALID_PRODUCT_ID_2 = "22222222-2222-2222-2222-222222222222";

	@Test
	void shouldCreateOrderWithHappyPath() {
		var repository = new InMemoryOrderRepository();
		var priceProvider = new StubProductPriceProvider(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var creator = new OrderCreator(repository, priceProvider);

		var result = creator.run(
			List.of(new CreateOrderLineInput(VALID_PRODUCT_ID, 2)),
			null,
			null
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().lines()).hasSize(1);
		assertThat(result.value().total()).isEqualByComparingTo("20.00");
		assertThat(result.value().status().name()).isEqualTo("pending");
	}

	@Test
	void shouldCreateOrderWithMultipleLines() {
		var repository = new InMemoryOrderRepository();
		var priceProvider = new StubProductPriceProvider()
			.with(VALID_PRODUCT_ID, new BigDecimal("10.00"))
			.with(VALID_PRODUCT_ID_2, new BigDecimal("2.50"));
		var creator = new OrderCreator(repository, priceProvider);

		var result = creator.run(
			List.of(
				new CreateOrderLineInput(VALID_PRODUCT_ID, 2),
				new CreateOrderLineInput(VALID_PRODUCT_ID_2, 3)
			),
			null,
			null
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().lines()).hasSize(2);
		assertThat(result.value().total()).isEqualByComparingTo("27.50");
	}

	@Test
	void shouldRejectNonExistentProduct() {
		var repository = new InMemoryOrderRepository();
		var priceProvider = new StubProductPriceProvider(); // no products registered
		var creator = new OrderCreator(repository, priceProvider);

		var result = creator.run(
			List.of(new CreateOrderLineInput(VALID_PRODUCT_ID, 1)),
			null,
			null
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors())
			.allMatch(e -> e.field().equals("productId"));
	}

	@Test
	void shouldRejectMultipleNonExistentProducts() {
		var repository = new InMemoryOrderRepository();
		var priceProvider = new StubProductPriceProvider(); // no products registered
		var creator = new OrderCreator(repository, priceProvider);

		var result = creator.run(
			List.of(
				new CreateOrderLineInput(VALID_PRODUCT_ID, 1),
				new CreateOrderLineInput(VALID_PRODUCT_ID_2, 2)
			),
			null,
			null
		);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(CompositeValidationError.class);
		assertThat(((CompositeValidationError) result.error()).errors()).hasSize(2);
	}

	@Test
	void shouldPersistOrder() {
		var repository = new InMemoryOrderRepository();
		var priceProvider = new StubProductPriceProvider(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var creator = new OrderCreator(repository, priceProvider);

		var result = creator.run(
			List.of(new CreateOrderLineInput(VALID_PRODUCT_ID, 1)),
			null,
			null
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	void shouldCreateOrderWithTableId() {
		var repository = new InMemoryOrderRepository();
		var priceProvider = new StubProductPriceProvider(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var creator = new OrderCreator(repository, priceProvider);

		var result = creator.run(
			List.of(new CreateOrderLineInput(VALID_PRODUCT_ID, 1)),
			"33333333-3333-3333-3333-333333333333",
			null
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().tableId().asString()).isEqualTo("33333333-3333-3333-3333-333333333333");
	}

	@Test
	void shouldCreateOrderWithNotes() {
		var repository = new InMemoryOrderRepository();
		var priceProvider = new StubProductPriceProvider(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var creator = new OrderCreator(repository, priceProvider);

		var result = creator.run(
			List.of(new CreateOrderLineInput(VALID_PRODUCT_ID, 1)),
			null,
			"sin cebolla"
		);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().notes()).isEqualTo("sin cebolla");
	}

	private static final class InMemoryOrderRepository implements OrderRepository {

		private final java.util.Map<String, Order> storage = new java.util.concurrent.ConcurrentHashMap<>();

		@Override
		public Order save(Order order) {
			storage.put(order.id().asString(), order);
			return order;
		}

		public long count() {
			return storage.size();
		}
	}

	private static final class StubProductPriceProvider implements ProductPriceProvider {

		private final java.util.Map<String, BigDecimal> prices = new java.util.HashMap<>();

		StubProductPriceProvider() {
		}

		StubProductPriceProvider(String productId, BigDecimal price) {
			prices.put(productId, price);
		}

		StubProductPriceProvider with(String productId, BigDecimal price) {
			prices.put(productId, price);
			return this;
		}

		@Override
		public Optional<ProductPrice> findPrice(Id productId) {
			var price = prices.get(productId.asString());
			if (price == null) {
				return Optional.empty();
			}
			return Optional.of(new ProductPrice(productId, price));
		}
	}
}
