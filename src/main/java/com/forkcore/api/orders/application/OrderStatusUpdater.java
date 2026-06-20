package com.forkcore.api.orders.application;

import com.forkcore.api.orders.domain.Order;
import com.forkcore.api.orders.domain.OrderRepository;
import com.forkcore.api.orders.domain.vo.OrderStatus;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.ConflictError;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusUpdater {

	private final OrderRepository orderRepository;

	public OrderStatusUpdater(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public Result<Order> run(Id id, String newStatus) {
		var statusResult = OrderStatus.from(newStatus);
		if (statusResult.isFailure()) {
			return Result.failure(statusResult.error());
		}

		var orderOpt = orderRepository.findById(id);
		if (orderOpt.isEmpty()) {
			return Result.failure(new NotFoundError("order", id.asString()));
		}

		var order = orderOpt.get();
		var changeResult = order.changeStatus(statusResult.value());
		if (changeResult.isFailure()) {
			return changeResult;
		}

		return Result.success(orderRepository.save(order));
	}
}
