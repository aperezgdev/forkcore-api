package com.forkcore.api.orders.domain.vo;

import com.forkcore.api.shared.domain.result.Result;

public record OrderNotes(String value) {

	public static Result<OrderNotes> from(String value) {
		if (value == null || value.isBlank()) {
			return Result.success(null);
		}

		return Result.success(new OrderNotes(value.trim()));
	}
}
