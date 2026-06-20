package com.forkcore.api.orders.infrastructure.out.persistence;

import com.forkcore.api.orders.domain.Order;
import com.forkcore.api.orders.domain.OrderLine;
import com.forkcore.api.orders.domain.OrderRepository;
import com.forkcore.api.shared.domain.Id;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaOrderRepositoryAdapter implements OrderRepository {

	private final SpringDataOrderJpaRepository repository;

	public JpaOrderRepositoryAdapter(SpringDataOrderJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Order save(Order order) {
		var entity = toEntity(order);
		repository.save(entity);
		return order;
	}

	@Override
	public Optional<Order> findById(Id id) {
		return repository.findWithLinesById(id.value())
			.map(this::toDomain);
	}

	private OrderJpaEntity toEntity(Order order) {
		var orderId = order.id().value();
		var status = order.status().name();
		var total = order.total();
		var tableId = order.tableId() != null ? order.tableId().value() : null;
		var notes = order.notes();

		var entity = new OrderJpaEntity(orderId, status, total, tableId, notes, new java.util.ArrayList<>());
		var lineEntities = order.lines().stream()
			.map(line -> new OrderLineJpaEntity(
				line.id().value(),
				entity,
				line.productId().value(),
				line.quantity().value(),
				line.unitPrice()
			))
			.toList();

		return new OrderJpaEntity(orderId, status, total, tableId, notes, lineEntities);
	}

	private Order toDomain(OrderJpaEntity entity) {
		var lines = entity.lines().stream()
			.map(line -> OrderLine.fromPrimitives(
				line.id().toString(),
				line.productId().toString(),
				line.quantity(),
				line.unitPrice()
			))
			.toList();

		var tableId = entity.tableId() != null ? entity.tableId().toString() : null;

		return Order.fromPrimitives(
			entity.id().toString(),
			entity.status(),
			lines,
			tableId,
			entity.notes(),
			entity.total()
		);
	}
}
