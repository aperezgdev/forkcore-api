package com.forkcore.api.orders.domain;

import com.forkcore.api.shared.domain.Id;
import java.util.Optional;

public interface OrderRepository {

	Order save(Order order);

	Optional<Order> findById(Id id);
}
