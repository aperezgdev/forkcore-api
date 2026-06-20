package com.forkcore.api.orders.domain.vo;

import com.forkcore.api.shared.domain.error.ValidationError;
import com.forkcore.api.shared.domain.result.Result;

public enum OrderStatus {
	pending,
	in_progress,
	ready,
	delivered,
	cancelled;

	public static Result<OrderStatus> from(String value) {
		if (value == null || value.isBlank()) {
			return Result.failure(new ValidationError("status", "order status is required"));
		}

		var trimmed = value.trim();
		for (var status : values()) {
			if (status.name().equals(trimmed)) {
				return Result.success(status);
			}
		}

		return Result.failure(new ValidationError("status", "invalid order status: " + value));
	}
}
