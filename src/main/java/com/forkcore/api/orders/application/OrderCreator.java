package com.forkcore.api.orders.application;

import com.forkcore.api.orders.application.input.CreateOrderLineInput;
import com.forkcore.api.orders.domain.Order;
import com.forkcore.api.orders.domain.OrderLineInput;
import com.forkcore.api.orders.domain.OrderRepository;
import com.forkcore.api.orders.domain.ProductPriceProvider;
import com.forkcore.api.shared.domain.Id;
import com.forkcore.api.shared.domain.error.CompositeValidationError;
import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderCreator {

	private final OrderRepository orderRepository;
	private final ProductPriceProvider productPriceProvider;

	public OrderCreator(OrderRepository orderRepository, ProductPriceProvider productPriceProvider) {
		this.orderRepository = orderRepository;
		this.productPriceProvider = productPriceProvider;
	}

	public Result<Order> run(List<CreateOrderLineInput> lines, String tableId, String notes) {
		// Delegate null/empty lines validation to Order.create
		if (lines == null || lines.isEmpty()) {
			return Order.create(null, tableId, notes);
		}

		var errors = new ArrayList<ValidationError>();
		var resolvedLines = new ArrayList<OrderLineInput>();

		for (var line : lines) {
			// Resolve the Id for the price lookup. If the productId is not a valid UUID,
			// we cannot call findPrice; add an error and continue.
			var productIdResult = Id.from(line.productId());
			if (productIdResult.isFailure()) {
				errors.add(new ValidationError("productId", "must be a valid UUID: " + line.productId()));
				continue;
			}

			var priceOpt = productPriceProvider.findPrice(productIdResult.value());
			if (priceOpt.isEmpty()) {
				errors.add(new ValidationError("productId", "product not found or has no resolvable price: " + line.productId()));
				continue;
			}

			resolvedLines.add(new OrderLineInput(line.productId(), line.quantity(), priceOpt.get().unitPrice()));
		}

		if (!errors.isEmpty()) {
			return Result.failure(CompositeValidationError.from(errors.toArray(ValidationError[]::new)));
		}

		var orderResult = Order.create(resolvedLines, tableId, notes);
		if (orderResult.isFailure()) {
			return orderResult;
		}

		return Result.success(orderRepository.save(orderResult.value()));
	}
}
