package com.forkcore.api.orders.infrastructure.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.orders.application.OrderCreator;
import com.forkcore.api.orders.application.OrderStatusUpdater;
import com.forkcore.api.orders.application.input.CreateOrderLineInput;
import com.forkcore.api.orders.domain.Order;
import com.forkcore.api.orders.domain.OrderLine;
import com.forkcore.api.orders.domain.OrderRepository;
import com.forkcore.api.orders.domain.ProductPriceProvider;
import com.forkcore.api.orders.domain.vo.OrderLineQuantity;
import com.forkcore.api.orders.domain.vo.OrderLineUnitPrice;
import com.forkcore.api.orders.infrastructure.out.idempotency.IdempotencyKeyStore;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.ProductPrice;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class OrderControllerTest {

	private static final String VALID_PRODUCT_ID = "11111111-1111-1111-1111-111111111111";
	private static final String VALID_PRODUCT_ID_2 = "22222222-2222-2222-2222-222222222222";

	private InMemoryOrderRepository orderRepository;
	private StubProductPriceProvider priceProvider;
	private OrderCreator orderCreator;
	private InMemoryIdempotencyKeyStore idempotencyStore;
	private OrderRequestFingerprinter fingerprinter;
	private OrderController controller;

	@BeforeEach
	void setUp() {
		orderRepository = new InMemoryOrderRepository();
		priceProvider = new StubProductPriceProvider();
		orderCreator = new OrderCreator(orderRepository, priceProvider);
		idempotencyStore = new InMemoryIdempotencyKeyStore();
		fingerprinter = new OrderRequestFingerprinter();
		var objectMapper = new tools.jackson.databind.ObjectMapper();
		var orderStatusUpdater = new OrderStatusUpdater(orderRepository);
		controller = new OrderController(orderCreator, orderStatusUpdater, idempotencyStore, fingerprinter, objectMapper);
	}

	@Test
	void shouldCreateOrderAndReturn201WithLocation() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 2)),
			null,
			null
		);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().getLocation()).isNotNull();
		assertThat(response.getHeaders().getLocation().getPath()).matches("/orders/.+");
	}

	@Test
	void shouldReturnOrderResponseBodyOnSuccess() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 2)),
			null,
			null
		);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isInstanceOf(OrderResponse.class);
		var body = (OrderResponse) response.getBody();
		assertThat(body.status()).isEqualTo("pending");
		assertThat(body.total()).isEqualByComparingTo("20.00");
		assertThat(body.lines()).hasSize(1);
		assertThat(body.lines().get(0).productId()).isEqualTo(VALID_PRODUCT_ID);
		assertThat(body.lines().get(0).quantity()).isEqualTo(2);
		assertThat(body.lines().get(0).unitPrice()).isEqualByComparingTo("10.00");
	}

	@Test
	void shouldReturn400ForEmptyLines() {
		var request = new CreateOrderRequest(List.of(), null, null);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void shouldReturn400ForNullLines() {
		var request = new CreateOrderRequest(null, null, null);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void shouldReturn400ForInvalidProductId() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest("not-a-uuid", 1)),
			null,
			null
		);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void shouldReturn400ForInvalidQuantity() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 0)),
			null,
			null
		);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void shouldReturn400ForNonExistentProduct() {
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 1)),
			null,
			null
		);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void shouldReturn400ForInvalidTableId() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 1)),
			"not-a-uuid",
			null
		);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void shouldReturn400WhenIdempotencyKeyExceeds255Chars() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 1)),
			null,
			null
		);
		var longKey = "x".repeat(256);

		var response = controller.create(request, longKey);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNull();
	}

	@Test
	void shouldCacheResponseOnIdempotencyHit() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 1)),
			null,
			null
		);

		var firstResponse = controller.create(request, "K-1");
		assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		var secondResponse = controller.create(request, "K-1");
		assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		assertThat(secondResponse.getHeaders().getLocation())
			.isEqualTo(firstResponse.getHeaders().getLocation());
		assertThat(orderRepository.count()).isEqualTo(1);
	}

	@Test
	void shouldReturn409OnIdempotencyKeyConflict() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var request1 = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 1)),
			null,
			null
		);
		var request2 = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 2)),
			null,
			null
		);

		var firstResponse = controller.create(request1, "K-2");
		assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		var secondResponse = controller.create(request2, "K-2");
		assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(secondResponse.getBody()).isNull();
		assertThat(orderRepository.count()).isEqualTo(1);
	}

	@Test
	void shouldNotUseIdempotencyWhenKeyIsBlank() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 1)),
			null,
			null
		);

		var firstResponse = controller.create(request, "   ");
		assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		var firstLocation = firstResponse.getHeaders().getLocation();

		var secondResponse = controller.create(request, "   ");
		assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(secondResponse.getHeaders().getLocation()).isNotEqualTo(firstLocation);

		assertThat(orderRepository.count()).isEqualTo(2);
	}

	@Test
	void shouldReturn201WithMultipleLinesAndCorrectTotal() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		priceProvider.with(VALID_PRODUCT_ID_2, new BigDecimal("2.50"));
		var request = new CreateOrderRequest(
			List.of(
				new CreateOrderLineRequest(VALID_PRODUCT_ID, 2),
				new CreateOrderLineRequest(VALID_PRODUCT_ID_2, 3)
			),
			null,
			null
		);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		var body = (OrderResponse) response.getBody();
		assertThat(body.total()).isEqualByComparingTo("27.50");
		assertThat(body.lines()).hasSize(2);
	}

	@Test
	void shouldReturn201WithTableIdAndNotes() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));
		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 1)),
			"33333333-3333-3333-3333-333333333333",
			"sin cebolla"
		);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		var body = (OrderResponse) response.getBody();
		assertThat(body.tableId()).isEqualTo("33333333-3333-3333-3333-333333333333");
		assertThat(body.notes()).isEqualTo("sin cebolla");
	}

	@Test
	void shouldIgnoreUnknownFieldsInRequest() {
		priceProvider.with(VALID_PRODUCT_ID, new BigDecimal("10.00"));

		var request = new CreateOrderRequest(
			List.of(new CreateOrderLineRequest(VALID_PRODUCT_ID, 1)),
			null,
			null
		);

		var response = controller.create(request, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		var body = (OrderResponse) response.getBody();
		assertThat(body.status()).isEqualTo("pending");
		assertThat(body.total()).isEqualByComparingTo("10.00");
	}

	private static final class InMemoryOrderRepository implements OrderRepository {

		private final java.util.Map<String, Order> storage = new java.util.concurrent.ConcurrentHashMap<>();

		@Override
		public Order save(Order order) {
			storage.put(order.id().asString(), order);
			return order;
		}

		@Override
		public java.util.Optional<Order> findById(Id id) {
			return java.util.Optional.ofNullable(storage.get(id.asString()));
		}

		public long count() {
			return storage.size();
		}
	}

	private static final class StubProductPriceProvider implements ProductPriceProvider {

		private final java.util.Map<String, BigDecimal> prices = new java.util.HashMap<>();

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

	private static final class InMemoryIdempotencyKeyStore implements IdempotencyKeyStore {

		private final java.util.Map<String, com.forkcore.api.orders.infrastructure.out.idempotency.IdempotencyEntry> storage = new java.util.concurrent.ConcurrentHashMap<>();

		@Override
		public Optional<com.forkcore.api.orders.infrastructure.out.idempotency.IdempotencyEntry> find(String key) {
			return Optional.ofNullable(storage.get(key));
		}

		@Override
		public void store(String key, com.forkcore.api.orders.infrastructure.out.idempotency.IdempotencyEntry entry) {
			storage.putIfAbsent(key, entry);
		}
	}
}
