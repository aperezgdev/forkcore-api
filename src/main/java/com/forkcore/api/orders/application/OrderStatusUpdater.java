package com.forkcore.api.orders.application;

import com.forkcore.api.orders.domain.Order;
import com.forkcore.api.orders.domain.OrderRepository;
import com.forkcore.api.orders.domain.vo.OrderStatus;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.ConflictError;
import com.forkcore.api.shared.domain.error.NotFoundError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusUpdater {

	private static final Logger LOG = LoggerFactory.getLogger(OrderStatusUpdater.class);

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
			LOG.debug("Order not found id={}", id.asString());
			return Result.failure(new NotFoundError("order", id.asString()));
		}

		var order = orderOpt.get();
		var previousStatus = order.status();
		var changeResult = order.changeStatus(statusResult.value());
		if (changeResult.isFailure()) {
			LOG.warn("Order status transition rejected id={} from={} to={}", id.asString(), previousStatus, newStatus);
			return changeResult;
		}

		var saved = orderRepository.save(order);
		LOG.info("Order status changed id={} from={} to={}", saved.id().asString(), previousStatus, saved.status());
		return Result.success(saved);
	}
}
