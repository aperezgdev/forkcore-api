package com.forkcore.api.orders.infrastructure.out.persistence;

import com.forkcore.api.orders.domain.Order;
import com.forkcore.api.orders.domain.OrderRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderRepository implements OrderRepository {

	private final Map<String, Order> storage = new ConcurrentHashMap<>();

	@Override
	public Order save(Order order) {
		storage.put(order.id().asString(), order);
		return order;
	}

	public void deleteAll() {
		storage.clear();
	}

	public long count() {
		return storage.size();
	}
}
