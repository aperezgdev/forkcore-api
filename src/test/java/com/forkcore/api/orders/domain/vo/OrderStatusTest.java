package com.forkcore.api.orders.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import com.forkcore.api.shared.domain.error.ValidationError;
import org.junit.jupiter.api.Test;

class OrderStatusTest {

	@Test
	void shouldAcceptPending() {
		var result = OrderStatus.from("pending");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value()).isEqualTo(OrderStatus.pending);
	}

	@Test
	void shouldAcceptInProgress() {
		var result = OrderStatus.from("in_progress");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value()).isEqualTo(OrderStatus.in_progress);
	}

	@Test
	void shouldAcceptReady() {
		var result = OrderStatus.from("ready");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value()).isEqualTo(OrderStatus.ready);
	}

	@Test
	void shouldAcceptDelivered() {
		var result = OrderStatus.from("delivered");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value()).isEqualTo(OrderStatus.delivered);
	}

	@Test
	void shouldAcceptCancelled() {
		var result = OrderStatus.from("cancelled");
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.value()).isEqualTo(OrderStatus.cancelled);
	}

	@Test
	void shouldRejectNull() {
		var result = OrderStatus.from(null);
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("status", "order status is required"));
	}

	@Test
	void shouldRejectBlank() {
		var result = OrderStatus.from("   ");
		assertThat(result.isFailure()).isTrue();
	}

	@Test
	void shouldRejectUnknownStatus() {
		var result = OrderStatus.from("unknown");
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("status", "invalid order status: unknown"));
	}

	@Test
	void shouldRejectUpperCase() {
		var result = OrderStatus.from("PENDING");
		assertThat(result.isFailure()).isTrue();
		assertThat(result.error()).isEqualTo(new ValidationError("status", "invalid order status: PENDING"));
	}
}
