package com.forkcore.api.orders.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.orders.domain.Order;
import com.forkcore.api.orders.domain.OrderLine;
import com.forkcore.api.orders.domain.OrderRepository;
import com.forkcore.api.orders.domain.vo.OrderStatus;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.ConflictError;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderStatusUpdaterTest {

	private static final String ORDER_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

	private StubOrderRepository repository;
	private OrderStatusUpdater updater;

	@BeforeEach
	void setUp() {
		repository = new StubOrderRepository();
		updater = new OrderStatusUpdater(repository);
	}

	@Test
	void shouldTransitionToValidTargetStatus() {
		var id = anId(ORDER_ID);
		repository.seed(anOrderWithStatus(OrderStatus.pending));

		var result = updater.run(id, "in_progress");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().status()).isEqualTo(OrderStatus.in_progress);
		assertThat(repository.findByIdCallCount()).isEqualTo(1);
		assertThat(repository.saveCallCount()).isEqualTo(1);
	}

	@Test
	void shouldReturnSuccessForSameStateNoOp() {
		var id = anId(ORDER_ID);
		repository.seed(anOrderWithStatus(OrderStatus.in_progress));

		var result = updater.run(id, "in_progress");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value().status()).isEqualTo(OrderStatus.in_progress);
		assertThat(repository.findByIdCallCount()).isEqualTo(1);
		assertThat(repository.saveCallCount()).isEqualTo(1);
	}

	@Test
	void shouldReturnNotFoundWhenOrderDoesNotExist() {
		var id = anId(ORDER_ID);

		var result = updater.run(id, "in_progress");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(NotFoundError.class);
		assertThat(((NotFoundError) result.error()).id()).isEqualTo(ORDER_ID);
		assertThat(repository.saveCallCount()).isEqualTo(0);
	}

	@Test
	void shouldReturnConflictForInvalidTransition() {
		var id = anId(ORDER_ID);
		repository.seed(anOrderWithStatus(OrderStatus.pending));

		var result = updater.run(id, "delivered");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(ConflictError.class);
		assertThat(((ConflictError) result.error()).field()).isEqualTo("order.status.transition");
		assertThat(repository.saveCallCount()).isEqualTo(0);
	}

	@Test
	void shouldReturnValidationErrorForInvalidStatusString() {
		var id = anId(ORDER_ID);
		repository.seed(anOrderWithStatus(OrderStatus.pending));

		var result = updater.run(id, "unknown_status");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(ValidationError.class);
		assertThat(((ValidationError) result.error()).field()).isEqualTo("status");
		assertThat(repository.findByIdCallCount()).isEqualTo(0);
		assertThat(repository.saveCallCount()).isEqualTo(0);
	}

	@Test
	void shouldReturnValidationErrorForEmptyStatus() {
		var id = anId(ORDER_ID);
		repository.seed(anOrderWithStatus(OrderStatus.pending));

		var result = updater.run(id, "");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(ValidationError.class);
		assertThat(repository.findByIdCallCount()).isEqualTo(0);
		assertThat(repository.saveCallCount()).isEqualTo(0);
	}

	@Test
	void shouldReturnValidationErrorForNullStatus() {
		var id = anId(ORDER_ID);
		repository.seed(anOrderWithStatus(OrderStatus.pending));

		var result = updater.run(id, null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(ValidationError.class);
		assertThat(repository.findByIdCallCount()).isEqualTo(0);
		assertThat(repository.saveCallCount()).isEqualTo(0);
	}

	@Test
	void shouldPrioritizeStatusValidationOverNotFound() {
		var id = anId(ORDER_ID);
		// No orders seeded — repository is empty

		var result = updater.run(id, "unknown_status");

		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isInstanceOf(ValidationError.class);
		assertThat(repository.findByIdCallCount()).isEqualTo(0);
		assertThat(repository.saveCallCount()).isEqualTo(0);
	}

	// --- helpers ---

	private static Id anId(String uuid) {
		return new Id(UUID.fromString(uuid));
	}

	private static Order anOrderWithStatus(OrderStatus status) {
		var lines = List.of(OrderLine.fromPrimitives(
			"11111111-1111-1111-1111-111111111111",
			"22222222-2222-2222-2222-222222222222",
			2,
			new BigDecimal("10.00")
		));
		return Order.fromPrimitives(
			ORDER_ID,
			status.name(),
			lines,
			null,
			null,
			new BigDecimal("20.00")
		);
	}

	private static final class StubOrderRepository implements OrderRepository {

		private final Map<String, Order> storage = new HashMap<>();
		private final List<Id> findByIdCalls = new ArrayList<>();
		private final List<Order> saveCalls = new ArrayList<>();

		@Override
		public Order save(Order order) {
			saveCalls.add(order);
			storage.put(order.id().asString(), order);
			return order;
		}

		@Override
		public Optional<Order> findById(Id id) {
			findByIdCalls.add(id);
			return Optional.ofNullable(storage.get(id.asString()));
		}

		void seed(Order order) {
			storage.put(order.id().asString(), order);
		}

		int findByIdCallCount() {
			return findByIdCalls.size();
		}

		int saveCallCount() {
			return saveCalls.size();
		}
	}
}
